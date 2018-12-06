package com.sap.sse.security.shared;

import com.sap.sse.common.Named;
import com.sap.sse.common.WithID;
import com.sap.sse.security.shared.impl.WildcardPermissionEncoder;

public interface IdentifierStrategy {

    <T> String getIdentifierAsString(T object);

    static IdentifierStrategy NAMED = new IdentifierStrategy() {

        @Override
        public <T> String getIdentifierAsString(T object) {
            Named namedObject = (Named) object;
            return WildcardPermissionEncoder.encode(namedObject.getName());
        }

    };

    static IdentifierStrategy ID = new IdentifierStrategy() {

        @Override
        public <T> String getIdentifierAsString(T object) {
            WithID objectWithId = (WithID) object;
            return objectWithId.getId().toString();
        }

    };

    static IdentifierStrategy STRING = new IdentifierStrategy() {

        @Override
        public <T> String getIdentifierAsString(T object) {
            return WildcardPermissionEncoder.encode((String) object);
        }

    };

    static IdentifierStrategy TO_SPECIFY = new IdentifierStrategy() {

        @Override
        public String getIdentifierAsString(Object object) {
            return object.toString();
        }

    };

    static IdentifierStrategy NO_OP = new IdentifierStrategy() {

        @Override
        public String getIdentifierAsString(Object object) {
            throw new UnsupportedOperationException();
        }

    };

}
