package com.sap.sailing.gwt.settings.client.embeddedmapandwindchart;

import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapSettings;
import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.BooleanSetting;

public class EmbeddedMapAndWindChartSettings extends AbstractGenericSerializableSettings {

    private static final long serialVersionUID = 272440351202821549L;

    private static final String PARAM_SHOW_COMPETITORS = "showCompetitors";
    private static final String PARAM_PLAY = "play";

    private transient BooleanSetting showCompetitors;
    private transient BooleanSetting play;
    private transient BooleanSetting showCourseGeometry;
    private transient BooleanSetting windUp;

    public EmbeddedMapAndWindChartSettings() {
    }

    public EmbeddedMapAndWindChartSettings(final boolean play) {
        this.play.setValue(play);;
    }

    @Override
    protected void addChildSettings() {
        showCompetitors = new BooleanSetting(PARAM_SHOW_COMPETITORS, this, false);
        play = new BooleanSetting(PARAM_PLAY, this, false);
        showCourseGeometry = new BooleanSetting(RaceMapSettings.PARAM_SHOW_COURSE_GEOMETRY, this, true);
        windUp = new BooleanSetting(RaceMapSettings.PARAM_MAP_ORIENTATION_WIND_UP, this, true);
    }

    public boolean isShowCompetitors() {
        return showCompetitors.getValue();
    }

    public boolean isPlay() {
        return play.getValue();
    }

    public boolean isShowCourseGeometry() {
        return showCourseGeometry.getValue();
    }

    public boolean isWindUp() {
        return windUp.getValue();
    }

}
