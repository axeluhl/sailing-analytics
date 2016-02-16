package com.sap.sailing.gwt.home.shared.places.imprint;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.places.imprint.data.ComponentData;
import com.sap.sailing.gwt.home.shared.places.imprint.data.LicenseData;

public interface ImprintView {
    Widget asWidget();

    void registerPresenter(Presenter p);

    interface Presenter {
        void didSelect(ComponentData selectedObject);
    }

    void showComponents(ComponentData[] components);

    void resetComponent();

    void showComponents(ComponentData component, LicenseData license);

    void showLicenseText(String text);
}
