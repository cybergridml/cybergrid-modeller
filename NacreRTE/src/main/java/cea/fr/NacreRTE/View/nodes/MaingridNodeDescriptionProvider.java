package cea.fr.NacreRTE.View.nodes;

import cea.fr.NacreRTE.NacreViewNodeToolsWithoutSectionSwitch;
import cea.fr.NacreRTE.library.NacreLibElementType;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.LineStyle;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.NodeStyleDescription;
import org.eclipse.sirius.components.view.diagram.NodeTool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MaingridNodeDescriptionProvider extends AbstractNacreUsageNodeDescriptionProvider {

    public MaingridNodeDescriptionProvider(IColorProvider colorProvider, NacreDescriptionNameGenerator nameGenerator) {
        super(NacreLibElementType.MAINGRID, colorProvider, nameGenerator);
    }

    @Override
    protected NodeStyleDescription createNodeStyle() {

        var layoutStrategy = this.diagramBuilderHelper.newListLayoutStrategyDescription()
                .areChildNodesDraggableExpression("false")
                .bottomGapExpression("10")
                .growableNodes(new NodeDescription[0]) // ✅ plus simple et safe
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
        return Collections.emptySet(); // ✅ jamais null
    }

    @Override
    protected Set<NodeDescription> getReusedBorderNodes(IViewDiagramElementFinder cache) {
        return Collections.emptySet(); // ✅ jamais null
    }

    @Override
    protected List<NodeDescription> getAllNodeDescriptions(IViewDiagramElementFinder cache) {
        return Collections.emptyList(); // ✅ jamais null
    }

    @Override
    protected List<NodeTool> getToolsWithoutSection(NodeDescription nodeDescription, IViewDiagramElementFinder cache) {

        NacreViewNodeToolsWithoutSectionSwitch toolsSwitch =
                new NacreViewNodeToolsWithoutSectionSwitch(
                        NacreLibElementType.MAINGRID,
                        getDescriptionNameGenerator(),
                        getAllNodeDescriptions(cache),
                        cache
                );

        List<NodeTool> result = toolsSwitch.doSwitch(this.eClass);

        // ✅ FIX PRINCIPAL : éviter le null
        return result != null ? result : Collections.emptyList();
    }
}