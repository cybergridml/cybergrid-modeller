package cea.fr.CybergridML.view.edges;

import cea.fr.CybergridML.view.CybergridMLDescriptionNameGenerator;
import cea.fr.CybergridML.view.CybergridMLViewConstants;
import cea.fr.CybergridML.domain.CybergridMLLibElementType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.sirius.components.view.Operation;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import cea.fr.CybergridML.util.CybergridMLAQLUtils;
import org.eclipse.sirius.components.view.diagram.*;

public class ControlEdgeDescriptionProvider extends AbstractCybergridMLEdgeDescriptionProvider {

    private static final String EDGE_NAME = "Control";
    private final CybergridMLDescriptionNameGenerator cybergridMLNameGenerator;
    private final DiagramBuilders diagramBuilder = new DiagramBuilders();

    public ControlEdgeDescriptionProvider(IColorProvider colorProvider, CybergridMLDescriptionNameGenerator nameGenerator) {
        this(colorProvider, nameGenerator, true);
    }

    public ControlEdgeDescriptionProvider(IColorProvider colorProvider, CybergridMLDescriptionNameGenerator nameGenerator, boolean withHazardTool) {
        super(Objects.requireNonNull(colorProvider), withHazardTool);
        this.cybergridMLNameGenerator = Objects.requireNonNull(nameGenerator);
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
        return cybergridMLNameGenerator.getEdgeName(EDGE_NAME);
    }

    @Override
    protected List<NodeDescription> getSourceNodes(IViewDiagramElementFinder cache) {
        List<NodeDescription> nodes = new ArrayList<>();
        cache.getNodeDescription(cybergridMLNameGenerator.getNodeName(CybergridMLLibElementType.CENTRALCONTROLLER.getServiceSuffix())).ifPresent(nodes::add);
        cache.getNodeDescription(cybergridMLNameGenerator.getNodeName(CybergridMLLibElementType.ZONALCONTROLLER.getServiceSuffix())).ifPresent(nodes::add);

        return nodes;
    }

    @Override
    protected List<NodeDescription> getTargetNodes(IViewDiagramElementFinder cache) {
        List<NodeDescription> nodes = new ArrayList<>();
        cache.getNodeDescription(cybergridMLNameGenerator.getNodeName(CybergridMLLibElementType.MAINGRID.getServiceSuffix())).ifPresent(nodes::add);
        cache.getNodeDescription(cybergridMLNameGenerator.getNodeName(CybergridMLLibElementType.ZONE.getServiceSuffix())).ifPresent(nodes::add);

        return nodes;
    }

    @Override
    public void link(DiagramDescription diagramDescription, IViewDiagramElementFinder cache) {
        super.link(diagramDescription, cache);

        var mainGrid = cache.getNodeDescription(
                cybergridMLNameGenerator.getNodeName(
                        CybergridMLLibElementType.MAINGRID.getServiceSuffix()));

        var zone = cache.getNodeDescription(
                cybergridMLNameGenerator.getNodeName(
                        CybergridMLLibElementType.ZONE.getServiceSuffix()));

        // CentralController -> MainGrid
        cache.getNodeDescription(
                        cybergridMLNameGenerator.getNodeName(
                                CybergridMLLibElementType.CENTRALCONTROLLER.getServiceSuffix()))
                .ifPresent(sourceNode -> {

                    var changeContext = this.viewBuilderHelper.newChangeContext()
                            .expression(CybergridMLAQLUtils.getServiceCallExpression(
                                    "semanticEdgeSource",
                                    "createAllocateEdge",
                                    List.of("semanticEdgeTarget", "edgeSource",
                                            "editingContext", "diagramServices")))
                            .build();

                    mainGrid.ifPresent(target -> {
                        var edgeTool = this.diagramBuilderHelper.newEdgeTool()
                                .name(EDGE_NAME)
                                .body(new Operation[] { changeContext })
                                .targetElementDescriptions(target)
                                .build();

                        sourceNode.getPalette().getEdgeTools().add(edgeTool);
                    });
                });

        // ZonalController -> Zone
        cache.getNodeDescription(
                        cybergridMLNameGenerator.getNodeName(
                                CybergridMLLibElementType.ZONALCONTROLLER.getServiceSuffix()))
                .ifPresent(sourceNode -> {

                    var changeContext = this.viewBuilderHelper.newChangeContext()
                            .expression(CybergridMLAQLUtils.getServiceCallExpression(
                                    "semanticEdgeSource",
                                    "createAllocateEdge",
                                    List.of("semanticEdgeTarget", "edgeSource",
                                            "editingContext", "diagramServices")))
                            .build();

                    zone.ifPresent(target -> {
                        var edgeTool = this.diagramBuilderHelper.newEdgeTool()
                                .name(EDGE_NAME)
                                .body(new Operation[] { changeContext })
                                .targetElementDescriptions(target)
                                .build();

                        sourceNode.getPalette().getEdgeTools().add(edgeTool);
                    });
                });
    }

    private EdgeStyle generateEdgeStyle() {
        return this.diagramBuilder
                .newEdgeStyle()
                .edgeWidth(1)
                .borderSize(0)
                .lineStyle(LineStyle.DASH)
                .color(colorProvider.getColor(CybergridMLViewConstants.CONTROLLER_EDGE_COLOR))
                .sourceArrowStyle(ArrowStyle.NONE)
                .targetArrowStyle(ArrowStyle.INPUT_ARROW)
                .build();
    }
}
