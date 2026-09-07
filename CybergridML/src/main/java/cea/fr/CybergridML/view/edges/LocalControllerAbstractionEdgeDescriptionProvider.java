package cea.fr.CybergridML.view.edges;

import org.eclipse.syson.util.DescriptionNameGenerator;
import cea.fr.CybergridML.view.colors.CybergridMLViewConstants;
import cea.fr.CybergridML.domain.CybergridMLLibElementType;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.*;
import cea.fr.CybergridML.util.CybergridMLAQLUtils;
import org.eclipse.syson.diagram.common.view.edges.AbstractAllocateEdgeDescriptionProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Edge provider for abstraction between LocalController and SoftwareResource.
 */
public class LocalControllerAbstractionEdgeDescriptionProvider extends AbstractAllocateEdgeDescriptionProvider {

    private static final String EDGE_NAME = "LC Abstract";
    private final DescriptionNameGenerator cybergridMLNameGenerator;
    private final DiagramBuilders diagramBuilder = new DiagramBuilders();

    public LocalControllerAbstractionEdgeDescriptionProvider(IColorProvider colorProvider, DescriptionNameGenerator nameGenerator) {
        super(Objects.requireNonNull(colorProvider));
        this.cybergridMLNameGenerator = Objects.requireNonNull(nameGenerator);
    }

    @Override
    public EdgeDescription create() {
        EdgeDescription description = super.create();
        description.setCenterLabelExpression("'LC Abstract'");
        description.setStyle(generateEdgeStyle());
        return description;
    }

    @Override
    protected String getName() {
        return cybergridMLNameGenerator.getEdgeName(EDGE_NAME);
    }

    @Override
    protected List<NodeDescription> getSourceNodes(IViewDiagramElementFinder cache) {
        List<NodeDescription> nodes = new ArrayList<>();
        cache.getNodeDescription(cybergridMLNameGenerator.getNodeName(CybergridMLLibElementType.SOFTWARERESOURCE.getServiceSuffix())).ifPresent(nodes::add);
        return nodes;
    }

    @Override
    protected List<NodeDescription> getTargetNodes(IViewDiagramElementFinder cache) {
        List<NodeDescription> nodes = new ArrayList<>();
        cache.getNodeDescription(cybergridMLNameGenerator.getNodeName(CybergridMLLibElementType.LOCALCONTROLLER.getServiceSuffix())).ifPresent(nodes::add);
        return nodes;
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
                    .body(changeContext)
                    .targetElementDescriptions(targetNodes.toArray(NodeDescription[]::new))
                    .build();

            sourceNode.getPalette().getEdgeTools().add(edgeTool);
        });
    }

    private EdgeStyle generateEdgeStyle() {
        return this.diagramBuilder
                .newEdgeStyle()
                .edgeWidth(1)
                .borderSize(0)
                .lineStyle(LineStyle.DASH)
                .color(colorProvider.getColor(CybergridMLViewConstants.CONNECT_EDGE_COLOR))
                .sourceArrowStyle(ArrowStyle.NONE)
                .targetArrowStyle(ArrowStyle.INPUT_ARROW)
                .build();
    }

}
