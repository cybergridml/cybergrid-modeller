package cea.fr.NacreRTE.View.nodes;

import cea.fr.NacreRTE.library.NacreLibElementType;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.*;
import org.eclipse.syson.diagram.common.view.nodes.AbstractUsageNodeDescriptionProvider;
import org.eclipse.syson.util.SysMLMetamodelHelper;

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
        String nacreLibDefName = getLcaElementType().getNacreLibDefinitionName();
        return "aql:self.ownedMember->filter(" + domainType + ")->select(p | p.type->exists(t | t.qualifiedName = '" + nacreLibDefName + "'))";
    }

    @Override
    protected String getNodeDescriptionName() {
        return this.getDescriptionNameGenerator().getNodeName(getLcaElementType().getServiceSuffix());
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
                .synchronizationPolicy(SynchronizationPolicy.SYNCHRONIZED)
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
    protected List<NodeToolSection> getToolSections(NodeDescription nodeDescription, IViewDiagramElementFinder cache) {
        return List.of();
    }

    @Override
    protected NacreDescriptionNameGenerator getDescriptionNameGenerator() {
        return (NacreDescriptionNameGenerator) super.getDescriptionNameGenerator();
    }

    protected NacreLibElementType getLcaElementType() {
        return nacreElementType;
    }

}