
package cea.fr.NacreRTE;



import cea.fr.NacreRTE.View.nodes.MaingridNodeDescriptionProvider;
import cea.fr.NacreRTE.View.nodes.NacreDescriptionNameGenerator;
import cea.fr.NacreRTE.View.nodes.ZoneNodeDescriptionProvider;
import cea.fr.NacreRTE.library.NacreLibElementType;
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
import org.eclipse.syson.util.SysMLMetamodelHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class NacreRepresentationDescriptionProviderT implements IRepresentationDescriptionProvider {

    public static final String DESCRIPTION_NAME = "NacreT view";
    


    public static final List<NacreLibElementType> ELEMENTS = List.of(
            NacreLibElementType.MAINGRID,
            NacreLibElementType.ZONE
    );



    private final DiagramBuilders diagramBuilderHelper = new DiagramBuilders();
    private final NacreDescriptionNameGenerator descriptionNameGenerator = new NacreDescriptionNameGenerator();
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
       providers.add(new ZoneNodeDescriptionProvider(nacreColorProvider, nameGenerator));
       return providers;
   }

    protected String getName() {
        return DESCRIPTION_NAME;
    }

    protected DiagramPalette createDiagramPalette(IViewDiagramElementFinder cache) {
        return this.diagramBuilderHelper.newDiagramPalette()
                //.dropNodeTool(this.createDropFromDiagramTool(cache))
                .dropTool(this.nacreToolDescriptionService.createDropFromExplorerTool())
                .nodeTools(this.createNodeTools(cache))
                .build();
    }


    /**
     * Create the tools shown in the main tools section when clicking on the diagram background
     */
    private NodeTool[] createNodeTools(IViewDiagramElementFinder cache) {
        var nodeTools = new ArrayList<NodeTool>();

        ELEMENTS.forEach(element -> nodeTools.addAll(this.getCreationToolsForElement(element, cache)));
        nodeTools.sort(Comparator.comparing(NodeTool::getName));

        return nodeTools.toArray(NodeTool[]::new);
    }

    /**
     * Create the creation tool for the given element (Activity, Container, ..)
     */
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

//    private DropNodeTool createDropFromDiagramTool(IViewDiagramElementFinder cache) {
//        var dropElementFromDiagram = this.viewBuilderHelper.newChangeContext().expression(
//                AQLUtils.getServiceCallExpression("droppedElement", "dropElementFromDiagram", List.of("droppedNode",
//                        "targetElement", "targetNode", "editingContext", "diagramContext", "convertedNodes")));
//
//        var activityNodeDescription = cache.getNodeDescription(descriptionNameGenerator.getNodeName(LcaElementType.ACTIVITY.getServiceSuffix()));
//        var containerNodeDescription = cache.getNodeDescription(descriptionNameGenerator.getNodeName(LcaElementType.CONTAINER.getServiceSuffix()));
//        if (activityNodeDescription.isPresent() && containerNodeDescription.isPresent()) {
//            return this.diagramBuilderHelper.newDropNodeTool().name("Drop from Diagram")
//                    .acceptedNodeTypes(activityNodeDescription.get())
//                    .acceptedNodeTypes(containerNodeDescription.get())
//                    .body(dropElementFromDiagram.build()).build();
//        }
//
//        return null;
//    }

    private void addPropertiesCompartmentItemNodes(List<IDiagramElementDescriptionProvider<?>> providers, NacreLibElementType lcaElementType, IColorProvider lcaColorProvider, NacreDescriptionNameGenerator nameGenerator) {
//        EReference nestedEReference = NacreLibElementType.isSubclassifier() ?
//                LcaNestedElements.PROPERTY.getDefinitionNestedEReference() :
//                LcaNestedElements.PROPERTY.getUsageNestedEReference();
//        providers.add(new PropertyCompartmentItemNodeDescriptionProviderAbstract(
//                lcaElementType,
//                nestedEReference,
//                lcaColorProvider,
//                nameGenerator));
//        providers.add(new InheritedPropertyCompartmentItemNodeDescriptionProvider(
//                lcaElementType,
//                nestedEReference,
//                lcaColorProvider,
//                nameGenerator));
    }

    private void addPropertiesCompartmentNode(List<IDiagramElementDescriptionProvider<?>> providers, NacreLibElementType nacreElementType, IColorProvider lcaColorProvider, NacreDescriptionNameGenerator nameGenerator) {
//        EReference nestedEReference = lcaElementType.isSubclassifier() ?
//                LcaNestedElements.PROPERTY.getDefinitionNestedEReference() :
//                LcaNestedElements.PROPERTY.getUsageNestedEReference();
//        providers.add(new PropertiesCompartmentNodeDescriptionProvider(
//                lcaElementType,
//                nestedEReference,
//                lcaColorProvider,
//                nameGenerator));
   }

//    private void addPortsCompartment(List<IDiagramElementDescriptionProvider<?>> providers, LcaElementType lcaElementType, IColorProvider lcaColorProvider, LcaDescriptionNameGenerator nameGenerator) {
//        EReference nestedEReference = lcaElementType.isSubclassifier() ?
//                LcaNestedElements.PORTS.getDefinitionNestedEReference() :
//                LcaNestedElements.PORTS.getUsageNestedEReference();
//        providers.add(new PortsCompartmentNodeDescriptionProvider(
//                lcaElementType,
//                nestedEReference,
//                lcaColorProvider,
//                nameGenerator));
//    }

//    private void addProcessFlowCompartment(List<IDiagramElementDescriptionProvider<?>> providers, LcaElementType lcaElementType, IColorProvider colorProvider, LcaDescriptionNameGenerator nameGenerator) {
//        EReference nestedEReference = lcaElementType.isSubclassifier() ?
//                LcaNestedElements.PROCESS.getDefinitionNestedEReference() :
//                LcaNestedElements.PROCESS.getUsageNestedEReference();
//        providers.add(new ProcessFlowCompartmentNodeDescriptionProvider(
//                lcaElementType,
//                nestedEReference,
//                colorProvider,
//                nameGenerator));
//    }
}
