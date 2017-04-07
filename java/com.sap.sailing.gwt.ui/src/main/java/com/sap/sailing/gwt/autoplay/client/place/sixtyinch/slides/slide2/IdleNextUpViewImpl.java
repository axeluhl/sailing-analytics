package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide2;

import java.util.ArrayList;
import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sse.common.Util.Pair;

public class IdleNextUpViewImpl extends Composite implements IdleUpNextView {
    private static IdleNextUpViewImplUiBinder uiBinder = GWT.create(IdleNextUpViewImplUiBinder.class);

    @UiField
    SimplePanel mainPanelUi;

    @UiField
    HTMLPanel dataPanel;

    interface Dummy extends ClientBundle {
        ImageResource dummy();
    }

    interface IdleNextUpViewImplUiBinder extends UiBinder<Widget, IdleNextUpViewImpl> {
    }

    public IdleNextUpViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void startingWith(IdleUpNextPresenter p, AcceptsOneWidget panel) {
        panel.setWidget(this);
    }

    @Override
    public void setBackgroudImage(String string) {
        mainPanelUi.getElement().getStyle().setBackgroundImage(string);
    }

    @Override
    public void setData(ArrayList<Pair<RegattaAndRaceIdentifier, Date>> data) {
        dataPanel.clear();
        for (Pair<RegattaAndRaceIdentifier, Date> race : data) {
            dataPanel
                    .add(new Label(race.getB() + " " + race.getA().getRegattaName() + " " + race.getA().getRaceName()));
        }
    }
}
