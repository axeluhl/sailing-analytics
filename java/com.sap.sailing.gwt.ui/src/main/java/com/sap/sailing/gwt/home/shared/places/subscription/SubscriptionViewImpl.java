package com.sap.sailing.gwt.home.shared.places.subscription;

import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.sap.sse.security.ui.shared.subscription.SubscriptionPlanDTO;

public class SubscriptionViewImpl extends Composite implements SubscriptionView {

    private final FlowPanel container = new FlowPanel();

    public SubscriptionViewImpl() {
        initWidget(container);
        container.getElement().getStyle().setMargin(5, Unit.EM);
    }

    @Override
    public void addSubscriptionPlan(final SubscriptionPlanDTO plan, final boolean highlight) {
        final Label label = new Label(plan.getName());
        label.getElement().getStyle().setFontWeight(highlight ? FontWeight.BOLD : FontWeight.NORMAL);
        container.add(label);
    }

}
