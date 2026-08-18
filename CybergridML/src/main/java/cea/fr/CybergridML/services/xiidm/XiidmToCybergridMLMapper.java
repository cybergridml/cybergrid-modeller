package cea.fr.CybergridML.services.xiidm;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import cea.fr.CybergridML.domain.CybergridMLLibElementType;
import cea.fr.CybergridML.services.CybergridMLElementInitializerSwitch;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.syson.sysml.AttributeUsage;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Expression;
import org.eclipse.syson.sysml.FeatureMembership;
import org.eclipse.syson.sysml.FeatureValue;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.SysmlFactory;
import org.springframework.stereotype.Service;

/**
 * Creates CybergridML parts from an {@link XiidmNetwork}, under the grid the user clicked.
 * Substations and buses come from {@code substation} and {@code bus}. Generators and loads come
 * from {@code generator} and {@code load}. Each line or transformer becomes two Terminal parts,
 * one per connected bus. Voltage level is not kept as a separate element, only its
 * {@code nominalV} is copied onto the bus. Unmapped elements are skipped with a warning.
 */
@Service
public class XiidmToCybergridMLMapper {

    /**
     * Runs the three import passes in order, and returns the created substations
     * plus counts and warnings for user feedback.
     */
    private static final String PRECISION = "precision";

    public XiidmImportResult importInto(Element grid, XiidmNetwork network, IEditingContext editingContext) {
        var warnings = new ArrayList<String>();
        var createdSubstations = new ArrayList<Element>();
        var busesByXiidmId = new LinkedHashMap<String, PartUsage>();

        int busCount = this.createSubstationsAndBuses(grid, network, editingContext, createdSubstations, busesByXiidmId, warnings);
        var equipmentCounts = this.createEquipments(network, editingContext, busesByXiidmId, warnings);
        int terminalCount = this.createTerminals(network, editingContext, busesByXiidmId, warnings);

        return new XiidmImportResult(List.copyOf(createdSubstations), createdSubstations.size(), busCount,
                equipmentCounts.generators(), equipmentCounts.loads(), terminalCount, List.copyOf(warnings));
    }

    /**
     * First pass. Creates substations and their buses, so equipment and branches
     * can find the bus they belong to no matter the order they appear in the file.
     */
    private int createSubstationsAndBuses(Element grid, XiidmNetwork network, IEditingContext editingContext,
            List<Element> createdSubstations, Map<String, PartUsage> busesByXiidmId, List<String> warnings) {
        int busCount = 0;
        for (XiidmNetwork.Substation substation : network.substations()) {
            var substationPart = this.createPart(grid, CybergridMLLibElementType.SUBSTATION, substation.id(), editingContext);
            createdSubstations.add(substationPart);

            for (XiidmNetwork.VoltageLevel voltageLevel : substation.voltageLevels()) {
                for (XiidmNetwork.Bus bus : voltageLevel.buses()) {
                    if (bus.id() == null || bus.id().isBlank()) {
                        warnings.add("A bus of voltage level '%s' has no id and was skipped.".formatted(voltageLevel.id()));
                        continue;
                    }
                    if (busesByXiidmId.containsKey(bus.id())) {
                        warnings.add("Bus '%s' is declared more than once; only the first one was imported.".formatted(bus.id()));
                        continue;
                    }
                    var busPart = this.createPart(substationPart, CybergridMLLibElementType.BUS, bus.id(), editingContext);
                    this.setNumericValue(busPart, "nominalLevel", voltageLevel.nominalV());
                    busesByXiidmId.put(bus.id(), busPart);
                    busCount++;
                }
            }
        }
        return busCount;
    }

    /**
     * Second pass. Creates generators and loads under the bus they are attached to.
     */
    private EquipmentCounts createEquipments(XiidmNetwork network, IEditingContext editingContext,
            Map<String, PartUsage> busesByXiidmId, List<String> warnings) {
        int generators = 0;
        int loads = 0;
        for (XiidmNetwork.Substation substation : network.substations()) {
            for (XiidmNetwork.VoltageLevel voltageLevel : substation.voltageLevels()) {
                for (XiidmNetwork.Equipment equipment : voltageLevel.equipments()) {
                    if (equipment.kind() == XiidmNetwork.EquipmentKind.SHUNT_COMPENSATOR) {
                        warnings.add("Shunt compensator '%s' has no CybergridML equivalent and was skipped.".formatted(equipment.id()));
                        continue;
                    }
                    var busPart = busesByXiidmId.get(equipment.busId());
                    if (busPart == null) {
                        warnings.add("'%s' references the unknown bus '%s' and was skipped.".formatted(equipment.id(), equipment.busId()));
                        continue;
                    }
                    var type = equipment.kind() == XiidmNetwork.EquipmentKind.GENERATOR
                            ? CybergridMLLibElementType.GENERATOR
                            : CybergridMLLibElementType.LOAD;
                    var equipmentPart = this.createPart(busPart, type, equipment.id(), editingContext);
                    this.setNumericValue(equipmentPart, "p", equipment.p());
                    this.setNumericValue(equipmentPart, "q", equipment.q());
                    if (type == CybergridMLLibElementType.GENERATOR) {
                        generators++;
                    } else {
                        loads++;
                    }
                }
            }
        }
        return new EquipmentCounts(generators, loads);
    }

    /**
     * Third pass. Creates two Terminal parts per line or transformer, one on each
     * connected bus.
     */
    private int createTerminals(XiidmNetwork network, IEditingContext editingContext, Map<String, PartUsage> busesByXiidmId,
            List<String> warnings) {
        int created = 0;
        for (XiidmNetwork.Branch branch : network.branches()) {
            String[] busIds = { branch.bus1(), branch.bus2() };
            for (int side = 1; side <= 2; side++) {
                String busId = busIds[side - 1];
                var busPart = busesByXiidmId.get(busId);
                if (busPart == null) {
                    warnings.add("'%s' references the unknown bus '%s' on side %d; that terminal was skipped."
                            .formatted(branch.id(), busId, side));
                    continue;
                }
                var terminal = this.createPart(busPart, CybergridMLLibElementType.TERMINAL, branch.id() + "_" + side, editingContext);
                this.setStringValue(terminal, "branch", branch.id());
                this.setIntegerValue(terminal, "side", side);
                created++;
            }
        }
        return created;
    }

    /**
     * Creates a part typed from the CybergridML library, then renames it using
     * the XIIDM id.
     */
    private PartUsage createPart(Element owner, CybergridMLLibElementType type, String xiidmId, IEditingContext editingContext) {
        String name = this.uniqueName(owner, sanitizeName(xiidmId, type));

        var featureMembership = SysmlFactory.eINSTANCE.createFeatureMembership();
        owner.getOwnedRelationship().add(featureMembership);
        var partUsage = SysmlFactory.eINSTANCE.createPartUsage();
        featureMembership.getOwnedRelatedElement().add(partUsage);
        new CybergridMLElementInitializerSwitch(editingContext, type).doSwitch(partUsage);
        partUsage.setDeclaredName(name);
        return partUsage;
    }

    /**
     * Converts an XIIDM id like {@code B1-G} into a valid SysML name like {@code B1_G}.
     */
    private static String sanitizeName(String xiidmId, CybergridMLLibElementType type) {
        if (xiidmId == null || xiidmId.isBlank()) {
            return type.getName().toLowerCase();
        }
        String name = xiidmId.trim().replaceAll("[^A-Za-z0-9_]", "_");
        if (!Character.isLetter(name.charAt(0)) && name.charAt(0) != '_') {
            name = "_" + name;
        }
        return name;
    }

    private String uniqueName(Element owner, String baseName) {
        Set<String> usedNames = owner.getOwnedRelationship().stream()
                .filter(FeatureMembership.class::isInstance)
                .map(FeatureMembership.class::cast)
                .flatMap(featureMembership -> featureMembership.getOwnedRelatedElement().stream())
                .map(Element::getDeclaredName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (!usedNames.contains(baseName)) {
            return baseName;
        }
        int index = 2;
        while (usedNames.contains(baseName + "_" + index)) {
            index++;
        }
        return baseName + "_" + index;
    }

    private void setNumericValue(Element owner, String attributeName, Double value) {
        if (value == null) {
            return;
        }
        this.findAttribute(owner, attributeName).ifPresent(attribute -> {
            var literal = SysmlFactory.eINSTANCE.createLiteralRational();
            literal.setValue(value);
            this.setFeatureValue(this.valueHolder(attribute), literal);
        });
    }

    private void setIntegerValue(Element owner, String attributeName, int value) {
        this.findAttribute(owner, attributeName).ifPresent(attribute -> {
            var literal = SysmlFactory.eINSTANCE.createLiteralInteger();
            literal.setValue(value);
            this.setFeatureValue(this.valueHolder(attribute), literal);
        });
    }

    private void setStringValue(Element owner, String attributeName, String value) {
        if (value == null) {
            return;
        }
        this.findAttribute(owner, attributeName).ifPresent(attribute -> {
            var literal = SysmlFactory.eINSTANCE.createLiteralString();
            literal.setValue(value);
            this.setFeatureValue(this.valueHolder(attribute), literal);
        });
    }

    /**
     * Value lives on a nested precision attribute for {@code NFP_*} types, or directly
     * on the attribute itself for plain scalars.
     */
    private AttributeUsage valueHolder(AttributeUsage attribute) {
        return this.findAttribute(attribute, PRECISION).orElse(attribute);
    }

    private Optional<AttributeUsage> findAttribute(Element owner, String attributeName) {
        return owner.getOwnedRelationship().stream()
                .filter(FeatureMembership.class::isInstance)
                .map(FeatureMembership.class::cast)
                .flatMap(featureMembership -> featureMembership.getOwnedRelatedElement().stream())
                .filter(AttributeUsage.class::isInstance)
                .map(AttributeUsage.class::cast)
                .filter(attribute -> attributeName.equals(attribute.getDeclaredName()))
                .findFirst();
    }

    private void setFeatureValue(AttributeUsage attribute, Expression literal) {
        attribute.getOwnedRelationship().removeIf(FeatureValue.class::isInstance);
        var featureValue = SysmlFactory.eINSTANCE.createFeatureValue();
        featureValue.getOwnedRelatedElement().add(literal);
        attribute.getOwnedRelationship().add(featureValue);
    }

    private record EquipmentCounts(int generators, int loads) {
    }
}
