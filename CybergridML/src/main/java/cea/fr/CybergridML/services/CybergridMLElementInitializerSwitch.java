package cea.fr.CybergridML.services;

import cea.fr.CybergridML.domain.CybergridMLLibElementType;
import cea.fr.CybergridML.domain.util.CybergridMLDefinitionsUtil;
import cea.fr.CybergridML.domain.util.CybergridMLPropertyUtil;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.syson.services.ElementInitializerSwitch;
import org.eclipse.syson.services.UtilService;
import org.eclipse.syson.sysml.*;


public class CybergridMLElementInitializerSwitch extends ElementInitializerSwitch {

    private final UtilService utilService;
    private final IEditingContext editingContext;
    private final CybergridMLLibElementType cybergridMLLibElementType;

    public CybergridMLElementInitializerSwitch(IEditingContext editingContext, CybergridMLLibElementType cybergridMLLibElementType) {
        super();
        this.utilService = new UtilService();
        this.editingContext = editingContext;
        this.cybergridMLLibElementType = cybergridMLLibElementType;
    }

    @Override
    public Element caseUsage(Usage usage) {
        if (cybergridMLLibElementType != null) {
            long existingElements = this.countElements(usage, cybergridMLLibElementType);
            usage.setDeclaredName(cybergridMLLibElementType.getName().toLowerCase() + existingElements);
        }
        usage.setIsComposite(true);
        return usage;
    }

    @Override
    public Element casePartUsage(PartUsage usage) {
        if (cybergridMLLibElementType == CybergridMLLibElementType.MAINGRID) {
            Definition maingridDefinition = CybergridMLDefinitionsUtil.getMaingridDefinition(editingContext);
            utilService.setFeatureTyping(usage, maingridDefinition);
            CybergridMLPropertyUtil.addAttributesFromDefinition(usage, maingridDefinition);
        }
        if (cybergridMLLibElementType == CybergridMLLibElementType.ZONE) {
            Definition zoneDefinition = CybergridMLDefinitionsUtil.getZoneDefinition(editingContext);
            utilService.setFeatureTyping(usage, zoneDefinition);
            CybergridMLPropertyUtil.addAttributesFromDefinition(usage, zoneDefinition);
        }
        if (cybergridMLLibElementType == CybergridMLLibElementType.ZONALCONTROLLER) {
            Definition zonalControllerDefinition = CybergridMLDefinitionsUtil.getZonalControllerDefinition(editingContext);
            utilService.setFeatureTyping(usage, zonalControllerDefinition);
            CybergridMLPropertyUtil.addAttributesFromDefinition(usage, zonalControllerDefinition);
        }
        if (cybergridMLLibElementType == CybergridMLLibElementType.LOCALCONTROLLER) {
            Definition localControllerDefinition = CybergridMLDefinitionsUtil.getLocalControllerDefinition(editingContext);
            utilService.setFeatureTyping(usage, localControllerDefinition);
            CybergridMLPropertyUtil.addAttributesFromDefinition(usage, localControllerDefinition);
        }
        if (cybergridMLLibElementType == CybergridMLLibElementType.CENTRALCONTROLLER) {
            Definition centralcontollerDefinition = CybergridMLDefinitionsUtil.getCentralControllerDefinition(editingContext);
            utilService.setFeatureTyping(usage, centralcontollerDefinition);
            CybergridMLPropertyUtil.addAttributesFromDefinition(usage, centralcontollerDefinition);
        }
        if (cybergridMLLibElementType == CybergridMLLibElementType.SUBSTATION) {
            Definition substationDefinition = CybergridMLDefinitionsUtil.getSubstationDefinition(editingContext);
            utilService.setFeatureTyping(usage, substationDefinition);
            CybergridMLPropertyUtil.addAttributesFromDefinition(usage, substationDefinition);
        }
        if (cybergridMLLibElementType == CybergridMLLibElementType.GENERATOR) {
            Definition generatorDefinition = CybergridMLDefinitionsUtil.getGeneratorDefinition(editingContext);
            utilService.setFeatureTyping(usage, generatorDefinition);
            CybergridMLPropertyUtil.addAttributesFromDefinition(usage, generatorDefinition);
        }
        if (cybergridMLLibElementType == CybergridMLLibElementType.BATTERY) {
            Definition BatteryDefinition = CybergridMLDefinitionsUtil.getBatteryDefinition(editingContext);
            utilService.setFeatureTyping(usage, BatteryDefinition);
            CybergridMLPropertyUtil.addAttributesFromDefinition(usage, BatteryDefinition);
        }
        if (cybergridMLLibElementType == CybergridMLLibElementType.BUS) {
            Definition busDefinition = CybergridMLDefinitionsUtil.getBusDefinition(editingContext);
            utilService.setFeatureTyping(usage, busDefinition);
            CybergridMLPropertyUtil.addAttributesFromDefinition(usage, busDefinition);
        }
        if (cybergridMLLibElementType == CybergridMLLibElementType.LOAD) {
            Definition loadDefinition = CybergridMLDefinitionsUtil.getLoadDefinition(editingContext);
            utilService.setFeatureTyping(usage, loadDefinition);
            CybergridMLPropertyUtil.addAttributesFromDefinition(usage, loadDefinition);
        }
        if (cybergridMLLibElementType == CybergridMLLibElementType.LINE) {
            Definition lineDefinition = CybergridMLDefinitionsUtil.getLineDefinition(editingContext);
            utilService.setFeatureTyping(usage, lineDefinition);
            CybergridMLPropertyUtil.addAttributesFromDefinition(usage, lineDefinition);
        }
        if (cybergridMLLibElementType == CybergridMLLibElementType.TERMINAL) {
            Definition terminalDefinition = CybergridMLDefinitionsUtil.getTerminalDefinition(editingContext);
            utilService.setFeatureTyping(usage, terminalDefinition);
            CybergridMLPropertyUtil.addAttributesFromDefinition(usage, terminalDefinition);
        }
        if (cybergridMLLibElementType == CybergridMLLibElementType.COMMUNICATIONMEDIA) {
            Definition communicationMediaDefinition = CybergridMLDefinitionsUtil.getCommunicationMediaDefinition(editingContext);
            utilService.setFeatureTyping(usage, communicationMediaDefinition);
            CybergridMLPropertyUtil.addAttributesFromDefinition(usage, communicationMediaDefinition);
        }
        if (cybergridMLLibElementType == CybergridMLLibElementType.COMPUTINGRESOURCE) {
            Definition computingResourceDefinition = CybergridMLDefinitionsUtil.getComputingResourceDefinition(editingContext);
            utilService.setFeatureTyping(usage, computingResourceDefinition);
            CybergridMLPropertyUtil.addAttributesFromDefinition(usage, computingResourceDefinition);
        }
        if (cybergridMLLibElementType == CybergridMLLibElementType.SOFTWARERESOURCE) {
            Definition softwareResourceDefinition = CybergridMLDefinitionsUtil.getSoftwareResourceDefinition(editingContext);
            utilService.setFeatureTyping(usage, softwareResourceDefinition);
            CybergridMLPropertyUtil.addAttributesFromDefinition(usage, softwareResourceDefinition);
        }
        if (cybergridMLLibElementType == CybergridMLLibElementType.REDUNDANCYCLUSTER) {
            Definition redundancyClusterDefinition = CybergridMLDefinitionsUtil.getRedundancyClusterDefinition(editingContext);
            utilService.setFeatureTyping(usage, redundancyClusterDefinition);
            CybergridMLPropertyUtil.addAttributesFromDefinition(usage, redundancyClusterDefinition);
        }
        caseUsage(usage);
        return usage;
    }

    @Override
    public Element caseDefinition(Definition definition) {
        if (cybergridMLLibElementType != null) {
            long elementsNb = this.countElements(definition, cybergridMLLibElementType);
            definition.setDeclaredName(cybergridMLLibElementType.getName().toLowerCase() + elementsNb);
        }
        return definition;
    }

    private long countElements(Element element, CybergridMLLibElementType cybergridMLLibElementType) {
        Namespace owningNamespace = element.getOwningNamespace();
        boolean doFilterDefinitions = element instanceof Definition;
        if (owningNamespace != null) {
            return owningNamespace.getOwnedMember().stream()
                    .filter(namespaceElt -> doFilterDefinitions ? namespaceElt instanceof Definition : namespaceElt instanceof Usage)
                    .filter(namespaceElt -> namespaceElt instanceof Feature f && cybergridMLLibElementType == CybergridMLLibElementType.getCybergridMLElementFromUsage(f))
                    .count();
        }
        return 0;
    }
}

