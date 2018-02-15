package com.sap.sailing.gwt.home.shared.partials.windfinder;

import java.util.function.BiFunction;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.windfinder.SpotDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.TimePoint;

public class WindfinderControl extends Widget {

    interface WindfinderLaunchPadItemUiBinder extends UiBinder<Element, WindfinderControl> {
    }

    private static WindfinderLaunchPadItemUiBinder uiBinder = GWT.create(WindfinderLaunchPadItemUiBinder.class);

    @UiField WindfinderResources local_res;
    @UiField DivElement labelUi, iconUi;
    private final WindfinderLaunchPadController launchPadController;
    private Iterable<SpotDTO> spotData;

    public WindfinderControl(Iterable<SpotDTO> spotData, BiFunction<SpotDTO, TimePoint, String> windfinderUrlFactory) {
        this(windfinderUrlFactory);
        this.setSpotData(spotData);
    }

    public WindfinderControl(BiFunction<SpotDTO, TimePoint, String> windfinderUrlFactory) {
        WindfinderResources.INSTANCE.css().ensureInjected();
        this.launchPadController = new WindfinderLaunchPadController(windfinderUrlFactory);
        setElement(uiBinder.createAndBindUi(this));
        this.labelUi.setInnerHTML(StringMessages.INSTANCE.windFinderWeatherData());
        this.iconUi.setInnerHTML(local_res.windfinderLogo().getText());
        sinkEvents(Event.ONCLICK);
        this.setVisible(false);
    }
    
    public void setSpotData(Iterable<SpotDTO> spotData) {
        this.spotData = spotData;
        this.setVisible(true);
    }

    @Override
    public void onBrowserEvent(Event event) {
        // Only handle click events
        if (event.getTypeInt() != Event.ONCLICK) {
            return;
        }

        // If rendered as direct link button, open link in new tab directly instead of showing the menu popup
        if (launchPadController.renderAsDirectLink(spotData)) {
            Window.open(launchPadController.getDirectLinkUrl(spotData), "_blank", "");
            return;
        }

        launchPadController.showLaunchPad(spotData, this.getElement());
    }
}
