package cea.fr.CybergridML.view.representations;

import cea.fr.CybergridML.view.services.description.CybergridMLToolDescriptionService;
import cea.fr.CybergridML.view.edges.AbstractEdgeDescriptionProvider;
import cea.fr.CybergridML.view.nodes.CentralControllerNodeDescriptionProvider;
import cea.fr.CybergridML.view.CybergridMLDescriptionNameGenerator;
import cea.fr.CybergridML.view.nodes.SoftwareResourceNodeDescriptionProvider;
import cea.fr.CybergridML.domain.CybergridMLLibElementType;
import cea.fr.CybergridML.util.CybergridMLAQLUtils;
import org.eclipse.sirius.components.view.RepresentationDescription;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.builder.providers.IDiagramElementDescriptionProvider;
import org.eclipse.sirius.components.view.builder.providers.IRepresentationDescriptionProvider;
import org.eclipse.sirius.components.view.diagram.*;
import org.eclipse.syson.diagram.common.view.ViewDiagramElementFinder;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.util.IDescriptionNameGenerator;
import org.eclipse.syson.util.SysMLMetamodelHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CfraRepresentationDescriptionProvider implements IRepresentationDescriptionProvider {

    public static final String DESCRIPTION_NAME = "CFRA view";

    public static final List<CybergridMLLibElementType> ELEMENTS = List.of(
            CybergridMLLibElementType.CENTRALCONTROLLER,
            CybergridMLLibElementType.SOFTWARERESOURCE
    );

    private final DiagramBuilders diagramBuilderHelper = new DiagramBuilders();
    private final CybergridMLDescriptionNameGenerator descriptionNameGenerator = new CybergridMLDescriptionNameGenerator("CYBERGRIDML_CFRA");
    private final CybergridMLToolDescriptionService cybergridMLToolDescriptionService = new CybergridMLToolDescriptionService(this.descriptionNameGenerator);

    public RepresentationDescription create(IColorProvider colorProvider) {
        String domainType = SysMLMetamodelHelper.buildQualifiedName(SysmlPackage.eINSTANCE.getNamespace());
        var diagramDescriptionBuilder = this.diagramBuilderHelper.newDiagramDescription();
        diagramDescriptionBuilder.arrangeLayoutDirection(ArrangeLayoutDirection.RIGHT)
                .layoutOption(DiagramLayoutOption.NONE)
                .domainType(domainType)
                .preconditionExpression(CybergridMLAQLUtils.getSelfServiceCallExpression("canCreateDiagram"))
                .name(DESCRIPTION_NAME)
                .style(this.diagramBuilderHelper.newDiagramStyleDescription().build())
                .titleExpression("aql:'%s'+ Sequence{self.existingViewUsagesCountForRepresentationCreation(), 1}->sum()".formatted(DESCRIPTION_NAME));
        var diagramDescription = diagramDescriptionBuilder.build();

        var cache = new ViewDiagramElementFinder();
        var diagramElementDescriptionProviders = this.createDiagramElementDescriptionProviders(colorProvider, descriptionNameGenerator);

        diagramElementDescriptionProviders.stream()
                .map(IDiagramElementDescriptionProvider::create)
                .forEach(cache::put);

        diagramElementDescriptionProviders.forEach(d -> d.link(diagramDescription, cache));
        var palette = this.createDiagramPalette(cache);
        diagramDescription.setPalette(palette);
        return diagramDescription;
    }

    protected List<IDiagramElementDescriptionProvider<? extends DiagramElementDescription>> createDiagramElementDescriptionProviders(
            IColorProvider cybergridMLColorProvider, CybergridMLDescriptionNameGenerator nameGenerator) {
        List<IDiagramElementDescriptionProvider<?>> providers = new ArrayList<>();
        providers.add(new CentralControllerNodeDescriptionProvider(cybergridMLColorProvider, nameGenerator, false));
        providers.add(new SoftwareResourceNodeDescriptionProvider(cybergridMLColorProvider, nameGenerator, false));
        providers.add(new AbstractEdgeDescriptionProvider(cybergridMLColorProvider, nameGenerator));

        return providers;
    }

    protected IDescriptionNameGenerator getDescriptionNameGenerator() {
        return this.descriptionNameGenerator;
    }

    protected String getName() {
        return DESCRIPTION_NAME;
    }

    protected DiagramPalette createDiagramPalette(IViewDiagramElementFinder cache) {
        return this.diagramBuilderHelper.newDiagramPalette()
                .dropTool(this.cybergridMLToolDescriptionService.createDropFromExplorerTool())
                .nodeTools(this.createNodeTools(cache))
                .build();
    }

    private NodeTool[] createNodeTools(IViewDiagramElementFinder cache) {
        var nodeTools = new ArrayList<NodeTool>();
        ELEMENTS.forEach(element -> nodeTools.addAll(this.getCreationToolsForElement(element, cache)));
        nodeTools.sort(Comparator.comparing(NodeTool::getName));
        return nodeTools.toArray(NodeTool[]::new);
    }

    private List<NodeTool> getCreationToolsForElement(CybergridMLLibElementType cybergridMLElementType, IViewDiagramElementFinder cache) {
        List<NodeTool> elementCreationTools = new ArrayList<>();
        NodeDescription nodeDescription = cache.getNodeDescription(
                descriptionNameGenerator.getNodeName(cybergridMLElementType.getServiceSuffix())).orElse(null);
        if (nodeDescription != null) {
            NodeTool nodeTool = this.cybergridMLToolDescriptionService.createNodeToolFromDiagramBackground(nodeDescription, cybergridMLElementType);
            elementCreationTools.add(nodeTool);
        }
        return elementCreationTools;
    }

}
