package cea.fr.CybergridML.view.services;

public enum CybergridMLViewDefinitionKind {
    CRA_VIEW,
    CFA_VIEW,
    CFRA_VIEW;

    public static CybergridMLViewDefinitionKind getKind(String viewDefinitionName) {
        if ("Nacre::CRA".equals(viewDefinitionName)) {
            return CRA_VIEW;
        }
        if ("Nacre::CFA".equals(viewDefinitionName)) {
            return CFA_VIEW;
        }
        if ("Nacre::CFRA".equals(viewDefinitionName)) {
            return CFRA_VIEW;
        }
        return null;
    }
}
