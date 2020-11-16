package com.sap.sailing.gwt.managementconsole.mvp;

import com.google.gwt.user.client.ui.IsWidget;

public interface View<P extends Presenter> extends IsWidget {

    void setPresenter(P presenter);

}
