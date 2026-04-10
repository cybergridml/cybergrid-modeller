package cea.fr.NacreRTE.services.api;


public enum NacreViewDefinitionKind {
    CRA_VIEW,
    CFA_VIEW,
    CFRA_VIEW;


    public static NacreViewDefinitionKind getKind(String viewDefinitionName) {
        NacreViewDefinitionKind kind = null;
        if ("Nacre::CRA".equals(viewDefinitionName)) {
            kind = CRA_VIEW;
        }
        if ("Nacre::CFRA".equals(viewDefinitionName)) {
            kind = CFA_VIEW;
        }
        if ("Nacre::CFRA".equals(viewDefinitionName)) {
            kind = CFRA_VIEW;
        }
        return kind;
    }

    public static boolean isCRAView(NacreViewDefinitionKind kind) {
        return kind == CRA_VIEW;
    }


    public static boolean isCFAView(NacreViewDefinitionKind kind) {
        return kind == CFA_VIEW;
    }
    public static boolean isCFRAView(NacreViewDefinitionKind kind) {
        return kind == CFRA_VIEW;
    }

}
