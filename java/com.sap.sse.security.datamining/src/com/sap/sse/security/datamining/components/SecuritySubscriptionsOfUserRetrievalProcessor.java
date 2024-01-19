package com.sap.sse.security.datamining.components;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;
import com.sap.sse.security.datamining.data.HasSubscriptionContext;
import com.sap.sse.security.datamining.data.HasUserContext;
import com.sap.sse.security.datamining.data.impl.SubscriptionWithContext;
import com.sap.sse.security.shared.subscription.Subscription;

public class SecuritySubscriptionsOfUserRetrievalProcessor extends AbstractRetrievalProcessor<HasUserContext, HasSubscriptionContext> {
    public SecuritySubscriptionsOfUserRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasSubscriptionContext, ?>> resultReceivers, int retrievalLevel,
            String retrievedDataTypeMessageKey) {
        super(HasUserContext.class, HasSubscriptionContext.class, executor, resultReceivers,
                retrievalLevel, retrievedDataTypeMessageKey);
    }

    @Override
    protected Iterable<HasSubscriptionContext> retrieveData(HasUserContext element) {
        final Set<HasSubscriptionContext> data = new HashSet<>();
        final Iterable<Subscription> subscriptions = element.getUser().getSubscriptions();
        if (subscriptions != null) {
            for (final Subscription subscription : subscriptions) {
                if (isAborted()) {
                    break;
                }
                data.add(new SubscriptionWithContext(element, subscription));
            }
        }
        return data;
    }
}