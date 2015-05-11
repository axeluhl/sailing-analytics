package com.sap.sailing.gwt.home.client.place.event.partials.raceListLive;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.LinkUtil;
import com.sap.sailing.gwt.ui.shared.dispatch.event.LiveRaceDTO;

public class RacesListLiveRow extends Composite {

    private static RacesListLiveRowUiBinder uiBinder = GWT.create(RacesListLiveRowUiBinder.class);

    interface RacesListLiveRowUiBinder extends UiBinder<Widget, RacesListLiveRow> {
    }
    
    @UiField DivElement fleetCorner;
    @UiField TableCellElement regatta;
    @UiField TableCellElement race;
    @UiField TableCellElement fleetName;
    @UiField TableCellElement start;
    @UiField ImageElement flag;
    @UiField TableCellElement wind;
    @UiField TableCellElement from;
    @UiField TableCellElement area;
    @UiField TableCellElement course;
    @UiField DivElement statusText;
    @UiField DivElement statusProgress;
    @UiField AnchorElement watchNowAnchor;

    private DateTimeFormat startTimeFormat = DateTimeFormat.getFormat(PredefinedFormat.HOUR24_MINUTE);

    public RacesListLiveRow(LiveRaceDTO raceData) {
        initWidget(uiBinder.createAndBindUi(this));
        initEventListener(raceData);
        setData(raceData);
    }

    void setData(LiveRaceDTO raceData) {
        fleetCorner.getStyle().setBorderColor(raceData.getFleetColor()); // TODO border-top-color!?
        regatta.setInnerText(raceData.getRegattaName());
        race.setInnerText(raceData.getRaceName());
        fleetName.setInnerText(raceData.getFleetName());
        start.setInnerText(raceData.getStart() == null ? "-" : startTimeFormat.format(raceData.getStart()));
        // flag.setSrc(""); TODO
        wind.setInnerText("TODO Wind");
        from.setInnerText("TODO From");
        area.setInnerText("TODO Area");
        course.setInnerText("TODO Course");
        statusText.setInnerText("TODO Status");
        statusProgress.getStyle().setWidth(50, Unit.PCT);
    }

    private void initEventListener(final LiveRaceDTO raceData) {
        Event.sinkEvents(watchNowAnchor, Event.ONCLICK);
        Event.setEventListener(watchNowAnchor, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                if (LinkUtil.handleLinkClick(event)) {
                    // TODO
                    Window.alert("Watch now: " + raceData.getRegattaName() + " " + raceData.getRaceName());
                }
            }
        });
    }

}
