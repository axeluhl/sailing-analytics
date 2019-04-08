package com.sap.sailing.gwt.home.shared.places.user.confirmation;

import com.google.gwt.http.client.URL;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.sap.sailing.gwt.common.client.AbstractBasePlace;
import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;
import com.sap.sailing.gwt.home.shared.places.PlaceTokenPrefixes;

public class ConfirmationPlace extends AbstractBasePlace implements HasMobileVersion {
    private final String name;
    private final String validationSecret;
    private final Action action;

    public enum Action {
        ERROR, RESET_EXECUTED, MAIL_VERIFIED;  
    };

    public ConfirmationPlace(Action action) {
        this(action, "");
    }

    public ConfirmationPlace(Action action, String name) {
        this(action, name, "");
    }

    public ConfirmationPlace(Action action, String name, String validationSecret) {
        this.action = action;
        this.name = name;
        this.validationSecret = validationSecret;
    }

    public Action getAction() {
        return action;
    }

    public String getName() {
        return name;
    }

    public String getValidationSecret() {
        return validationSecret;
    }

    @Override
    public String toString() {
        return "ConfirmationPlace [name=" + name + ", validationSecret=" + validationSecret + "]";
    }

    @Prefix(PlaceTokenPrefixes.UserConfirmation)
    public static class Tokenizer implements PlaceTokenizer<ConfirmationPlace> {
        private final RegExp keyValRegex = RegExp.compile("(.*)=(.*)");

        @Override
        public ConfirmationPlace getPlace(String token) {
            String[] split = token.split("\\?");
            String actionToken = split[0];
            String contentToken = split[1];
            Action action = null;
            String name = "";
            String validationSecret = "";
            try {
                action = Action.valueOf(actionToken);
                for (String keyValToken : contentToken.split("&")) {
                    MatchResult matched = keyValRegex.exec(keyValToken);
                    if (matched != null) {
                        String key = matched.getGroup(1);
                        String value = matched.getGroup(2);
                        if (key != null && !key.isEmpty() && value != null && !value.isEmpty()) {
                            final String decoded = URL.decodeQueryString(value);
                            if ("u".equals(key)) {
                                name = decoded;
                            } else if ("v".equals(key)) {
                                validationSecret = decoded;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                action = Action.ERROR;
            }
            return new ConfirmationPlace(action, name, validationSecret);
        }

        @Override
        public String getToken(ConfirmationPlace place) {
            final StringBuilder result = new StringBuilder(place.getAction().name());
            if (place.getName() != null || place.getValidationSecret() != null) {
                result.append('?');
                if (place.getName() != null) {
                    result.append("u=");
                    result.append(URL.encodeQueryString(place.getName()));
                }
                if (place.getValidationSecret() != null) {
                    if (place.getName() != null) {
                        result.append('&');
                    }
                    result.append("v=");
                    result.append(URL.encodeQueryString(place.getValidationSecret()));
                }
            }
            return result.toString();
        }
    }
}
