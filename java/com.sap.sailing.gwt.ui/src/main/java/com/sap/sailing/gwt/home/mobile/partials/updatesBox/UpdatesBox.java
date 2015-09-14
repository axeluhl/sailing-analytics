package com.sap.sailing.gwt.home.mobile.partials.updatesBox;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.FontStyle;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.shared.refresh.RefreshManager;
import com.sap.sailing.gwt.home.client.shared.refresh.RefreshableWidget;
import com.sap.sailing.gwt.home.mobile.partials.section.MobileSection;
import com.sap.sailing.gwt.home.mobile.partials.sectionHeader.SectionHeaderContent;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.dispatch.ListResult;
import com.sap.sailing.gwt.ui.shared.dispatch.news.NewsEntryDTO;

public class UpdatesBox extends Composite implements RefreshableWidget<ListResult<NewsEntryDTO>> {
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    interface MyUiBinder extends UiBinder<Widget, UpdatesBox> {
    }

    @UiField
    MobileSection itemContainerUi;
    @UiField
    SectionHeaderContent headerUi;

    private boolean dontDrillDown = false;
    private boolean dontHide = false;
    private NewsItemLinkProvider presenter;
    private final RefreshManager refreshManager;

    public UpdatesBox(NewsItemLinkProvider presenter, RefreshManager refreshManager) {
        this.presenter = presenter;
        this.refreshManager = refreshManager;
        UpdatesBoxResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        if (!dontHide) {
            getElement().getStyle().setDisplay(Display.NONE);
        }
    }


    @Override
    public void setData(final ListResult<NewsEntryDTO> data) {
        setData(data == null ? null :data.getValues());
    }

    public void setData(final List<NewsEntryDTO> data) {
        int nrOfNews = data == null ? 0 : data.size();
        GWT.log("Nr of news items: " + nrOfNews);
        itemContainerUi.clearContent();
        if (nrOfNews == 0) {
            if (!dontHide) {
                getElement().getStyle().setDisplay(Display.NONE);
            } else {
                itemContainerUi.addContent(getNoNewsInfoWidget());
            }
        } else {
            getElement().getStyle().clearDisplay();
            for (NewsEntryDTO newsEntryDTO : data) {
                itemContainerUi.addContent(new UpdatesBoxItem(newsEntryDTO, refreshManager.getDispatchSystem().getCurrentServerTime(), presenter));
            }
            if (!dontDrillDown) {
                headerUi.setClickAction(presenter.getNewsPlaceNavigation(data));
            }
        }
    }
    
    private Widget getNoNewsInfoWidget() {
        Label label = new Label(StringMessages.INSTANCE.noNews());
        label.getElement().getStyle().setPadding(1, Unit.EM);
        label.getElement().getStyle().setFontWeight(FontWeight.BOLD);
        label.getElement().getStyle().setFontStyle(FontStyle.ITALIC);
        return label;
    }

    public void setDontDrillDown(boolean dontDrillDown) {
        this.dontDrillDown = dontDrillDown;
    }

    public void setDontHide(boolean dontHide) {
        this.dontHide = dontHide;
        getElement().getStyle().clearDisplay();
    }
}
