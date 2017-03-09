package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base;

import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.start.SixtyInchSetting;

public class SlideContextImpl implements SlideContext {
    private SixtyInchSetting settings;

    public SlideContextImpl(SixtyInchSetting settings) {
        if (settings == null) {
            throw new IllegalStateException("No settings in ctx creation");
        }
        this.settings = settings;
    }

    @Override
    public SixtyInchSetting getSettings() {
        return settings;
    }
}
