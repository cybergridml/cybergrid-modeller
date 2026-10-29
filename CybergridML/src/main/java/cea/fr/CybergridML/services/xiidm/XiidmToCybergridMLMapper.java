/*******************************************************************************
 * Copyright (c) 2026 CEA LIST - RTE.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     CEA LIST - Initial API and implementation
 *******************************************************************************/

package cea.fr.CybergridML.services.xiidm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import cea.fr.CybergridML.domain.CybergridMLDefinitionsUtil;
import cea.fr.CybergridML.domain.CybergridMLLibElementType;
import cea.fr.CybergridML.domain.CybergridMLPropertyUtil;
import cea.fr.CybergridML.services.CybergridMLElementInitializerSwitch;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.syson.services.UtilService;
import org.eclipse.syson.sysml.AllocationUsage;
import org.eclipse.syson.sysml.AttributeUsage;
import org.eclipse.syson.sysml.Definition;
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

    private final UtilService utilService = new UtilService();

    public XiidmImportResult importInto(Element grid, XiidmNetwork network, IEditingContext editingContext) {
        var warnings = new ArrayList<String>();
        var createdSubstations = new ArrayList<Element>();
        var busesByXiidmId = new LinkedHashMap<String, PartUsage>();
        var computingResourcesBySubstationId = new LinkedHashMap<String, PartUsage>();
        var substationIdByBusId = new LinkedHashMap<String, String>();

        var session = new ImportSession(editingContext);
        Element craView = this.findCraView(grid);
        Element cfraView = this.findCfraView(grid);

        var substationCounts = this.createSubstationsAndBuses(grid, craView, network, session, createdSubstations,
                busesByXiidmId, computingResourcesBySubstationId, substationIdByBusId, warnings);
        var equipmentCounts = this.createEquipments(craView, cfraView, network, session, busesByXiidmId,
                computingResourcesBySubstationId, substationIdByBusId, warnings);
        var terminalCounts = this.createTerminals(craView, cfraView, network, session, busesByXiidmId,
                computingResourcesBySubstationId, substationIdByBusId, warnings);

        if (session.skippedOnOtherBuses > 0) {
            warnings.add("Only the first bus of each substation is imported: %d equipment(s) and terminal(s) on the other buses were skipped."
                    .formatted(session.skippedOnOtherBuses));
        }

        int totalLocalControllers = equipmentCounts.localControllers() + terminalCounts.localControllers();
        int totalSoftwareResources = equipmentCounts.softwareResources() + terminalCounts.softwareResources();
        return new XiidmImportResult(List.copyOf(createdSubstations), createdSubstations.size(),
                substationCounts.buses(), equipmentCounts.generators(), equipmentCounts.loads(),
                terminalCounts.terminals(), totalLocalControllers,
                substationCounts.computingResources(), totalSoftwareResources, List.copyOf(warnings));
    }

    /** Pass 1: create substations and their first bus (the other buses of a substation are not imported). */
    private SubstationCounts createSubstationsAndBuses(Element grid, Element craView, XiidmNetwork network, ImportSession session,
            List<Element> createdSubstations, Map<String, PartUsage> busesByXiidmId,
            Map<String, PartUsage> computingResourcesBySubstationId, Map<String, String> substationIdByBusId, List<String> warnings) {
        int busCount = 0;
        int computingResources = 0;
        for (XiidmNetwork.Substation substation : network.substations()) {
            var substationPart = this.createPart(grid, CybergridMLLibElementType.SUBSTATION, substation.id(), session);
            createdSubstations.add(substationPart);

            if (craView != null) {
                var computingResource = this.createPart(craView, CybergridMLLibElementType.COMPUTINGRESOURCE,
                        "CR_" + substation.id(), session);
                computingResourcesBySubstationId.put(substation.id(), computingResource);
                computingResources++;
            }

            // Only one voltage level matters per substation: its first bus is imported, the others are ignored.
            var firstBus = this.findFirstBus(substation);
            String firstBusId = firstBus.map(first -> first.bus().id()).orElse(null);
            substation.voltageLevels().stream()
                    .flatMap(voltageLevel -> voltageLevel.buses().stream())
                    .map(XiidmNetwork.Bus::id)
                    .filter(busId -> busId != null && !busId.equals(firstBusId))
                    .forEach(session.otherBusIds::add);
            if (firstBus.isEmpty()) {
                continue;
            }
            var bus = firstBus.get().bus();
            if (busesByXiidmId.containsKey(bus.id())) {
                warnings.add("Bus '%s' is declared more than once; only the first one was imported.".formatted(bus.id()));
                continue;
            }
            var busPart = this.createPart(substationPart, CybergridMLLibElementType.BUS, bus.id(), session);
            this.setNumericValue(busPart, "nominalLevel", firstBus.get().voltageLevel().nominalV());
            busesByXiidmId.put(bus.id(), busPart);
            substationIdByBusId.put(bus.id(), substation.id());
            busCount++;
        }
        return new SubstationCounts(busCount, computingResources);
    }

    /** Pass 2: create generators, loads, and their LocalControllers. */
    private EquipmentCounts createEquipments(Element craView, Element cfraView, XiidmNetwork network, ImportSession session,
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
                    if (busPart == null && session.otherBusIds.contains(equipment.busId())) {
                        session.skippedOnOtherBuses++;
                        continue;
                    }
                    if (busPart == null) {
                        warnings.add("'%s' references the unknown bus '%s' and was skipped.".formatted(equipment.id(), equipment.busId()));
                        continue;
                    }
                    var type = equipment.kind() == XiidmNetwork.EquipmentKind.GENERATOR
                            ? CybergridMLLibElementType.GENERATOR
                            : CybergridMLLibElementType.LOAD;
                    var equipmentPart = this.createPart(busPart, type, equipment.id(), session);
                    this.setNumericValue(equipmentPart, "p", equipment.p());
                    this.setNumericValue(equipmentPart, "q", equipment.q());

                    var localController = this.createPart(equipmentPart, CybergridMLLibElementType.LOCALCONTROLLER,
                            "LC_" + equipment.id(), session);
                    localControllers++;

                    if (craView != null) {
                        String categoryCode = this.getCategoryCode(type);
                        var softwareResource = this.createPart(craView, CybergridMLLibElementType.SOFTWARERESOURCE,
                                "SR_" + categoryCode + "_" + equipment.id(), session);
                        softwareResources++;

                        String substationId = substationIdByBusId.get(equipment.busId());
                        if (substationId != null) {
                            var computingResource = computingResourcesBySubstationId.get(substationId);
                            if (computingResource != null) {
                                this.createAllocation(softwareResource, computingResource);
                            }
                        }

                        // Create SoftwareResource -> LocalController abstraction in CFRA view
                        if (cfraView != null) {
                            this.createAllocationIn(cfraView, softwareResource, localController);
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
    private TerminalCounts createTerminals(Element craView, Element cfraView, XiidmNetwork network, ImportSession session,
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
                if (busPart == null && session.otherBusIds.contains(busId)) {
                    session.skippedOnOtherBuses++;
                    continue;
                }
                if (busPart == null) {
                    warnings.add("'%s' references the unknown bus '%s' on side %d; that terminal was skipped."
                            .formatted(branch.id(), busId, side));
                    continue;
                }
                String terminalId = branch.id() + "_" + side;
                var terminal = this.createPart(busPart, CybergridMLLibElementType.TERMINAL, terminalId, session);
                this.setStringValue(terminal, branch.id());
                this.setIntegerValue(terminal, side);
                terminals++;

                var localController = this.createPart(terminal, CybergridMLLibElementType.LOCALCONTROLLER,
                        "LC_" + terminalId, session);
                localControllers++;

                if (craView != null) {
                    var softwareResource = this.createPart(craView, CybergridMLLibElementType.SOFTWARERESOURCE,
                            "SR_tie_" + terminalId, session);
                    softwareResources++;

                    String substationId = substationIdByBusId.get(busId);
                    if (substationId != null) {
                        var computingResource = computingResourcesBySubstationId.get(substationId);
                        if (computingResource != null) {
                            this.createAllocation(softwareResource, computingResource);
                        }
                    }

                    // Create SoftwareResource -> LocalController abstraction in CFRA view
                    if (cfraView != null) {
                        this.createAllocationIn(cfraView, softwareResource, localController);
                    }
                }
            }
        }
        return new TerminalCounts(terminals, localControllers, softwareResources);
    }

    private PartUsage createPart(Element owner, CybergridMLLibElementType type, String xiidmId, ImportSession session) {
        String name = session.uniqueName(owner, sanitizeName(xiidmId, type));

        var partUsage = SysmlFactory.eINSTANCE.createPartUsage();
        Definition definition = CybergridMLDefinitionsUtil.getDefinition(session.editingContext, type);
        this.utilService.setFeatureTyping(partUsage, definition);
        CybergridMLPropertyUtil.addAttributesFromDefinition(partUsage, definition);
        partUsage.setIsComposite(true);
        partUsage.setDeclaredName(name);

        var featureMembership = SysmlFactory.eINSTANCE.createFeatureMembership();
        featureMembership.getOwnedRelatedElement().add(partUsage);
        owner.getOwnedRelationship().add(featureMembership);

        // MembershipExpose makes elements appear in the diagram
//        if (owner instanceof ViewUsage viewUsage) {
//            var membershipExpose = SysmlFactory.eINSTANCE.createMembershipExpose();
//            membershipExpose.setImportedMembership(featureMembership);
//            membershipExpose.setIsRecursive(false);
//            viewUsage.getOwnedRelationship().add(membershipExpose);
//        }

        return partUsage;
    }

    /** First bus (with an id) of the substation, in file order, with its voltage level. */
    private Optional<BusInVoltageLevel> findFirstBus(XiidmNetwork.Substation substation) {
        return substation.voltageLevels().stream()
                .flatMap(voltageLevel -> voltageLevel.buses().stream()
                        .filter(bus -> bus.id() != null && !bus.id().isBlank())
                        .map(bus -> new BusInVoltageLevel(voltageLevel, bus)))
                .findFirst();
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

    /** State kept for one import, so creating a part costs the same whatever the size of the model. */
    private static final class ImportSession {

        private final IEditingContext editingContext;

        private final Map<Element, Set<String>> usedNamesByOwner = new HashMap<>();

        /** Buses that are not the first one of their substation: they are not imported, nor what is connected to them. */
        private final Set<String> otherBusIds = new HashSet<>();

        private int skippedOnOtherBuses;

        ImportSession(IEditingContext editingContext) {
            this.editingContext = editingContext;
        }

        /** Adds a suffix if the name is already used under this owner (e.g. B1 -> B1_2). Owner names are read once per import. */
        String uniqueName(Element owner, String baseName) {
            Set<String> usedNames = this.usedNamesByOwner.computeIfAbsent(owner, o -> o.getOwnedRelationship().stream()
                    .filter(FeatureMembership.class::isInstance)
                    .map(FeatureMembership.class::cast)
                    .flatMap(featureMembership -> featureMembership.getOwnedRelatedElement().stream())
                    .map(Element::getDeclaredName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(HashSet::new)));
            String name = baseName;
            int index = 2;
            while (usedNames.contains(name)) {
                name = baseName + "_" + index++;
            }
            usedNames.add(name);
            return name;
        }
    }

    private record BusInVoltageLevel(XiidmNetwork.VoltageLevel voltageLevel, XiidmNetwork.Bus bus) {
    }

    private record SubstationCounts(int buses, int computingResources) {
    }

    private record EquipmentCounts(int generators, int loads, int localControllers, int softwareResources) {
    }

    private record TerminalCounts(int terminals, int localControllers, int softwareResources) {
    }
}
