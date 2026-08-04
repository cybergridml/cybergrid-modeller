package cea.fr.CybergridML.view.edges;

import cea.fr.CybergridML.view.CybergridMLDescriptionNameGenerator;
import cea.fr.CybergridML.view.CybergridMLViewConstants;
import cea.fr.CybergridML.domain.CybergridMLLibElementType;
import org.eclipse.sirius.components.view.Operation;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import cea.fr.CybergridML.util.CybergridMLAQLUtils;
import org.eclipse.sirius.components.view.diagram.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LinkEdgeDescriptionProvider extends AbstractCybergridMLEdgeDescriptionProvider {

    private static final String EDGE_NAME = "Link";
    private final CybergridMLDescriptionNameGenerator cybergridMLNameGenerator;
    private final DiagramBuilders diagramBuilder = new DiagramBuilders();

    public LinkEdgeDescriptionProvider(IColorProvider colorProvider, CybergridMLDescriptionNameGenerator nameGenerator) {
        super(Objects.requireNonNull(colorProvider));
        this.cybergridMLNameGenerator = Objects.requireNonNull(nameGenerator);
    }

    @Override
    public EdgeDescription create() {
        EdgeDescription description = super.create();
        description.setCenterLabelExpression("'Link'");
        description.setStyle(generateEdgeStyle());
        return description;
    }

    @Override
    protected String getName() {
        return cybergridMLNameGenerator.getEdgeName(EDGE_NAME);
    }

    @Override
    protected List<NodeDescription> getSourceNodes(IViewDiagramElementFinder cache) {
        return buildLinkNodes(cache);
    }

    @Override
    protected List<NodeDescription> getTargetNodes(IViewDiagramElementFinder cache) {
        return buildLinkNodes(cache);
    }

    @Override
    public void link(DiagramDescription diagramDescription, IViewDiagramElementFinder cache) {
        super.link(diagramDescription, cache);

        var targetNodes = getTargetNodes(cache);

        getSourceNodes(cache).forEach(sourceNode -> {
            var changeContext = this.viewBuilderHelper.newChangeContext()
                    .expression(CybergridMLAQLUtils.getServiceCallExpression("semanticEdgeSource", "createAllocateEdge",
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

    private List<NodeDescription> buildLinkNodes(IViewDiagramElementFinder cache) {
        List<NodeDescription> nodes = new ArrayList<>();
        cache.getNodeDescription(cybergridMLNameGenerator.getNodeName(CybergridMLLibElementType.COMMUNICATIONMEDIA.getServiceSuffix())).ifPresent(nodes::add);
        return nodes;
    }

    private EdgeStyle generateEdgeStyle() {
        return this.diagramBuilder
                .newEdgeStyle()
                .edgeWidth(1)
                .borderSize(0)
                .lineStyle(LineStyle.DASH)
                .color(colorProvider.getColor(CybergridMLViewConstants.LINK_EDGE_COLOR))
                .sourceArrowStyle(ArrowStyle.NONE)
                .targetArrowStyle(ArrowStyle.NONE)
                .build();
    }
}
