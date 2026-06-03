package cea.fr.NacreRTE.View.edges;

import cea.fr.NacreRTE.View.nodes.NacreDescriptionNameGenerator;
import cea.fr.NacreRTE.View.nodes.NacreViewConstants;
import cea.fr.NacreRTE.library.NacreLibElementType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.sirius.components.view.Operation;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.*;
import org.eclipse.syson.util.AQLUtils;

public class ControlEdgeDescriptionProvider extends AbstractNacreEdgeDescriptionProvider {

    private static final String EDGE_NAME = "Control";
    private final NacreDescriptionNameGenerator nacreNameGenerator;
    private final DiagramBuilders diagramBuilder = new DiagramBuilders();

    public ControlEdgeDescriptionProvider(IColorProvider colorProvider, NacreDescriptionNameGenerator nameGenerator) {
        this(colorProvider, nameGenerator, true);
    }

    public ControlEdgeDescriptionProvider(IColorProvider colorProvider, NacreDescriptionNameGenerator nameGenerator, boolean withHazardTool) {
        super(Objects.requireNonNull(colorProvider), withHazardTool);
        this.nacreNameGenerator = Objects.requireNonNull(nameGenerator);
    }

    @Override
    public EdgeDescription create() {
        EdgeDescription description = super.create();
        description.setCenterLabelExpression("'Control'");
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
        cache.getNodeDescription(nacreNameGenerator.getNodeName(NacreLibElementType.CENTRALCONTROLLER.getServiceSuffix())).ifPresent(nodes::add);
        return nodes;
    }

    @Override
    protected List<NodeDescription> getTargetNodes(IViewDiagramElementFinder cache) {
        List<NodeDescription> nodes = new ArrayList<>();
        cache.getNodeDescription(nacreNameGenerator.getNodeName(NacreLibElementType.MAINGRID.getServiceSuffix())).ifPresent(nodes::add);
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

    private List<NodeDescription> buildControlNodes(IViewDiagramElementFinder cache) {
        List<NodeDescription> nodes = new ArrayList<>();
        cache.getNodeDescription(nacreNameGenerator.getNodeName(NacreLibElementType.MAINGRID.getServiceSuffix())).ifPresent(nodes::add);
        cache.getNodeDescription(nacreNameGenerator.getNodeName(NacreLibElementType.CENTRALCONTROLLER.getServiceSuffix())).ifPresent(nodes::add);
        return nodes;
    }

    private EdgeStyle generateEdgeStyle() {
        return this.diagramBuilder
                .newEdgeStyle()
                .edgeWidth(1)
                .borderSize(0)
                .lineStyle(LineStyle.DASH)
                .color(colorProvider.getColor(NacreViewConstants.CONTROLLER_EDGE_COLOR))
                .sourceArrowStyle(ArrowStyle.NONE)
                .targetArrowStyle(ArrowStyle.INPUT_ARROW)
                .build();
    }
}
