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
import org.eclipse.syson.sysml.AllocationUsage;
import org.eclipse.syson.sysml.AttributeUsage;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Expression;
import org.eclipse.syson.sysml.FeatureMembership;
import org.eclipse.syson.sysml.FeatureValue;
import org.eclipse.syson.sysml.Namespace;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.SysmlFactory;
import org.eclipse.syson.sysml.Usage;
import org.eclipse.syson.sysml.ViewUsage;
import org.springframework.stereotype.Service;

/** Maps {@link XiidmNetwork} to CybergridML parts. */
@Service
public class XiidmToCybergridMLMapper {

    private static final String PRECISION = "precision";

    public XiidmImportResult importInto(Element grid, XiidmNetwork network, IEditingContext editingContext) {
        var warnings = new ArrayList<String>();
        var createdSubstations = new ArrayList<Element>();
        var busesByXiidmId = new LinkedHashMap<String, PartUsage>();
        var computingResourcesBySubstationId = new LinkedHashMap<String, PartUsage>();
        var substationIdByBusId = new LinkedHashMap<String, String>();

        Element craView = this.findCraView(grid);
        Element cfraView = this.findCfraView(grid);

        var substationCounts = this.createSubstationsAndBuses(grid, craView, network, editingContext, createdSubstations,
                busesByXiidmId, computingResourcesBySubstationId, substationIdByBusId, warnings);
        var equipmentCounts = this.createEquipments(craView, cfraView, network, editingContext, busesByXiidmId,
                computingResourcesBySubstationId, substationIdByBusId, warnings);
        var terminalCounts = this.createTerminals(craView, cfraView, network, editingContext, busesByXiidmId,
                computingResourcesBySubstationId, substationIdByBusId, warnings);

        int totalLocalControllers = equipmentCounts.localControllers() + terminalCounts.localControllers();
        int totalSoftwareResources = equipmentCounts.softwareResources() + terminalCounts.softwareResources();
        return new XiidmImportResult(List.copyOf(createdSubstations), createdSubstations.size(),
                substationCounts.buses(), equipmentCounts.generators(), equipmentCounts.loads(),
                terminalCounts.terminals(), totalLocalControllers,
                substationCounts.computingResources(), totalSoftwareResources, List.copyOf(warnings));
    }

    /** Pass 1: create substations and buses. */
    private SubstationCounts createSubstationsAndBuses(Element grid, Element craView, XiidmNetwork network, IEditingContext editingContext,
            List<Element> createdSubstations, Map<String, PartUsage> busesByXiidmId,
            Map<String, PartUsage> computingResourcesBySubstationId, Map<String, String> substationIdByBusId, List<String> warnings) {
        int busCount = 0;
        int computingResources = 0;
        for (XiidmNetwork.Substation substation : network.substations()) {
            var substationPart = this.createPart(grid, CybergridMLLibElementType.SUBSTATION, substation.id(), editingContext);
            createdSubstations.add(substationPart);

            if (craView != null) {
                var computingResource = this.createPart(craView, CybergridMLLibElementType.COMPUTINGRESOURCE,
                        "CR_" + substation.id(), editingContext);
                computingResourcesBySubstationId.put(substation.id(), computingResource);
                computingResources++;
            }

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
                    substationIdByBusId.put(bus.id(), substation.id());
                    busCount++;
                }
            }
        }
        return new SubstationCounts(busCount, computingResources);
    }

    /** Pass 2: create generators, loads, and their LocalControllers. */
    private EquipmentCounts createEquipments(Element craView, Element cfraView, XiidmNetwork network, IEditingContext editingContext,
            Map<String, PartUsage> busesByXiidmId, Map<String, PartUsage> computingResourcesBySubstationId,
            Map<String, String> substationIdByBusId, List<String> warnings) {
        int generators = 0;
        int loads = 0;
        int localControllers = 0;
        int softwareResources = 0;
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

                    var localController = this.createPart(equipmentPart, CybergridMLLibElementType.LOCALCONTROLLER,
                            "LC_" + equipment.id(), editingContext);
                    localControllers++;

                    if (craView != null) {
                        String categoryCode = this.getCategoryCode(type);
                        var softwareResource = this.createPart(craView, CybergridMLLibElementType.SOFTWARERESOURCE,
                                "SR_" + categoryCode + "_" + equipment.id(), editingContext);
                        softwareResources++;

                        String substationId = substationIdByBusId.get(equipment.busId());
                        if (substationId != null) {
                            var computingResource = computingResourcesBySubstationId.get(substationId);
                            if (computingResource != null) {
                                this.createAllocation(softwareResource, computingResource);
                            }
                        }

                        // Create LocalController -> SoftwareResource abstraction in CFRA view
                        if (cfraView != null) {
                            this.createAllocationIn(cfraView, localController, softwareResource);
                        }
                    }

                    if (type == CybergridMLLibElementType.GENERATOR) {
                        generators++;
                    } else {
                        loads++;
                    }
                }
            }
        }
        return new EquipmentCounts(generators, loads, localControllers, softwareResources);
    }

    /** Pass 3: create terminals and their LocalControllers. */
    private TerminalCounts createTerminals(Element craView, Element cfraView, XiidmNetwork network, IEditingContext editingContext,
            Map<String, PartUsage> busesByXiidmId, Map<String, PartUsage> computingResourcesBySubstationId,
            Map<String, String> substationIdByBusId, List<String> warnings) {
        int terminals = 0;
        int localControllers = 0;
        int softwareResources = 0;
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
                String terminalId = branch.id() + "_" + side;
                var terminal = this.createPart(busPart, CybergridMLLibElementType.TERMINAL, terminalId, editingContext);
                this.setStringValue(terminal, branch.id());
                this.setIntegerValue(terminal, side);
                terminals++;

                var localController = this.createPart(terminal, CybergridMLLibElementType.LOCALCONTROLLER,
                        "LC_" + terminalId, editingContext);
                localControllers++;

                if (craView != null) {
                    var softwareResource = this.createPart(craView, CybergridMLLibElementType.SOFTWARERESOURCE,
                            "SR_tie_" + terminalId, editingContext);
                    softwareResources++;

                    String substationId = substationIdByBusId.get(busId);
                    if (substationId != null) {
                        var computingResource = computingResourcesBySubstationId.get(substationId);
                        if (computingResource != null) {
                            this.createAllocation(softwareResource, computingResource);
                        }
                    }

                    // Create LocalController -> SoftwareResource abstraction in CFRA view
                    if (cfraView != null) {
                        this.createAllocationIn(cfraView, localController, softwareResource);
                    }
                }
            }
        }
        return new TerminalCounts(terminals, localControllers, softwareResources);
    }

    private PartUsage createPart(Element owner, CybergridMLLibElementType type, String xiidmId, IEditingContext editingContext) {
        String name = this.uniqueName(owner, sanitizeName(xiidmId, type));

        var featureMembership = SysmlFactory.eINSTANCE.createFeatureMembership();
        owner.getOwnedRelationship().add(featureMembership);
        var partUsage = SysmlFactory.eINSTANCE.createPartUsage();
        featureMembership.getOwnedRelatedElement().add(partUsage);
        new CybergridMLElementInitializerSwitch(editingContext, type).doSwitch(partUsage);
        partUsage.setDeclaredName(name);

        // MembershipExpose makes elements appear in the diagram
        if (owner instanceof ViewUsage viewUsage) {
            var membershipExpose = SysmlFactory.eINSTANCE.createMembershipExpose();
            membershipExpose.setImportedMembership(featureMembership);
            membershipExpose.setIsRecursive(false);
            viewUsage.getOwnedRelationship().add(membershipExpose);
        }

        return partUsage;
    }

    /** Converts XIIDM id to valid SysML name (e.g. B1-G -> B1_G). */
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

    private void setIntegerValue(Element owner, int value) {
        this.findAttribute(owner, "side").ifPresent(attribute -> {
            var literal = SysmlFactory.eINSTANCE.createLiteralInteger();
            literal.setValue(value);
            this.setFeatureValue(this.valueHolder(attribute), literal);
        });
    }

    private void setStringValue(Element owner, String value) {
        if (value == null) {
            return;
        }
        this.findAttribute(owner, "branch").ifPresent(attribute -> {
            var literal = SysmlFactory.eINSTANCE.createLiteralString();
            literal.setValue(value);
            this.setFeatureValue(this.valueHolder(attribute), literal);
        });
    }

    /** For NFP_* types, value is on nested precision attribute. */
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

    private String getCategoryCode(CybergridMLLibElementType type) {
        return switch (type) {
            case GENERATOR -> "gen";
            case LOAD -> "loa";
            case BATTERY -> "bat";
            case TERMINAL -> "tie";
            default -> "eqp";
        };
    }

    private Element findCraView(Element grid) {
        return this.findSiblingView(grid, "CRA");
    }

    private Element findCfraView(Element grid) {
        return this.findSiblingView(grid, "CFRA");
    }

    private Element findSiblingView(Element grid, String viewName) {
        Namespace cfaView = grid.getOwningNamespace();
        if (cfaView == null) {
            return null;
        }

        Namespace parentPackage = cfaView.getOwningNamespace();
        if (parentPackage == null) {
            return null;
        }

        return parentPackage.getOwnedMember().stream()
                .filter(ViewUsage.class::isInstance)
                .filter(member -> viewName.equals(member.getDeclaredName()))
                .findFirst()
                .orElse(null);
    }

    private void createAllocation(PartUsage source, PartUsage target) {
        var owner = source.getOwner();
        if (owner == null) {
            return;
        }

        var owningMembership = SysmlFactory.eINSTANCE.createOwningMembership();
        owner.getOwnedRelationship().add(owningMembership);

        var allocation = SysmlFactory.eINSTANCE.createAllocationUsage();
        owningMembership.getOwnedRelatedElement().add(allocation);

        this.addEndToAllocation(allocation, source);
        this.addEndToAllocation(allocation, target);
    }

    /**CFRA: Creates an allocation with an explicit owner (used for cross-view abstractions). */
    private void createAllocationIn(Element owner, PartUsage source, PartUsage target) {
        if (owner == null) {
            return;
        }

        var owningMembership = SysmlFactory.eINSTANCE.createOwningMembership();
        owner.getOwnedRelationship().add(owningMembership);

        var allocation = SysmlFactory.eINSTANCE.createAllocationUsage();
        owningMembership.getOwnedRelatedElement().add(allocation);

        this.addEndToAllocation(allocation, source);
        this.addEndToAllocation(allocation, target);
    }

    private void addEndToAllocation(AllocationUsage allocation, Usage end) {
        var endFeatureMembership = SysmlFactory.eINSTANCE.createEndFeatureMembership();
        allocation.getOwnedRelationship().add(endFeatureMembership);

        var feature = SysmlFactory.eINSTANCE.createFeature();
        endFeatureMembership.getOwnedRelatedElement().add(feature);

        // Name the feature after the referenced element for clarity in UI
        if (end.getDeclaredName() != null) {
            feature.setDeclaredName(end.getDeclaredName());
        }

        var referenceSubsetting = SysmlFactory.eINSTANCE.createReferenceSubsetting();
        feature.getOwnedRelationship().add(referenceSubsetting);
        referenceSubsetting.setReferencedFeature(end);
    }

    private record SubstationCounts(int buses, int computingResources) {
    }

    private record EquipmentCounts(int generators, int loads, int localControllers, int softwareResources) {
    }

    private record TerminalCounts(int terminals, int localControllers, int softwareResources) {
    }
}
