package com.sap.sailing.gwt.home.client.place.event.partials.updatesBox;

import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.UListElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.EventView;
import com.sap.sailing.gwt.home.client.place.event.EventView.Presenter;
import com.sap.sailing.gwt.ui.shared.dispatch.news.NewsEntryDTO;

public class UpdatesBox extends Composite {
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    interface MyUiBinder extends UiBinder<Widget, UpdatesBox> {
    }
    
    @UiField(provided = true) FlowPanel entries = new FlowPanel(UListElement.TAG);
    private Presenter presenter;
    
    public UpdatesBox(EventView.Presenter presenter) {
        this.presenter = presenter;
        
        UpdatesBoxResources.INSTANCE.css().ensureInjected();
        
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void setData(Collection<NewsEntryDTO> newsEntries) {
        entries.clear();
        for (NewsEntryDTO newsEntryDTO : newsEntries) {
            entries.add(new UpdatesBoxItem(newsEntryDTO, presenter));
        }
    }
}
