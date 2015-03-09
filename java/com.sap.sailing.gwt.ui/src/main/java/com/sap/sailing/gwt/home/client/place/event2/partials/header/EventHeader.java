package com.sap.sailing.gwt.home.client.place.event2.partials.header;

import java.util.Date;

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
import com.sap.sailing.gwt.common.client.LinkUtil;
import com.sap.sailing.gwt.common.client.i18n.TextMessages;
import com.sap.sailing.gwt.home.client.place.event2.AbstractEventPlace;
import com.sap.sailing.gwt.home.client.place.event2.EventView;
import com.sap.sailing.gwt.home.client.place.event2.EventView.PlaceCallback;
import com.sap.sailing.gwt.home.client.place.event2.EventView.Presenter;
import com.sap.sailing.gwt.home.client.place.event2.partials.sharing.SharingButtons;
import com.sap.sailing.gwt.home.client.place.event2.partials.sharing.SharingMetadataProvider;
import com.sap.sailing.gwt.home.client.shared.EventDatesFormatterUtil;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO.EventState;
import com.sap.sailing.gwt.ui.shared.eventview.HasRegattaMetadata;

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
    @UiField DivElement eventVenueContainer;
    @UiField AnchorElement eventLink;
    @UiField DivElement competitors;
    @UiField SpanElement competitorsCount;
    @UiField DivElement races;
    @UiField SpanElement racesCount;
    @UiField DivElement trackedRaces;
    @UiField SpanElement trackedRacesCount;
    @UiField DivElement eventCategory;
    
    @UiField FlowPanel dropdownContent;
    
    @UiField SharingButtons sharing;

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
        initSharing();
    }

    private void initSharing() {
        sharing.setUp(new SharingMetadataProvider() {
            @Override
            public String getSharingTitle() {
                return event.getName();
            }
        });
    }

    private void initFields() {
        String logoUrl = event.getLogoImageURL() != null ? event.getLogoImageURL() : EventHeaderResources.INSTANCE.defaultEventLogoImage().getSafeUri().asString();
        eventLogo.setSrc(logoUrl);
        eventLogo.setAlt(event.getName());
        
        String nameToShow;
        if(presenter.showRegattaMetadata()) {
            HasRegattaMetadata regattaMetadata = presenter.getRegattaMetadata();
            nameToShow = regattaMetadata.getDisplayName();
            
            if(regattaMetadata.getCompetitorsCount() > 0) {
                competitorsCount.setInnerText(""+regattaMetadata.getCompetitorsCount());
            } else {
                hide(competitors);
            }
            if(regattaMetadata.getRaceCount() > 0) {
                racesCount.setInnerText(""+regattaMetadata.getRaceCount());
            } else {
                hide(races);
            }
            if(regattaMetadata.getTrackedRacesCount() > 0) {
                trackedRacesCount.setInnerText(""+regattaMetadata.getTrackedRacesCount());
            } else {
                hide(trackedRaces);
            }
            if(regattaMetadata.getBoatCategory() != null) {
                eventCategory.setInnerText(regattaMetadata.getBoatCategory());
            } else {
                hide(eventCategory);
            }
            Date startDate = regattaMetadata.getStartDate() != null ? regattaMetadata.getStartDate() : event.startDate;
            Date endDate = regattaMetadata.getEndDate() != null ? regattaMetadata.getEndDate() : event.endDate;
            eventDate.setInnerHTML(EventDatesFormatterUtil.formatDateRangeWithYear(startDate, endDate));
            
            hide(eventVenueContainer, eventLink);
        } else {
            nameToShow = event.getName();
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
            
            hide(competitors, races, trackedRaces);
        }
        
        initTitleAndSelection(nameToShow);
    }

    private void initTitleAndSelection(String nameToShow) {
        if(!presenter.needsSelectionInHeader()) {
            eventName.setInnerText(nameToShow);
            fillEventState(eventState);
            hide(dropdownTitle);
        } else {
            dropdownEventName.setInnerText(nameToShow);
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
            public void forPlace(final AbstractEventPlace place, String title, boolean active) {
                DropdownItem dropdownItem = new DropdownItem(title, presenter.getUrl(place), active);
                dropdownItem.addDomHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        if(LinkUtil.handleLinkClick((Event) event.getNativeEvent())) {
                            event.preventDefault();
                            presenter.navigateTo(place);
                        }
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
//            element.getStyle().setDisplay(Display.NONE);
            element.removeFromParent();
        }
    }
}
