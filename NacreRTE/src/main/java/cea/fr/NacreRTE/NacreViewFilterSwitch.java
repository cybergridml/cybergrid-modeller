package cea.fr.NacreRTE;

import cea.fr.NacreRTE.services.NacreViewDefinitionKind;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Namespace;
import org.eclipse.syson.sysml.helper.EMFUtils;
import org.eclipse.syson.sysml.util.SysmlSwitch;

import java.util.List;
import java.util.Objects;

public class NacreViewFilterSwitch extends SysmlSwitch<Boolean> {

    private final NacreViewDefinitionKind kind;
    private final List<Element> exposedElements;
    private final Element parentElement;

    public NacreViewFilterSwitch(NacreViewDefinitionKind kind, List<Element> exposedElements, Element parentElement) {
        this.kind = kind;
        this.exposedElements = Objects.requireNonNull(exposedElements);
        this.parentElement = parentElement;
    }
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

    @Override
    public Boolean defaultCase(EObject object) {
        return Boolean.TRUE;
    }

}
