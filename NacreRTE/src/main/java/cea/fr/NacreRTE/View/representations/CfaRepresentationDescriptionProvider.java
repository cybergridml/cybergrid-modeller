package cea.fr.NacreRTE.View.representations;

import cea.fr.NacreRTE.NacreToolDescriptionService;
import cea.fr.NacreRTE.View.edges.ControlEdgeDescriptionProvider;
import cea.fr.NacreRTE.View.nodes.*;
import cea.fr.NacreRTE.library.NacreLibElementType;
import org.eclipse.sirius.components.view.RepresentationDescription;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.builder.providers.IDiagramElementDescriptionProvider;
import org.eclipse.sirius.components.view.builder.providers.IRepresentationDescriptionProvider;
import org.eclipse.sirius.components.view.diagram.*;
import org.eclipse.syson.diagram.common.view.ViewDiagramElementFinder;
import org.eclipse.syson.standard.diagrams.view.edges.*;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.util.AQLUtils;
import org.eclipse.syson.util.IDescriptionNameGenerator;
import org.eclipse.syson.util.SysMLMetamodelHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CfaRepresentationDescriptionProvider implements IRepresentationDescriptionProvider {

    public static final String DESCRIPTION_NAME = "CFA view";

    public static final List<NacreLibElementType> ELEMENTS = List.of(
            NacreLibElementType.MAINGRID,
            //NacreLibElementType.ZONE,
            //NacreLibElementType.SUBSTATION,
            NacreLibElementType.CENTRALCONTROLLER
           // NacreLibElementType.ZONALCONTROlLER
    );

    private final DiagramBuilders diagramBuilderHelper = new DiagramBuilders();
    private final NacreDescriptionNameGenerator descriptionNameGenerator = new NacreDescriptionNameGenerator("NACRE_CFA");
    private final NacreToolDescriptionService nacreToolDescriptionService = new NacreToolDescriptionService(this.descriptionNameGenerator);

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
            IColorProvider nacreColorProvider, NacreDescriptionNameGenerator nameGenerator) {
        List<IDiagramElementDescriptionProvider<?>> providers = new ArrayList<>();
        providers.add(new MaingridNodeDescriptionProvider(nacreColorProvider, nameGenerator));
       // providers.add(new ZoneNodeDescriptionProvider(nacreColorProvider, nameGenerator));
       // providers.add(new SubstationNodeDescriptionProvider(nacreColorProvider, nameGenerator));
       // providers.add(new ZonalcontrollerNodeDescriptionProvider(nacreColorProvider, nameGenerator));
        providers.add(new CentralcontrollerNodeDescriptionProvider(nacreColorProvider, nameGenerator));
        providers.add(new ControlEdgeDescriptionProvider(nacreColorProvider, nameGenerator));
        providers.addAll(this.createAllEdgeDescriptionProviders(nacreColorProvider));
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

    private List<NodeTool> getCreationToolsForElement(NacreLibElementType nacreElementType, IViewDiagramElementFinder cache) {
        List<NodeTool> elementCreationTools = new ArrayList<>();
        NodeDescription nodeDescription = cache.getNodeDescription(
                descriptionNameGenerator.getNodeName(nacreElementType.getServiceSuffix())).orElse(null);
        if (nodeDescription != null) {
            NodeTool nodeTool = this.nacreToolDescriptionService.createNodeToolFromDiagramBackground(nodeDescription, nacreElementType);
            elementCreationTools.add(nodeTool);
        }
        return elementCreationTools;
    }

    private List<IDiagramElementDescriptionProvider<?>> createAllEdgeDescriptionProviders(IColorProvider colorProvider) {
        var edgeDescriptionProviders = new ArrayList<IDiagramElementDescriptionProvider<?>>();
        edgeDescriptionProviders.add(new AllocateEdgeDescriptionProvider(colorProvider));
        return edgeDescriptionProviders;
    }
}
