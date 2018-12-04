package com.sap.sailing.domain.common.security.test;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;

import org.junit.Test;

import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sse.common.Named;
import com.sap.sse.common.WithID;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.IdentifierStrategy;
import com.sap.sse.security.shared.impl.WildcardPermissionEncoder;

public class SecuredDomainTypeTest {

    @Test
    public void testNamedIdentifierStrategy()  {
        HasPermissions t = new SecuredDomainType<>("TEST", IdentifierStrategy.NAMED);
        @SuppressWarnings("serial") Named testObject = new Named() {
            @Override
            public String getName() {
                return " test_1 :a ";
            }
        };
        
        assertEquals(WildcardPermissionEncoder.encode(" test_1 :a "), t.identifierStrategy().getIdentifierAsString(testObject));
    }
    
    @Test
    public void testWithIdIdentifierStrategy()  {
        HasPermissions t = new SecuredDomainType<>("TEST", IdentifierStrategy.ID);
        WithID testObject = new WithID() {
            @Override
            public Serializable getId() {
                return 12345;
            }
        };
        
        assertEquals("12345", t.identifierStrategy().getIdentifierAsString(testObject));
    }
}
