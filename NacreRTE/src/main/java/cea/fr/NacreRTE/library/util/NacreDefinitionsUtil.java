package cea.fr.NacreRTE.library.util;

import cea.fr.NacreRTE.library.NacreLibElementType;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.syson.sysml.Definition;
import org.eclipse.syson.sysml.SysmlFactory;

import java.util.Optional;
public class NacreDefinitionsUtil {

    private NacreDefinitionsUtil() {
    }

    public static Definition getMaingridDefinition(IEditingContext editingContext) {
        Resource nResource = NacreLibraryUtil.getLcaLibraryResource(editingContext);
        return getNacreDefinition(nResource, NacreLibElementType.MAINGRID, false)
                .orElse(SysmlFactory.eINSTANCE.createDefinition());
    }
    public static Definition getZoneDefinition(IEditingContext editingContext) {
        Resource nResource = NacreLibraryUtil.getLcaLibraryResource(editingContext);
        return getNacreDefinition(nResource, NacreLibElementType.ZONE, false)
                .orElse(SysmlFactory.eINSTANCE.createDefinition());
    }
    public static Definition getSubstationDefinition(IEditingContext editingContext) {
        Resource nResource = NacreLibraryUtil.getLcaLibraryResource(editingContext);
        return getNacreDefinition(nResource, NacreLibElementType.SUBSTATION, false)
                .orElse(SysmlFactory.eINSTANCE.createDefinition());
    }
    public static Definition getCentralControllerDefinition(IEditingContext editingContext) {
        Resource nResource = NacreLibraryUtil.getLcaLibraryResource(editingContext);
        return getNacreDefinition(nResource, NacreLibElementType.CENTRALCONTROLLER, false)
                .orElse(SysmlFactory.eINSTANCE.createDefinition());
    }
    public static Definition getZONALCONTROlLERDefinition(IEditingContext editingContext) {
        Resource nResource = NacreLibraryUtil.getLcaLibraryResource(editingContext);
        return getNacreDefinition(nResource, NacreLibElementType.ZONALCONTROlLER, false)
                .orElse(SysmlFactory.eINSTANCE.createDefinition());
    }




    public static Optional<Definition> getNacreDefinition(Resource resource, NacreLibElementType nacreLibElementType, boolean isSubclassifier) {
        if (resource == null || nacreLibElementType == null) {
            return Optional.empty();
        }

        TreeIterator<EObject> contentsIter = resource.getAllContents();
        while (contentsIter.hasNext()) {
            EObject eObject = contentsIter.next();
            if (eObject instanceof Definition definition && NacreLibraryUtil.typeHasNacreLibraryDefName(definition, nacreLibElementType, isSubclassifier)) {
                return Optional.of(definition);
            }
        }
        return Optional.empty();
    }

    public static Optional<Definition> getNacreDefinition(IEditingContext editingContext, NacreLibElementType nacreLibElementType, boolean isSubclassifier) {
        Resource nResource = NacreLibraryUtil.getLcaLibraryResource(editingContext);
        return getNacreDefinition(nResource, nacreLibElementType, isSubclassifier);
    }
}
