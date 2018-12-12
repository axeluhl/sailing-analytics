package com.sap.sse.security.shared;

import java.io.Serializable;

import com.sap.sse.common.Named;
import com.sap.sse.common.WithID;
import com.sap.sse.security.shared.impl.WildcardPermissionEncoder;

public interface IdentifierStrategy {

    String getIdentifierAsString(Object... object);
    String convertNewIdentifer(Object... object);

    /**
     * Identifier strategy that is used for object types that implement the
     * interface {@link Named}.
     */
    static IdentifierStrategy NAMED = new IdentifierStrategy() {

        @Override
        public String getIdentifierAsString(Object... object) {
            assert object.length == 1;
            Named namedObject = (Named) object[0];
            return WildcardPermissionEncoder.encode(namedObject.getName());
        }

        @Override
        public String convertNewIdentifer(Object... object) {
            assert object.length == 1;
            String identifer = (String) object[0];
            return WildcardPermissionEncoder.encode(identifer);
        }

    };

    /**
     * Identifier strategy that can be used for object types that implement the
     * interface {@link WithID}.
     */
    static IdentifierStrategy ID = new IdentifierStrategy() {

        @Override
        public String getIdentifierAsString(Object... object) {
            assert object.length == 1;
            WithID objectWithId = (WithID) object[0];
            return objectWithId.getId().toString();
        }

        @Override
        public String convertNewIdentifer(Object... object) {
            assert object.length == 1;
            Serializable identifier = (Serializable) object[0];
            return identifier.toString();
        }

    };

    /**
     * Identifier strategy that can be used for object types are represented
     * by a {@link String} object.
     */
    static IdentifierStrategy STRING = new IdentifierStrategy() {

        @Override
        public String getIdentifierAsString(Object... object) {
            assert object.length == 1;
            return WildcardPermissionEncoder.encode((String) object[0]);
        }

        @Override
        public String convertNewIdentifer(Object... object) {
            assert object.length == 1;
            String identifier = (String) object[0];
            return WildcardPermissionEncoder.encode(identifier);
        }

    };

    /**
     * Identifier strategy that can be used for object types are represented
     * by a {@link String} object. This is used to specify the concrete case
     * of a server that is identified by a servename.
     */
    static IdentifierStrategy SERVERNAME = STRING;

    /**
     * Used to mark a permission that does not require a conceret object
     * permission. If used to build a permission, it will throw a
     * {@link UnsupportedOperationException}.
     */
    static IdentifierStrategy NO_OP = new IdentifierStrategy() {

        @Override
        public String getIdentifierAsString(Object... object) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String convertNewIdentifer(Object... object) {
            throw new UnsupportedOperationException();
        }

    };

}
