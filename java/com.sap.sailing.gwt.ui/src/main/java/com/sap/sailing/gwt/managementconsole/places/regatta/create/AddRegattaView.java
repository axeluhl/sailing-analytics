package com.sap.sailing.gwt.managementconsole.places.regatta.create;

import com.google.gwt.user.client.ui.RequiresResize;
import com.sap.sailing.gwt.managementconsole.mvp.View;

public interface AddRegattaView extends View<AddRegattaView.Presenter>, RequiresResize {

    interface Presenter extends com.sap.sailing.gwt.managementconsole.mvp.Presenter {
        void cancelAddRegatta();
        void addRegatta();
    }

}
