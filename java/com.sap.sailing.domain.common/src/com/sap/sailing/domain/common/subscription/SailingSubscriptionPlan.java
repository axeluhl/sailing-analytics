package com.sap.sailing.domain.common.subscription;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sap.sse.security.shared.subscription.AllDataMiningRole;
import com.sap.sse.security.shared.subscription.ArchiveDataMiningRole;
import com.sap.sse.security.shared.subscription.SubscriptionPlan;
import com.sap.sse.security.shared.subscription.SubscriptionPlanRole;
import com.sap.sse.security.shared.subscription.SubscriptionPrice;
import com.sap.sse.security.shared.subscription.SubscriptionPrice.PaymentInterval;

/**
 * Payment subscription plans. A subscription plan has a name, a {@link String}-based ID, and a set of
 * {@link SubscriptionPlanRole roles} it grants to a subscribing user. These roles can specify how they are to be
 * qualified when assigned, regarding user and group qualifications. See
 * {@link SubscriptionPlanRole.GroupQualificationMode} and {@link SubscriptionPlanRole.UserQualificationMode} for more
 * details.
 * 
 */
public class SailingSubscriptionPlan extends SubscriptionPlan {
    private static final String USD_CURRENCY_CODE = "usd";
    private static final long serialVersionUID = 2563619370274543312L;
    private static final String WEEKLY_PREMIUM_PLAN_ID = "weekly_premium";
    private static final String YEARLY_PREMIUM_PLAN_ID = "yearly_premium";
    private static final String WEEKLY_DATA_MINING_ARCHIVE_PLAN_ID = "weekly_data_mining_archive";
    private static final String YEARLY_DATA_MINING_ARCHIVE_PLAN_ID = "yearly_data_mining_archive";
    private static final String TRIAL_DATA_MINING_ALL_PLAN_ID = "trial_data_mining_all";
    private static final String WEEKLY_DATA_MINING_ALL_PLAN_ID = "weekly_data_mining_all";
    private static final String YEARLY_DATA_MINING_ALL_PLAN_ID = "yearly_data_mining_all";
    private static final String WEEKLY_PREMIUM_PLAN_ITEMPRICE_ID = "weekly_premium_usd_weekly";
    private static final String YEARLY_PREMIUM_PLAN_ITEMPRICE_ID = "yearly_premium_usd_yearly";
    private static final String WEEKLY_DATA_MINING_ARCHIVE_PLAN_ITEMPRICE_ID = "weekly_data_mining_archive_usd_weekly";
    private static final String YEARLY_DATA_MINING_ARCHIVE_PLAN_ITEMPRICE_ID = "yearly_data_mining_archive_usd_yearly";
    private static final String TRIAL_DATA_MINING_ALL_PLAN_ITEMPRICE_ID = "trial_data_mining_all_usd_daily";
    private static final String WEEKLY_DATA_MINING_ALL_PLAN_ITEMPRICE_ID = "weekly_data_mining_all_usd_weekly";
    private static final String YEARLY_DATA_MINING_ALL_PLAN_ITEMPRICE_ID = "yearly_data_mining_all_usd_yearly";
    private static final Map<String, SubscriptionPlan> plansById = new HashMap<>();

    private SailingSubscriptionPlan(String id, Set<SubscriptionPrice> prices, Set<PlanCategory> excludesPlanIds,
            Boolean isOneTimePlan, PlanGroup group, SubscriptionPlanRole... roles) {
        super(id, prices, excludesPlanIds, isOneTimePlan, group, roles);
        plansById.put(id, this);
    }

    public static final SubscriptionPlan PREMIUM_WEEKLY = new SailingSubscriptionPlan(WEEKLY_PREMIUM_PLAN_ID,
            Stream.of(new SubscriptionPrice(WEEKLY_PREMIUM_PLAN_ITEMPRICE_ID, new BigDecimal(8.99), USD_CURRENCY_CODE,
                    PaymentInterval.WEEK, true)).collect(Collectors.toSet()),
            Stream.of(PlanCategory.PREMIUM).collect(Collectors.toSet()), false,
            PlanGroup.PREMIUM,
            new SubscriptionPlanRole(PremiumRole.getRoleId()));
    public static final SubscriptionPlan PREMIUM_YEARLY = new SailingSubscriptionPlan(YEARLY_PREMIUM_PLAN_ID,
            Stream.of(new SubscriptionPrice(YEARLY_PREMIUM_PLAN_ITEMPRICE_ID, new BigDecimal(49.99), USD_CURRENCY_CODE,
                    PaymentInterval.YEAR, false)).collect(Collectors.toSet()),
            Stream.of(PlanCategory.PREMIUM).collect(Collectors.toSet()), false,
            PlanGroup.PREMIUM,
            new SubscriptionPlanRole(PremiumRole.getRoleId()));

    public static final SubscriptionPlan DATA_MINING_ARCHIVE_WEEKLY = new SailingSubscriptionPlan(
            WEEKLY_DATA_MINING_ARCHIVE_PLAN_ID,
            Stream.of(new SubscriptionPrice(WEEKLY_DATA_MINING_ARCHIVE_PLAN_ITEMPRICE_ID, new BigDecimal(89.99),
                    USD_CURRENCY_CODE, PaymentInterval.WEEK, true)).collect(Collectors.toSet()),
            Stream.of(PlanCategory.DATA_MINING, PlanCategory.PREMIUM).collect(Collectors.toSet()), false,
            PlanGroup.DATA_MINING_ARCHIVE,
            new SubscriptionPlanRole(PremiumRole.getRoleId()),
            new SubscriptionPlanRole(ArchiveDataMiningRole.getRoleId()));
    public static final SubscriptionPlan DATA_MINING_ARCHIVE_YEARLY = new SailingSubscriptionPlan(
            YEARLY_DATA_MINING_ARCHIVE_PLAN_ID,
            Stream.of(new SubscriptionPrice(YEARLY_DATA_MINING_ARCHIVE_PLAN_ITEMPRICE_ID, new BigDecimal(499.99),
                    USD_CURRENCY_CODE, PaymentInterval.YEAR, false)).collect(Collectors.toSet()),
            Stream.of(PlanCategory.DATA_MINING, PlanCategory.PREMIUM).collect(Collectors.toSet()), false,
            PlanGroup.DATA_MINING_ARCHIVE,
            new SubscriptionPlanRole(PremiumRole.getRoleId()),
            new SubscriptionPlanRole(ArchiveDataMiningRole.getRoleId()));

    public static final SubscriptionPlan DATA_MINING_ALL_WEEKLY = new SailingSubscriptionPlan(
            WEEKLY_DATA_MINING_ALL_PLAN_ID,
            Stream.of(new SubscriptionPrice(WEEKLY_DATA_MINING_ALL_PLAN_ITEMPRICE_ID, new BigDecimal(189.99),
                    USD_CURRENCY_CODE, PaymentInterval.WEEK, true)).collect(Collectors.toSet()),
            Stream.of(PlanCategory.DATA_MINING, PlanCategory.PREMIUM).collect(Collectors.toSet()), false,
            PlanGroup.DATA_MINING_ALL,
            new SubscriptionPlanRole(PremiumRole.getRoleId()), new SubscriptionPlanRole(AllDataMiningRole.getRoleId()));
    public static final SubscriptionPlan DATA_MINING_ALL_YEARLY = new SailingSubscriptionPlan(
            YEARLY_DATA_MINING_ALL_PLAN_ID,
            Stream.of(new SubscriptionPrice(YEARLY_DATA_MINING_ALL_PLAN_ITEMPRICE_ID, new BigDecimal(999.99),
                    USD_CURRENCY_CODE, PaymentInterval.YEAR, false)).collect(Collectors.toSet()),
            Stream.of(PlanCategory.DATA_MINING, PlanCategory.PREMIUM).collect(Collectors.toSet()), false,
            PlanGroup.DATA_MINING_ALL,
            new SubscriptionPlanRole(PremiumRole.getRoleId()), new SubscriptionPlanRole(AllDataMiningRole.getRoleId()));
    
    public static final SubscriptionPlan DATA_MINING_ALL_TRIAL = new SailingSubscriptionPlan(
            TRIAL_DATA_MINING_ALL_PLAN_ID,
            Stream.of(new SubscriptionPrice(TRIAL_DATA_MINING_ALL_PLAN_ITEMPRICE_ID, new BigDecimal(0),
                    USD_CURRENCY_CODE, PaymentInterval.DAY, false)).collect(Collectors.toSet()),
            Stream.of(PlanCategory.DATA_MINING, PlanCategory.PREMIUM).collect(Collectors.toSet()), true,
            PlanGroup.TRIAL,
            new SubscriptionPlanRole(PremiumRole.getRoleId()), new SubscriptionPlanRole(AllDataMiningRole.getRoleId()));

    public static Map<Serializable, SubscriptionPlan> getAllInstances() {
        return Collections.unmodifiableMap(plansById);
    }

}
