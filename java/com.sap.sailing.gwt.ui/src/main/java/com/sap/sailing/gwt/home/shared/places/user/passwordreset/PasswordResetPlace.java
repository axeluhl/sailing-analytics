package com.sap.sailing.gwt.home.shared.places.user.passwordreset;

import com.google.gwt.http.client.URL;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.sap.sailing.gwt.common.client.AbstractBasePlace;
import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;
import com.sap.sailing.gwt.home.shared.places.PlaceTokenPrefixes;

public class PasswordResetPlace extends AbstractBasePlace implements HasMobileVersion {
    private final String name;
    private final String validationSecret;

    public PasswordResetPlace() {
        this.name = "";
        this.validationSecret = "";
    }

    public PasswordResetPlace(String name, String validationSecret) {
        this.name = name;
        this.validationSecret = validationSecret;
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
    public static class Tokenizer implements PlaceTokenizer<PasswordResetPlace> {
        private final RegExp keyValRegex = RegExp.compile("(.*)=(.*)");

        @Override
        public PasswordResetPlace getPlace(String token) {
            String contentToken;
            String name = "";
            String validationSecret = "";
            if (token.startsWith("?")) {
                contentToken = token.substring(1);
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
                return new PasswordResetPlace(name, validationSecret);
            }
            return new PasswordResetPlace();
        }

        @Override
        public String getToken(PasswordResetPlace place) {
            return "";
        }
    }
}
