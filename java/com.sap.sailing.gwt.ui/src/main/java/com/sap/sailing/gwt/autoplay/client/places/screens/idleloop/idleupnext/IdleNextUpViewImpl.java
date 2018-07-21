package com.sap.sailing.gwt.autoplay.client.places.screens.idleloop.idleupnext;

import java.util.ArrayList;
import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
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
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Util.Pair;

public class IdleNextUpViewImpl extends Composite implements IdleUpNextView {
    private static final int MAX_RACES_IN_LIST = 8;
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
        ensureDebugId("IdleNextUpView");
    }

    @Override
    public void startingWith(IdleUpNextPresenter p, AcceptsOneWidget panel) {
        panel.setWidget(this);
    }

    @Override
    public void setBackgroudImage(String string) {
        mainPanelUi.getElement().getStyle().setProperty("backgroundPosition", "center center");
        mainPanelUi.getElement().getStyle().setBackgroundImage(string);
    }

    @Override
    public void setData(ArrayList<Pair<RegattaAndRaceIdentifier, Date>> data) {
        dataPanel.clear();
        if (data == null) {
            Label lbl = new Label(StringMessages.INSTANCE.noUpcomingRaceDataAvailable());
            lbl.ensureDebugId("upComingDataLabel");
            dataPanel.add(lbl);
        } else if (data.isEmpty()) {
            Label lbl = new Label(StringMessages.INSTANCE.nothingUpcoming());
            lbl.ensureDebugId("upComingDataLabel");
            dataPanel.add(lbl);
        } else {
            Label lbl = new Label(StringMessages.INSTANCE.upcoming());
            lbl.ensureDebugId("upComingDataLabel");
            dataPanel.add(lbl);
            int index = 1;
            for (Pair<RegattaAndRaceIdentifier, Date> race : data) {
                if (index > MAX_RACES_IN_LIST) {
                    GWT.log("Count of races" + index);
                    return;
                }
                String formatedDate;
                index++;
                DateTimeFormat simpleFormat = DateTimeFormat.getFormat("HH:mm");
                formatedDate = simpleFormat.format(race.getB());
                String raceName = race.getA().getRaceName();
                dataPanel.add(new NextUpEntry(formatedDate, raceName));
            }
        }
    }
}
