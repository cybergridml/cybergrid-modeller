package cea.fr.CybergridML.domain;

import java.util.Objects;

public enum CybergridMLViewType {

    CRA_VIEW("CyberGridML::CRA"),
    CFA_VIEW("CyberGridML::CFA"),
    CFRA_VIEW("CyberGridML::CFRA");

    private final String CybergridMLLibDefinitionName;

    CybergridMLViewType(String CybergridMLLibDefinitionName) {
        this.CybergridMLLibDefinitionName = Objects.requireNonNull(CybergridMLLibDefinitionName);
    }

    public String getCybergridMLLibDefinitionName() {
        return CybergridMLLibDefinitionName;
    }
}

