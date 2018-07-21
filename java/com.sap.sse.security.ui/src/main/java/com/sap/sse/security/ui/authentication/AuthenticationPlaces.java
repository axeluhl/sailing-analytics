package com.sap.sse.security.ui.authentication;

import java.util.function.Supplier;

import com.sap.sse.security.ui.authentication.create.CreateAccountPlace;
import com.sap.sse.security.ui.authentication.signin.SignInPlace;

/**
 * Enumeration to specify a requested {@link AbstractAuthenticationPlace authentication places}.
 */
public enum AuthenticationPlaces {

    /** {@link AuthenticationPlaces} representation for {@link SignInPlace} */
    SIGN_IN(SignInPlace::new),
    /** {@link AuthenticationPlaces} representation for {@link CreateAccountPlace} */
    CREATE_ACCOUNT(CreateAccountPlace::new);

    private final Supplier<? extends AbstractAuthenticationPlace> placeSupplier;

    private AuthenticationPlaces(Supplier<? extends AbstractAuthenticationPlace> placeSupplier) {
        this.placeSupplier = placeSupplier;
    }

    /**
     * @return new instance of the {@link AbstractAuthenticationPlace} represented by this {@link AuthenticationPlaces}
     */
    public AbstractAuthenticationPlace getPlace() {
        return this.placeSupplier.get();
    }

}
