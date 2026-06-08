package cea.fr.NacreRTE.domain;

import java.util.Objects;

public enum NacreViewType {

    CRA_VIEW("Nacre::CRA"),
    CFA_VIEW("Nacre::CFA"),
    CFRA_VIEW("Nacre::CFRA");

    private final String nacreLibDefinitionName;

    NacreViewType(String nacreLibDefinitionName) {
        this.nacreLibDefinitionName = Objects.requireNonNull(nacreLibDefinitionName);
    }

    public String getNacreLibDefinitionName() {
        return nacreLibDefinitionName;
    }
}

