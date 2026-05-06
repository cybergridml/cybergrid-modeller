
package cea.fr.NacreRTE.library;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.syson.sysml.*;

import java.util.Objects;


public enum NacreLibElementType {

    MAINGRID("NACREProfile::CFA::MainGrid", "MainGrid", "MainGrid", SysmlPackage.eINSTANCE.getPartUsage(), false),
    ZONE("NACREProfile::CFA::Zone", "Zone", "Zone", SysmlPackage.eINSTANCE.getPartUsage(), false),
    SUBSTATION("NACREProfile::CFA::Substation", "Substation", "Substation", SysmlPackage.eINSTANCE.getPartUsage(), false),
    CENTRALCONTROLLER("NACREProfile::CFA::CentralController", "CentralController", "CentralController", SysmlPackage.eINSTANCE.getPartUsage(), false),
    ZONALCONTROlLER("NACREProfile::CFA::ZonalController", "ZonalController", "ZonalController", SysmlPackage.eINSTANCE.getPartUsage(), false),
    //CONTROL("NACREProfile::CFA::Control", "Control", "Control", SysmlPackage.eINSTANCE.getAllocationUsage(), false),
    ;

    /**
     * Name of the LCA library definition that the element specializes or is typed by.
     */
    private final String nacreLibDefinitionName;
    /**
     * Name of the LCA element type as known by the editor's user.
     */
    private final String name;
    /**
     * Suffix used in the creation method's names.
     */
    private final String serviceSuffix;
    /**
     * SysML eclass of the LCA element
     */
    private final EClass eClass;
    /**
     * True if the element specializes the Lca Library Definition, false if it is typed by it.
     */
    private final boolean isSubclassifier;


    NacreLibElementType(String nacreLibDefinitionName, String name, String serviceSuffix, EClass eClass, boolean isSubclassifier) {
        this.nacreLibDefinitionName = Objects.requireNonNull(nacreLibDefinitionName);
        this.name = Objects.requireNonNull(name);
        this.serviceSuffix = Objects.requireNonNull(serviceSuffix);
        this.eClass = Objects.requireNonNull(eClass);
        this.isSubclassifier = isSubclassifier;
    }

    public static NacreLibElementType getNacreElementFromUsage(Feature feature) {
        return feature.getType().stream()
                .filter(Objects::nonNull)
                .map(Type::getQualifiedName)
                .map(qualifiedName -> getNacreElementByNacreLibDefinitionName(qualifiedName, false))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public static NacreLibElementType getNacreElementFromDefinition(Definition definition) {
        return definition.getOwnedSubclassification().stream()
                .map(Subclassification::getSuperclassifier)
                .map(Type::getQualifiedName)
                .map(qualifiedName -> getNacreElementByNacreLibDefinitionName(qualifiedName, true))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public static NacreLibElementType getNacreElementFromName(String name) {
        for (NacreLibElementType nacreLibElementType : NacreLibElementType.values())
            if (nacreLibElementType.getName().equals(name))
                return nacreLibElementType;
        return null;
    }


    private static NacreLibElementType getNacreElementByNacreLibDefinitionName(String name, boolean isSubclassifier) {
        for (NacreLibElementType nacreLibElementType : NacreLibElementType.values())
            if (nacreLibElementType.getNacreLibDefinitionName().equals(name) && nacreLibElementType.isSubclassifier == isSubclassifier)
                return nacreLibElementType;
        return null;
    }


    public String getNacreLibDefinitionName() {
        return nacreLibDefinitionName;
    }

    public String getName() {
        return name;
    }

    public String getServiceSuffix() {
        return serviceSuffix;
    }

    public EClass geteClass() {
        return eClass;
    }

    public boolean isSubclassifier() {
        return isSubclassifier;
    }
}
