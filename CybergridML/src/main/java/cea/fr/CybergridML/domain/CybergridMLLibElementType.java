package cea.fr.CybergridML.domain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.syson.sysml.*;

import java.util.Objects;


public enum CybergridMLLibElementType {

    MAINGRID("NACREProfile::CFA::MainGrid", "MainGrid", "MainGrid", SysmlPackage.eINSTANCE.getPartUsage(), false),
    ZONE("NACREProfile::CFA::Zone", "Zone", "Zone", SysmlPackage.eINSTANCE.getPartUsage(), false),
    SUBSTATION("NACREProfile::CFA::Substation", "Substation", "Substation", SysmlPackage.eINSTANCE.getPartUsage(), false),
    BUS("NACREProfile::CFA::Bus", "Bus", "Bus", SysmlPackage.eINSTANCE.getPartUsage(), false),
    CENTRALCONTROLLER("NACREProfile::CFA::CentralController", "CentralController", "CentralController", SysmlPackage.eINSTANCE.getPartUsage(), false),
    ZONALCONTROLLER("NACREProfile::CFA::ZonalController", "ZonalController", "ZonalController", SysmlPackage.eINSTANCE.getPartUsage(), false),
    LOCALCONTROLLER("NACREProfile::CFA::LocalController", "LocalController", "LocalController", SysmlPackage.eINSTANCE.getPartUsage(), false),
    COMMUNICATIONMEDIA("NACREProfile::CRA::CommunicationMedia", "CommunicationMedia", "CommunicationMedia", SysmlPackage.eINSTANCE.getPartUsage(), false),
    COMPUTINGRESOURCE("NACREProfile::CRA::ComputingResource", "ComputingResource", "ComputingResource", SysmlPackage.eINSTANCE.getPartUsage(), false),
    REDUNDANCYCLUSTER("NACREProfile::CRA::RedundancyCluster", "RedundancyCluster", "RedundancyCluster", SysmlPackage.eINSTANCE.getPartUsage(), false),
    GENERATOR("NACREProfile::CRA::Generator", "Generator", "Generator", SysmlPackage.eINSTANCE.getPartUsage(), false),
    SOFTWARERESOURCE("NACREProfile::CRA::SoftwareResource", "SoftwareResource", "SoftwareResource", SysmlPackage.eINSTANCE.getPartUsage(), false),
    BATTERY("NACREProfile::CRA::Battery", "Battery", "Battery", SysmlPackage.eINSTANCE.getPartUsage(), false),
    LOAD("NACREProfile::CRA::Load", "Load", "Load", SysmlPackage.eINSTANCE.getPartUsage(), false),
    LINE("NACREProfile::CRA::Line", "Line", "Line", SysmlPackage.eINSTANCE.getPartUsage(), false),
    TERMINAL("NACREProfile::CRA::Terminal", "Terminal", "Terminal", SysmlPackage.eINSTANCE.getPartUsage(), false),

    ;

    private final String cybergridMLLibDefinitionName;
    private final String name;
    private final String serviceSuffix;
    private final EClass eClass;
    private final boolean isSubclassifier;

    CybergridMLLibElementType(String cybergridMLLibDefinitionName, String name, String serviceSuffix, EClass eClass, boolean isSubclassifier) {
        this.cybergridMLLibDefinitionName = Objects.requireNonNull(cybergridMLLibDefinitionName);
        this.name = Objects.requireNonNull(name);
        this.serviceSuffix = Objects.requireNonNull(serviceSuffix);
        this.eClass = Objects.requireNonNull(eClass);
        this.isSubclassifier = isSubclassifier;
    }

    public static CybergridMLLibElementType getCybergridMLElementFromUsage(Feature feature) {
        return feature.getType().stream()
                .filter(Objects::nonNull)
                .map(Type::getQualifiedName)
                .map(qualifiedName -> getCybergridMLElementByCybergridMLLibDefinitionName(qualifiedName, false))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public static CybergridMLLibElementType getCybergridMLElementFromName(String name) {
        for (CybergridMLLibElementType cybergridMLLibElementType : CybergridMLLibElementType.values())
            if (cybergridMLLibElementType.getName().equals(name))
                return cybergridMLLibElementType;
        return null;
    }

    private static CybergridMLLibElementType getCybergridMLElementByCybergridMLLibDefinitionName(String name, boolean isSubclassifier) {
        for (CybergridMLLibElementType cybergridMLLibElementType : CybergridMLLibElementType.values())
            if (cybergridMLLibElementType.getCybergridMLLibDefinitionName().equals(name) && cybergridMLLibElementType.isSubclassifier == isSubclassifier)
                return cybergridMLLibElementType;
        return null;
    }

    public String getCybergridMLLibDefinitionName() {
        return cybergridMLLibDefinitionName;
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

