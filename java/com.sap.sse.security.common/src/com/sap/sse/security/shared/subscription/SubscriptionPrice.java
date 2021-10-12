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
    

    @Override
    public String toString() {
        return "SubscriptionPrice [price=" + price + ", paymentInterval=" + paymentInterval + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((paymentInterval == null) ? 0 : paymentInterval.hashCode());
        result = prime * result + ((price == null) ? 0 : price.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SubscriptionPrice other = (SubscriptionPrice) obj;
        if (paymentInterval != other.paymentInterval)
            return false;
        if (price == null) {
            if (other.price != null)
                return false;
        } else if (!price.equals(other.price))
            return false;
        return true;
    }
    
}
