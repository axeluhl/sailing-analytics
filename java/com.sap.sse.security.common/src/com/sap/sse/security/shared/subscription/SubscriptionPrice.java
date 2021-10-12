package com.sap.sse.security.shared.subscription;

import java.io.Serializable;
import java.math.BigDecimal;

public class SubscriptionPrice implements Serializable{
    private static final long serialVersionUID = -1448071130685381256L;
    private BigDecimal price;
    private PaymentInterval paymentInterval;
    
    /*
     * For GWT Serialization
     */
    @Deprecated
    public SubscriptionPrice() {}
    
    public SubscriptionPrice(String price, PaymentInterval paymentInterval) {
        super();
        this.price = new BigDecimal(price);
        this.paymentInterval = paymentInterval;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public PaymentInterval getPaymentInterval() {
        return paymentInterval;
    }

    public void setPaymentInterval(PaymentInterval paymentInterval) {
        this.paymentInterval = paymentInterval;
    }

    public enum PaymentInterval {
        YEAR, MONTH, WEEK, DAY
    }
}
