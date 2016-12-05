package com.sap.sailing.gwt.home.desktop.partials.regattaheader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.eventview.RegattaMetadataDTO;
import com.sap.sailing.gwt.home.communication.eventview.RegattaMetadataDTO.RaceDataInfo;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;

public class RegattaHeader extends Composite {

    private static RegattaHeaderUiBinder uiBinder = GWT.create(RegattaHeaderUiBinder.class);

    interface RegattaHeaderUiBinder extends UiBinder<Widget, RegattaHeader> {
    }

    @UiField
    RegattaHeaderResources local_res;

    @UiField
    AnchorElement headerBodyUi;
    @UiField
    AnchorElement headerArrowUi;
    @UiField
    AnchorElement dataIndicatorsUi;
    @UiField
    DivElement gpsDataIndicatorUi;
    @UiField
    DivElement windDataIndicatorUi;
    @UiField
    DivElement videoDataIndicatorUi;
    @UiField
    DivElement audioDataIndicatorUi;

    public RegattaHeader(RegattaMetadataDTO regattaMetadata, boolean showStateMarker) {
        initWidget(uiBinder.createAndBindUi(this));
        headerBodyUi.appendChild(new RegattaHeaderBody(regattaMetadata, showStateMarker).getElement());
        this.initDataIndicators(regattaMetadata.getRaceDataInfo());

        // setup click listener for icon legend bubble here, it may be replaced by setRegattaNavigation later on
        addLegendBubble();
    }

    private void addLegendBubble() {
        DOM.sinkEvents(dataIndicatorsUi, Event.ONCLICK);
        Event.setEventListener(dataIndicatorsUi, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                final RegattaHeaderLegendPopup pop = new RegattaHeaderLegendPopup();
                pop.setVisible(false);
                pop.show();
                // Pausing until the event loop is clear appears to give the browser
                // sufficient time to apply CSS styling. We use the popup's offset
                // width and height to calculate the display position, but those
                // dimensions are not accurate until styling has been applied.
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        // the arrow is ~16 pixels long
                        pop.setPopupPosition(dataIndicatorsUi.getAbsoluteLeft() - pop.getOffsetWidth() - 16,
                                RegattaHeader.this.getAbsoluteTop() + RegattaHeader.this.getOffsetHeight() / 2
                                        - pop.getOffsetHeight() / 2);
                        pop.setVisible(true);
                    }
                });
                event.preventDefault();
                event.stopPropagation();
            }
        });

    }

    public void setRegattaNavigation(PlaceNavigation<?> placeNavigation) {
        headerArrowUi.getStyle().clearDisplay();
        dataIndicatorsUi.addClassName(local_res.css().regattaheader_indicators_next_to_arrow());
        placeNavigation.configureAnchorElement(headerBodyUi);
        placeNavigation.configureAnchorElement(headerArrowUi);
    }

    public void setRegattaRacesNavigation(PlaceNavigation<?> placeNavigation) {
        placeNavigation.configureAnchorElement(dataIndicatorsUi);
    }

    private void initDataIndicators(RaceDataInfo raceDataInfo) {
        String disabledStyle = local_res.css().regattaheader_indicator_disabled();
        UIObject.setStyleName(gpsDataIndicatorUi, disabledStyle, !raceDataInfo.hasGPSData());
        UIObject.setStyleName(windDataIndicatorUi, disabledStyle, !raceDataInfo.hasWindData());
        UIObject.setStyleName(videoDataIndicatorUi, disabledStyle, !raceDataInfo.hasVideoData());
        UIObject.setStyleName(audioDataIndicatorUi, disabledStyle, !raceDataInfo.hasAudioData());
    }

}
