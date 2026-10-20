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
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.*;
import org.eclipse.sirius.components.view.emf.diagram.ViewDiagramDescriptionConverter;
import org.eclipse.syson.diagram.services.aql.DiagramMutationAQLService;
import org.eclipse.syson.util.ServiceMethod;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MainGridNodeDescriptionProvider extends AbstractCybergridMLUsageNodeDescriptionProvider {

    /**
     * Label of the XIIDM import tool. Also used by the frontend extension to recognize the dialog to render.
     */
    public static final String IMPORT_XIIDM_TOOL_NAME = "Import XIIDM file";

    public MainGridNodeDescriptionProvider(IColorProvider colorProvider, DescriptionNameGenerator nameGenerator) {
        this(colorProvider, nameGenerator, true);
    }

    public MainGridNodeDescriptionProvider(IColorProvider colorProvider, DescriptionNameGenerator nameGenerator, boolean withHazardTool) {
        super(CybergridMLLibElementType.MAINGRID, colorProvider, nameGenerator, withHazardTool);
    }

    @Override
    protected NodeStyleDescription createNodeStyle() {

        var layoutStrategy = this.diagramBuilderHelper.newFreeFormLayoutStrategyDescription()
                .build();

        return this.diagramBuilderHelper.newRectangularNodeStyleDescription()
                .borderColor(this.colorProvider.getColor(CybergridMLViewConstants.MAINGRID_NODE_BORDER_COLOR))
                .borderSize(3)
                .borderRadius(10)
                .background(this.colorProvider.getColor(CybergridMLViewConstants.MAINGRID_NODE_BACKGROUND_COLOR))
                .childrenLayoutStrategy(layoutStrategy)
                .build();
    }

    @Override
    protected Set<NodeDescription> getReusedChildren(IViewDiagramElementFinder cache) {
        var nodes = new LinkedHashSet<NodeDescription>();

        cache.getNodeDescription(getDescriptionNameGenerator().getNodeName(CybergridMLLibElementType.ZONE.getServiceSuffix()))
                .ifPresent(nodes::add);
        cache.getNodeDescription(getDescriptionNameGenerator().getNodeName(CybergridMLLibElementType.SUBSTATION.getServiceSuffix()))
                .ifPresent(nodes::add);
        return nodes;
    }
    @Override
    protected Set<NodeDescription> getReusedBorderNodes(IViewDiagramElementFinder cache) {
        return Set.of();
    }
    @Override
    protected List<NodeDescription> getAllNodeDescriptions(IViewDiagramElementFinder cache) {
        return List.of();
    }
    @Override
    protected InsideLabelStyle createInsideLabelStyle() {
        InsideLabelStyle labelStyle = super.createInsideLabelStyle();
        labelStyle.setLabelIcon("/images/maingrid.svg");
        return labelStyle;
    }
    @Override
    protected List<NodeTool> getToolsWithoutSection(NodeDescription nodeDescription, IViewDiagramElementFinder cache) {
        var tools = new ArrayList<NodeTool>();

        tools.add(createChildCreationTool(CybergridMLLibElementType.ZONE.getName(), "createZone"));
        tools.add(createChildCreationTool(CybergridMLLibElementType.SUBSTATION.getName(), "createSubstation"));
        tools.add(createImportXiidmTool());


        return tools;
    }

    /**
     * Tool uploading an XIIDM (PowSyBl) network file and creating the matching parts inside this grid.
     *
     * <p>The file picker itself lives in the frontend: the tool carries a {@link SelectionDialogDescription} used
     * purely as a marker, and the {@code XiidmImportDialog} extension registered by the application replaces its
     * rendering with a file upload dialog. That dialog hands the file back as the {@code xiidmContent} and
     * {@code xiidmFileName} tool variables, which the body below passes to the import service. The marker has no
     * tree description on purpose, so the built-in selection dialog machinery skips it.
     */
    private NodeTool createImportXiidmTool() {
        var exposeImportedElement = this.viewBuilderHelper.newChangeContext()
                .expression(ServiceMethod.of4(DiagramMutationAQLService::expose)
                        .aqlSelf(IEditingContext.EDITING_CONTEXT, DiagramContext.DIAGRAM_CONTEXT, Node.SELECTED_NODE,
                                ViewDiagramDescriptionConverter.CONVERTED_NODES_VARIABLE));

        var importAndExpose = this.viewBuilderHelper.newChangeContext()
                .expression("aql:self.importXiidm(xiidmContent, xiidmFileName, %s)".formatted(IEditingContext.EDITING_CONTEXT))
                .children(exposeImportedElement.build())
                .build();

        var fileUploadDialogMarker = this.diagramBuilderHelper.newSelectionDialogDescription()
                .descriptionExpression("Select an XIIDM file to import into this grid")
                .build();

        return this.diagramBuilderHelper.newNodeTool()
                .name(IMPORT_XIIDM_TOOL_NAME)
                .iconURLsExpression("/images/import_xiidm.svg")
                .dialogDescription(fileUploadDialogMarker)
                .body(importAndExpose)
                .build();
    }

    private NodeTool createChildCreationTool(String toolName, String createServiceMethodName) {
        var exposeNewInstance = this.viewBuilderHelper.newChangeContext()
                .expression(ServiceMethod.of4(DiagramMutationAQLService::expose)
                        .aqlSelf(IEditingContext.EDITING_CONTEXT, DiagramContext.DIAGRAM_CONTEXT, Node.SELECTED_NODE,
                                ViewDiagramDescriptionConverter.CONVERTED_NODES_VARIABLE));

        var createAndExposeNewInstance = this.viewBuilderHelper.newChangeContext()
                .expression("aql:self.%s(%s)".formatted(createServiceMethodName, IEditingContext.EDITING_CONTEXT))
                .children(exposeNewInstance.build())
                .build();

        return this.diagramBuilderHelper.newNodeTool()
                .name(toolName)
                .body(createAndExposeNewInstance)
                .build();
    }



}