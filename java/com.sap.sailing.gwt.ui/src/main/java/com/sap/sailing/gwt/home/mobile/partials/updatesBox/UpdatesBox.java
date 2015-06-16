package com.sap.sailing.gwt.home.mobile.partials.updatesBox;

import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.mobile.partials.section.MobileSection;
import com.sap.sailing.gwt.home.mobile.partials.sectionHeader.SectionHeaderContent;
import com.sap.sailing.gwt.home.mobile.places.event.EventView.Presenter;
import com.sap.sailing.gwt.ui.shared.dispatch.news.NewsEntryDTO;

public class UpdatesBox extends Composite {
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    interface MyUiBinder extends UiBinder<Widget, UpdatesBox> {
    }
    
    @UiField
    MobileSection itemContainerUi;
    @UiField
    SectionHeaderContent headerUi;

    private Presenter presenter;
    
    public UpdatesBox(Presenter presenter) {
        this.presenter = presenter;
        
        UpdatesBoxResources.INSTANCE.css().ensureInjected();
        
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void setData(final Collection<NewsEntryDTO> newsEntries) {
        itemContainerUi.clearContent();
        for (NewsEntryDTO newsEntryDTO : newsEntries) {
            itemContainerUi.addContent(new UpdatesBoxItem(newsEntryDTO, presenter));
        }
        if (!newsEntries.isEmpty()) {
            headerUi.setClickAction(new Command() {
                @Override
                public void execute() {
                    presenter.gotoNewsPlace(newsEntries);
                }
            });
        }

    }
}
