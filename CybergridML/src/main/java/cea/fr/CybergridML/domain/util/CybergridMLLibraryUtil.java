package cea.fr.CybergridML.domain.util;

import cea.fr.CybergridML.domain.CybergridMLLibElementType;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.emf.services.api.IEMFEditingContext;
import org.eclipse.syson.sysml.*;

import java.util.Optional;

public class CybergridMLLibraryUtil {


    public static final String CYBERGRIDML_LIBRARY_SCHEME = "cybergridmllibrary";

    private CybergridMLLibraryUtil() {
    }

    public static boolean typeHasCybergridMLLibraryDefName(Type type, CybergridMLLibElementType cybergridMLElementType, boolean isSubClassifier) {
        if (cybergridMLElementType == null) {
            return false;
        }
        return cybergridMLElementType.getCybergridMLLibDefinitionName().equals(type.getQualifiedName()) && cybergridMLElementType.isSubclassifier() == isSubClassifier;
    }

    public static Resource getCybergridMLLibraryResource(IEditingContext editingContext) {
        return Optional.of(editingContext)
                .filter(IEMFEditingContext.class::isInstance)
                .map(IEMFEditingContext.class::cast)
                .map(IEMFEditingContext::getDomain)
                .map(EditingDomain::getResourceSet)
                .map(ResourceSet::getResources)
                .stream()
                .flatMap(EList::stream)
                .filter(resource -> resource.getURI().toString().contains(CybergridMLLibraryUtil.CYBERGRIDML_LIBRARY_SCHEME))
                .findFirst()
                .orElse(null);
    }




}

