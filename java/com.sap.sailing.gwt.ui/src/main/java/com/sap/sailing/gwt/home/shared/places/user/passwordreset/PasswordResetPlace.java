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
    private final String email;
    private final String resetSecret;

    public PasswordResetPlace() {
        this("", "", "");
    }

    public PasswordResetPlace(String name, String email, String validationSecret) {
        this.name = name;
        this.email = email;
        this.resetSecret = validationSecret;
    }

    public String getName() {
        return name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getResetSecret() {
        return resetSecret;
    }

    @Override
    public String toString() {
        return "PasswordResetPlace [name=" + name + ", email=" + email + ", resetSecret=" + resetSecret + "]";
    }

    @Prefix(PlaceTokenPrefixes.UserPasswordReset)
    public static class Tokenizer implements PlaceTokenizer<PasswordResetPlace> {
        private final RegExp keyValRegex = RegExp.compile("(.*)=(.*)");

        @Override
        public PasswordResetPlace getPlace(String token) {
            String contentToken;
            String name = "", email = "", resetSecret = "";
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
                            } else if ("e".equals(key)) {
                                email = decoded;
                            } else if ("s".equals(key)) {
                                resetSecret = decoded;
                            }
                        }
                    }
                }
            }
            return new PasswordResetPlace(name, email, resetSecret);
        }

        @Override
        public String getToken(PasswordResetPlace place) {
            return "";
        }
    }
}
