package com.sap.sse.security.test;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.Test;

import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.RoleDefinition;
import com.sap.sse.security.shared.RoleDefinitionImpl;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;

/**
 * See also bug 4927 (https://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=4927). Role identifiers are probably not
 * constructed correctly, using the wrong {@link TypeRelativeObjectIdentifier} constructor.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class RoleIdentifierTest {
    private RoleDefinition roleDefinition;
    
    @Test
    public void testRoleDefinitionIdentifier() {
        roleDefinition = new RoleDefinitionImpl(UUID.randomUUID(), "Test Role") {
            private static final long serialVersionUID = 6504908715414284115L;
            @Override
            public String getIdAsString() {
                return "abc'::\\\\://|\\?/\\///\\";
            }
        };
        final QualifiedObjectIdentifier idFromPermission =
                roleDefinition.getIdentifier().getPermission(DefaultActions.READ).getQualifiedObjectIdentifiers().iterator().next();
        final QualifiedObjectIdentifier idFromRoleDefinition = roleDefinition.getIdentifier();
        assertEquals(idFromRoleDefinition, idFromPermission);
    }
}
