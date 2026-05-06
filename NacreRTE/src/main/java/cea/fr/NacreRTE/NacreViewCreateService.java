package cea.fr.NacreRTE;

import cea.fr.NacreRTE.library.NacreLibElementType;
import cea.fr.NacreRTE.services.NacreElementInitializerSwitch;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IFeedbackMessageService;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.core.api.IReadOnlyObjectPredicate;
import org.eclipse.syson.diagram.common.view.services.ShowDiagramsInheritedMembersService;
import org.eclipse.syson.diagram.common.view.services.ViewCreateService;
import org.eclipse.syson.services.UtilService;
import org.eclipse.syson.sysml.*;
import org.eclipse.syson.sysml.Package;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NacreViewCreateService extends ViewCreateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NacreViewCreateService.class);
    private final UtilService utilService;

    public NacreViewCreateService(IObjectSearchService objectSearchService,
                                IReadOnlyObjectPredicate readOnlyObjectPredicate,
                                ShowDiagramsInheritedMembersService showDiagramsInheritedMembersService,
                                IFeedbackMessageService feedbackMessageService) {
        super(objectSearchService, readOnlyObjectPredicate, showDiagramsInheritedMembersService, feedbackMessageService);
        this.utilService = new UtilService();
    }

    @Override
    public boolean canCreateDiagram(Element element) {
        return element instanceof Package || element instanceof PartUsage;
    }

    public Element elementInitializer(Element element, IEditingContext editingContext, String lcaElementTypeName) {
        NacreElementInitializerSwitch elementInitializerSwitch = new NacreElementInitializerSwitch(editingContext, NacreLibElementType.getNacreElementFromName(lcaElementTypeName));
        return elementInitializerSwitch.doSwitch(element);
    }



    /**
     * Called by the container creation tool triggered when clicking on a node's free form compartment.
     */
    public PartUsage createMaingrid(Element ownerElement, IEditingContext editingContext) {
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
    public PartUsage createCentralcontroller(Element ownerElement, IEditingContext editingContext) {
        var featureMembership = SysmlFactory.eINSTANCE.createFeatureMembership();
        ownerElement.getOwnedRelationship().add(featureMembership);
        var newPartUsage = SysmlFactory.eINSTANCE.createPartUsage();
        featureMembership.getOwnedRelatedElement().add(newPartUsage);

        new NacreElementInitializerSwitch(editingContext, NacreLibElementType.CENTRALCONTROLLER).doSwitch(newPartUsage);
        return newPartUsage;
    }
    public PartUsage createZonalcontroller(Element ownerElement, IEditingContext editingContext) {
        var featureMembership = SysmlFactory.eINSTANCE.createFeatureMembership();
        ownerElement.getOwnedRelationship().add(featureMembership);
        var newPartUsage = SysmlFactory.eINSTANCE.createPartUsage();
        featureMembership.getOwnedRelatedElement().add(newPartUsage);

        new NacreElementInitializerSwitch(editingContext, NacreLibElementType.ZONALCONTROlLER).doSwitch(newPartUsage);
        return newPartUsage;
    }




    /**
     * Called by the property creation tool triggered when clicking on a node's list compartment.
     */

//    public List<Feature> getNacreInheritedCompartmentItems(Type type, String nacreElementTypeName, String eReferenceName) {
//        List<Feature> features = super.getInheritedCompartmentItems(type, eReferenceName);
//        NacreLibElementType nacreElementType = NacreLibElementType.getNacreElementFromName(nacreElementTypeName);
//        return features.stream().filter(feat -> NacreLibElementType.isTypedWith(feat, nacreElementType)).toList();
//    }



    private boolean areDirectionsCompatible(PortUsage source, PortUsage target) {
        return (utilService.isOutFeature(source) && utilService.isInFeature(target)) ||
                (utilService.isInFeature(source) && utilService.isOutFeature(target)) ||
                (utilService.isInOutFeature(source) || utilService.isInOutFeature(target)) ||
                (utilService.isOutFeature(source) && utilService.isOutFeature(target)) ||
                (utilService.isInFeature(source) && utilService.isInFeature(target));
    }

}
