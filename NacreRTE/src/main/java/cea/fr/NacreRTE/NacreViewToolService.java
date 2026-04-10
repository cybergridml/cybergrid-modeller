package cea.fr.NacreRTE;

import cea.fr.NacreRTE.services.NacreUtilService;
import cea.fr.NacreRTE.services.api.NacreViewDefinitionKind;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IFeedbackMessageService;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.diagrams.IDiagramElement;
import org.eclipse.sirius.components.view.emf.IViewRepresentationDescriptionSearchService;
import org.eclipse.syson.diagram.common.view.services.ViewToolService;
import org.eclipse.syson.services.api.ISysMLMoveElementService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Feature;
import org.eclipse.syson.sysml.PortUsage;
import org.eclipse.syson.sysml.SysmlPackage;

import java.util.List;

/**
 * Tool-related Java services used by LCA diagrams.
 */
public class NacreViewToolService extends ViewToolService {

    private final NacreUtilService nacreUtilService;

    public NacreViewToolService(IIdentityService identityService, IObjectSearchService objectSearchService, IViewRepresentationDescriptionSearchService viewRepresentationDescriptionSearchService, IFeedbackMessageService feedbackMessageService, ISysMLMoveElementService moveService) {
        super(identityService, objectSearchService, viewRepresentationDescriptionSearchService, feedbackMessageService, moveService);
        nacreUtilService = new NacreUtilService();
    }

    @Override
    public boolean toolShouldBeAvailable(Element element, IEditingContext editingContext, DiagramContext diagramContext, EClass newElementType) {
        NacreViewDefinitionKind viewDefinitionKind = this.nacreUtilService.getNacreViewDefinitionKind(element, List.of(), editingContext);

        return switch (viewDefinitionKind) {
            case CFA_VIEW -> this.toolShouldBeAvailableOnCFAView(newElementType);
            case CRA_VIEW -> this.toolShouldBeAvailableOnCRAView(newElementType);
            case CFRA_VIEW -> this.toolShouldBeAvailableOnCFRAView(newElementType);
        };
    }

    private boolean toolShouldBeAvailableOnCFAView(EClass newElementType) {
        return SysmlPackage.eINSTANCE.getActionDefinition().equals(newElementType) ||
                SysmlPackage.eINSTANCE.getPartDefinition().equals(newElementType);
    }

    private boolean toolShouldBeAvailableOnCFRAView(EClass newElementType) {
        return SysmlPackage.eINSTANCE.getActionUsage().equals(newElementType) ||
                SysmlPackage.eINSTANCE.getPartUsage().equals(newElementType);

    }
    private boolean toolShouldBeAvailableOnCRAView(EClass newElementType) {
        return SysmlPackage.eINSTANCE.getActionUsage().equals(newElementType) ||
                SysmlPackage.eINSTANCE.getPartUsage().equals(newElementType);

    }
}
