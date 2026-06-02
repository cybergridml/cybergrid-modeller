package cea.fr.NacreRTE.View.nodes;

import cea.fr.NacreRTE.library.NacreLibElementType;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.*;
import org.eclipse.syson.diagram.common.view.nodes.AbstractUsageNodeDescriptionProvider;
import org.eclipse.syson.util.SysMLMetamodelHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Imen Azouzi (CEA LIST) <imen.azouzi@cea.fr>
 */
public abstract class AbstractNacreUsageNodeDescriptionProvider extends AbstractUsageNodeDescriptionProvider {

    private final NacreLibElementType nacreElementType;

    protected AbstractNacreUsageNodeDescriptionProvider(NacreLibElementType nacreElementType, IColorProvider colorProvider, NacreDescriptionNameGenerator nameGenerator) {
        super(nacreElementType.geteClass(), colorProvider, nameGenerator);
        this.nacreElementType = nacreElementType;
    }



    @Override
    protected String getSemanticCandidatesExpression(String domainType) {
        String nacreLibDefName = getNacreElementType().getNacreLibDefinitionName();
        return "aql:self.ownedMember->filter(" + domainType + ")->select(p | p.type->exists(t | t.qualifiedName = '" + nacreLibDefName + "'))";
    }

    @Override
    protected String getNodeDescriptionName() {
        return this.getDescriptionNameGenerator().getNodeName(getNacreElementType().getServiceSuffix());
    }

    protected String getDomainType() {
        return SysMLMetamodelHelper.buildQualifiedName(nacreElementType.geteClass());
    }

    @Override
    public NodeDescription create() {
        return this.diagramBuilderHelper.newNodeDescription()
                .collapsible(true)
                .domainType(getDomainType())
                .name(getNodeDescriptionName())
                .semanticCandidatesExpression(getSemanticCandidatesExpression(getDomainType()))
                .insideLabel(this.createInsideLabelDescription())
                .defaultHeightExpression("70")
                .defaultWidthExpression("150")
                .userResizable(UserResizableDirection.BOTH)
                .keepAspectRatio(false)
                .style(this.createNodeStyle())
                .conditionalStyles(this.createHazardConditionalNodeStyle())
                .synchronizationPolicy(SynchronizationPolicy.SYNCHRONIZED)
                .build();
    }

    private ConditionalNodeStyle createHazardConditionalNodeStyle() {
        var layoutStrategy = this.diagramBuilderHelper.newListLayoutStrategyDescription()
                .areChildNodesDraggableExpression("false")
                .bottomGapExpression("10")
                .growableNodes(new ArrayList<NodeDescription>().toArray(NodeDescription[]::new))
                .build();

        var hazardStyle = this.diagramBuilderHelper.newRectangularNodeStyleDescription()
                .borderColor(this.colorProvider.getColor(NacreViewConstants.HAZARD_NODE_BORDER_COLOR))
                .borderSize(3)
                .borderLineStyle(LineStyle.DASH)
                .background(this.colorProvider.getColor(NacreViewConstants.HAZARD_NODE_BACKGROUND_COLOR))
                .childrenLayoutStrategy(layoutStrategy)
                .build();

        return this.diagramBuilderHelper.newConditionalNodeStyle()
                .condition("aql:self.hasHazard()")
                .style(hazardStyle)
                .build();
    }

    @Override
    protected InsideLabelDescription createInsideLabelDescription() {
        InsideLabelDescription label = super.createInsideLabelDescription();
        label.setLabelExpression("aql:self.declaredName");
        return label;
    }

protected abstract NodeStyleDescription createNodeStyle();

    @Override
    public void link(DiagramDescription diagramDescription, IViewDiagramElementFinder cache) {
        super.link(diagramDescription, cache);
        cache.getNodeDescription(this.getNodeDescriptionName()).ifPresent(nodeDescription -> {
            var palette = nodeDescription.getPalette();
            if (palette != null) {
                palette.getQuickAccessTools().removeIf(t -> "Delete from Diagram".equals(t.getName()));
                palette.getQuickAccessTools().add(this.createHazardNodeTool());
            }
        });
    }

    @Override
    protected List<NodeToolSection> getToolSections(NodeDescription nodeDescription, IViewDiagramElementFinder cache) {
        return List.of();
    }

    @Override
    protected List<NodeTool> getToolsWithoutSection(NodeDescription nodeDescription, IViewDiagramElementFinder cache) {
        return List.of();
    }

    private NodeTool createHazardNodeTool() {
        var toggleBody = this.viewBuilderHelper.newChangeContext()
                .expression("aql:self.toggleHazard(editingContext)")
                .build();

        return this.diagramBuilderHelper.newNodeTool()
                .name("Toggle Hazard")
                .iconURLsExpression("/images/hazard.svg")
                .body(toggleBody)
                .build();
    }

    @Override
    protected NacreDescriptionNameGenerator getDescriptionNameGenerator() {
        return (NacreDescriptionNameGenerator) super.getDescriptionNameGenerator();
    }

    protected NacreLibElementType getNacreElementType() {
        return nacreElementType;
    }

}
