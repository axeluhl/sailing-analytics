package com.sap.sailing.domain.common.security.test;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.UUID;

import org.junit.Test;

import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sse.common.Named;
import com.sap.sse.common.WithID;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.IdentifierStrategy;
import com.sap.sse.security.shared.WildcardPermission;  
import com.sap.sse.security.shared.impl.WildcardPermissionEncoder;

public class SecuredDomainTypeTest {
    
    final static HasPermissions TYPE_NAMED = new SecuredDomainType("TEST_NAMED", IdentifierStrategy.NAMED);
    final static HasPermissions TYPE_ID = new SecuredDomainType("TEST_ID", IdentifierStrategy.ID);
    final static HasPermissions TYPE_STRING = new SecuredDomainType("TEST_STRING", IdentifierStrategy.STRING);
    
    
    final static WildcardPermissionEncoder wildcardPermissionEncoder = new WildcardPermissionEncoder();
    final static String NAMED_NAME = " Sail & :More 2018 ";
    final static String NAMED_NAME_ENCODED = wildcardPermissionEncoder.encodeStringList(NAMED_NAME);
    final static UUID WITHID_ID = UUID.randomUUID();
    

    @Test
    public void testNamedIdentifierStrategy()  {
        Named testObject = createNewNamed(NAMED_NAME);
        assertEquals(NAMED_NAME_ENCODED, TYPE_NAMED.identifierStrategy().getIdentifierAsString(testObject));
    }
    
    @Test
    public void testWithIdIdentifierStrategy()  {
        WithID testObject = createNewWithId(12345);
        assertEquals("12345", TYPE_ID.identifierStrategy().getIdentifierAsString(testObject));
    }
    
    @Test
    public void testStringIdentifierStrategy()  {
        assertEquals(NAMED_NAME_ENCODED, TYPE_STRING.identifierStrategy().getIdentifierAsString(NAMED_NAME));
    }

    @Test
    public void testNamedHAsPermission() {
        assertEquals(new WildcardPermission("TEST_NAMED:READ"), TYPE_NAMED.getPermission(HasPermissions.DefaultActions.READ));
        WildcardPermission p1 = TYPE_NAMED.getPermissionForObject(HasPermissions.DefaultActions.READ, createNewNamed(NAMED_NAME));
        assertEquals(new WildcardPermission("TEST_NAMED:READ:" + NAMED_NAME_ENCODED), p1);
        
        String p2 = TYPE_NAMED.getStringPermissionForObject(HasPermissions.DefaultActions.READ, createNewNamed(NAMED_NAME));
        assertEquals("TEST_NAMED:READ:" + NAMED_NAME_ENCODED, p2);
    }

    @Test
    public void testWithIdermission() {
        assertEquals(new WildcardPermission("TEST_ID:READ"), TYPE_ID.getPermission(HasPermissions.DefaultActions.READ));
        WildcardPermission p1 = TYPE_ID.getPermissionForObject(HasPermissions.DefaultActions.READ, createNewWithId(WITHID_ID));
        assertEquals(new WildcardPermission("TEST_ID:READ:" + WITHID_ID.toString()), p1);
        
        String p2 = TYPE_ID.getStringPermissionForObject(HasPermissions.DefaultActions.READ, createNewWithId(WITHID_ID));
        assertEquals("TEST_ID:READ:" + WITHID_ID.toString(), p2);
    }
    
    @Test
    public void testStringNamedPermission() {
        assertEquals(new WildcardPermission("TEST_STRING:READ"), TYPE_STRING.getPermission(HasPermissions.DefaultActions.READ));
        String s = " //:\\!§$%&/()=?*+~#'-.,;' ";
        WildcardPermission p = TYPE_STRING.getPermissionForObject(HasPermissions.DefaultActions.READ, s);
        assertEquals(new WildcardPermission("TEST_STRING:READ:" + wildcardPermissionEncoder.encodeStringList(s)), p);
    }

    private Named createNewNamed(String name) {
        return new Named() {
            private static final long serialVersionUID = 1L;

            @Override
            public String getName() {
                return name;
            }
        };
    }
    
    private WithID createNewWithId(Serializable id) {
        return new WithID() {
            @Override
            public Serializable getId() {
                return id;
            }
        };
    }
}
