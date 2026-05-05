/*******************************************************************************
 * Copyright (c) 2023, 2026 Obeo.
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
import cea.fr.NacreRTE.library.NacreLibElementType;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.*;
import org.eclipse.syson.diagram.common.view.edges.AbstractBindingConnectorAsUsageEdgeDescriptionProvider;
import org.eclipse.syson.sysml.*;
import org.eclipse.syson.util.AQLConstants;
import org.eclipse.syson.util.IDescriptionNameGenerator;
import org.eclipse.syson.util.SysMLMetamodelHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Used to describe an {@link BindingConnectorAsUsage} edge between two {@link PortUsage}, {@link ItemUsage} or
 * {@link ReferenceUsage}.
 *
 * @author frouene
 */
public class BindingConnectorAsUsageEdgeDescriptionProvider extends AbstractBindingConnectorAsUsageEdgeDescriptionProvider {

    private final NacreDescriptionNameGenerator nacreNameGenerator;
    private static final String EDGE_NAME = "Control";
    private final DiagramBuilders diagramBuilder = new DiagramBuilders();

    public BindingConnectorAsUsageEdgeDescriptionProvider(IColorProvider colorProvider, NacreDescriptionNameGenerator nacreNameGenerator) {
        super(colorProvider);
        this.nacreNameGenerator = Objects.requireNonNull(nacreNameGenerator);
    }

    @Override
    public EdgeDescription create() {
        String domainType = SysMLMetamodelHelper.buildQualifiedName(SysmlPackage.eINSTANCE.getPartUsage());
        return this.diagramBuilder.newEdgeDescription()
                .domainType(domainType)
                .name(getName())
                .semanticCandidatesExpression("aql:self.getAllReachable(" + domainType + ")")
                //.sourceExpression(AQLConstants.AQL_SELF + ".source")
                //.targetExpression(AQLConstants.AQL_SELF + ".target")
                .synchronizationPolicy(SynchronizationPolicy.SYNCHRONIZED)
                .centerLabelExpression("")
                .isDomainBasedEdge(true)
                .style(this.generateEdgeStyle())
                .build();

    }

    @Override
    public String getName() {
        return this.nacreNameGenerator.getEdgeName(EDGE_NAME);
    }

    @Override
    protected List<NodeDescription> getSourceNodes(IViewDiagramElementFinder cache) {
        List<NodeDescription> nodes = new ArrayList<>();
        cache.getNodeDescription(this.nacreNameGenerator.getBorderNodeName(NacreLibElementType.MAINGRID.geteClass())).ifPresent(nodes::add);
        return nodes;
    }

    @Override
    protected List<NodeDescription> getTargetNodes(IViewDiagramElementFinder cache) {
        List<NodeDescription> nodes = new ArrayList<>();
        cache.getNodeDescription(this.nacreNameGenerator.getBorderNodeName(NacreLibElementType.CENTRALCONTROLLER.geteClass())).ifPresent(nodes::add);

        return nodes;
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
