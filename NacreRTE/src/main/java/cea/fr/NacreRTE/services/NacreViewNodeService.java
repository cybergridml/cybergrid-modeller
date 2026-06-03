package cea.fr.NacreRTE.services;


import cea.fr.NacreRTE.NacreViewFilterSwitch;
import org.springframework.stereotype.Service;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.syson.diagram.common.view.services.RevealCompartmentSwitch;
import org.eclipse.syson.diagram.common.view.services.ViewNodeService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Feature;
import org.eclipse.syson.sysml.Namespace;
import org.eclipse.syson.sysml.ViewUsage;
import org.eclipse.syson.sysml.helper.EMFUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
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
                exposedElements.addAll(this.getDirectExposedElements(viewUsage));
            } else {
                exposedElements.addAll(viewUsage.getExposedElement());
            }
            var filteredExposedElements = exposedElements.stream()
                    .filter(elt -> this.isTypeOf(elt, domainType)
                            && this.isTypedWithNacreDefinition(elt, nacreElementTypeName)
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
        } else if (element instanceof Namespace namespace && (ancestors == null || ancestors.isEmpty())) {
            namespace.getOwnedMember().stream()
                    .filter(elt -> this.isTypeOf(elt, domainType))
                    .filter(elt -> this.isTypedWithNacreDefinition(elt, nacreElementTypeName))
                    .forEach(elementsToExpose::add);
        } else if (ancestors != null) {
            var viewUsageContainingElement = ancestors.stream()
                    .filter(ViewUsage.class::isInstance)
                    .map(ViewUsage.class::cast)
                    .findFirst();
            if (viewUsageContainingElement.isPresent()) {
                List<Element> exposedNacreElements = this.getExposedNacreElements(viewUsageContainingElement.get(), element, nacreElementTypeName, domainType, ancestors, editingContext,
                        diagramContext);
                elementsToExpose.addAll(exposedNacreElements);
            }
        }
        return elementsToExpose;
    }

    private boolean isTypedWithNacreDefinition(Element elt, String nacreElementTypeName) {
        if (nacreElementTypeName == null || nacreElementTypeName.isEmpty()) {
            return true;
        }
        if (!(elt instanceof Feature feature)) {
            return false;
        }
        return feature.getType().stream()
                .filter(Objects::nonNull)
                .anyMatch(t -> nacreElementTypeName.equals(t.getQualifiedName()));
    }

    private static boolean isAncestor(Element parent, Element elt) {
        return parent == null || (!Objects.equals(parent, elt) && EMFUtils.isAncestor(parent, elt));
    }
}
