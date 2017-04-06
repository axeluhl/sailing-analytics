package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide6;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide6.statistik.PreRaceStatisticsBox;
import com.sap.sailing.gwt.home.communication.event.sixtyinch.GetSixtyInchStatisticDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMap;
import com.sap.sse.gwt.client.panels.ResizableFlowPanel;

public class PreRaceRacemapViewImpl extends ResizeComposite implements PreRaceRacemapView {
    private static PreRaceRacemapViewImplUiBinder uiBinder = GWT.create(PreRaceRacemapViewImplUiBinder.class);

    @UiField
    ResizableFlowPanel racemap;

    @UiField
    ResizableFlowPanel raceInfoHolder;

    PreRaceStatisticsBox statistics = new PreRaceStatisticsBox(false);

    private NumberFormat compactFormat = NumberFormat.getFormat("#.00");;

    interface PreRaceRacemapViewImplUiBinder extends UiBinder<Widget, PreRaceRacemapViewImpl> {
    }

    public PreRaceRacemapViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
    }


    @Override
    public void showErrorNoLive(PreRaceRacemapPresenterImpl slide7PresenterImpl, AcceptsOneWidget panel, Throwable error) {
        panel.setWidget(new Label("Could not load RaceMap: " + error.getMessage()));
    }

    @Override
    public void startingWith(Slide7Presenter p, AcceptsOneWidget panel, RaceMap raceMap) {
        panel.setWidget(this);
        racemap.add(raceMap);

        raceInfoHolder.add(statistics);
    }


    @Override
    public void updateStatistic(GetSixtyInchStatisticDTO result, String url, String windSpeed, String windDegree) {
        GWT.log("Update statistic!");
        statistics.clear();
        statistics.addItem(PreRaceStatisticsBox.ICON_COMPATITORS_COUNT, StringMessages.INSTANCE.competitors(),
                result.getCompetitors());
        statistics.addItem(PreRaceStatisticsBox.ICON_WIND_FIX, StringMessages.INSTANCE.wind(), windSpeed);
        statistics.addItem(PreRaceStatisticsBox.ICON_WIND_FIX, StringMessages.INSTANCE.averageDirection(), windDegree);

        statistics.addItem("", "18 Legs", result.getLegs());
        statistics.addItem(PreRaceStatisticsBox.ICON_SUM_MILES, "18 Approximate Distance (m)",
                compactFormat.format(result.getDistance().getSeaMiles()));
        statistics.addItem(PreRaceStatisticsBox.ICON_FASTEST_SAILOR, "18 Approximate Time (s)",
                compactFormat.format(result.getDuration().asMinutes()));
        
        statistics.addItem("", StringMessages.INSTANCE.url(), url);

    }

}
