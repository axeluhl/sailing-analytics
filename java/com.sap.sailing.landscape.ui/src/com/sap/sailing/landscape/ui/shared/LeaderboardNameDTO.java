package com.sap.sailing.landscape.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.common.Named;

/**
 * This class is supposed to represent leader board names. It's just a wrapper for ensuring that we can use the
 * {@link Named} functionality in the UI.
 * 
 * @author I569653
 *
 */

public class LeaderboardDTO implements IsSerializable, Named {
    private static final long serialVersionUID = -173460185784328741L;
    String name;

    LeaderboardDTO() {
    };

    public LeaderboardDTO(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

}
