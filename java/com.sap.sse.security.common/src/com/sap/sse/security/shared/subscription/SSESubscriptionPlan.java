package com.sap.sse.security.shared.subscription;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.sse.security.shared.PredefinedRoles;

/**
 * Payment subscription plans. A subscription plan has a name, a {@link String}-based ID, and a set of
 * {@link SubscriptionPlanRole roles} it grants to a subscribing user. These roles can specify how they are to be
 * qualified when assigned, regarding user and group qualifications. See
 * {@link SubscriptionPlanRole.GroupQualificationMode} and {@link SubscriptionPlanRole.UserQualificationMode} for
 * more details.
 * 
 * @author Tu Tran
 */
public class SSESubscriptionPlan extends SubscriptionPlan {
    private static final long serialVersionUID = 9061666338780737555L;
    private static final Map<String, SSESubscriptionPlan> plansById = new HashMap<String, SSESubscriptionPlan>();
    
    public static final SSESubscriptionPlan STARTER = new SSESubscriptionPlan("starter", "Starter", new SubscriptionPlanRole[] {
            new SubscriptionPlanRole(PredefinedRoles.spectator.getId(),
                    SubscriptionPlanRole.GroupQualificationMode.DEFAULT_QUALIFIED_USER_TENANT,
                    SubscriptionPlanRole.UserQualificationMode.SUBSCRIBING_USER),
            new SubscriptionPlanRole(PredefinedRoles.mediaeditor.getId(),
                    SubscriptionPlanRole.GroupQualificationMode.DEFAULT_QUALIFIED_USER_TENANT,
                    SubscriptionPlanRole.UserQualificationMode.SUBSCRIBING_USER) },
            convertPermissionsIterable(PredefinedRoles.spectator.getPermissions(), PredefinedRoles.mediaeditor.getPermissions())
            );
    public static final SSESubscriptionPlan PREMIUM = new SSESubscriptionPlan("premium", "Premium", new SubscriptionPlanRole[] {
            new SubscriptionPlanRole(PredefinedRoles.spectator.getId(),
                    SubscriptionPlanRole.GroupQualificationMode.DEFAULT_QUALIFIED_USER_TENANT,
                    SubscriptionPlanRole.UserQualificationMode.SUBSCRIBING_USER),
            new SubscriptionPlanRole(PredefinedRoles.moderator.getId(),
                    SubscriptionPlanRole.GroupQualificationMode.SUBSCRIBING_USER_DEFAULT_TENANT,
                    SubscriptionPlanRole.UserQualificationMode.NONE) 
            }, 
            convertPermissionsIterable(PredefinedRoles.spectator.getPermissions(), PredefinedRoles.moderator.getPermissions())
            );

    private SSESubscriptionPlan(String id, String name, SubscriptionPlanRole[] roles, List<String> features) {
        super(id, name, roles, features);
        plansById.put(id, this);
    }
    
    public static Map<Serializable, SubscriptionPlan> getAllInstances(){
        return Collections.unmodifiableMap(plansById);
    }

}
