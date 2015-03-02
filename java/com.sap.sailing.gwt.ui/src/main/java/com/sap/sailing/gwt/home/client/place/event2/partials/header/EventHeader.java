package com.sap.sailing.gwt.home.client.place.event2.partials.header;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.sap.sailing.gwt.common.client.i18n.TextMessages;
import com.sap.sailing.gwt.home.client.place.event2.AbstractEventPlace;
import com.sap.sailing.gwt.home.client.place.event2.EventView;
import com.sap.sailing.gwt.home.client.place.event2.EventView.PlaceCallback;
import com.sap.sailing.gwt.home.client.place.event2.EventView.Presenter;
import com.sap.sailing.gwt.home.client.shared.EventDatesFormatterUtil;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO.EventState;

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
    @UiField SpanElement eventVenue;
    @UiField AnchorElement eventLink;
    @UiField DivElement competitors;
    @UiField SpanElement competitorsCount;
    @UiField DivElement races;
    @UiField SpanElement racesCount;
    @UiField DivElement trackedRaces;
    @UiField SpanElement trackedRacesCount;
    @UiField DivElement eventCategory;
    
    @UiField FlowPanel dropdownContent;

    private EventViewDTO event;

    private Presenter presenter;

    private HandlerRegistration reg;
    boolean dropdownShown = false;
    
    public EventHeader(EventView.Presenter presenter) {
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
        String venue = event.getVenue().getName();
        if(event.getVenueCountry() != null && !event.getVenueCountry().isEmpty()) {
            venue += ", " + event.getVenueCountry();
        }
        eventVenue.setInnerText(venue);
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
        if(!presenter.needsSelectionInHeader()) {
            eventName.setInnerText(presenter.getEventName());
            fillEventState(eventState);
            hide(dropdownTitle);
        } else {
            dropdownEventName.setInnerText(presenter.getEventName());
            fillEventState(dropdownEventState);
            hide(staticTitle);
            
            initDropdown();
        }
    }

    private void initDropdown() {
        Event.sinkEvents(dropdownTrigger, Event.ONCLICK);
        Event.setEventListener(dropdownTrigger, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                if(dropdownShown) {
                    return;
                }
                dropdownContent.getElement().getStyle().setDisplay(Display.BLOCK);
                dropdownShown = true;
                reg = Event.addNativePreviewHandler(new NativePreviewHandler() {
                    @Override
                    public void onPreviewNativeEvent(NativePreviewEvent event) {
                        EventTarget eventTarget = event.getNativeEvent().getEventTarget();
                        if(!Element.is(eventTarget)) {
                            return;
                        }
                        Element evtElement = Element.as(eventTarget);
                        if(event.getTypeInt() == Event.ONCLICK && !dropdownContent.getElement().isOrHasChild(evtElement)) {
                            Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                                @Override
                                public void execute() {
                                    dropdownContent.getElement().getStyle().clearDisplay();
                                    dropdownShown = false;
                                    reg.removeHandler();
                                    reg = null;
                                }
                            });
                        }
                    }
                });
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

    private void fillEventState(DivElement eventStateElement) {
        if(event.getState() == EventState.FINISHED) {
            eventStateElement.setInnerText(i18n.finished());
            eventStateElement.setAttribute("data-labeltype", "finished");
        } else if(event.getState() == EventState.RUNNING) {
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
