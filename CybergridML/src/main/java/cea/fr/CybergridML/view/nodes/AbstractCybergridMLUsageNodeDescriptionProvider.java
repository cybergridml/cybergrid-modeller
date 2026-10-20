/*******************************************************************************
 * Copyright (c) 2026 CEA LIST - RTE.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     CEA LIST - Initial API and implementation
 *******************************************************************************/

package cea.fr.CybergridML.view.nodes;

import org.eclipse.syson.util.DescriptionNameGenerator;
import cea.fr.CybergridML.view.colors.CybergridMLViewConstants;

import cea.fr.CybergridML.domain.CybergridMLLibElementType;
import cea.fr.CybergridML.util.CybergridMLAQLUtils;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.*;
import org.eclipse.syson.diagram.common.view.nodes.AbstractUsageNodeDescriptionProvider;
import org.eclipse.syson.util.AQLUtils;
import org.eclipse.syson.util.SysMLMetamodelHelper;

import java.util.ArrayList;
import java.util.List;


public abstract class AbstractCybergridMLUsageNodeDescriptionProvider extends AbstractUsageNodeDescriptionProvider {

    private final CybergridMLLibElementType cybergridMLElementType;
    private final boolean withHazardTool;

    protected AbstractCybergridMLUsageNodeDescriptionProvider(CybergridMLLibElementType cybergridMLElementType, IColorProvider colorProvider, DescriptionNameGenerator nameGenerator) {
        this(cybergridMLElementType, colorProvider, nameGenerator, true);
    }

    protected AbstractCybergridMLUsageNodeDescriptionProvider(CybergridMLLibElementType cybergridMLElementType, IColorProvider colorProvider, DescriptionNameGenerator nameGenerator, boolean withHazardTool) {
        super(cybergridMLElementType.geteClass(), colorProvider, nameGenerator);
        this.cybergridMLElementType = cybergridMLElementType;
        this.withHazardTool = withHazardTool;
    }



    @Override
    protected String getSemanticCandidatesExpression(String domainType) {
        return CybergridMLAQLUtils.getSelfServiceCallExpression("getExposedNacreElements",
                List.of(AQLUtils.aqlString(getCybergridMLElementType().getCybergridMLLibDefinitionName()),
                        domainType,
                        "ancestors",
                        IEditingContext.EDITING_CONTEXT,
                        DiagramContext.DIAGRAM_CONTEXT));
    }

    @Override
    protected String getNodeDescriptionName() {
        return this.getDescriptionNameGenerator().getNodeName(getCybergridMLElementType().getServiceSuffix());
    }

    protected String getDomainType() {
        return SysMLMetamodelHelper.buildQualifiedName(cybergridMLElementType.geteClass());
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
                .borderColor(this.colorProvider.getColor(CybergridMLViewConstants.HAZARD_NODE_BORDER_COLOR))
                .borderSize(3)
                .borderLineStyle(LineStyle.DASH)
                .background(this.colorProvider.getColor(CybergridMLViewConstants.HAZARD_NODE_BACKGROUND_COLOR))
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
                if (withHazardTool) {
                    palette.getQuickAccessTools().add(this.createHazardNodeTool());
                }
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
    protected DescriptionNameGenerator getDescriptionNameGenerator() {
        return (DescriptionNameGenerator) super.getDescriptionNameGenerator();
    }

    protected CybergridMLLibElementType getCybergridMLElementType() {
        return cybergridMLElementType;
    }

}
