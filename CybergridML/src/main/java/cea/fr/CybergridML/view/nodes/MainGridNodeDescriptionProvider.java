package cea.fr.CybergridML.view.nodes;

import cea.fr.CybergridML.view.CybergridMLDescriptionNameGenerator;
import cea.fr.CybergridML.view.CybergridMLViewConstants;

import cea.fr.CybergridML.domain.CybergridMLLibElementType;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.*;
import org.eclipse.sirius.components.view.emf.diagram.ViewDiagramDescriptionConverter;
import org.eclipse.syson.diagram.services.aql.DiagramMutationAQLService;
import org.eclipse.syson.util.ServiceMethod;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MainGridNodeDescriptionProvider extends AbstractCybergridMLUsageNodeDescriptionProvider {

    public MainGridNodeDescriptionProvider(IColorProvider colorProvider, CybergridMLDescriptionNameGenerator nameGenerator) {
        this(colorProvider, nameGenerator, true);
    }

    public MainGridNodeDescriptionProvider(IColorProvider colorProvider, CybergridMLDescriptionNameGenerator nameGenerator, boolean withHazardTool) {
        super(CybergridMLLibElementType.MAINGRID, colorProvider, nameGenerator, withHazardTool);
    }

    @Override
    protected NodeStyleDescription createNodeStyle() {

        var layoutStrategy = this.diagramBuilderHelper.newFreeFormLayoutStrategyDescription()
                .build();

        return this.diagramBuilderHelper.newRectangularNodeStyleDescription()
                .borderColor(this.colorProvider.getColor(CybergridMLViewConstants.MAINGRID_NODE_BORDER_COLOR))
                .borderSize(3)
                .borderRadius(10)
                .background(this.colorProvider.getColor(CybergridMLViewConstants.MAINGRID_NODE_BACKGROUND_COLOR))
                .childrenLayoutStrategy(layoutStrategy)
                .build();
    }

    @Override
    protected Set<NodeDescription> getReusedChildren(IViewDiagramElementFinder cache) {
        var nodes = new LinkedHashSet<NodeDescription>();

        cache.getNodeDescription(getDescriptionNameGenerator().getNodeName(CybergridMLLibElementType.ZONE.getServiceSuffix()))
                .ifPresent(nodes::add);
        return nodes;
    }
    @Override
    protected Set<NodeDescription> getReusedBorderNodes(IViewDiagramElementFinder cache) {
        return Set.of();
    }
    @Override
    protected List<NodeDescription> getAllNodeDescriptions(IViewDiagramElementFinder cache) {
        return List.of();
    }
    @Override
    protected InsideLabelStyle createInsideLabelStyle() {
        InsideLabelStyle labelStyle = super.createInsideLabelStyle();
        labelStyle.setLabelIcon("/images/maingrid.svg");
        return labelStyle;
    }
    @Override
    protected List<NodeTool> getToolsWithoutSection(NodeDescription nodeDescription, IViewDiagramElementFinder cache) {
        var tools = new ArrayList<NodeTool>();

        tools.add(createChildCreationTool(CybergridMLLibElementType.ZONE.getName(), "createZone"));

        return tools;
    }

    private NodeTool createChildCreationTool(String toolName, String createServiceMethodName) {
        var exposeNewInstance = this.viewBuilderHelper.newChangeContext()
                .expression(ServiceMethod.of4(DiagramMutationAQLService::expose)
                        .aqlSelf(IEditingContext.EDITING_CONTEXT, DiagramContext.DIAGRAM_CONTEXT, Node.SELECTED_NODE,
                                ViewDiagramDescriptionConverter.CONVERTED_NODES_VARIABLE));

        var createAndExposeNewInstance = this.viewBuilderHelper.newChangeContext()
                .expression("aql:self.%s(%s)".formatted(createServiceMethodName, IEditingContext.EDITING_CONTEXT))
                .children(exposeNewInstance.build())
                .build();

        return this.diagramBuilderHelper.newNodeTool()
                .name(toolName)
                .body(createAndExposeNewInstance)
                .build();
    }



}