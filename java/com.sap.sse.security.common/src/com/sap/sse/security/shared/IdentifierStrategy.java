package com.sap.sse.security.shared;

import com.sap.sse.common.Named;
import com.sap.sse.common.WithID;
import com.sap.sse.security.shared.impl.WildcardPermissionEncoder;

public interface IdentifierStrategy<T> {

    String getIdentifierAsString(T object);

    String getNewIdentifer(Object... params);

    static IdentifierStrategy<Named> NAMED = new IdentifierStrategy<Named>() {

        @Override
        public String getIdentifierAsString(Named object) {
            return WildcardPermissionEncoder.encode(object.getName());
        }

        @Override
        public String getNewIdentifer(Object... params) {
            if (params.length != 1) {
                throw new IllegalArgumentException("NAMED requires exactly one parameter for the name of the object.");
            }
            return WildcardPermissionEncoder.encode(params[1].toString());
        }
    };

    static IdentifierStrategy<WithID> ID = new IdentifierStrategy<WithID>() {

        @Override
        public String getIdentifierAsString(WithID object) {
            return object.getId().toString();
        }

        @Override
        public String getNewIdentifer(Object... params) {
            if (params.length != 0) {
                throw new IllegalArgumentException("ID requires no parameter.");
            }
            return null;
        }
        
    };
    
    static IdentifierStrategy<Object> TO_SPECIFY = new IdentifierStrategy<Object>() {

        @Override
        public String getIdentifierAsString(Object object) {
            return object.toString();
        }

        @Override
        public String getNewIdentifer(Object... params) {
            if (params.length != 0) {
                throw new IllegalArgumentException("ID requires no parameter.");
            }
            return null;
        }
        
    };

    static IdentifierStrategy<Object> NO_OP = new IdentifierStrategy<Object>() {

        @Override
        public String getIdentifierAsString(Object object) {
            throw new  UnsupportedOperationException();
        }

        @Override
        public String getNewIdentifer(Object... params) {
            throw new  UnsupportedOperationException();
        }
        
    };

}
