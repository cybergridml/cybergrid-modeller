package cea.fr.CybergridML.domain;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.syson.sysml.Definition;
import org.eclipse.syson.sysml.MetadataDefinition;
import org.eclipse.syson.sysml.SysmlFactory;

import java.util.Optional;

public class CybergridMLDefinitionsUtil {

    private CybergridMLDefinitionsUtil() {
    }

    public static Definition getDefinition(IEditingContext editingContext, CybergridMLLibElementType cybergridMLLibElementType) {
        Resource nResource = CybergridMLLibraryUtil.getCybergridMLLibraryResource(editingContext);
        return getCybergridMLDefinition(nResource, cybergridMLLibElementType, false)
                .orElse(SysmlFactory.eINSTANCE.createDefinition());
    }

    public static Optional<Definition> getCybergridMLDefinition(Resource resource, CybergridMLLibElementType cybergridMLLibElementType, boolean isSubclassifier) {
        if (resource == null || cybergridMLLibElementType == null) {
            return Optional.empty();
        }
        TreeIterator<EObject> contentsIter = resource.getAllContents();
        while (contentsIter.hasNext()) {
            EObject eObject = contentsIter.next();
            if (eObject instanceof Definition definition && CybergridMLLibraryUtil.typeHasCybergridMLLibraryDefName(definition, cybergridMLLibElementType, isSubclassifier)) {
                return Optional.of(definition);
            }
        }
        return Optional.empty();
    }

    public static Optional<MetadataDefinition> getHazardDefinition(IEditingContext editingContext) {
        Resource resource = CybergridMLLibraryUtil.getCybergridMLLibraryResource(editingContext);
        if (resource == null) {
            return Optional.empty();
        }
        TreeIterator<EObject> it = resource.getAllContents();
        while (it.hasNext()) {
            EObject obj = it.next();
            if (obj instanceof MetadataDefinition md && "CyberGridMLDatatypes::Hazard".equals(md.getQualifiedName())) {
                return Optional.of(md);
            }
        }
        return Optional.empty();
    }
}
