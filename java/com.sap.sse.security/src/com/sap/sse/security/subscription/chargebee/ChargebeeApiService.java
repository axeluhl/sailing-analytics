package com.sap.sse.security.subscription.chargebee;

import static com.chargebee.models.Subscription.cancel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.chargebee.APIException;
import com.chargebee.Environment;
import com.chargebee.ListResult;
import com.chargebee.Result;
import com.chargebee.ListResult.Entry;
import com.chargebee.filters.enums.SortOrder;
import com.chargebee.internal.ListRequest;
import com.chargebee.models.Invoice;
import com.chargebee.models.Invoice.InvoiceListRequest;
import com.chargebee.models.Subscription.SubscriptionListRequest;
import com.chargebee.models.Transaction;
import com.chargebee.models.Transaction.TransactionListRequest;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.subscription.Subscription;
import com.sap.sse.security.shared.subscription.chargebee.ChargebeeSubscription;
import com.sap.sse.security.subscription.SubscriptionApiService;
import com.sap.sse.security.subscription.SubscriptionCancelResult;

public class ChargebeeApiService implements SubscriptionApiService {
    private static final Logger logger = Logger.getLogger(ChargebeeApiService.class.getName());

    private static ChargebeeApiService instance;
    private static boolean inited;

    // Chargebee has API rate limits
    // Threshold value for test site: ~750 API calls in 5 minutes.
    // Threshold value for live site: ~150 API calls per site per minute.
    // So to prevent the limit would be reached, a request has a frame of ~400ms, and a next request should be made
    // after 400ms from previous request
    private static final long TIME_FOR_API_REQUEST_MS = 400;

    public static ChargebeeApiService getInstance() {
        if (instance == null) {
            initialize();
            instance = new ChargebeeApiService();
        }
        return instance;
    }

    public static void initialize() {
        if (!inited) {
            Environment.configure(ChargebeeConfiguration.getInstance().getSite(),
                    ChargebeeConfiguration.getInstance().getApiKey());
        }
    }

    private ChargebeeApiService() {
    }

    @Override
    public Iterable<Subscription> getUserSubscriptions(User user) throws Exception {
        logger.info(() -> "Fetching subscription list for user: " + user.getName());
        List<Subscription> subscriptions = new ArrayList<Subscription>();
        String offset = null;
        do {
            try {
                Pair<Iterable<SubscriptionItem>, String> result = fetchSubscriptions(user, offset);
                Iterable<SubscriptionItem> subscriptionItems = result.getA();
                if (subscriptionItems != null) {
                    for (SubscriptionItem item : subscriptionItems) {
                        subscriptions.add(item.toSubscription());
                    }
                }
                offset = result.getB();
            } catch (APIException e) {
                logger.log(Level.SEVERE, "Chargebee API request error: " + e.getMessage(), e);
                throw e;
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to fetch subscriptions: " + e.getMessage(), e);
                throw e;
            }
        } while (offset != null && !offset.isEmpty());
        logger.info(() -> "Finish fetching user subscription list");
        return subscriptions;
    }

    @Override
    public SubscriptionCancelResult cancelSubscription(String subscriptionId) throws Exception {
        SubscriptionCancelResult result;
        try {
            Subscription subscription = fetchSubscription(subscriptionId);
            if (subscription != null) {
                if (subscription.getSubscriptionStatus().equals(ChargebeeSubscription.SUBSCRIPTION_STATUS_CANCELLED)) {
                    result = new SubscriptionCancelResult(/* success */ true, subscription);
                } else {
                    result = requestCancel(subscriptionId);
                }
            } else {
                result = new SubscriptionCancelResult(/* success */false, /* subscription */null, /* deleted */true);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in cancel subscription ", e);
            result = new SubscriptionCancelResult(/* success */false, /* subscription */null);
        }

        return result;
    }

    /**
     * Fetch and return subscription from provider API
     */
    private Subscription fetchSubscription(String subscriptionId) throws Exception {
        final Subscription result;
        SubscriptionListRequest request = com.chargebee.models.Subscription.list().limit(1).id().is(subscriptionId)
                .includeDeleted(false);
        RequestListResult apiResponse = requestApiList(request);
        if (apiResponse.isRateLimitReached()) {
            result = fetchSubscription(subscriptionId);
        } else {
            if (apiResponse.hasResult()) {
                ListResult.Entry entry = apiResponse.getResult().get(0);
                com.chargebee.models.Subscription subscription = entry.subscription();
                result = (new SubscriptionItem(subscription, null, null)).toSubscription();
            } else {
                result = null;
            }
        }
        return result;
    }

    /**
     * Fetch user subscriptions with current offset. Return subscription list result, and next offset for continue
     * fetching
     */
    private Pair<Iterable<SubscriptionItem>, String> fetchSubscriptions(User user, String offset) throws Exception {
        final Pair<Iterable<SubscriptionItem>, String> result;
        SubscriptionListRequest request = com.chargebee.models.Subscription.list().limit(100).customerId()
                .is(user.getName()).includeDeleted(false).sortByCreatedAt(SortOrder.DESC);
        if (offset != null && !offset.isEmpty()) {
            request.offset(offset);
        }
        RequestListResult apiResponse = requestApiList(request);
        if (apiResponse.isRateLimitReached()) {
            result = fetchSubscriptions(user, offset);
        } else {
            final List<SubscriptionItem> subscriptions;
            if (apiResponse.hasResult()) {
                subscriptions = new ArrayList<ChargebeeApiService.SubscriptionItem>();
                for (ListResult.Entry entry : apiResponse.getResult()) {
                    com.chargebee.models.Subscription subscription = entry.subscription();
                    if (subscription != null) {
                        Invoice invoice = null;
                        Transaction transaction = null;
                        if (!subscription.deleted()) {
                            invoice = fetchInvoice(user, subscription.id());
                            transaction = fetchTransaction(user, subscription.id());
                        }
                        subscriptions.add(new SubscriptionItem(subscription, invoice, transaction));
                    }

                }
            } else {
                subscriptions = null;
            }

            String nextOffset = apiResponse.getResult() != null ? apiResponse.getResult().nextOffset() : null;
            result = new Pair<Iterable<SubscriptionItem>, String>(subscriptions, nextOffset);
        }

        return result;
    }

    /**
     * Fetch and return latest invoice model of a subscription
     */
    private Invoice fetchInvoice(User user, String subscriptionId) throws Exception {
        InvoiceListRequest request = Invoice.list().limit(1).subscriptionId().is(subscriptionId).customerId()
                .is(user.getName()).sortByDate(SortOrder.DESC);
        RequestListResult result = requestApiList(request);
        final Invoice invoice;
        if (result.isRateLimitReached()) {
            invoice = fetchInvoice(user, subscriptionId);
        } else {
            if (result.hasResult()) {
                invoice = result.getFirstItem().invoice();
            } else {
                invoice = null;
            }
        }
        return invoice;
    }

    /**
     * Fetch and return latest transaction model of a subscription
     */
    private Transaction fetchTransaction(User user, String subscriptionId) throws Exception {
        TransactionListRequest request = Transaction.list().limit(1).subscriptionId().is(subscriptionId).customerId()
                .is(user.getName()).sortByDate(SortOrder.DESC);
        RequestListResult result = requestApiList(request);
        final Transaction transaction;
        if (result.isRateLimitReached()) {
            transaction = fetchTransaction(user, subscriptionId);
        } else {
            if (result.hasResult()) {
                transaction = result.getFirstItem().transaction();
            } else {
                transaction = null;
            }
        }
        return transaction;
    }

    /**
     * Send API request to Chargebee, and check for API rate limit error, return true if the rate limit has been reached
     */
    private RequestListResult requestApiList(ListRequest<?> request) throws Exception {
        long startTime = TimePoint.now().asMillis();
        ListResult result = request.request();
        final boolean isRateLimitReached = checkAndProcessRateLimit(startTime, result.httpCode);
        return new RequestListResult(isRateLimitReached, result);
    }

    /**
     * Send request to API for canceling a subscription
     */
    private SubscriptionCancelResult requestCancel(String subscriptionId) throws Exception {
        final SubscriptionCancelResult cancelResult;
        long startTime = TimePoint.now().asMillis();
        Result result = cancel(subscriptionId).request();
        final boolean isRateLimitReached = checkAndProcessRateLimit(startTime, result.httpCode);
        if (isRateLimitReached) {
            cancelResult = requestCancel(subscriptionId);
        } else {
            Subscription subscription = null;
            boolean success = false;
            if (result.subscription().status().name().toLowerCase()
                    .equals(ChargebeeSubscription.SUBSCRIPTION_STATUS_CANCELLED)) {
                subscription = (new SubscriptionItem(result.subscription(), null, null)).toSubscription();
                success = true;
            }
            cancelResult = new SubscriptionCancelResult(success, subscription, /* deleted */ false);
        }
        return cancelResult;
    }

    private boolean checkAndProcessRateLimit(long requestStartTime, int httpCode) throws InterruptedException {
        final boolean isRateLimitReached = checkForRateLimitReached(httpCode);
        if (!isRateLimitReached) {
            long comsumedTime = TimePoint.now().asMillis() - requestStartTime;
            if (comsumedTime < TIME_FOR_API_REQUEST_MS) {
                TimeUnit.MILLISECONDS.sleep(TIME_FOR_API_REQUEST_MS - comsumedTime);
            }
        }
        return isRateLimitReached;
    }

    private boolean checkForRateLimitReached(int httpCode) throws InterruptedException {
        final boolean isLimitReached;
        // Check for request rate limit error
        if (httpCode == 429) {
            logger.warning(() -> "API rate limit has been reached");
            // Threshold value for test site: ~750 API calls in 5 minutes.
            // Threshold value for live site: ~150 API calls per site per minute.
            // In this case we will wait for 65 seconds and then try request again
            isLimitReached = true;
            TimeUnit.SECONDS.sleep(65);

        } else {
            isLimitReached = false;
        }
        return isLimitReached;
    }

    private class RequestListResult {
        private boolean rateLimitReached;
        private ListResult result;

        public RequestListResult(boolean isRateLimitReached, ListResult result) {
            this.rateLimitReached = isRateLimitReached;
            this.result = result;
        }

        public boolean isRateLimitReached() {
            return rateLimitReached;
        }

        public ListResult getResult() {
            return result;
        }

        public boolean hasResult() {
            return result != null && !result.isEmpty();
        }

        public Entry getFirstItem() {
            return result.get(0);
        }
    }

    private class SubscriptionItem {
        private com.chargebee.models.Subscription subscription;
        private Invoice invoice;
        private Transaction transaction;

        public SubscriptionItem(com.chargebee.models.Subscription subscription, Invoice invoice,
                Transaction transaction) {
            this.subscription = subscription;
            this.invoice = invoice;
            this.transaction = transaction;
        }

        public ChargebeeSubscription toSubscription() {
            String subscriptionStatus = null;
            if (subscription.status() != null) {
                subscriptionStatus = stringToLowerCase(subscription.status().name());
            }
            String transactionType = null;
            String transactionStatus = null;
            if (transaction != null) {
                transactionType = stringToLowerCase(transaction.type().name());
                transactionStatus = stringToLowerCase(transaction.status().name());
            }
            String invoiceId = null;
            String invoiceStatus = null;
            if (invoice != null) {
                invoiceId = invoice.id();
                invoiceStatus = stringToLowerCase(invoice.status().name());
            }
            String paymentStatus = ChargebeeSubscription.determinePaymentStatus(transactionType, transactionStatus,
                    invoiceStatus);
            return new ChargebeeSubscription(subscription.id(), subscription.planId(), subscription.customerId(),
                    TimePoint.of(subscription.trialStart()), TimePoint.of(subscription.trialEnd()), subscriptionStatus,
                    paymentStatus, transactionType, transactionStatus, invoiceId, invoiceStatus,
                    TimePoint.of(subscription.createdAt()), TimePoint.of(subscription.updatedAt()), TimePoint.now(),
                    TimePoint.now());
        }

        private String stringToLowerCase(String str) {
            return str == null ? null : str.toLowerCase();
        }
    }
}
