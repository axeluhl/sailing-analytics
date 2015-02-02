package com.sap.sailing.gwt.home.client.place.whatsnew;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.common.client.i18n.TextMessages;

public class WhatsNewActivity extends AbstractActivity {
    private final WhatsNewPlace place;
    private final WhatsNewClientFactory clientFactory;
    
    public WhatsNewActivity(WhatsNewPlace place, WhatsNewClientFactory clientFactory) {
        this.place = place;
        this.clientFactory = clientFactory;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        WhatsNewView whatsNewView = clientFactory.createWhatsNewView(place.getNavigationTab());
        panel.setWidget(whatsNewView.asWidget());
        Window.setTitle(TextMessages.INSTANCE.sapSailing() + " - " + TextMessages.INSTANCE.whatsNew());
    }
}
