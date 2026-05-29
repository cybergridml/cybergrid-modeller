package cea.fr.NacreRTE.View.edges;

import cea.fr.NacreRTE.View.nodes.NacreViewConstants;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.*;
import org.eclipse.syson.diagram.common.view.edges.AbstractAllocateEdgeDescriptionProvider;

public abstract class AbstractNacreEdgeDescriptionProvider extends AbstractAllocateEdgeDescriptionProvider {

    protected AbstractNacreEdgeDescriptionProvider(IColorProvider colorProvider) {
        super(colorProvider);
    }

    @Override
    public EdgeDescription create() {
        EdgeDescription description = super.create();
        description.getConditionalStyles().add(createHazardConditionalEdgeStyle());
        return description;
    }

    @Override
    public void link(org.eclipse.sirius.components.view.diagram.DiagramDescription diagramDescription, IViewDiagramElementFinder cache) {
        super.link(diagramDescription, cache);
        cache.getEdgeDescription(this.getName()).ifPresent(edgeDescription -> {
            var palette = edgeDescription.getPalette();
            if (palette != null) {
                palette.getQuickAccessTools().add(createHazardEdgeTool());
            }
        });
    }

    private ConditionalEdgeStyle createHazardConditionalEdgeStyle() {
        return this.diagramBuilderHelper.newConditionalEdgeStyle()
                .condition("aql:self.hasHazard()")
                .color(this.colorProvider.getColor(NacreViewConstants.HAZARD_NODE_BORDER_COLOR))
                .edgeWidth(3)
                .lineStyle(LineStyle.DASH)
                .sourceArrowStyle(getHazardSourceArrowStyle())
                .targetArrowStyle(getHazardTargetArrowStyle())
                .build();
    }

    private NodeTool createHazardEdgeTool() {
        var toggleBody = this.viewBuilderHelper.newChangeContext()
                .expression("aql:self.toggleHazard(editingContext)")
                .build();

        return this.diagramBuilderHelper.newNodeTool()
                .name("Toggle Hazard")
                .iconURLsExpression("/images/hazard.svg")
                .body(toggleBody)
                .build();
    }

    protected ArrowStyle getHazardSourceArrowStyle() {
        return ArrowStyle.NONE;
    }

    protected ArrowStyle getHazardTargetArrowStyle() {
        return ArrowStyle.NONE;
    }
}
