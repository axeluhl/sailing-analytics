package com.sap.sailing.gwt.ui.shared.subscription;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.gwt.ui.client.subscription.SubscriptionService;
import com.sap.sse.security.shared.dto.NamedDTO;

/**
 * SubscriptionPlan data transfer object {@link SubscriptionService#getAllSubscriptionPlans()}
 */
public class SubscriptionPlanDTO extends NamedDTO implements IsSerializable {
    private static final long serialVersionUID = -1990028347487353679L;
    String id;

    List<String> features;

    private String error;

    /**
     * For GWT Serialization only
     */
    @Deprecated
    public SubscriptionPlanDTO() {
    }

    public SubscriptionPlanDTO(String id, String name, List<String> features, String error) {
        super(name);
        this.id = id;
        this.features = features;
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
}
