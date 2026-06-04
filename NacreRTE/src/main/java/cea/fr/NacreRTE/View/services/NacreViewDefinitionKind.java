package cea.fr.NacreRTE.view.services;

public enum NacreViewDefinitionKind {
    CRA_VIEW,
    CFA_VIEW,
    CFRA_VIEW;

    public static NacreViewDefinitionKind getKind(String viewDefinitionName) {
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
