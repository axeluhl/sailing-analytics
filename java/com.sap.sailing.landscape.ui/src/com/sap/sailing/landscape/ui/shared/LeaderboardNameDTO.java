package com.sap.sailing.landscape.ui.shared;

import com.sap.sse.common.Named;
import com.sap.sse.security.shared.dto.NamedDTO;

/**
 * This class is supposed to represent leader board names. It's just a wrapper for ensuring that we can use the
 * {@link Named} functionality in the UI.
 * 
 * @author I569653
 *
 */
public class LeaderboardNameDTO extends NamedDTO {
    private static final long serialVersionUID = -173460185784328741L;

    @SuppressWarnings({ "deprecation", "unused" })
    private LeaderboardNameDTO() {} // for GWT serialisation only

    public LeaderboardNameDTO(String name) {
        super(name);
    }
}