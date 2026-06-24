package cea.fr.CybergridML.view.representations;

import cea.fr.CybergridML.view.CybergridMLDescriptionNameGenerator;
import cea.fr.CybergridML.view.nodes.CommunicationMediaNodeDescriptionProvider;
import cea.fr.CybergridML.view.nodes.ComputingResourceNodeDescriptionProvider;
import cea.fr.CybergridML.view.nodes.RedundancyClusterNodeDescriptionProvider;
import cea.fr.CybergridML.view.nodes.SoftwareResourceNodeDescriptionProvider;
import cea.fr.CybergridML.view.services.CybergridMLToolDescriptionService;
import cea.fr.CybergridML.view.edges.AllocateCraEdgeDescriptionProvider;
import cea.fr.CybergridML.view.edges.ConnectEdgeDescriptionProvider;
import cea.fr.CybergridML.view.edges.ControlEdgeDescriptionProvider;
import cea.fr.CybergridML.view.edges.LinkEdgeDescriptionProvider;
import cea.fr.CybergridML.view.nodes.*;
import cea.fr.CybergridML.domain.CybergridMLLibElementType;
import org.eclipse.sirius.components.view.RepresentationDescription;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.builder.providers.IDiagramElementDescriptionProvider;
import org.eclipse.sirius.components.view.builder.providers.IRepresentationDescriptionProvider;
import org.eclipse.sirius.components.view.diagram.*;
import org.eclipse.syson.diagram.common.view.ViewDiagramElementFinder;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.util.AQLUtils;
import org.eclipse.syson.util.IDescriptionNameGenerator;
import org.eclipse.syson.util.SysMLMetamodelHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CraRepresentationDescriptionProvider implements IRepresentationDescriptionProvider {

    public static final String DESCRIPTION_NAME = "CRA view";

    public static final List<CybergridMLLibElementType> ELEMENTS = List.of(
            CybergridMLLibElementType.COMMUNICATIONMEDIA,
            CybergridMLLibElementType.COMPUTINGRESOURCE,
            CybergridMLLibElementType.REDUNDANCYCLUSTER,
            CybergridMLLibElementType.SOFTWARERESOURCE
    );

    private final DiagramBuilders diagramBuilderHelper = new DiagramBuilders();
    private final CybergridMLDescriptionNameGenerator descriptionNameGenerator = new CybergridMLDescriptionNameGenerator("NACRE_CRA");
    private final CybergridMLToolDescriptionService nacreToolDescriptionService = new CybergridMLToolDescriptionService(this.descriptionNameGenerator);

    public RepresentationDescription create(IColorProvider colorProvider) {
        String domainType = SysMLMetamodelHelper.buildQualifiedName(SysmlPackage.eINSTANCE.getNamespace());
        var diagramDescriptionBuilder = this.diagramBuilderHelper.newDiagramDescription();
        diagramDescriptionBuilder.arrangeLayoutDirection(ArrangeLayoutDirection.RIGHT)
                .autoLayout(false)
                .domainType(domainType)
                .preconditionExpression(AQLUtils.getSelfServiceCallExpression("canCreateDiagram"))
                .name(DESCRIPTION_NAME)
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
            IColorProvider nacreColorProvider, CybergridMLDescriptionNameGenerator nameGenerator) {
        List<IDiagramElementDescriptionProvider<?>> providers = new ArrayList<>();
        providers.add(new CommunicationMediaNodeDescriptionProvider(nacreColorProvider, nameGenerator));
        providers.add(new ComputingResourceNodeDescriptionProvider(nacreColorProvider, nameGenerator));
        providers.add(new SoftwareResourceNodeDescriptionProvider(nacreColorProvider, nameGenerator));
        providers.add(new ControlEdgeDescriptionProvider(nacreColorProvider, nameGenerator));
        providers.add(new AllocateCraEdgeDescriptionProvider(nacreColorProvider, nameGenerator));
        providers.add(new LinkEdgeDescriptionProvider(nacreColorProvider, nameGenerator));
        providers.add(new ConnectEdgeDescriptionProvider(nacreColorProvider, nameGenerator));
        providers.add(new RedundancyClusterNodeDescriptionProvider(nacreColorProvider, nameGenerator));
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
                .dropTool(this.nacreToolDescriptionService.createDropFromExplorerTool())
                .nodeTools(this.createNodeTools(cache))
                .build();
    }

    private NodeTool[] createNodeTools(IViewDiagramElementFinder cache) {
        var nodeTools = new ArrayList<NodeTool>();
        ELEMENTS.forEach(element -> nodeTools.addAll(this.getCreationToolsForElement(element, cache)));
        nodeTools.sort(Comparator.comparing(NodeTool::getName));
        return nodeTools.toArray(NodeTool[]::new);
    }

    private List<NodeTool> getCreationToolsForElement(CybergridMLLibElementType nacreElementType, IViewDiagramElementFinder cache) {
        List<NodeTool> elementCreationTools = new ArrayList<>();
        NodeDescription nodeDescription = cache.getNodeDescription(
                descriptionNameGenerator.getNodeName(nacreElementType.getServiceSuffix())).orElse(null);
        if (nodeDescription != null) {
            NodeTool nodeTool = this.nacreToolDescriptionService.createNodeToolFromDiagramBackground(nodeDescription, nacreElementType);
            elementCreationTools.add(nodeTool);
        }
        return elementCreationTools;
    }
}
