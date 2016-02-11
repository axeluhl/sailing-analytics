package com.sap.sailing.gwt.home.shared.places.imprint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.places.imprint.data.ComponentData;
import com.sap.sailing.gwt.home.shared.places.imprint.data.LicenseData;

public class ImprintViewImpl extends Composite implements ImprintView {
    private static SponsoringPageViewUiBinder uiBinder = GWT.create(SponsoringPageViewUiBinder.class);

    interface SponsoringPageViewUiBinder extends UiBinder<Widget, ImprintViewImpl> {
    }

    private Presenter currentPresenter;

    @UiField(provided = true)
    ValueListBox<ComponentData> componentListUi;
    @UiField
    Element ownerUi;
    @UiField
    Element nameUi;
    @UiField
    AnchorElement homepageUi;
    @UiField
    DivElement acknowledgmentUi;
    @UiField
    DivElement licenseTextUi;

    public ImprintViewImpl() {
        super();
        componentListUi = new ValueListBox<ComponentData>(new Renderer<ComponentData>() {
            @Override
            public String render(ComponentData c) {
                if (c == null) {
                    return "--";
                } else {
                    return c.getName() + ", v." + c.getVersion();
                }
            }

            @Override
            public void render(ComponentData c, Appendable appendable) throws IOException {
                if (c == null) {
                    appendable.append("--");
                } else {
                    appendable.append(c.getName() + ", v." + c.getVersion());
                }
            }
        });

        initWidget(uiBinder.createAndBindUi(this));
        componentListUi.addValueChangeHandler(new ValueChangeHandler<ComponentData>() {
            @Override
            public void onValueChange(ValueChangeEvent<ComponentData> event) {
                if (currentPresenter != null) {
                    currentPresenter.didSelect(event.getValue());
                }
          }
        });
    }

    @Override
    public void registerPresenter(Presenter currentPresenter) {
        this.currentPresenter = currentPresenter;
    }


    @Override
    public void showComponents(final ComponentData[] components) {

        if (components == null || components.length == 0) {
            componentListUi.setVisible(false);
            componentListUi.setAcceptableValues(new ArrayList<ComponentData>());
            ownerUi.setInnerText("");
            nameUi.setInnerText("--");
            homepageUi.getStyle().setVisibility(Style.Visibility.HIDDEN);
            acknowledgmentUi.getStyle().setVisibility(Style.Visibility.HIDDEN);
            licenseTextUi.getStyle().setVisibility(Style.Visibility.HIDDEN);
        } else {
            componentListUi.setValue(null);
            componentListUi.setAcceptableValues(Arrays.asList(components));
            componentListUi.setVisible(true);
            ownerUi.setInnerText("--");
            nameUi.setInnerText("");
            homepageUi.getStyle().setVisibility(Style.Visibility.VISIBLE);
            acknowledgmentUi.getStyle().setVisibility(Style.Visibility.VISIBLE);
            licenseTextUi.getStyle().setVisibility(Style.Visibility.VISIBLE);
            componentListUi.setValue(components[0], true);
        }
    }

    @Override
    public void showComponents(ComponentData component, LicenseData license) {
        nameUi.setInnerText(component.getName() + ", v." + component.getVersion());
        ownerUi.setInnerText(component.getOwner());
        homepageUi.setHref(component.getHomepage());
        homepageUi.setInnerText(component.getHomepage());
        SafeHtmlBuilder ackSB = new SafeHtmlBuilder();
        for (String ack : component.getAcknowledgements()) {
            ackSB.appendEscaped(ack);
            ackSB.appendHtmlConstant("<br />");
        }
        acknowledgmentUi.setInnerSafeHtml(ackSB.toSafeHtml());
        licenseTextUi.setInnerText("");
    }

    @Override
    public void resetComponent() {
        ownerUi.setInnerText("--");
        nameUi.setInnerText("");
        homepageUi.setInnerText("");
        acknowledgmentUi.setInnerText("");
        licenseTextUi.setInnerText("");
    }

    @Override
    public void showLicenseText(String text) {
        SafeHtmlBuilder sb = new SafeHtmlBuilder();
        sb.appendEscapedLines(text);
        licenseTextUi.setInnerSafeHtml(sb.toSafeHtml());
    }

}
