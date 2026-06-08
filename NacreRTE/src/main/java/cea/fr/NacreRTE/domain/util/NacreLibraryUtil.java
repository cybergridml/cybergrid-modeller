package cea.fr.NacreRTE.domain.util;

import cea.fr.NacreRTE.domain.NacreLibElementType;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.emf.services.api.IEMFEditingContext;
import org.eclipse.syson.sysml.*;
import org.eclipse.syson.sysml.Package;

import java.util.Arrays;
import java.util.Optional;

public class NacreLibraryUtil {


    public static final String NACRE_LIBRARY_SCHEME = "nacrelibrary";

    private NacreLibraryUtil() {
    }

    public static boolean typeHasNacreLibraryDefName(Type type, NacreLibElementType nacreElementType, boolean isSubClassifier) {
        if (nacreElementType == null) {
            return false;
        }
        return nacreElementType.getNacreLibDefinitionName().equals(type.getQualifiedName()) && nacreElementType.isSubclassifier() == isSubClassifier;
    }

    public static Resource getLcaLibraryResource(IEditingContext editingContext) {
        return Optional.of(editingContext)
                .filter(IEMFEditingContext.class::isInstance)
                .map(IEMFEditingContext.class::cast)
                .map(IEMFEditingContext::getDomain)
                .map(EditingDomain::getResourceSet)
                .map(ResourceSet::getResources)
                .stream()
                .flatMap(EList::stream)
                .filter(resource -> resource.getURI().toString().contains(NacreLibraryUtil.NACRE_LIBRARY_SCHEME))
                .findFirst()
                .orElse(null);
    }




}

