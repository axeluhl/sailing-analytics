package com.sap.sailing.gwt.home.client;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.LIElement;
import com.google.gwt.dom.client.UListElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.dto.EventDTO;

public class EventsPageView extends Composite implements EventsPagePresenter.MyView {
    private static EventsPageViewUiBinder uiBinder = GWT.create(EventsPageViewUiBinder.class);

    interface EventsPageViewUiBinder extends UiBinder<Widget, EventsPageView> {
    }

    @UiField
    TextBox queryInput;
    @UiField
    Button searchButton;
    @UiField
    UListElement results;
    
    public EventsPageView() {
        super();

        initWidget(uiBinder.createAndBindUi(this));
        queryInput.getElement().setId("queryInput");
    }

    @UiHandler("searchButton")
    void buttonClick(ClickEvent event) {
        for (int i = 1; i <= 3; i++) {
            results.appendChild(createResultsItem("Result " + i));
        }
    }
 
    private Element createResultsItem(String value) {
        LIElement result = Document.get().createLIElement();
        result.setInnerHTML(value);
        return result;
    }
    
    @Override
    public void addToSlot(Object slot, IsWidget content) {
    }

    @Override
    public void removeFromSlot(Object slot, IsWidget content) {
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
    }

	@Override
	public void setEvents(List<EventDTO> events) {
		results.removeAllChildren();

		for(EventDTO event: events) {
            results.appendChild(createResultsItem(event.getName()));
		}
	}

}

