package com.sap.sailing.gwt.home.shared.places.imprint;

import java.util.HashMap;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.shared.places.imprint.data.ComponentData;
import com.sap.sailing.gwt.home.shared.places.imprint.data.ImprintData;
import com.sap.sailing.gwt.home.shared.places.imprint.data.LicenseData;

public class ImprintActivity extends AbstractActivity implements ImprintView.Presenter {
    private ImprintData imprintData;
    private ImprintView view;
    private HashMap<String, LicenseData> licenseMap = new HashMap<String, LicenseData>();

    public ImprintActivity(ImprintPlace place) {
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        view = new ImprintViewImpl();
        view.registerPresenter(this);
        panel.setWidget(view.asWidget());
        RequestBuilder rb = new RequestBuilder(RequestBuilder.GET, GWT.getHostPageBaseURL() + "imprint.json");
        rb.setCallback(new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                try {
                    JSONValue jsonValue = JSONParser.parseStrict(response.getText());
                    JSONObject obj;
                    if ((obj = jsonValue.isObject()) != null) {
                        ImprintData data = (ImprintData) (obj.getJavaScriptObject());
                        imprintData = data;
                        for (LicenseData license : data.getLicenses()) {
                            licenseMap.put(license.getKey(), license);
                        }
                        view.showComponents(imprintData.getComponents());
                    }
                } catch (Exception e) {
                    GWT.log("Error parsing and casting data", e);
                }
            }

            @Override
            public void onError(Request request, Throwable exception) {
                GWT.log("Request error " + exception.getMessage());
            }
        });
        try {
            rb.send();
        } catch (RequestException e) {
            GWT.log("Request error " + e.getMessage());
        }
    }

    @Override
    public void didSelect(ComponentData selectedComponent) {
        if (selectedComponent == null) {
            view.resetComponent();
        } else {
            LicenseData licenseData = licenseMap.get(selectedComponent.getKey());
            view.showComponents(selectedComponent, licenseData);
            if (licenseData != null) {
                String licenseUrl = GWT.getHostPageBaseURL() + "licenses/" + licenseData.getFile();
                RequestBuilder rb = new RequestBuilder(RequestBuilder.GET, licenseUrl);
                rb.setCallback(new RequestCallback() {
                    @Override
                    public void onResponseReceived(Request request, Response response) {
                        view.showLicenseText(response.getText());
                    }

                    @Override
                    public void onError(Request request, Throwable exception) {
                    }
                });
                try {
                    rb.send();
                } catch (RequestException e) {
                    GWT.log("Request error " + e.getMessage());
                }
            }
        }
    }
}
