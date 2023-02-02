package com.sap.sse.security.shared.subscription;

import java.io.Serializable;
import java.math.BigDecimal;

public class SubscriptionPrice implements Serializable {
    private static final long serialVersionUID = -1448071130685381256L;
    private String priceId;
    private BigDecimal price;
    private PaymentInterval paymentInterval;
    private String currencyCode;
    private Boolean disablePlan;
    private Boolean isOneTimePayment;

    /*
     * For GWT Serialization
     */
    @Deprecated
    public SubscriptionPrice() {
    }

    public SubscriptionPrice(String priceId, BigDecimal price, String currencyCode, PaymentInterval paymentInterval,
            boolean isOneTimePayment) {
        super();
        this.priceId = priceId;
        this.price = price;
        this.currencyCode = currencyCode;
        this.paymentInterval = paymentInterval;
        this.isOneTimePayment = isOneTimePayment;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
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

    public String getPriceId() {
        return priceId;
    }

    public void setPriceId(String priceId) {
        this.priceId = priceId;
    }

    public Boolean getDisablePlan() {
        return disablePlan;
    }

    public void setDisablePlan(Boolean disablePlan) {
        this.disablePlan = disablePlan;
    }

    public Boolean getIsOneTimePayment() {
        return isOneTimePayment;
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
        result = prime * result + ((priceId == null) ? 0 : priceId.hashCode());
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
        if (priceId == null) {
            if (other.priceId != null)
                return false;
        } else if (!priceId.equals(other.priceId))
            return false;
        return true;
    }

}
