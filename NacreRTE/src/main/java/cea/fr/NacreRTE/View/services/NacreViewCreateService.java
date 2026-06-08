package cea.fr.NacreRTE.view.services;

import cea.fr.NacreRTE.domain.NacreLibElementType;
import cea.fr.NacreRTE.services.NacreElementInitializerSwitch;
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
public class NacreViewCreateService extends ViewCreateService {

    public NacreViewCreateService(IObjectSearchService objectSearchService,
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
        NacreElementInitializerSwitch elementInitializerSwitch = new NacreElementInitializerSwitch(editingContext, NacreLibElementType.getNacreElementFromName(lcaElementTypeName));
        return elementInitializerSwitch.doSwitch(element);
    }

    public PartUsage createMainGrid(Element ownerElement, IEditingContext editingContext) {
        var featureMembership = SysmlFactory.eINSTANCE.createFeatureMembership();
        ownerElement.getOwnedRelationship().add(featureMembership);
        var newPartUsage = SysmlFactory.eINSTANCE.createPartUsage();
        featureMembership.getOwnedRelatedElement().add(newPartUsage);
        new NacreElementInitializerSwitch(editingContext, NacreLibElementType.MAINGRID).doSwitch(newPartUsage);
        return newPartUsage;
    }

    public PartUsage createZone(Element ownerElement, IEditingContext editingContext) {
        var featureMembership = SysmlFactory.eINSTANCE.createFeatureMembership();
        ownerElement.getOwnedRelationship().add(featureMembership);
        var newPartUsage = SysmlFactory.eINSTANCE.createPartUsage();
        featureMembership.getOwnedRelatedElement().add(newPartUsage);
        new NacreElementInitializerSwitch(editingContext, NacreLibElementType.ZONE).doSwitch(newPartUsage);
        return newPartUsage;
    }

    public PartUsage createSubstation(Element ownerElement, IEditingContext editingContext) {
        var featureMembership = SysmlFactory.eINSTANCE.createFeatureMembership();
        ownerElement.getOwnedRelationship().add(featureMembership);
        var newPartUsage = SysmlFactory.eINSTANCE.createPartUsage();
        featureMembership.getOwnedRelatedElement().add(newPartUsage);
        new NacreElementInitializerSwitch(editingContext, NacreLibElementType.SUBSTATION).doSwitch(newPartUsage);
        return newPartUsage;
    }

    public PartUsage createCentralController(Element ownerElement, IEditingContext editingContext) {
        var featureMembership = SysmlFactory.eINSTANCE.createFeatureMembership();
        ownerElement.getOwnedRelationship().add(featureMembership);
        var newPartUsage = SysmlFactory.eINSTANCE.createPartUsage();
        featureMembership.getOwnedRelatedElement().add(newPartUsage);
        new NacreElementInitializerSwitch(editingContext, NacreLibElementType.CENTRALCONTROLLER).doSwitch(newPartUsage);
        return newPartUsage;
    }

    public PartUsage createZonalController(Element ownerElement, IEditingContext editingContext) {
        var featureMembership = SysmlFactory.eINSTANCE.createFeatureMembership();
        ownerElement.getOwnedRelationship().add(featureMembership);
        var newPartUsage = SysmlFactory.eINSTANCE.createPartUsage();
        featureMembership.getOwnedRelatedElement().add(newPartUsage);
        new NacreElementInitializerSwitch(editingContext, NacreLibElementType.ZONALCONTROLLER).doSwitch(newPartUsage);
        return newPartUsage;
    }

    public PartUsage createCommunicationMedia(Element ownerElement, IEditingContext editingContext) {
        var featureMembership = SysmlFactory.eINSTANCE.createFeatureMembership();
        ownerElement.getOwnedRelationship().add(featureMembership);
        var newPartUsage = SysmlFactory.eINSTANCE.createPartUsage();
        featureMembership.getOwnedRelatedElement().add(newPartUsage);
        new NacreElementInitializerSwitch(editingContext, NacreLibElementType.COMMUNICATIONMEDIA).doSwitch(newPartUsage);
        return newPartUsage;
    }

    public PartUsage createComputingResource(Element ownerElement, IEditingContext editingContext) {
        var featureMembership = SysmlFactory.eINSTANCE.createFeatureMembership();
        ownerElement.getOwnedRelationship().add(featureMembership);
        var newPartUsage = SysmlFactory.eINSTANCE.createPartUsage();
        featureMembership.getOwnedRelatedElement().add(newPartUsage);
        new NacreElementInitializerSwitch(editingContext, NacreLibElementType.COMPUTINGRESOURCE).doSwitch(newPartUsage);
        return newPartUsage;
    }

    public PartUsage createSoftwareResource(Element ownerElement, IEditingContext editingContext) {
        var featureMembership = SysmlFactory.eINSTANCE.createFeatureMembership();
        ownerElement.getOwnedRelationship().add(featureMembership);
        var newPartUsage = SysmlFactory.eINSTANCE.createPartUsage();
        featureMembership.getOwnedRelatedElement().add(newPartUsage);
        new NacreElementInitializerSwitch(editingContext, NacreLibElementType.SOFTWARERESOURCE).doSwitch(newPartUsage);
        return newPartUsage;
    }
}
