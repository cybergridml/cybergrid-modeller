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
import org.eclipse.syson.diagram.common.view.edges.AbstractAllocateEdgeDescriptionProvider;
import org.eclipse.syson.util.AQLUtils;


public class ControlEdgeDescriptionProvider extends AbstractAllocateEdgeDescriptionProvider {

    private static final String EDGE_NAME = "Control";
    private final NacreDescriptionNameGenerator NacreNameGenerator;
    private final DiagramBuilders diagramBuilder = new DiagramBuilders();

    public ControlEdgeDescriptionProvider(IColorProvider colorProvider, NacreDescriptionNameGenerator nameGenerator) {
        super(Objects.requireNonNull(colorProvider));
        this.NacreNameGenerator = Objects.requireNonNull(nameGenerator);
    }

    @Override
    public EdgeDescription create() {
        EdgeDescription description = super.create();
        description.setCenterLabelExpression("");
        description.setStyle(generateEdgeStyle());
        return description;
    }



    @Override
    protected String getName() {
        return NacreNameGenerator.getEdgeName(EDGE_NAME);
    }

    @Override
    protected List<NodeDescription> getSourceNodes(IViewDiagramElementFinder cache) {
        List<NodeDescription> sourceNodes = new ArrayList<>();
        cache.getNodeDescription(this.NacreNameGenerator.getNodeName(NacreLibElementType.MAINGRID.getServiceSuffix())).ifPresent(sourceNodes::add);
        cache.getNodeDescription(this.NacreNameGenerator.getNodeName(NacreLibElementType.CENTRALCONTROLLER.getServiceSuffix())).ifPresent(sourceNodes::add);
        return sourceNodes;
    }

    @Override
    protected List<NodeDescription> getTargetNodes(IViewDiagramElementFinder cache) {
        List<NodeDescription> targetNodes = new ArrayList<>();
        cache.getNodeDescription(this.NacreNameGenerator.getNodeName(NacreLibElementType.MAINGRID.getServiceSuffix())).ifPresent(targetNodes::add);
        cache.getNodeDescription(this.NacreNameGenerator.getNodeName(NacreLibElementType.CENTRALCONTROLLER.getServiceSuffix())).ifPresent(targetNodes::add);
        return targetNodes;
    }

    @Override
    public void link(DiagramDescription diagramDescription, IViewDiagramElementFinder cache) {
        super.link(diagramDescription, cache);

        var targetNodes = new ArrayList<NodeDescription>();
        cache.getNodeDescription(this.NacreNameGenerator.getNodeName(NacreLibElementType.MAINGRID.getServiceSuffix())).ifPresent(targetNodes::add);
        cache.getNodeDescription(this.NacreNameGenerator.getNodeName(NacreLibElementType.CENTRALCONTROLLER.getServiceSuffix())).ifPresent(targetNodes::add);

        var changeContext = this.viewBuilderHelper.newChangeContext()
                .expression(AQLUtils.getServiceCallExpression("semanticEdgeSource", "createAllocateEdge",
                        List.of("semanticEdgeTarget", "edgeSource", "editingContext", "diagramServices")))
                .build();

        var edgeTool = this.diagramBuilderHelper.newEdgeTool()
                .name(EDGE_NAME)
                .body(new Operation[] { changeContext })
                .targetElementDescriptions(targetNodes.toArray(NodeDescription[]::new))
                .build();

        getSourceNodes(cache).forEach(sourceNode -> sourceNode.getPalette().getEdgeTools().add(edgeTool));
    }

    private EdgeStyle generateEdgeStyle() {
        return this.diagramBuilder
                .newEdgeStyle()
                .edgeWidth(1)
                .borderSize(0)
                .lineStyle(LineStyle.SOLID)
                .color(colorProvider.getColor(NacreViewConstants.CONTROLLER_EDGE_COLOR))
                .sourceArrowStyle(ArrowStyle.NONE)
                .targetArrowStyle(ArrowStyle.NONE)
                .build();
    }





}