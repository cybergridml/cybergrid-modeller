package cea.fr.NacreRTE.View.nodes;

import cea.fr.NacreRTE.library.NacreLibElementType;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.LineStyle;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.NodeStyleDescription;
import org.eclipse.sirius.components.view.diagram.NodeTool;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ZoneNodeDescriptionProvider extends AbstractNacreUsageNodeDescriptionProvider {

    public ZoneNodeDescriptionProvider(IColorProvider colorProvider, NacreDescriptionNameGenerator nameGenerator) {
        super(NacreLibElementType.ZONE, colorProvider, nameGenerator);
    }

    @Override
    protected NodeStyleDescription createNodeStyle() {

        var layoutStrategy = this.diagramBuilderHelper.newListLayoutStrategyDescription()
                .areChildNodesDraggableExpression("false")
                .bottomGapExpression("10")
                .growableNodes(new ArrayList<NodeDescription>().toArray(NodeDescription[]::new))
                .build();

        return this.diagramBuilderHelper.newRectangularNodeStyleDescription()
                .borderColor(this.colorProvider.getColor(NacreViewConstants.MAINGRID_NODE_BORDER_COLOR))
                .borderSize(3)
                .borderLineStyle(LineStyle.DASH)
                .background(this.colorProvider.getColor(NacreViewConstants.MAINGRID_NODE_BACKGROUND_COLOR))
                .childrenLayoutStrategy(layoutStrategy)
                .build();
    }

    @Override
    protected Set<NodeDescription> getReusedChildren(IViewDiagramElementFinder cache) {
        return Set.of();
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
    protected List<NodeTool> getToolsWithoutSection(NodeDescription nodeDescription, IViewDiagramElementFinder cache) {
        return List.of();
    }

//
//    @Override
//    protected List<NodeTool> getToolsWithoutSection(NodeDescription nodeDescription, IViewDiagramElementFinder cache) {
//        NacreViewNodeToolsWithoutSectionSwitch toolsWithoutSectionSwitch = new NacreViewNodeToolsWithoutSectionSwitch(
//                NacreLibElementType.MAINGRID,
//                getDescriptionNameGenerator(),
//                this.getAllNodeDescriptions(cache),
//                cache);
//        return toolsWithoutSectionSwitch.doSwitch(this.eClass);
//    }


//    @Override
//    protected Set<NodeDescription> getReusedChildren(IViewDiagramElementFinder cache) {
//        var nodes = new LinkedHashSet<NodeDescription>();
//
//        String propertiesCompartmentName = this.getDescriptionNameGenerator().getLcaCompartmentName(
//                LcaElementType.CONTAINER,
//                LcaNestedElements.PROPERTY.getNestedElementName());
//        String processFlowCompartmentName = this.getDescriptionNameGenerator().getLcaCompartmentName(
//                LcaElementType.CONTAINER,
//                LcaNestedElements.PROCESS.getNestedElementName());
//        String portsCompartmentName = this.getDescriptionNameGenerator().getLcaCompartmentName(
//                LcaElementType.CONTAINER,
//                LcaNestedElements.PORTS.getNestedElementName());
//
//        cache.getNodeDescription(propertiesCompartmentName).ifPresent(nodes::add);
//        cache.getNodeDescription(processFlowCompartmentName).ifPresent(nodes::add);
//        cache.getNodeDescription(portsCompartmentName).ifPresent(nodes::add);
//
//        return nodes;
//    }

//    @Override
//    protected Set<NodeDescription> getReusedBorderNodes(IViewDiagramElementFinder cache) {
//        var borderNodes = new LinkedHashSet<NodeDescription>();
//        cache.getNodeDescription(this.getDescriptionNameGenerator().getLcaBorderNodeName(LcaElementType.TECHNO_PORT, LcaNestedElements.PORTS.getUsageNestedEReference().getName())).ifPresent(borderNodes::add);
//        cache.getNodeDescription(this.getDescriptionNameGenerator().getLcaBorderNodeName(LcaElementType.BIO_PORT, LcaNestedElements.PORTS.getUsageNestedEReference().getName())).ifPresent(borderNodes::add);
//        cache.getNodeDescription(this.getDescriptionNameGenerator().getLcaInheritedBorderNodeName(LcaElementType.TECHNO_PORT, LcaNestedElements.PORTS.getUsageNestedEReference().getName())).ifPresent(borderNodes::add);
//        cache.getNodeDescription(this.getDescriptionNameGenerator().getLcaInheritedBorderNodeName(LcaElementType.BIO_PORT, LcaNestedElements.PORTS.getUsageNestedEReference().getName())).ifPresent(borderNodes::add);
//        return borderNodes;
//    }

}