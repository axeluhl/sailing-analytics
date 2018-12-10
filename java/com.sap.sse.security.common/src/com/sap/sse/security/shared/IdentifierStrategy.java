package com.sap.sse.security.shared;

import com.sap.sse.common.Named;
import com.sap.sse.common.WithID;
import com.sap.sse.security.shared.impl.WildcardPermissionEncoder;

public interface IdentifierStrategy {

    <T> String getIdentifierAsString(T object);

    /**
     * Identifier strategy that is used for object types that implement the
     * interface {@link Named}.
     */
    static IdentifierStrategy NAMED = new IdentifierStrategy() {

        @Override
        public <T> String getIdentifierAsString(T object) {
            Named namedObject = (Named) object;
            return WildcardPermissionEncoder.encode(namedObject.getName());
        }

    };

    /**
     * Identifier strategy that can be used for object types that implement the
     * interface {@link WithID}.
     */
    static IdentifierStrategy ID = new IdentifierStrategy() {

        @Override
        public <T> String getIdentifierAsString(T object) {
            WithID objectWithId = (WithID) object;
            return objectWithId.getId().toString();
        }

    };

    /**
     * Identifier strategy that can be used for object types are represented
     * by a {@link String} object.
     */
    static IdentifierStrategy STRING = new IdentifierStrategy() {

        @Override
        public <T> String getIdentifierAsString(T object) {
            return WildcardPermissionEncoder.encode((String) object);
        }

    };

    /**
     * Identifier strategy that can be used for object types are represented
     * by a {@link String} object. This is used to specify the concrete case
     * of a server that is identified by a servename.
     */
    static IdentifierStrategy SERVERNAME = STRING;

    @Deprecated
    static IdentifierStrategy TO_SPECIFY = new IdentifierStrategy() {

        @Override
        public String getIdentifierAsString(Object object) {
            return object.toString();
        }

    };

    /**
     * Used to mark a permission that does not require a conceret object
     * permission. If used to build a permission, it will throw a
     * {@link UnsupportedOperationException}.
     */
    static IdentifierStrategy NO_OP = new IdentifierStrategy() {

        @Override
        public String getIdentifierAsString(Object object) {
            throw new UnsupportedOperationException();
        }

    };

}
