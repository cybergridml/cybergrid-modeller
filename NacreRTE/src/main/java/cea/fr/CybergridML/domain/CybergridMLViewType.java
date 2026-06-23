package cea.fr.CybergridML.domain;

import java.util.Objects;

public enum CybergridMLViewType {

    CRA_VIEW("Nacre::CRA"),
    CFA_VIEW("Nacre::CFA"),
    CFRA_VIEW("Nacre::CFRA");

    private final String nacreLibDefinitionName;

    CybergridMLViewType(String nacreLibDefinitionName) {
        this.nacreLibDefinitionName = Objects.requireNonNull(nacreLibDefinitionName);
    }

    public String getNacreLibDefinitionName() {
        return nacreLibDefinitionName;
    }
}

