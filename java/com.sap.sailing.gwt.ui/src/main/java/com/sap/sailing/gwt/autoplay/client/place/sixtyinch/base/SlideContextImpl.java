package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base;

import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.start.SixtyInchSetting;

public class SlideContextImpl implements SlideContext {
    private SixtyInchSetting settings;

    public SlideContextImpl(SixtyInchSetting settings) {
        this.settings = settings;
    }

    @Override
    public SixtyInchSetting getSettings() {
        return settings;
    }
}
