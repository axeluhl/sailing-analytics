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
    private static final String BASIC_PLAN_ID = "basic";
    private static final String ADVANCED_PLAN_ID = "cbdemo_advanced-USD-monthly";
    private static final String AAAS_PLAN_ID = "aaas";
    private static final String BASIC_PLAN_NAME_KEY = "basic_plan_name";
    private static final String ADVANCED_PLAN_NAME_KEY = "advanced_plan_name";
    private static final String AAAS_PLAN_NAME_KEY = "aaas_plan_name";
    private static final String BASIC_PLAN_DESC_KEY = "basic_plan_desc";
    private static final String ADVANCED_PLAN_DESC_KEY = "advanced_plan_desc";
    private static final String AAAS_PLAN_DESC_KEY = "aaas_plan_desc";
    private static final Map<String, SubscriptionPlan> plansById = new HashMap<>();

    private SailingSubscriptionPlan(String id, StringMessagesKey nameMessageKey, StringMessagesKey descMessageKey,
            BigDecimal price, SubscriptionPlanRole... roles) {
        super(id, nameMessageKey, descMessageKey, price, roles);
        plansById.put(id, this);
    }

    public static final SubscriptionPlan Basic = new SailingSubscriptionPlan(BASIC_PLAN_ID,
            new StringMessagesKey(BASIC_PLAN_NAME_KEY), new StringMessagesKey(BASIC_PLAN_DESC_KEY), new BigDecimal(100),
            new SubscriptionPlanRole(StreamletViewerRole.getRoleId(),
                    new StringMessagesKey(StreamletViewerRole.getMessageKey())));
    
    public static final SubscriptionPlan Advanced = new SailingSubscriptionPlan(ADVANCED_PLAN_ID,
            new StringMessagesKey(ADVANCED_PLAN_NAME_KEY), new StringMessagesKey(ADVANCED_PLAN_DESC_KEY), new BigDecimal(100),
            new SubscriptionPlanRole(StreamletViewerRole.getRoleId(),
                    new StringMessagesKey(StreamletViewerRole.getMessageKey())));
    
    public static final SubscriptionPlan AAAS = new SailingSubscriptionPlan(AAAS_PLAN_ID,
            new StringMessagesKey(AAAS_PLAN_NAME_KEY), new StringMessagesKey(AAAS_PLAN_DESC_KEY), new BigDecimal(100),
            new SubscriptionPlanRole(StreamletViewerRole.getRoleId(),
                    new StringMessagesKey(StreamletViewerRole.getMessageKey())));

    public static Map<Serializable, SubscriptionPlan> getAllInstances() {
        return Collections.unmodifiableMap(plansById);
    }
}
