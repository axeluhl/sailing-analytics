package com.sap.sailing.gwt.managementconsole.places.regatta.overview.partials;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.managementconsole.places.regatta.overview.RegattaOverviewResources;
import com.sap.sailing.gwt.managementconsole.places.regatta.overview.RegattaOverviewView.Presenter;
import com.sap.sailing.gwt.ui.common.client.DateAndTimeFormatterUtil;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;

public class RegattaCard extends Composite {

    interface RegattaCardUiBinder extends UiBinder<Widget, RegattaCard> {
    }

    private static RegattaCardUiBinder uiBinder = GWT.create(RegattaCardUiBinder.class);

    @UiField
    RegattaOverviewResources local_res;

    @UiField
    Element card, title, subTitle;

    @UiField
    Anchor advancedSettingsRegattaAnchor;

    private final RegattaDTO regatta;
    private final Presenter presenter;

    public RegattaCard(final RegattaDTO regatta, final Presenter presenter) {
        initWidget(uiBinder.createAndBindUi(this));
        this.regatta = regatta;
        this.presenter = presenter;
        local_res.style().ensureInjected();

        // TODO get race count
        // regatta.getRaceCount();
        // TODO get icon for boatclass
        // regatta.boatClass;
        
        final String title = regatta.getName();
        String venue = "-";
        venue = regatta.getName();
        
        String time = "-";
        if (regatta.startDate != null && regatta.endDate != null) {
            time = DateAndTimeFormatterUtil.formatDateRange(regatta.startDate, regatta.endDate);
        } else if (regatta.startDate != null) {
            time = DateAndTimeFormatterUtil.formatDateAndTime(regatta.startDate);
        }

        this.title.setInnerSafeHtml(SafeHtmlUtils.fromString(title));
        this.subTitle.setInnerSafeHtml(SafeHtmlUtils.fromString(venue + ", " + time));
    }

    @UiHandler("advancedSettingsRegattaAnchor")
    void onSettingsIconClick(final ClickEvent regatta) {
        presenter.navigateToRegatta(this.regatta);
    }

}
