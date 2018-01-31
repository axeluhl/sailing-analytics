package com.sap.sailing.gwt.settings.client.leaderboard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;

public abstract class LeaderboardPanelLifecycle<T extends LeaderboardSettings>
        implements ComponentLifecycle<T> {
    public static final String ID = "lb";

    protected final StringMessages stringMessages;

    protected Collection<DetailType> availableDetailTypes;
    
    public LeaderboardPanelLifecycle(StringMessages stringMessages, Collection<DetailType> availableDetailTypes) {
        this.stringMessages = stringMessages;
        this.availableDetailTypes = Collections.unmodifiableCollection(availableDetailTypes);
    }
    
    public List<DetailType> reduceToAvailableTypes(Collection<DetailType> toFilter) {
        //keeping this function pure
        ArrayList<DetailType> returnValue = new ArrayList<>(toFilter);
        returnValue.retainAll(availableDetailTypes);
        return returnValue;
    }
    
    @Override
    public String getLocalizedShortName() {
        return stringMessages.leaderboard();
    }

    @Override
    public String getComponentId() {
        return ID;
    }

    @Override
    public boolean hasSettings() {
        return true;
    }
}
