package com.sap.sailing.gwt.home.shared.partials.anniversary;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.home.communication.anniversary.AnniversariesDTO;

/**
 * Abstract implementation of {@link AnniversariesView} providing common functionalities.
 */
public abstract class AbstractAnniversaries extends Composite implements AnniversariesView {

    private final FlowPanel panel;
    private final AnniversariesPresenter presenter;

    protected AbstractAnniversaries() {
        initWidget(this.panel = new FlowPanel());
        this.presenter = new AnniversariesPresenter(this);
    }

    @Override
    public void setData(AnniversariesDTO data) {
        this.presenter.setData(data);
    }

    @Override
    public void clearAnniversaries() {
        this.panel.clear();
    }

    protected <T extends IsWidget> T addAnniversaryItem(T anniversaryItem) {
        this.panel.add(anniversaryItem);
        return anniversaryItem;
    }

}
