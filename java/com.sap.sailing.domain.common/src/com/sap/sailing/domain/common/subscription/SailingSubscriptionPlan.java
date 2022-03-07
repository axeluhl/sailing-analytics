package com.sap.sailing.domain.common.subscription;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sap.sse.security.shared.subscription.SubscriptionPlan;
import com.sap.sse.security.shared.subscription.SubscriptionPlanRole;
import com.sap.sse.security.shared.subscription.SubscriptionPrice;
import com.sap.sse.security.shared.subscription.SubscriptionPrice.PaymentInterval;

/**
 * Payment subscription plans. A subscription plan has a name, a {@link String}-based ID, and a set of
 * {@link SubscriptionPlanRole roles} it grants to a subscribing user. These roles can specify how they are to be
 * qualified when assigned, regarding user and group qualifications. See
 * {@link SubscriptionPlanRole.GroupQualificationMode} and {@link SubscriptionPlanRole.UserQualificationMode} for
 * more details.
 * 
 */
public class SailingSubscriptionPlan extends SubscriptionPlan {
    private static final String USD_CURRENCY_CODE = "usd";
    private static final BigDecimal YEARLY_PLAN_PRICE = new BigDecimal(49.99);
    private static final BigDecimal WEEKLY_PLAN_PRICE = new BigDecimal(4.99);
    private static final BigDecimal TRIAL_PLAN_PRICE = new BigDecimal(0);
    private static final long serialVersionUID = 2563619370274543312L;
    private static final String YEARLY_PLAN_ID = "yearly_premium";
    private static final String WEEKLY_PLAN_ID = "weekly_premium";
    private static final String TRIAL_PLAN_ID = "Trial";
    private static final String TRIAL_PLAN_ITEMPRICE_ID = "Trial-USD-Daily";
    private static final String WEEKLY_PLAN_ITEMPRICE_ID = "weekly_premium_usd_weekly";
    private static final String YEARLY_PLAN_ITEMPRICE_ID = "yearly_premium_usd_yearly";
    private static final Map<String, SubscriptionPlan> plansById = new HashMap<>();

    private SailingSubscriptionPlan(String id, Set<SubscriptionPrice> prices, SubscriptionPlanRole... roles) {
        super(id, prices, roles);
        plansById.put(id, this);
    }

    public static final SubscriptionPlan YEARLY = new SailingSubscriptionPlan(YEARLY_PLAN_ID,
            Stream.of(new SubscriptionPrice(YEARLY_PLAN_ITEMPRICE_ID, YEARLY_PLAN_PRICE, USD_CURRENCY_CODE,
                    PaymentInterval.YEAR)).collect(Collectors.toSet()),
            new SubscriptionPlanRole(PremiumRole.getRoleId()));

    public static final SubscriptionPlan WEEKLY = new SailingSubscriptionPlan(WEEKLY_PLAN_ID,
            Stream.of(new SubscriptionPrice(WEEKLY_PLAN_ITEMPRICE_ID, WEEKLY_PLAN_PRICE, USD_CURRENCY_CODE,
                    PaymentInterval.WEEK)).collect(Collectors.toSet()),
            new SubscriptionPlanRole(PremiumRole.getRoleId()));
    public static final SubscriptionPlan TRIAL = new SailingSubscriptionPlan(TRIAL_PLAN_ID,
            Stream.of(new SubscriptionPrice(TRIAL_PLAN_ITEMPRICE_ID, TRIAL_PLAN_PRICE, USD_CURRENCY_CODE,
                    PaymentInterval.DAY)).collect(Collectors.toSet()),
            new SubscriptionPlanRole(PremiumRole.getRoleId()));

    public static Map<Serializable, SubscriptionPlan> getAllInstances() {
        return Collections.unmodifiableMap(plansById);
    }
}
