package cea.fr.NacreRTE.view.edges;

import cea.fr.NacreRTE.view.NacreDescriptionNameGenerator;
import cea.fr.NacreRTE.view.NacreViewConstants;
import cea.fr.NacreRTE.domain.NacreLibElementType;
import org.eclipse.sirius.components.view.Operation;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.*;
import org.eclipse.syson.util.AQLUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AllocateCraEdgeDescriptionProvider extends AbstractNacreEdgeDescriptionProvider {

    private static final String EDGE_NAME = "Allocate";
    private final NacreDescriptionNameGenerator nacreNameGenerator;
    private final DiagramBuilders diagramBuilder = new DiagramBuilders();

    public AllocateCraEdgeDescriptionProvider(IColorProvider colorProvider, NacreDescriptionNameGenerator nameGenerator) {
        super(Objects.requireNonNull(colorProvider));
        this.nacreNameGenerator = Objects.requireNonNull(nameGenerator);
    }

    @Override
    public EdgeDescription create() {
        EdgeDescription description = super.create();
        description.setCenterLabelExpression("'Allocate'");
        description.setStyle(generateEdgeStyle());
        return description;
    }

    @Override
    protected String getName() {
        return nacreNameGenerator.getEdgeName(EDGE_NAME);
    }

    @Override
    protected List<NodeDescription> getSourceNodes(IViewDiagramElementFinder cache) {
        List<NodeDescription> nodes = new ArrayList<>();
        cache.getNodeDescription(nacreNameGenerator.getNodeName(NacreLibElementType.SOFTWARERESOURCE.getServiceSuffix())).ifPresent(nodes::add);
        return nodes;
    }

    @Override
    protected List<NodeDescription> getTargetNodes(IViewDiagramElementFinder cache) {
        List<NodeDescription> nodes = new ArrayList<>();
        cache.getNodeDescription(nacreNameGenerator.getNodeName(NacreLibElementType.COMPUTINGRESOURCE.getServiceSuffix())).ifPresent(nodes::add);
        return nodes;
    }

    @Override
    public void link(DiagramDescription diagramDescription, IViewDiagramElementFinder cache) {
        super.link(diagramDescription, cache);

        var targetNodes = getTargetNodes(cache);

        getSourceNodes(cache).forEach(sourceNode -> {
            var changeContext = this.viewBuilderHelper.newChangeContext()
                    .expression(AQLUtils.getServiceCallExpression("semanticEdgeSource", "createAllocateEdge",
                            List.of("semanticEdgeTarget", "edgeSource", "editingContext", "diagramServices")))
                    .build();

            var edgeTool = this.diagramBuilderHelper.newEdgeTool()
                    .name(EDGE_NAME)
                    .body(new Operation[] { changeContext })
                    .targetElementDescriptions(targetNodes.toArray(NodeDescription[]::new))
                    .build();

            sourceNode.getPalette().getEdgeTools().add(edgeTool);
        });
    }

    @Override
    protected ArrowStyle getHazardTargetArrowStyle() {
        return ArrowStyle.INPUT_ARROW;
    }

    private EdgeStyle generateEdgeStyle() {
        return this.diagramBuilder
                .newEdgeStyle()
                .edgeWidth(1)
                .borderSize(0)
                .lineStyle(LineStyle.DASH)
                .color(colorProvider.getColor(NacreViewConstants.ALLOCATE_EDGE_COLOR))
                .sourceArrowStyle(ArrowStyle.NONE)
                .targetArrowStyle(ArrowStyle.INPUT_ARROW)
                .build();
    }
}
