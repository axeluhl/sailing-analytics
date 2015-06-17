package com.sap.sailing.gwt.home.mobile.partials.updatesBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload.RefreshableWidget;
import com.sap.sailing.gwt.home.mobile.partials.section.MobileSection;
import com.sap.sailing.gwt.home.mobile.partials.sectionHeader.SectionHeaderContent;
import com.sap.sailing.gwt.home.mobile.places.latestnews.NewsItemLinkProvider;
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
    private NewsItemLinkProvider presenter;

    public UpdatesBox(NewsItemLinkProvider presenter) {
        this.presenter = presenter;
        UpdatesBoxResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        getElement().getStyle().setDisplay(Display.NONE);
    }

    @Override
    public void setData(final ListResult<NewsEntryDTO> data, long nextUpdate, int updateNo) {
        int nrOfNews = data.getValues().size();
        GWT.log("Nr of news items: " + nrOfNews);
        if (nrOfNews == 0) {
            getElement().getStyle().setDisplay(Display.NONE);
        } else {

            getElement().getStyle().clearDisplay();
            itemContainerUi.clearContent();
            int count = 0;
            for (NewsEntryDTO newsEntryDTO : data.getValues()) {
                itemContainerUi.addContent(new UpdatesBoxItem(newsEntryDTO, presenter));
                count++;
                if (count > 3) {
                    break;
                }
            }
            if (!data.getValues().isEmpty()) {
                headerUi.setClickAction(new Command() {
                    @Override
                    public void execute() {
                        presenter.gotoNewsPlace(data.getValues());
                    }
                });
            }
        }
    }
}
