package cea.fr.CybergridML.view.services;

import cea.fr.CybergridML.domain.CybergridMLLibElementType;
import cea.fr.CybergridML.services.CybergridMLElementInitializerSwitch;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.springframework.stereotype.Service;
import org.eclipse.sirius.components.core.api.IFeedbackMessageService;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.core.api.IReadOnlyObjectPredicate;
import org.eclipse.syson.diagram.common.view.services.ShowDiagramsInheritedMembersService;
import org.eclipse.syson.diagram.common.view.services.ViewCreateService;
import org.eclipse.syson.sysml.*;
import org.eclipse.syson.sysml.Package;


@Service
public class CybergridMLViewCreateService extends ViewCreateService {

    public CybergridMLViewCreateService(IObjectSearchService objectSearchService,
                                        IReadOnlyObjectPredicate readOnlyObjectPredicate,
                                        ShowDiagramsInheritedMembersService showDiagramsInheritedMembersService,
                                        IFeedbackMessageService feedbackMessageService) {
        super(objectSearchService, readOnlyObjectPredicate, showDiagramsInheritedMembersService, feedbackMessageService);
    }

    @Override
    public boolean canCreateDiagram(Element element) {
        return element instanceof Package || element instanceof ViewUsage;
    }

    public Element elementInitializer(Element element, IEditingContext editingContext, String lcaElementTypeName) {
        CybergridMLElementInitializerSwitch elementInitializerSwitch = new CybergridMLElementInitializerSwitch(editingContext, CybergridMLLibElementType.getCybergridMLElementFromName(lcaElementTypeName));
        return elementInitializerSwitch.doSwitch(element);
    }

    public PartUsage createMainGrid(Element ownerElement, IEditingContext editingContext) {
        var featureMembership = SysmlFactory.eINSTANCE.createFeatureMembership();
        ownerElement.getOwnedRelationship().add(featureMembership);
        var newPartUsage = SysmlFactory.eINSTANCE.createPartUsage();
        featureMembership.getOwnedRelatedElement().add(newPartUsage);
        new CybergridMLElementInitializerSwitch(editingContext, CybergridMLLibElementType.MAINGRID).doSwitch(newPartUsage);
        return newPartUsage;
    }

    public PartUsage createZone(Element ownerElement, IEditingContext editingContext) {
        var featureMembership = SysmlFactory.eINSTANCE.createFeatureMembership();
        ownerElement.getOwnedRelationship().add(featureMembership);
        var newPartUsage = SysmlFactory.eINSTANCE.createPartUsage();
        featureMembership.getOwnedRelatedElement().add(newPartUsage);
        new CybergridMLElementInitializerSwitch(editingContext, CybergridMLLibElementType.ZONE).doSwitch(newPartUsage);
        return newPartUsage;
    }

    public PartUsage createSubstation(Element ownerElement, IEditingContext editingContext) {
        var featureMembership = SysmlFactory.eINSTANCE.createFeatureMembership();
        ownerElement.getOwnedRelationship().add(featureMembership);
        var newPartUsage = SysmlFactory.eINSTANCE.createPartUsage();
        featureMembership.getOwnedRelatedElement().add(newPartUsage);
        new CybergridMLElementInitializerSwitch(editingContext, CybergridMLLibElementType.SUBSTATION).doSwitch(newPartUsage);
        return newPartUsage;
    }

    public PartUsage createCentralController(Element ownerElement, IEditingContext editingContext) {
        var featureMembership = SysmlFactory.eINSTANCE.createFeatureMembership();
        ownerElement.getOwnedRelationship().add(featureMembership);
        var newPartUsage = SysmlFactory.eINSTANCE.createPartUsage();
        featureMembership.getOwnedRelatedElement().add(newPartUsage);
        new CybergridMLElementInitializerSwitch(editingContext, CybergridMLLibElementType.CENTRALCONTROLLER).doSwitch(newPartUsage);
        return newPartUsage;
    }

    public PartUsage createZonalController(Element ownerElement, IEditingContext editingContext) {
        var featureMembership = SysmlFactory.eINSTANCE.createFeatureMembership();
        ownerElement.getOwnedRelationship().add(featureMembership);
        var newPartUsage = SysmlFactory.eINSTANCE.createPartUsage();
        featureMembership.getOwnedRelatedElement().add(newPartUsage);
        new CybergridMLElementInitializerSwitch(editingContext, CybergridMLLibElementType.ZONALCONTROLLER).doSwitch(newPartUsage);
        return newPartUsage;
    }

    public PartUsage createCommunicationMedia(Element ownerElement, IEditingContext editingContext) {
        var featureMembership = SysmlFactory.eINSTANCE.createFeatureMembership();
        ownerElement.getOwnedRelationship().add(featureMembership);
        var newPartUsage = SysmlFactory.eINSTANCE.createPartUsage();
        featureMembership.getOwnedRelatedElement().add(newPartUsage);
        new CybergridMLElementInitializerSwitch(editingContext, CybergridMLLibElementType.COMMUNICATIONMEDIA).doSwitch(newPartUsage);
        return newPartUsage;
    }

    public PartUsage createComputingResource(Element ownerElement, IEditingContext editingContext) {
        var featureMembership = SysmlFactory.eINSTANCE.createFeatureMembership();
        ownerElement.getOwnedRelationship().add(featureMembership);
        var newPartUsage = SysmlFactory.eINSTANCE.createPartUsage();
        featureMembership.getOwnedRelatedElement().add(newPartUsage);
        new CybergridMLElementInitializerSwitch(editingContext, CybergridMLLibElementType.COMPUTINGRESOURCE).doSwitch(newPartUsage);
        return newPartUsage;
    }

    public PartUsage createSoftwareResource(Element ownerElement, IEditingContext editingContext) {
        var featureMembership = SysmlFactory.eINSTANCE.createFeatureMembership();
        ownerElement.getOwnedRelationship().add(featureMembership);
        var newPartUsage = SysmlFactory.eINSTANCE.createPartUsage();
        featureMembership.getOwnedRelatedElement().add(newPartUsage);
        new CybergridMLElementInitializerSwitch(editingContext, CybergridMLLibElementType.SOFTWARERESOURCE).doSwitch(newPartUsage);
        return newPartUsage;
    }
    public PartUsage createRedundancyCluster(Element ownerElement, IEditingContext editingContext) {
        var featureMembership = SysmlFactory.eINSTANCE.createFeatureMembership();
        ownerElement.getOwnedRelationship().add(featureMembership);
        var newPartUsage = SysmlFactory.eINSTANCE.createPartUsage();
        featureMembership.getOwnedRelatedElement().add(newPartUsage);
        new CybergridMLElementInitializerSwitch(editingContext, CybergridMLLibElementType.REDUNDANCYCLUSTER).doSwitch(newPartUsage);
        return newPartUsage;
    }
}
