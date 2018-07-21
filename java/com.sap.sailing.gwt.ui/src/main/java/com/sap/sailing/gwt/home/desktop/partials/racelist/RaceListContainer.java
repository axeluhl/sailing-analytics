package com.sap.sailing.gwt.home.desktop.partials.racelist;


import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.race.RaceMetadataDTO;
import com.sap.sailing.gwt.home.shared.refresh.RefreshableWidget;
import com.sap.sse.gwt.dispatch.shared.commands.CollectionResult;

public class RaceListContainer<T extends RaceMetadataDTO<?>> extends Composite
        implements RefreshableWidget<CollectionResult<T>> {

    private static RaceListContainerUiBinder uiBinder = GWT.create(RaceListContainerUiBinder.class);

    interface RaceListContainerUiBinder extends UiBinder<Widget, RaceListContainer<?>> {
    }
    
    @UiField HeadingElement titleUi;
    @UiField DivElement infoUi;
    @UiField DivElement noContentPlaceholderUi;
    @UiField(provided = true) AbstractRaceList<T> raceListUi;
    @UiField DivElement iconWind;
    @UiField DivElement iconVideo;
    @UiField DivElement iconAudio;

    private String noContentPlaceholderText;

    public RaceListContainer(String title, AbstractRaceList<T> raceList) {
        this(title, null, raceList);
    }
    
    public RaceListContainer(String title, String noContentPlaceholderText, AbstractRaceList<T> raceList) {
        this.noContentPlaceholderText = noContentPlaceholderText;
        RaceListResources.INSTANCE.css().ensureInjected();
        this.raceListUi = raceList;
        initWidget(uiBinder.createAndBindUi(this));
        this.titleUi.setInnerText(title);
        raceListUi.setVisible(false);
    }
    
    @Override
    public void setData(CollectionResult<T> data) {
        setRaceListData(data == null ? null : data.getValues());
    }
    
    public void setRaceListData(Collection<T> data) {
        if (data == null || data.isEmpty()) {
            if (noContentPlaceholderText == null) {
                getElement().getStyle().setDisplay(Display.NONE);
            } else {
                noContentPlaceholderUi.setInnerText(noContentPlaceholderText);
                noContentPlaceholderUi.getStyle().clearDisplay();
                raceListUi.setVisible(false);
            }
            iconWind.getStyle().setDisplay(Display.NONE);
            iconVideo.getStyle().setDisplay(Display.NONE);
            iconAudio.getStyle().setDisplay(Display.NONE);
        } else {
            noContentPlaceholderUi.getStyle().setDisplay(Display.NONE);
            getElement().getStyle().clearDisplay();
            raceListUi.setVisible(true);
            this.raceListUi.setTableData(data);
            iconWind.getStyle().setDisplay(raceListUi.hasWind() ? Display.INLINE_BLOCK : Display.NONE);
            iconVideo.getStyle().setDisplay(raceListUi.hasVideos() ? Display.INLINE_BLOCK : Display.NONE);
            iconAudio.getStyle().setDisplay(raceListUi.hasAudios() ? Display.INLINE_BLOCK : Display.NONE);
        }
    }
    
    public void setInfoText(String infoText) {
        this.infoUi.setInnerText(infoText);
        this.infoUi.getStyle().clearDisplay();
    }
}
