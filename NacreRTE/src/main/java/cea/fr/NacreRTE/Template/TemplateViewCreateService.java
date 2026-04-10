package cea.fr.NacreRTE.Template;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.SysmlPackage;

import java.util.List;

public class TemplateViewCreateService {

    /**
     * Returns {@code true} if the diagram can be created on the provided {@code element}.
     *
     * @param element the element to check
     * @return {@code true} if the diagram can be created on the provided {@code element}
     */
    public boolean canCreateDiagram(Element element) {
        List<EClass> acceptedRootTypes = List.of(SysmlPackage.eINSTANCE.getPackage());
        return acceptedRootTypes.stream().anyMatch(rootType -> rootType.isSuperTypeOf(element.eClass()));
    }
}