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
    private final CybergridMLLibElementType nacreLibElementType;

    public CybergridMLElementInitializerSwitch(IEditingContext editingContext, CybergridMLLibElementType nacreLibElementType) {
        super();
        this.utilService = new UtilService();
        this.editingContext = editingContext;
        this.nacreLibElementType = nacreLibElementType;
    }

    @Override
    public Element caseUsage(Usage usage) {
        if (nacreLibElementType != null) {
            long existingElements = this.countElements(usage, nacreLibElementType);
            usage.setDeclaredName(nacreLibElementType.getName().toLowerCase() + existingElements);
        }
        usage.setIsComposite(true);
        return usage;
    }

    @Override
    public Element casePartUsage(PartUsage usage) {
        if (nacreLibElementType == CybergridMLLibElementType.MAINGRID) {
            Definition maingridDefinition = CybergridMLDefinitionsUtil.getMaingridDefinition(editingContext);
            utilService.setFeatureTyping(usage, maingridDefinition);
            CybergridMLPropertyUtil.addAttributesFromDefinition(usage, maingridDefinition);
        }
        if (nacreLibElementType == CybergridMLLibElementType.ZONE) {
            Definition zoneDefinition = CybergridMLDefinitionsUtil.getZoneDefinition(editingContext);
            utilService.setFeatureTyping(usage, zoneDefinition);
            CybergridMLPropertyUtil.addAttributesFromDefinition(usage, zoneDefinition);
        }
        if (nacreLibElementType == CybergridMLLibElementType.ZONALCONTROLLER) {
            Definition zonalControllerDefinition = CybergridMLDefinitionsUtil.getZonalControllerDefinition(editingContext);
            utilService.setFeatureTyping(usage, zonalControllerDefinition);
            CybergridMLPropertyUtil.addAttributesFromDefinition(usage, zonalControllerDefinition);
        }
        if (nacreLibElementType == CybergridMLLibElementType.LOCALCONTROLLER) {
            Definition localControllerDefinition = CybergridMLDefinitionsUtil.getLocalControllerDefinition(editingContext);
            utilService.setFeatureTyping(usage, localControllerDefinition);
            CybergridMLPropertyUtil.addAttributesFromDefinition(usage, localControllerDefinition);
        }
        if (nacreLibElementType == CybergridMLLibElementType.CENTRALCONTROLLER) {
            Definition centralcontollerDefinition = CybergridMLDefinitionsUtil.getCentralControllerDefinition(editingContext);
            utilService.setFeatureTyping(usage, centralcontollerDefinition);
            CybergridMLPropertyUtil.addAttributesFromDefinition(usage, centralcontollerDefinition);
        }
        if (nacreLibElementType == CybergridMLLibElementType.SUBSTATION) {
            Definition substationDefinition = CybergridMLDefinitionsUtil.getSubstationDefinition(editingContext);
            utilService.setFeatureTyping(usage, substationDefinition);
            CybergridMLPropertyUtil.addAttributesFromDefinition(usage, substationDefinition);
        }
        if (nacreLibElementType == CybergridMLLibElementType.GENERATOR) {
            Definition generatorDefinition = CybergridMLDefinitionsUtil.getGeneratorDefinition(editingContext);
            utilService.setFeatureTyping(usage, generatorDefinition);
            CybergridMLPropertyUtil.addAttributesFromDefinition(usage, generatorDefinition);
        }
        if (nacreLibElementType == CybergridMLLibElementType.BATTERY) {
            Definition BatteryDefinition = CybergridMLDefinitionsUtil.getBatteryDefinition(editingContext);
            utilService.setFeatureTyping(usage, BatteryDefinition);
            CybergridMLPropertyUtil.addAttributesFromDefinition(usage, BatteryDefinition);
        }
        if (nacreLibElementType == CybergridMLLibElementType.BUS) {
            Definition busDefinition = CybergridMLDefinitionsUtil.getBusDefinition(editingContext);
            utilService.setFeatureTyping(usage, busDefinition);
            CybergridMLPropertyUtil.addAttributesFromDefinition(usage, busDefinition);
        }
        if (nacreLibElementType == CybergridMLLibElementType.LOAD) {
            Definition loadDefinition = CybergridMLDefinitionsUtil.getLoadDefinition(editingContext);
            utilService.setFeatureTyping(usage, loadDefinition);
            CybergridMLPropertyUtil.addAttributesFromDefinition(usage, loadDefinition);
        }
        if (nacreLibElementType == CybergridMLLibElementType.LINE) {
            Definition lineDefinition = CybergridMLDefinitionsUtil.getLineDefinition(editingContext);
            utilService.setFeatureTyping(usage, lineDefinition);
            CybergridMLPropertyUtil.addAttributesFromDefinition(usage, lineDefinition);
        }
        if (nacreLibElementType == CybergridMLLibElementType.TERMINAL) {
            Definition terminalDefinition = CybergridMLDefinitionsUtil.getTerminalDefinition(editingContext);
            utilService.setFeatureTyping(usage, terminalDefinition);
            CybergridMLPropertyUtil.addAttributesFromDefinition(usage, terminalDefinition);
        }
        if (nacreLibElementType == CybergridMLLibElementType.COMMUNICATIONMEDIA) {
            Definition communicationMediaDefinition = CybergridMLDefinitionsUtil.getCommunicationMediaDefinition(editingContext);
            utilService.setFeatureTyping(usage, communicationMediaDefinition);
            CybergridMLPropertyUtil.addAttributesFromDefinition(usage, communicationMediaDefinition);
        }
        if (nacreLibElementType == CybergridMLLibElementType.COMPUTINGRESOURCE) {
            Definition computingResourceDefinition = CybergridMLDefinitionsUtil.getComputingResourceDefinition(editingContext);
            utilService.setFeatureTyping(usage, computingResourceDefinition);
            CybergridMLPropertyUtil.addAttributesFromDefinition(usage, computingResourceDefinition);
        }
        if (nacreLibElementType == CybergridMLLibElementType.SOFTWARERESOURCE) {
            Definition softwareResourceDefinition = CybergridMLDefinitionsUtil.getSoftwareResourceDefinition(editingContext);
            utilService.setFeatureTyping(usage, softwareResourceDefinition);
            CybergridMLPropertyUtil.addAttributesFromDefinition(usage, softwareResourceDefinition);
        }
        if (nacreLibElementType == CybergridMLLibElementType.REDUNDANCYCLUSTER) {
            Definition redundancyClusterDefinition = CybergridMLDefinitionsUtil.getRedundancyClusterDefinition(editingContext);
            utilService.setFeatureTyping(usage, redundancyClusterDefinition);
            CybergridMLPropertyUtil.addAttributesFromDefinition(usage, redundancyClusterDefinition);
        }
        caseUsage(usage);
        return usage;
    }

    @Override
    public Element caseDefinition(Definition definition) {
        if (nacreLibElementType != null) {
            long elementsNb = this.countElements(definition, nacreLibElementType);
            definition.setDeclaredName(nacreLibElementType.getName().toLowerCase() + elementsNb);
        }
        return definition;
    }

    private long countElements(Element element, CybergridMLLibElementType nacreLibElementType) {
        Namespace owningNamespace = element.getOwningNamespace();
        boolean doFilterDefinitions = element instanceof Definition;
        if (owningNamespace != null) {
            return owningNamespace.getOwnedMember().stream()
                    .filter(namespaceElt -> doFilterDefinitions ? namespaceElt instanceof Definition : namespaceElt instanceof Usage)
                    .filter(namespaceElt -> namespaceElt instanceof Feature f && nacreLibElementType == CybergridMLLibElementType.getNacreElementFromUsage(f))
                    .count();
        }
        return 0;
    }
}

