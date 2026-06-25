package cea.fr.CybergridML.view.services;

import org.eclipse.emf.ecore.EClass;
import org.springframework.stereotype.Service;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IFeedbackMessageService;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.view.emf.IViewRepresentationDescriptionSearchService;
import org.eclipse.syson.diagram.common.view.services.ViewToolService;
import org.eclipse.syson.services.api.ISysMLMoveElementService;
import org.eclipse.syson.sysml.*;

import java.util.List;

@Service
public class CybergridMLViewToolService extends ViewToolService {

    private final CybergridMLUtilService cybergridMLUtilService;

    public CybergridMLViewToolService(IIdentityService identityService, IObjectSearchService objectSearchService, IViewRepresentationDescriptionSearchService viewRepresentationDescriptionSearchService, IFeedbackMessageService feedbackMessageService, ISysMLMoveElementService moveService) {
        super(identityService, objectSearchService, viewRepresentationDescriptionSearchService, feedbackMessageService, moveService);
        cybergridMLUtilService = new CybergridMLUtilService();
    }

    @Override
    public boolean toolShouldBeAvailable(Element element, IEditingContext editingContext, DiagramContext diagramContext, EClass newElementType) {
        if (!(element instanceof ViewUsage)) {
            return true;
        }
        CybergridMLViewDefinitionKind viewDefinitionKind = this.cybergridMLUtilService.getCybergridMLViewDefinitionKind(element, List.of(), editingContext);
        if (viewDefinitionKind == null) {
            return true;
        }
        return switch (viewDefinitionKind) {
            case CFA_VIEW -> this.toolShouldBeAvailableOnCFAView(newElementType);
            case CRA_VIEW -> this.toolShouldBeAvailableOnCRAView(newElementType);
            case CFRA_VIEW -> this.toolShouldBeAvailableOnCFRAView(newElementType);
        };
    }

    private boolean toolShouldBeAvailableOnCFAView(EClass newElementType) {
        return SysmlPackage.eINSTANCE.getPartUsage().equals(newElementType);
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
