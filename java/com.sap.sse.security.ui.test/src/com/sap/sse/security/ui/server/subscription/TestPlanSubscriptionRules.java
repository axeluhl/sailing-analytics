package com.sap.sse.security.ui.server.subscription;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.sap.sailing.domain.common.subscription.SailingSubscriptionPlan;
import com.sap.sse.common.TimePoint;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.subscription.Subscription;
import com.sap.sse.security.shared.subscription.SubscriptionPlan;
import com.sap.sse.security.shared.subscription.chargebee.ChargebeeSubscription;
import com.sap.sse.security.ui.server.subscription.chargebee.ChargebeeSubscriptionWriteServiceImpl;
import com.sap.sse.security.ui.shared.subscription.SubscriptionPlanDTO;

public class TestPlanSubscriptionRules {
    @Test
    public void testThatDataMiningImpliesPemiumByCategory() {
        final SubscriptionPlan dataMiningArchiveYearly = SailingSubscriptionPlan.DATA_MINING_ARCHIVE_YEARLY;
        final SubscriptionPlan premiumYearly = SailingSubscriptionPlan.PREMIUM_YEARLY;
        final User user = mock(User.class);
        final Subscription premiumSubscription = new ChargebeeSubscription("subscriptionId", premiumYearly.getId(), "customerId",
                TimePoint.now(), TimePoint.now(), /* subscriptionStatus */ "active", /* paymentStatus */ "paid", "transactionType", "transactionStatus", "invoiceId",
                "invoiceStatus", /* reoccuringPaymentValue */ 49, /* currencyCode */ "USD", TimePoint.now(), TimePoint.now(),
                TimePoint.now(), TimePoint.now(), TimePoint.now(), /* cancelledAt */ null, TimePoint.now(), /* manualUpdatedAt */ null);
        when(user.getSubscriptionByPlan(premiumYearly.getId())).thenReturn(premiumSubscription);
        final SecurityService securityService = mock(SecurityService.class);
        when(securityService.getAllSubscriptionPlans()).thenReturn(SailingSubscriptionPlan.getAllInstances());
        final ChargebeeSubscriptionWriteServiceImpl service = new ChargebeeSubscriptionWriteServiceImpl() {
            private static final long serialVersionUID = 1L;

            @Override
            protected User getCurrentUser() {
                return user;
            }
            
            @Override
            protected SecurityService getSecurityService() {
                return securityService;
            }
        };
        final SubscriptionPlanDTO premiumYearlyDTO = service.convertToDto(premiumYearly);
        final SubscriptionPlanDTO dataMiningArchiveYearlyDTO = service.convertToDto(dataMiningArchiveYearly);
        assertTrue(premiumYearlyDTO.isUserSubscribedToPlan());
        assertTrue(premiumYearlyDTO.isUserSubscribedToAllPlanCategories());
        assertFalse(dataMiningArchiveYearlyDTO.isUserSubscribedToPlan());
        assertFalse(dataMiningArchiveYearlyDTO.isUserSubscribedToAllPlanCategories());
    }
}
