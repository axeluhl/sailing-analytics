package com.sap.sse.security.ui.client.subscription;

/**
 * View to display an overview of available subscriptions including information like prices, features, etc. as well as
 * the possibility to subscribe for on of the subscription using a provided payment period.
 */
public interface SubscribeView {

    /**
     * Called if modal checkout dialog is closed.
     */
    public void onCloseCheckoutModal();

    /**
     * Called if modal checkout has errors.
     *
     * @param error
     *            the {@link String error text} to show
     */
    public void onOpenCheckoutError(String error);

    /**
     * Called if payment has not yet finished.
     *
     * @param error
     *            the {@link String error text} to show
     */
    void onUnfinishedPayment(String message);

    /**
     * Called if payment has finished (or Trial has begun)
     * and the User has aquired the roles.
     *
     * @param error
     *            the {@link String error text} to show
     */
    void onFinishedPayment(String message);
}
