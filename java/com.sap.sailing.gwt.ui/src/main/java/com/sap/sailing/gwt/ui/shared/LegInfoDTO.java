package com.sap.sailing.gwt.ui.shared;

import com.sap.sailing.domain.common.LegType;
import com.sap.sse.security.shared.NamedDTO;

public class LegInfoDTO extends NamedDTO {
    private static final long serialVersionUID = -1259587647778615708L;

    public int legNumber;

    public LegType legType;

    public double legBearingInDegrees;

    public LegInfoDTO() {}

    public LegInfoDTO(int legNumber) {
        this.legNumber = legNumber;
    }

    @Override
    public String toString() {
        return "LegInfoDTO [name=" + getName() + ", legNumber=" + legNumber
                + ", legType=" + legType + ", legBearingInDegrees="
                + legBearingInDegrees + "]";
    }
}
