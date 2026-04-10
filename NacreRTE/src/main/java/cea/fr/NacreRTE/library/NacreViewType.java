package cea.fr.NacreRTE.library;

import java.util.Objects;

public enum NacreViewType {

    CRA_VIEW("Nacre::CRA", "CRA View"),
    CFA_VIEW("Nacre::CFA", "CFA View"),
    CFRA_VIEW("Nacre::CFRA", "CFRA View");


    /**
     * Name of the LCA library definition that the element specializes or is typed by.
     */
    private final String NacreLibDefinitionName;
    /**
     * Name of the LCA element type as known by the editor's user.
     */
    private final String name;


    NacreViewType(String NacreLibDefinitionName, String name) {
        this.NacreLibDefinitionName = Objects.requireNonNull(NacreLibDefinitionName);
        this.name = Objects.requireNonNull(name);
    }

    public String getNacreLibDefinitionName() {
        return NacreLibDefinitionName;
    }

    public String getName() {

        return name;
    }

}
