package cea.fr.NacreRTE.domain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.syson.sysml.*;

import java.util.Objects;


public enum NacreLibElementType {

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

    private final String nacreLibDefinitionName;
    private final String name;
    private final String serviceSuffix;
    private final EClass eClass;
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

