package com.sap.sailing.gwt.managementconsole.places.regatta.overview;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.managementconsole.places.regatta.overview.partials.RegattaCard;
import com.sap.sailing.gwt.managementconsole.resources.ManagementConsoleResources;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;

public class RegattaOverviewViewImpl extends Composite implements RegattaOverviewView {

    private Presenter presenter;

    interface RegattaOverviewViewImplUiBinder extends UiBinder<Widget, RegattaOverviewViewImpl> {
    }

    private static RegattaOverviewViewImplUiBinder uiBinder = GWT.create(RegattaOverviewViewImplUiBinder.class);

    @UiField
    RegattaOverviewResources local_res;

    @UiField
    ManagementConsoleResources app_res;

    @UiField
    FlowPanel cards;

    @UiField
    Anchor addEventAnchor, searchEventAnchor;

    public RegattaOverviewViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
        local_res.style().ensureInjected();
        app_res.icons().ensureInjected();
    }

    @Override
    public void setPresenter(final Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void renderRegattas(final List<RegattaDTO> regattas) {
        cards.clear();
        regattas.stream().map(regatta -> new RegattaCard(regatta, presenter)).forEach(cards::add);
    }

}
