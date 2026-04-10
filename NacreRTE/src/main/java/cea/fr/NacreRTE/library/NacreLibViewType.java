package cea.fr.NacreRTE.library;

import java.util.Objects;

public enum NacreLibViewType {

    CFA_VIEW("LCA::CFA", "CFA View"),
    CFRA_VIEW("LCA::CFRA", "CFRA View"),
    CRA_VIEW("LCA::CRA", "CRA View");


    /**
     * Name of the LCA library definition that the element specializes or is typed by.
     */
    private final String nacreLibDefinitionName;
    /**
     * Name of the LCA element type as known by the editor's user.
     */
    private final String name;


    NacreLibViewType(String nacreLibDefinitionName, String name) {
        this.nacreLibDefinitionName = Objects.requireNonNull(nacreLibDefinitionName);
        this.name = Objects.requireNonNull(name);
    }

    public String getLcaLibDefinitionName() {
        return nacreLibDefinitionName;
    }

    public String getName() {
        return name;
    }

}
