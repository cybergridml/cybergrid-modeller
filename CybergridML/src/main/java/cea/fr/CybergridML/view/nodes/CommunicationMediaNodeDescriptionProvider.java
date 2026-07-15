package cea.fr.CybergridML.view.nodes;

import cea.fr.CybergridML.view.CybergridMLDescriptionNameGenerator;
import cea.fr.CybergridML.view.CybergridMLViewConstants;

import cea.fr.CybergridML.domain.CybergridMLLibElementType;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.InsideLabelStyle;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.NodeStyleDescription;
import org.eclipse.sirius.components.view.diagram.NodeTool;
import org.eclipse.syson.util.AQLUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CommunicationMediaNodeDescriptionProvider extends AbstractCybergridMLUsageNodeDescriptionProvider {

    private static final String CONNECT_IN_BULK_TOOL_NAME = "Connect in Bulk";

    private static final String SELECTED_COMPUTING_RESOURCE_ITERATOR = "selectedComputingResource";

    public CommunicationMediaNodeDescriptionProvider(IColorProvider colorProvider, CybergridMLDescriptionNameGenerator nameGenerator) {
        super(CybergridMLLibElementType.COMMUNICATIONMEDIA, colorProvider, nameGenerator);
    }

    @Override
    public void link(DiagramDescription diagramDescription, IViewDiagramElementFinder cache) {
        super.link(diagramDescription, cache);
        cache.getNodeDescription(this.getNodeDescriptionName()).ifPresent(nodeDescription -> {
            var palette = nodeDescription.getPalette();
            if (palette != null) {
                palette.getNodeTools().add(this.createConnectInBulkNodeTool());
            }
        });
    }

    private NodeTool createConnectInBulkNodeTool() {
        var selectionDialogTree = this.diagramBuilderHelper.newSelectionDialogTreeDescription()
                .elementsExpression(AQLUtils.getServiceCallExpression(IEditingContext.EDITING_CONTEXT, "getComputingResourceSelectionDialogElements"))
                .childrenExpression(AQLUtils.getSelfServiceCallExpression("getComputingResourceSelectionDialogChildren"))
                .isSelectableExpression("aql:true")
                .build();

        var selectionDialog = this.diagramBuilderHelper.newSelectionDialogDescription()
                .selectionMessage("Select the ComputingResource elements to connect (hold Ctrl/Cmd and click to select several):")
                .selectionDialogTreeDescription(selectionDialogTree)
                .multiple(true)
                .build();

        var createConnectEdge = this.viewBuilderHelper.newChangeContext()
                .expression(AQLUtils.getServiceCallExpression(SELECTED_COMPUTING_RESOURCE_ITERATOR, "createAllocateEdge",
                        List.of("self", Node.SELECTED_NODE, IEditingContext.EDITING_CONTEXT, "diagramServices")))
                .build();

        var connectToEachSelectedResource = this.viewBuilderHelper.newFor()
                .expression("aql:selectedObjects")
                .iteratorName(SELECTED_COMPUTING_RESOURCE_ITERATOR)
                .children(createConnectEdge)
                .build();

        return this.diagramBuilderHelper.newNodeTool()
                .name(CONNECT_IN_BULK_TOOL_NAME)
                .iconURLsExpression("/images/computing_resource.png")
                .dialogDescription(selectionDialog)
                .body(connectToEachSelectedResource)
                .build();
    }

    @Override
    protected NodeStyleDescription createNodeStyle() {
        var layoutStrategy = this.diagramBuilderHelper.newListLayoutStrategyDescription()
                .areChildNodesDraggableExpression("false")
                .bottomGapExpression("10")
                .growableNodes(new ArrayList<NodeDescription>().toArray(NodeDescription[]::new))
                .build();

        return this.diagramBuilderHelper.newRectangularNodeStyleDescription()
                .borderColor(this.colorProvider.getColor(CybergridMLViewConstants.COMMUNICATIONMEDIA_NODE_BORDER_COLOR))
                .borderSize(2)
                .borderRadius(10)
                .background(this.colorProvider.getColor(CybergridMLViewConstants.COMMUNICATIONMEDIA_NODE_BACKGROUND_COLOR))
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
    protected InsideLabelStyle createInsideLabelStyle() {
        InsideLabelStyle labelStyle = super.createInsideLabelStyle();
        labelStyle.setLabelIcon("/images/communication_media.png");
        return labelStyle;
    }

}
