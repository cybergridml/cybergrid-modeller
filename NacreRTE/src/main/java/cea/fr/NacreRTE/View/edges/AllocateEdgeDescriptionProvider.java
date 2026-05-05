/*******************************************************************************
 * Copyright (c) 2024, 2025 Obeo.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package cea.fr.NacreRTE.View.edges;

import cea.fr.NacreRTE.View.nodes.NacreDescriptionNameGenerator;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.ArrowStyle;
import org.eclipse.sirius.components.view.diagram.EdgeStyle;
import org.eclipse.sirius.components.view.diagram.LineStyle;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.syson.diagram.common.view.edges.AbstractAllocateEdgeDescriptionProvider;
import org.eclipse.syson.standard.diagrams.view.SDVDescriptionNameGenerator;
import org.eclipse.syson.standard.diagrams.view.SDVDiagramDescriptionProvider;
import org.eclipse.syson.util.IDescriptionNameGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to create the Allocate edge description inside the General View diagram.
 *
 * @author Jerome Gout
 */
public class AllocateEdgeDescriptionProvider extends AbstractAllocateEdgeDescriptionProvider {

    private final IDescriptionNameGenerator descriptionNameGenerator;
    private final DiagramBuilders diagramBuilder = new DiagramBuilders();

    public AllocateEdgeDescriptionProvider(IColorProvider colorProvider, NacreDescriptionNameGenerator nameGenerator) {
        super(colorProvider);
        this.descriptionNameGenerator = new SDVDescriptionNameGenerator();
    }

    @Override
    protected String getName() {
        return this.descriptionNameGenerator.getEdgeName("Allocate");
    }

    @Override
    protected List<NodeDescription> getSourceNodes(IViewDiagramElementFinder cache) {
        return this.getSourceAndTarget(cache);
    }

    @Override
    protected List<NodeDescription> getTargetNodes(IViewDiagramElementFinder cache) {
        return this.getSourceAndTarget(cache);
    }

    private List<NodeDescription> getSourceAndTarget(IViewDiagramElementFinder cache) {
        var sourcesAndTargets = new ArrayList<NodeDescription>();
        SDVDiagramDescriptionProvider.USAGES.forEach(usage -> {
            cache.getNodeDescription(this.descriptionNameGenerator.getNodeName(usage)).ifPresent(sourcesAndTargets::add);
        });
        return sourcesAndTargets;
    }
    private EdgeStyle generateEdgeStyle() {
        return this.diagramBuilder
                .newEdgeStyle()
                .edgeWidth(1)
                .borderSize(0)
                .lineStyle(LineStyle.SOLID)
                .color(colorProvider.getColor("purple 500"))
                .sourceArrowStyle(ArrowStyle.NONE)
                .targetArrowStyle(ArrowStyle.NONE)
                .build();
    }
}
