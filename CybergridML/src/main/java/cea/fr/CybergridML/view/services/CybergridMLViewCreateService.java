package cea.fr.CybergridML.view.services;

import cea.fr.CybergridML.domain.CybergridMLLibElementType;
import cea.fr.CybergridML.services.CybergridMLElementInitializerSwitch;
import cea.fr.CybergridML.services.xiidm.XiidmParseException;
import cea.fr.CybergridML.services.xiidm.XiidmParser;
import cea.fr.CybergridML.services.xiidm.XiidmToCybergridMLMapper;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.representations.Message;
import org.eclipse.sirius.components.representations.MessageLevel;
import org.springframework.stereotype.Service;
import org.eclipse.sirius.components.core.api.IFeedbackMessageService;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.core.api.IReadOnlyObjectPredicate;
import org.eclipse.syson.diagram.common.view.services.ShowDiagramsInheritedMembersService;
import org.eclipse.syson.diagram.common.view.services.ViewCreateService;
import org.eclipse.syson.sysml.*;
import org.eclipse.syson.sysml.Package;

import java.util.List;
import java.util.Objects;


@Service
public class CybergridMLViewCreateService extends ViewCreateService {

    private final IFeedbackMessageService feedbackMessageService;

    private final XiidmParser xiidmParser;

    private final XiidmToCybergridMLMapper xiidmMapper;

    public CybergridMLViewCreateService(IObjectSearchService objectSearchService,
                                        IReadOnlyObjectPredicate readOnlyObjectPredicate,
                                        ShowDiagramsInheritedMembersService showDiagramsInheritedMembersService,
                                        IFeedbackMessageService feedbackMessageService,
                                        XiidmParser xiidmParser,
                                        XiidmToCybergridMLMapper xiidmMapper) {
        super(objectSearchService, readOnlyObjectPredicate, showDiagramsInheritedMembersService, feedbackMessageService);
        this.feedbackMessageService = Objects.requireNonNull(feedbackMessageService);
        this.xiidmParser = Objects.requireNonNull(xiidmParser);
        this.xiidmMapper = Objects.requireNonNull(xiidmMapper);
    }

    /**
     * Imports an XIIDM network as children of {@code self}, the grid the "Import XIIDM file" tool was invoked on.
     *
     * <p>The file content is handed over by the frontend as a tool variable (see the XiidmImportDialog extension).
     * Returns the substations that were created so that the tool can expose them in the diagram; an empty list when
     * nothing could be imported, in which case the reason is reported as a feedback message.
     */
    public List<Element> importXiidm(Element self, String xiidmContent, String xiidmFileName, IEditingContext editingContext) {
        if (xiidmContent == null || xiidmContent.isBlank()) {
            this.addMessage("No XIIDM content was received, nothing was imported.", MessageLevel.WARNING);
            return List.of();
        }
        try {
            var network = this.xiidmParser.parse(xiidmContent);
            var result = this.xiidmMapper.importInto(self, network, editingContext);
            this.addMessage("%s %s".formatted(fileLabel(xiidmFileName), result.summary()), MessageLevel.SUCCESS);
            result.warnings().forEach(warning -> this.addMessage(warning, MessageLevel.WARNING));
            return result.createdRootElements();
        } catch (XiidmParseException exception) {
            this.addMessage("%s %s".formatted(fileLabel(xiidmFileName), exception.getMessage()), MessageLevel.ERROR);
            return List.of();
        }
    }

    private static String fileLabel(String xiidmFileName) {
        return xiidmFileName == null || xiidmFileName.isBlank() ? "XIIDM import:" : "'%s':".formatted(xiidmFileName);
    }

    private void addMessage(String body, MessageLevel level) {
        this.feedbackMessageService.addFeedbackMessage(new Message(body, level));
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
