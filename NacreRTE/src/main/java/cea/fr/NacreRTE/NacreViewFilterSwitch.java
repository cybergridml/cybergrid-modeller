package cea.fr.NacreRTE;

import cea.fr.NacreRTE.services.api.NacreViewDefinitionKind;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.syson.diagram.common.view.services.ViewFilterSwitch;
import org.eclipse.syson.sysml.Definition;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Namespace;
import org.eclipse.syson.sysml.Usage;
import org.eclipse.syson.sysml.helper.EMFUtils;
import org.eclipse.syson.sysml.util.SysmlSwitch;

import java.util.List;
import java.util.Objects;

public class NacreViewFilterSwitch extends SysmlSwitch<Boolean> {
    /**
     * The kind of ViewDefinition the ViewUsage associated to the object on which this Switch is applied is typed by.
     */
    private final NacreViewDefinitionKind kind;

    /**
     * The list of exposed Elements associated the ViewUsage associated to the object on which this Switch is applied.
     */
    private final List<Element> exposedElements;

    /**
     * The parent Element of the object on which this Switch is applied. It can be <code>null</code>.
     */
    private final Element parentElement;

    public NacreViewFilterSwitch(NacreViewDefinitionKind kind, List<Element> exposedElements, Element parentElement) {
        this.kind = kind;
        this.exposedElements = Objects.requireNonNull(exposedElements);
        this.parentElement = parentElement;
    }

    @Override
    public Boolean defaultCase(EObject object) {
        boolean displayAsTreeNode = false;

        return displayAsTreeNode;
    }

    /**
     * Copied from {@link ViewFilterSwitch}
     * Check if the given object is an indirect nested node of the parentElement of this class.
     *
     * @param object the given object
     * @return <code>true</code> if the given object is an indirect node of the parentElement of this class
     */
    private boolean isIndirectNestedNode(EObject object) {
        boolean isIndirectNestedNode;
        if (this.parentElement instanceof Namespace elt && !elt.getOwnedMember().contains(object)) {
            isIndirectNestedNode = true;
        } else {
            isIndirectNestedNode = this.exposedElements.stream()
                    .anyMatch(exposedElement -> isIndirectNestedNodeOfExposedElement(object, exposedElement));
        }
        return isIndirectNestedNode;
    }

    private boolean isIndirectNestedNodeOfExposedElement(EObject object, Element exposedElement) {
        return !Objects.equals(exposedElement, object)
                && this.parentElement == null && EMFUtils.isAncestor(exposedElement, object);
    }


}
