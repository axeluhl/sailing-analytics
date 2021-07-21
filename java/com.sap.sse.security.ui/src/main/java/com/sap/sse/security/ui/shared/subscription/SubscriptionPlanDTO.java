package com.sap.sse.security.ui.shared.subscription;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.security.shared.dto.NamedDTO;
import com.sap.sse.security.ui.client.subscription.SubscriptionService;

/**
 * SubscriptionPlan data transfer object {@link SubscriptionService#getAllSubscriptionPlans()}
 */
public class SubscriptionPlanDTO extends NamedDTO implements IsSerializable {
    private static final long serialVersionUID = -1990028347487353679L;
    String id;

    ArrayList<String> features;

    private String error;
    
    private String price;

    /**
     * For GWT Serialization only
     */
    @Deprecated
    public SubscriptionPlanDTO() {
    }

    public SubscriptionPlanDTO(String id, String name, List<String> features, String price, String error) {
        super(name);
        this.id = id;
        this.features = new ArrayList<String>(features);
        this.price = price;
        this.error = error;
    }
    
    public String getId() {
        return id;
    }

    public String getError() {
        return error;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public ArrayList<String> getFeatures() {
        return features;
    }

    public String getPrice() {
        return price;
    }
}
