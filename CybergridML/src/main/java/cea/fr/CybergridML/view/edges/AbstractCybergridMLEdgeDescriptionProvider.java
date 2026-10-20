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

package cea.fr.CybergridML.view.edges;

import cea.fr.CybergridML.view.colors.CybergridMLViewConstants;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.*;
import org.eclipse.syson.diagram.common.view.edges.AbstractAllocateEdgeDescriptionProvider;

public abstract class AbstractCybergridMLEdgeDescriptionProvider extends AbstractAllocateEdgeDescriptionProvider {

    private final boolean withHazardTool;


    protected AbstractCybergridMLEdgeDescriptionProvider(IColorProvider colorProvider) {
        this(colorProvider, true);
    }

    protected AbstractCybergridMLEdgeDescriptionProvider(IColorProvider colorProvider, boolean withHazardTool) {
        super(colorProvider);
        this.withHazardTool = withHazardTool;
    }

    @Override
    public EdgeDescription create() {
        EdgeDescription description = super.create();
        description.getConditionalStyles().add(createHazardConditionalEdgeStyle());
        return description;
    }

    @Override
    public void link(org.eclipse.sirius.components.view.diagram.DiagramDescription diagramDescription, IViewDiagramElementFinder cache) {
        super.link(diagramDescription, cache);
        if (withHazardTool) {
            cache.getEdgeDescription(this.getName()).ifPresent(edgeDescription -> {
                var palette = edgeDescription.getPalette();
                if (palette != null) {
                    palette.getQuickAccessTools().add(createHazardEdgeTool());
                }
            });
        }
    }

    private ConditionalEdgeStyle createHazardConditionalEdgeStyle() {
        return this.diagramBuilderHelper.newConditionalEdgeStyle()
                .condition("aql:self.hasHazard()")
                .color(this.colorProvider.getColor(CybergridMLViewConstants.HAZARD_NODE_BORDER_COLOR))
                .edgeWidth(3)
                .lineStyle(LineStyle.DASH)
                .borderSize(0)
                .sourceArrowStyle(getHazardSourceArrowStyle())
                .targetArrowStyle(getHazardTargetArrowStyle())
                .build();
    }

    private NodeTool createHazardEdgeTool() {
        var toggleBody = this.viewBuilderHelper.newChangeContext()
                .expression("aql:self.toggleHazard(editingContext)")
                .build();

        return this.diagramBuilderHelper.newNodeTool()
                .name("Toggle Hazard")
                .iconURLsExpression("/images/hazard.svg")
                .body(toggleBody)
                .build();
    }

    protected ArrowStyle getHazardSourceArrowStyle() {
        return ArrowStyle.NONE;
    }

    protected ArrowStyle getHazardTargetArrowStyle() {
        return ArrowStyle.NONE;
    }
}
