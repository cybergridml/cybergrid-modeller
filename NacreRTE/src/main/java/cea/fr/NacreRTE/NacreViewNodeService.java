package cea.fr.NacreRTE;


import cea.fr.NacreRTE.library.util.NacreLibraryUtil;
import cea.fr.NacreRTE.services.NacreUtilService;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.syson.diagram.common.view.services.RevealCompartmentSwitch;
import org.eclipse.syson.diagram.common.view.services.ViewNodeService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.ViewUsage;
import org.eclipse.syson.sysml.helper.EMFUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NacreViewNodeService extends ViewNodeService {

    private final NacreUtilService nacreUtilService = new NacreUtilService();


    public NacreViewNodeService(IObjectSearchService objectSearchService) {
        super(objectSearchService);
    }

    @Override
    protected boolean needToRevealCompartment(Element element, boolean isGeneralView) {
        return new RevealCompartmentSwitch(true).doSwitch(element);
    }

    public List<Element> getExposedNacreElements(Element element, String nacreElementTypeName, EClass domainType, List<Object> ancestors, IEditingContext editingContext, DiagramContext diagramContext) {
        return this.getExposedNacreElements(element, null, nacreElementTypeName, domainType, ancestors, editingContext, diagramContext);
    }

    /**
     * {@link ViewNodeService#getExposedElements(Element, Element, EClass, List, IEditingContext, DiagramContext)}
     */
    public List<Element> getExposedNacreElements(Element element, Element parent, String nacreElementTypeName, EClass domainType, List<Object> ancestors, IEditingContext editingContext, DiagramContext diagramContext) {
        List<Element> elementsToExpose = new ArrayList<>();
        if (element instanceof ViewUsage viewUsage) {
            var exposedElements = new ArrayList<Element>();
            if (parent == null) {
                // we only want to display exposed elements that could be displayed as root nodes or nested tree nodes,
                // but not nodes that could be displayed inside other nodes
                exposedElements.addAll(this.getDirectExposedElements(viewUsage));
            } else {
                exposedElements.addAll(viewUsage.getExposedElement());
            }
            // keep only elements: with the correct Domain, with the correct parent, and LCA typed
            var filteredExposedElements = exposedElements.stream()
                    .filter(elt -> this.isTypeOf(elt, domainType)
                            && isAncestor(parent, elt))
                    .toList();
            elementsToExpose.addAll(filteredExposedElements);

            var viewDefKind = this.nacreUtilService.getNacreViewDefinitionKind(viewUsage, ancestors, editingContext);
            for (Element filteredExposedElement : filteredExposedElements) {
                boolean canBeDisplayed = new NacreViewFilterSwitch(viewDefKind, exposedElements, parent).doSwitch(filteredExposedElement);
                if (!canBeDisplayed) {
                    elementsToExpose.remove(filteredExposedElement);
                }
            }
        } else if (ancestors != null) {
            // Retrieve ViewUsage exposing the given element
            var viewUsageContainingElement = ancestors.stream()
                    .filter(ViewUsage.class::isInstance)
                    .map(ViewUsage.class::cast)
                    .findFirst();
            if (viewUsageContainingElement.isPresent()) {
                // expose all elements of this ViewUsage
                List<Element> exposedNacreElements = this.getExposedNacreElements(viewUsageContainingElement.get(), element, nacreElementTypeName, domainType, ancestors, editingContext,
                        diagramContext);
                elementsToExpose.addAll(exposedNacreElements);
            }
        }
        return elementsToExpose;
    }

    private static boolean isAncestor(Element parent, Element elt) {
        return parent == null || (!Objects.equals(parent, elt) && EMFUtils.isAncestor(parent, elt));
    }
}
