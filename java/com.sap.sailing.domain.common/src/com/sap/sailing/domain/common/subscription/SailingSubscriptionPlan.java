package com.sap.sailing.domain.common.subscription;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.sap.sse.security.shared.StringMessagesKey;
import com.sap.sse.security.shared.subscription.SubscriptionPlan;
import com.sap.sse.security.shared.subscription.SubscriptionPlanRole;

/**
 * Payment subscription plans. A subscription plan has a name, a {@link String}-based ID, and a set of
 * {@link SubscriptionPlanRole roles} it grants to a subscribing user. These roles can specify how they are to be
 * qualified when assigned, regarding user and group qualifications. See
 * {@link SubscriptionPlanRole.GroupQualificationMode} and {@link SubscriptionPlanRole.UserQualificationMode} for
 * more details.
 * 
 */
public class SailingSubscriptionPlan extends SubscriptionPlan {
    private static final long serialVersionUID = 2563619370274543312L;
    private static final String PRO_PLAN_NAME_KEY = "pro_plan_name";
    private static final String PRO_PLAN_DESC_KEY = "pro_plan_desc";
    private static final Map<String, SubscriptionPlan> plansById = new HashMap<>();

    private SailingSubscriptionPlan(String id, StringMessagesKey nameMessageKey, StringMessagesKey descMessageKey,
            BigDecimal price, SubscriptionPlanRole... roles) {
        super(id, nameMessageKey, descMessageKey, price, roles);
        plansById.put(id, this);
    }

    public static final SubscriptionPlan PRO = new SailingSubscriptionPlan("pro",
            new StringMessagesKey(PRO_PLAN_NAME_KEY), new StringMessagesKey(PRO_PLAN_DESC_KEY), new BigDecimal(100),
            new SubscriptionPlanRole(StreamletViewerRole.getRoleId(),
                    new StringMessagesKey(StreamletViewerRole.getMessageKey())));

    public static Map<Serializable, SubscriptionPlan> getAllInstances() {
        return Collections.unmodifiableMap(plansById);
    }
}
