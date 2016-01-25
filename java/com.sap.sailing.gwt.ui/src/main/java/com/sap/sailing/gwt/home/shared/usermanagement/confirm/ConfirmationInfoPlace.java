package com.sap.sailing.gwt.home.shared.usermanagement.confirm;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.common.client.AbstractBasePlace;
import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;
import com.sap.sailing.gwt.home.shared.places.PlaceTokenPrefixes;

public class ConfirmationInfoPlace extends AbstractBasePlace implements HasMobileVersion {
    private final String name;
    private final Action action;

    public enum Action {
        ERROR, ACCOUNT_CREATED, RESET_REQUESTED;  
    };

    public ConfirmationInfoPlace(Action action, String name) {
        this.action = action;
        this.name = name;
    }

    public Action getAction() {
        return action;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "ConfirmationInfoPlace [name=" + name + ", action=" + action + "]";
    }

    @Prefix(PlaceTokenPrefixes.UserConfirmationInfo)
    public static class Tokenizer implements PlaceTokenizer<ConfirmationInfoPlace> {

        @Override
        public ConfirmationInfoPlace getPlace(String token) {
            Action action = null;
            try {
                action = Action.valueOf(token);
            } catch (Exception e) {
                action = Action.ERROR;
            }
            return new ConfirmationInfoPlace(action, "");
        }

        @Override
        public String getToken(ConfirmationInfoPlace place) {
            return place.getAction().name();
        }
    }
}
