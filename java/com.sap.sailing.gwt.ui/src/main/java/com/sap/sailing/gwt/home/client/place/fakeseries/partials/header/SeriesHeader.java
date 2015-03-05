package com.sap.sailing.gwt.home.client.place.fakeseries.partials.header;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.PlaceNavigation;
import com.sap.sailing.gwt.home.client.place.event2.EventDefaultPlace;
import com.sap.sailing.gwt.home.client.place.fakeseries.SeriesView;
import com.sap.sailing.gwt.home.client.place.fakeseries.SeriesView.Presenter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.fakeseries.EventSeriesEventDTO;
import com.sap.sailing.gwt.ui.shared.fakeseries.EventSeriesViewDTO;

public class SeriesHeader extends Composite {
    private static SeriesHeaderUiBinder uiBinder = GWT.create(SeriesHeaderUiBinder.class);

    interface SeriesHeaderUiBinder extends UiBinder<Widget, SeriesHeader> {
    }
    
    @UiField StringMessages i18n;
    
    @UiField ImageElement eventLogo;
    @UiField HeadingElement staticTitle;
    @UiField SpanElement eventName;
    @UiField DivElement eventState;
    @UiField FlowPanel venues;

    private EventSeriesViewDTO series;

    private Presenter presenter;

    boolean dropdownShown = false;
    
    public SeriesHeader(SeriesView.Presenter presenter) {
        this.series = presenter.getCtx().getSeriesDTO();
        this.presenter = presenter;
        
        SeriesHeaderResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        initFields();
    }

    private void initFields() {
        String logoUrl = series.getLogoImageURL() != null ? series.getLogoImageURL() : SeriesHeaderResources.INSTANCE.defaultEventLogoImage().getSafeUri().asString();
        eventLogo.setSrc(logoUrl);
        eventLogo.setAlt(series.getDisplayName());
        eventName.setInnerText(series.getDisplayName());
        fillEventState(eventState);
        
        for (EventSeriesEventDTO eventOfSeries : series.getEvents()) {
            // TODO upcomming is not a link
            Anchor eventAnchor = new Anchor(eventOfSeries.getVenue());
            eventAnchor.addStyleName(SeriesHeaderResources.INSTANCE.css().eventheader_intro_details_item());
            final PlaceNavigation<EventDefaultPlace> eventNavigation = presenter.getEventNavigation(eventOfSeries.getId());
            eventAnchor.setHref(eventNavigation.getTargetUrl());
            venues.add(eventAnchor);
            eventAnchor.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    event.preventDefault();
                    eventNavigation.goToPlace();
                }
            });
        }
    }

    private void fillEventState(DivElement eventStateElement) {
        // TODO
//        if(event.getState() == EventState.FINISHED) {
//            eventStateElement.setInnerText(i18n.finished());
//            eventStateElement.setAttribute("data-labeltype", "finished");
//        } else if(event.getState() == EventState.RUNNING) {
//            eventStateElement.setInnerText(i18n.live());
//            eventStateElement.setAttribute("data-labeltype", "live");
//        } else {
            hide(eventStateElement);
//        }
    }

    private void hide(Element... elementsToHide) {
        for (Element element : elementsToHide) {
            element.getStyle().setDisplay(Display.NONE);
        }
    }
}
