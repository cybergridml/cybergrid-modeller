package cea.fr.NacreRTE.view.nodes;

import cea.fr.NacreRTE.view.NacreDescriptionNameGenerator;
import cea.fr.NacreRTE.view.NacreViewConstants;

import cea.fr.NacreRTE.domain.NacreLibElementType;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.NodeStyleDescription;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CommunicationMediaNodeDescriptionProvider extends AbstractNacreUsageNodeDescriptionProvider {

    public CommunicationMediaNodeDescriptionProvider(IColorProvider colorProvider, NacreDescriptionNameGenerator nameGenerator) {
        super(NacreLibElementType.COMMUNICATIONMEDIA, colorProvider, nameGenerator);
    }

    @Override
    protected NodeStyleDescription createNodeStyle() {
        var layoutStrategy = this.diagramBuilderHelper.newListLayoutStrategyDescription()
                .areChildNodesDraggableExpression("false")
                .bottomGapExpression("10")
                .growableNodes(new ArrayList<NodeDescription>().toArray(NodeDescription[]::new))
                .build();

        return this.diagramBuilderHelper.newRectangularNodeStyleDescription()
                .borderColor(this.colorProvider.getColor(NacreViewConstants.COMMUNICATIONMEDIA_NODE_BORDER_COLOR))
                .borderSize(2)
                .borderRadius(10)
                .background(this.colorProvider.getColor(NacreViewConstants.COMMUNICATIONMEDIA_NODE_BACKGROUND_COLOR))
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

}
