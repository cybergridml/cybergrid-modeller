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
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.InsideLabelStyle;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.NodeStyleDescription;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ComputingResourceNodeDescriptionProvider extends AbstractCybergridMLUsageNodeDescriptionProvider {

    public ComputingResourceNodeDescriptionProvider(IColorProvider colorProvider, DescriptionNameGenerator nameGenerator) {
        super(CybergridMLLibElementType.COMPUTINGRESOURCE, colorProvider, nameGenerator);
    }

    @Override
    protected NodeStyleDescription createNodeStyle() {
        var layoutStrategy = this.diagramBuilderHelper.newListLayoutStrategyDescription()
                .areChildNodesDraggableExpression("false")
                .bottomGapExpression("10")
                .growableNodes(new ArrayList<NodeDescription>().toArray(NodeDescription[]::new))
                .build();

        return this.diagramBuilderHelper.newRectangularNodeStyleDescription()
                .borderColor(this.colorProvider.getColor(CybergridMLViewConstants.COMPUTINGRESOURCE_NODE_BORDER_COLOR))
                .borderSize(3)
                .borderRadius(10)
                .background(this.colorProvider.getColor(CybergridMLViewConstants.COMPUTINGRESOURCE_NODE_BACKGROUND_COLOR))
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
        labelStyle.setLabelIcon("/images/computing_resource.png");
        return labelStyle;
    }
}
