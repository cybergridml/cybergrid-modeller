package cea.fr.NacreRTE.services;

import cea.fr.NacreRTE.library.NacreLibElementType;
import cea.fr.NacreRTE.library.util.NacreDefinitionsUtil;
import cea.fr.NacreRTE.library.util.NacrePropertyUtil;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.syson.services.ElementInitializerSwitch;
import org.eclipse.syson.services.UtilService;
import org.eclipse.syson.sysml.*;


public class NacreElementInitializerSwitch extends ElementInitializerSwitch {

    private final UtilService utilService;
    private final IEditingContext editingContext;
    private final NacreLibElementType nacreLibElementType;


    public NacreElementInitializerSwitch(IEditingContext editingContext, NacreLibElementType nacreLibElementType) {
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
        if (nacreLibElementType == NacreLibElementType.MAINGRID) {
            Definition maingridDefinition = NacreDefinitionsUtil.getMaingridDefinition(editingContext);
            utilService.setFeatureTyping(usage, maingridDefinition);
            NacrePropertyUtil.addAttributesFromDefinition(usage,maingridDefinition);

        }
        if (nacreLibElementType == NacreLibElementType.ZONE) {
            Definition zoneDefinition = NacreDefinitionsUtil.getZoneDefinition(editingContext);
            utilService.setFeatureTyping(usage, zoneDefinition);
            NacrePropertyUtil.addAttributesFromDefinition(usage,zoneDefinition);
        }
        if (nacreLibElementType == NacreLibElementType.ZONALCONTROlLER) {
            Definition zonalcontollerDefinition = NacreDefinitionsUtil.getZONALCONTROlLERDefinition(editingContext);
            utilService.setFeatureTyping(usage, zonalcontollerDefinition);
            NacrePropertyUtil.addAttributesFromDefinition(usage,zonalcontollerDefinition);
        }
        if (nacreLibElementType == NacreLibElementType.CENTRALCONTROLLER) {
            Definition centralcontollerDefinition = NacreDefinitionsUtil.getCentralControllerDefinition(editingContext);
            utilService.setFeatureTyping(usage, centralcontollerDefinition);
            NacrePropertyUtil.addAttributesFromDefinition(usage,centralcontollerDefinition);
        }
        if (nacreLibElementType == NacreLibElementType.SUBSTATION) {
            Definition substationDefinition = NacreDefinitionsUtil.getSubstationDefinition(editingContext);
            utilService.setFeatureTyping(usage, substationDefinition);
            NacrePropertyUtil.addAttributesFromDefinition(usage,substationDefinition);
        }
        if (nacreLibElementType == NacreLibElementType.COMMUNICATIONMEDIA) {
            Definition communicationMediaDefinition = NacreDefinitionsUtil.getCommunicationMediaDefinition(editingContext);
            utilService.setFeatureTyping(usage, communicationMediaDefinition);
            NacrePropertyUtil.addAttributesFromDefinition(usage,communicationMediaDefinition);
        }
        if (nacreLibElementType == NacreLibElementType.COMPUTINGRESOURCE) {
            Definition computingResourceDefinition = NacreDefinitionsUtil.getComputingResourceDefinition(editingContext);
            utilService.setFeatureTyping(usage, computingResourceDefinition);
            NacrePropertyUtil.addAttributesFromDefinition(usage,computingResourceDefinition);

        }
        if (nacreLibElementType == NacreLibElementType.SOFTWARERESOURCE) {
            Definition softwareResourceDefinition = NacreDefinitionsUtil.getSoftwareResourceDefinition(editingContext);
            utilService.setFeatureTyping(usage, softwareResourceDefinition);
            NacrePropertyUtil.addAttributesFromDefinition(usage,softwareResourceDefinition);

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





    private long countElements(Element element, NacreLibElementType nacreLibElementType) {
        Namespace owningNamespace = element.getOwningNamespace();
        boolean doFilterDefinitions = element instanceof Definition;
        if (owningNamespace != null) {
            return owningNamespace.getOwnedMember().stream()
                    .filter(namespaceElt -> doFilterDefinitions ? namespaceElt instanceof Definition : namespaceElt instanceof Usage)
                    .filter(namespaceElt -> namespaceElt instanceof Feature f && nacreLibElementType == NacreLibElementType.getNacreElementFromUsage(f))
                    .count();
        }
        return 0;
    }





}
