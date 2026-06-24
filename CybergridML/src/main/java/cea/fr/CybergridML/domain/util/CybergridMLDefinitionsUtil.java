package cea.fr.CybergridML.domain.util;

import cea.fr.CybergridML.domain.CybergridMLLibElementType;
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

    public static Definition getMaingridDefinition(IEditingContext editingContext) {
        Resource nResource = CybergridMLLibraryUtil.getLcaLibraryResource(editingContext);
        return getNacreDefinition(nResource, CybergridMLLibElementType.MAINGRID, false)
                .orElse(SysmlFactory.eINSTANCE.createDefinition());
    }

    public static Definition getZoneDefinition(IEditingContext editingContext) {
        Resource nResource = CybergridMLLibraryUtil.getLcaLibraryResource(editingContext);
        return getNacreDefinition(nResource, CybergridMLLibElementType.ZONE, false)
                .orElse(SysmlFactory.eINSTANCE.createDefinition());
    }

    public static Definition getSubstationDefinition(IEditingContext editingContext) {
        Resource nResource = CybergridMLLibraryUtil.getLcaLibraryResource(editingContext);
        return getNacreDefinition(nResource, CybergridMLLibElementType.SUBSTATION, false)
                .orElse(SysmlFactory.eINSTANCE.createDefinition());
    }
    public static Definition getBusDefinition(IEditingContext editingContext) {
        Resource nResource = CybergridMLLibraryUtil.getLcaLibraryResource(editingContext);
        return getNacreDefinition(nResource, CybergridMLLibElementType.BUS, false)
                .orElse(SysmlFactory.eINSTANCE.createDefinition());
    }

    public static Definition getCentralControllerDefinition(IEditingContext editingContext) {
        Resource nResource = CybergridMLLibraryUtil.getLcaLibraryResource(editingContext);
        return getNacreDefinition(nResource, CybergridMLLibElementType.CENTRALCONTROLLER, false)
                .orElse(SysmlFactory.eINSTANCE.createDefinition());
    }

    public static Definition getZonalControllerDefinition(IEditingContext editingContext) {
        Resource nResource = CybergridMLLibraryUtil.getLcaLibraryResource(editingContext);
        return getNacreDefinition(nResource, CybergridMLLibElementType.ZONALCONTROLLER, false)
                .orElse(SysmlFactory.eINSTANCE.createDefinition());
    }
    public static Definition getLocalControllerDefinition(IEditingContext editingContext) {
        Resource nResource = CybergridMLLibraryUtil.getLcaLibraryResource(editingContext);
        return getNacreDefinition(nResource, CybergridMLLibElementType.LOCALCONTROLLER, false)
                .orElse(SysmlFactory.eINSTANCE.createDefinition());
    }
    public static Definition getGeneratorDefinition(IEditingContext editingContext) {
        Resource nResource = CybergridMLLibraryUtil.getLcaLibraryResource(editingContext);
        return getNacreDefinition(nResource, CybergridMLLibElementType.GENERATOR, false)
                .orElse(SysmlFactory.eINSTANCE.createDefinition());
    }
    public static Definition getBatteryDefinition(IEditingContext editingContext) {
        Resource nResource = CybergridMLLibraryUtil.getLcaLibraryResource(editingContext);
        return getNacreDefinition(nResource, CybergridMLLibElementType.GENERATOR, false)
                .orElse(SysmlFactory.eINSTANCE.createDefinition());
    }


    public static Definition getCommunicationMediaDefinition(IEditingContext editingContext) {
        Resource nResource = CybergridMLLibraryUtil.getLcaLibraryResource(editingContext);
        return getNacreDefinition(nResource, CybergridMLLibElementType.COMMUNICATIONMEDIA, false)
                .orElse(SysmlFactory.eINSTANCE.createDefinition());
    }

    public static Definition getComputingResourceDefinition(IEditingContext editingContext) {
        Resource nResource = CybergridMLLibraryUtil.getLcaLibraryResource(editingContext);
        return getNacreDefinition(nResource, CybergridMLLibElementType.COMPUTINGRESOURCE, false)
                .orElse(SysmlFactory.eINSTANCE.createDefinition());
    }
    public static Definition getLoadDefinition(IEditingContext editingContext) {
        Resource nResource = CybergridMLLibraryUtil.getLcaLibraryResource(editingContext);
        return getNacreDefinition(nResource, CybergridMLLibElementType.LOAD, false)
                .orElse(SysmlFactory.eINSTANCE.createDefinition());
    }
    public static Definition getLineDefinition(IEditingContext editingContext) {
        Resource nResource = CybergridMLLibraryUtil.getLcaLibraryResource(editingContext);
        return getNacreDefinition(nResource, CybergridMLLibElementType.LINE, false)
                .orElse(SysmlFactory.eINSTANCE.createDefinition());
    }
    public static Definition getTerminalDefinition(IEditingContext editingContext) {
        Resource nResource = CybergridMLLibraryUtil.getLcaLibraryResource(editingContext);
        return getNacreDefinition(nResource, CybergridMLLibElementType.TERMINAL, false)
                .orElse(SysmlFactory.eINSTANCE.createDefinition());
    }

    public static Definition getSoftwareResourceDefinition(IEditingContext editingContext) {
        Resource nResource = CybergridMLLibraryUtil.getLcaLibraryResource(editingContext);
        return getNacreDefinition(nResource, CybergridMLLibElementType.SOFTWARERESOURCE, false)
                .orElse(SysmlFactory.eINSTANCE.createDefinition());
    }
    public static Definition getRedundancyClusterDefinition(IEditingContext editingContext) {
        Resource nResource = CybergridMLLibraryUtil.getLcaLibraryResource(editingContext);
        return getNacreDefinition(nResource, CybergridMLLibElementType.REDUNDANCYCLUSTER, false)
                .orElse(SysmlFactory.eINSTANCE.createDefinition());
    }

    public static Optional<Definition> getNacreDefinition(Resource resource, CybergridMLLibElementType nacreLibElementType, boolean isSubclassifier) {
        if (resource == null || nacreLibElementType == null) {
            return Optional.empty();
        }
        TreeIterator<EObject> contentsIter = resource.getAllContents();
        while (contentsIter.hasNext()) {
            EObject eObject = contentsIter.next();
            if (eObject instanceof Definition definition && CybergridMLLibraryUtil.typeHasNacreLibraryDefName(definition, nacreLibElementType, isSubclassifier)) {
                return Optional.of(definition);
            }
        }
        return Optional.empty();
    }

    public static Optional<MetadataDefinition> getHazardDefinition(IEditingContext editingContext) {
        Resource resource = CybergridMLLibraryUtil.getLcaLibraryResource(editingContext);
        if (resource == null) {
            return Optional.empty();
        }
        TreeIterator<EObject> it = resource.getAllContents();
        while (it.hasNext()) {
            EObject obj = it.next();
            if (obj instanceof MetadataDefinition md && "NACREDatatypes::Hazard".equals(md.getQualifiedName())) {
                return Optional.of(md);
            }
        }
        return Optional.empty();
    }
}
