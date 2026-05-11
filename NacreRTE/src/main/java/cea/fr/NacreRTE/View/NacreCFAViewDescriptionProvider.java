 package cea.fr.NacreRTE.View;

import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.providers.IRepresentationDescriptionProvider;
import org.eclipse.sirius.components.view.diagram.ArrangeLayoutDirection;
import org.eclipse.syson.common.view.api.IViewDescriptionProvider;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.util.AQLUtils;
import org.eclipse.syson.util.SysMLMetamodelHelper;
import org.springframework.stereotype.Service;

@Service
public class NacreCFAViewDescriptionProvider implements IViewDescriptionProvider {

    public static final String DESCRIPTION_NAME = "CFA View";

    @Override
    public String getViewId() {
        return "CFAViewDiagram";
    }

    @Override
    public IRepresentationDescriptionProvider getRepresentationDescriptionProvider() {
        return colorProvider -> new DiagramBuilders().newDiagramDescription()
                .arrangeLayoutDirection(ArrangeLayoutDirection.RIGHT)
                .autoLayout(false)
                .domainType(SysMLMetamodelHelper.buildQualifiedName(SysmlPackage.eINSTANCE.getElement()))
                .preconditionExpression(AQLUtils.getSelfServiceCallExpression("canCreateDiagram"))
                .name(DESCRIPTION_NAME)
                .titleExpression("aql:'%s'+ Sequence{self.existingViewUsagesCountForRepresentationCreation(), 1}->sum()".formatted(DESCRIPTION_NAME))
                .build();
    }
}
