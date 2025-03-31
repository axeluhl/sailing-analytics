package com.sap.sailing.gwt.autoplay.client.places.screens.preevent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.utils.Countdown.RemainingTime;
import com.sap.sailing.gwt.home.shared.utils.Countdown.Unit;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class IdlePreEventViewImpl extends Composite implements IdlePreEventNextView {
    private static IdlePreEventUpViewImplUiBinder uiBinder = GWT.create(IdlePreEventUpViewImplUiBinder.class);
    @UiField
    SimplePanel mainPanelUi;
    @UiField
    HTMLPanel dataPanel;

    interface Dummy extends ClientBundle {
        ImageResource dummy();
    }

    interface IdlePreEventUpViewImplUiBinder extends UiBinder<Widget, IdlePreEventViewImpl> {
    }

    public IdlePreEventViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void startingWith(IdlePreEventNextPresenter p, AcceptsOneWidget panel) {
        panel.setWidget(this);
    }

    @Override
    public void setBackgroudImage(String string) {
        mainPanelUi.getElement().getStyle().setProperty("backgroundPosition", "center center");
        mainPanelUi.getElement().getStyle().setBackgroundImage(string);
    }

    @Override
    public void setStartIn(RemainingTime untilStart) {
        dataPanel.clear();
        // do not be too specific, as this might expose differenes in event reloading, live delay and other smaller time
        // differences
        if (untilStart.unit == Unit.SECONDS) {
            dataPanel.add(new IdlePreEventEntry(StringMessages.INSTANCE.countDownEnd()));
        } else {
            dataPanel.add(
                    new IdlePreEventEntry(StringMessages.INSTANCE.countDown(untilStart.value, untilStart.unitI18n())));
        }
    }
}
