package cea.fr.CybergridML.domain;

import java.util.Objects;

public enum CybergridMLViewType {

    CRA_VIEW("Nacre::CRA"),
    CFA_VIEW("Nacre::CFA"),
    CFRA_VIEW("Nacre::CFRA");

    private final String CybergridMLLibDefinitionName;

    CybergridMLViewType(String CybergridMLLibDefinitionName) {
        this.CybergridMLLibDefinitionName = Objects.requireNonNull(CybergridMLLibDefinitionName);
    }

    public String getNacreLibDefinitionName() {
        return CybergridMLLibDefinitionName;
    }
}

