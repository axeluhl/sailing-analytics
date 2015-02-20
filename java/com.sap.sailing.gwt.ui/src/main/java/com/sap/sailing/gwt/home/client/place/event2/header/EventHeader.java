package com.sap.sailing.gwt.home.client.place.event2.header;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
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
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.i18n.TextMessages;
import com.sap.sailing.gwt.home.client.place.event2.AbstractEventPlace;
import com.sap.sailing.gwt.home.client.place.event2.EventView;
import com.sap.sailing.gwt.home.client.place.event2.EventView.PlaceCallback;
import com.sap.sailing.gwt.home.client.place.event2.EventView.Presenter;
import com.sap.sailing.gwt.home.client.shared.EventDatesFormatterUtil;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class EventHeader extends Composite {
    private static EventHeaderUiBinder uiBinder = GWT.create(EventHeaderUiBinder.class);

    interface EventHeaderUiBinder extends UiBinder<Widget, EventHeader> {
    }
    
    @UiField StringMessages i18n;
    
    @UiField ImageElement eventLogo;
    @UiField HeadingElement staticTitle;
    @UiField SpanElement eventName;
    @UiField DivElement eventState;
    @UiField DivElement dropdownTitle;
    @UiField SpanElement dropdownEventName;
    @UiField DivElement dropdownEventState;
    @UiField AnchorElement dropdownTrigger;
    @UiField DivElement eventDate;
    @UiField SpanElement eventVenueName;
    @UiField SpanElement eventVenueCountry;
    @UiField AnchorElement eventLink;
    @UiField DivElement competitors;
    @UiField SpanElement competitorsCount;
    @UiField DivElement races;
    @UiField SpanElement racesCount;
    @UiField DivElement trackedRaces;
    @UiField SpanElement trackedRacesCount;
    @UiField DivElement eventCategory;
    
    @UiField FlowPanel dropdownContent;

    private EventDTO event;

    private Presenter presenter;

    private boolean provideSelection;
    
    public EventHeader(EventView.Presenter presenter, boolean provideSelection) {
        this.provideSelection = provideSelection;
        this.event = presenter.getCtx().getEventDTO();
        this.presenter = presenter;
        
        EventHeaderResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        initFields();
        
        initTitleAndSelection();
    }

    private void initFields() {
        String logoUrl = event.getLogoImageURL() != null ? event.getLogoImageURL() : EventHeaderResources.INSTANCE.defaultEventLogoImage().getSafeUri().asString();
        eventLogo.setSrc(logoUrl);
        eventLogo.setAlt(event.getName());
        
        eventDate.setInnerHTML(EventDatesFormatterUtil.formatDateRangeWithYear(event.startDate, event.endDate));
        eventVenueName.setInnerText(event.venue.getName());
//        TODO eventVenueCountry
        if(event.getOfficialWebsiteURL() != null) {
            String title = withoutPrefix(event.getOfficialWebsiteURL(), "http://", "https://");
            if(title.length() > 35) {
                title = TextMessages.INSTANCE.officalEventWebsite();
            }
            eventLink.setInnerText(title);
            eventLink.setHref(event.getOfficialWebsiteURL());
        } else {
            hide(eventLink);
        }
        
        // TODO Multi-Regatta-Event
//        if(event.regattas.size() > 1) {
////            TODO competitorsCount.setInnerText(text);
////            TODO racesCount;
////            TODO eventCategory.setInnerText(event.get);
//            if(event.isFinished()) {
////                TODO trackedRacesCount;
//            } else {
//                hide(trackedRaces);
//            }
//        } else {
            hide(competitors, races, trackedRaces);
//        }
    }

    private void initTitleAndSelection() {
        if(!provideSelection || !presenter.needsSelectionInHeader()) {
            eventName.setInnerText(event.getName());
            fillEventState(eventState);
            hide(dropdownTitle);
        } else {
            dropdownEventName.setInnerText(event.getName());
            fillEventState(dropdownEventState);
            hide(staticTitle);
            
            Event.sinkEvents(dropdownTrigger, Event.ONCLICK);
            Event.setEventListener(dropdownTrigger, new EventListener() {
                @Override
                public void onBrowserEvent(Event event) {
                    dropdownContent.getElement().getStyle().setDisplay(Display.BLOCK);
                }
            });
            
            presenter.forPlaceSelection(new PlaceCallback() {
                @Override
                public void forPlace(final AbstractEventPlace place, String title) {
                    DropdownItem dropdownItem = new DropdownItem(title, presenter.getUrl(place));
                    dropdownItem.addDomHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            event.preventDefault();
                            presenter.navigateTo(place);
                        }
                    }, ClickEvent.getType());
                    dropdownContent.add(dropdownItem);
                }
            });
        }
    }

    private void fillEventState(DivElement eventStateElement) {
        if(event.isFinished()) {
            eventStateElement.setInnerText(i18n.finished());
            eventStateElement.setAttribute("data-labeltype", "finished");
        } else if(event.isRunning()) {
            eventStateElement.setInnerText(i18n.live());
            eventStateElement.setAttribute("data-labeltype", "live");
        } else {
            hide(eventStateElement);
        }
    }

    private String withoutPrefix(String title, String... prefixes) {
        for (String prefix : prefixes) {
            if(title.startsWith(prefix)) {
                return title.substring(prefix.length(), title.length());
            }
        }
        return title;
    }

    private void hide(Element... elementsToHide) {
        for (Element element : elementsToHide) {
            element.getStyle().setDisplay(Display.NONE);
        }
    }
}
