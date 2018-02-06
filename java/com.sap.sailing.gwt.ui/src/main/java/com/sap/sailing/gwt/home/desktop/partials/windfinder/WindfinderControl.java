package com.sap.sailing.gwt.home.desktop.partials.windfinder;

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
import com.sap.sse.common.TimePoint;

public class WindfinderControl extends Widget {

    interface WindfinderLaunchPadItemUiBinder extends UiBinder<Element, WindfinderControl> {
    }

    private static WindfinderLaunchPadItemUiBinder uiBinder = GWT.create(WindfinderLaunchPadItemUiBinder.class);

    @UiField
    DivElement labelUi;
    private final WindfinderLaunchPadController launchPadController;
    private Iterable<SpotDTO> spots;

    public WindfinderControl(Iterable<SpotDTO> spots, BiFunction<SpotDTO, TimePoint, String> windfinderUrlFactory) {
        WindfinderResources.INSTANCE.css().ensureInjected();
        this.launchPadController = new WindfinderLaunchPadController(windfinderUrlFactory);
        this.spots = spots;
        setElement(uiBinder.createAndBindUi(this));
        sinkEvents(Event.ONCLICK);
    }

    @Override
    public void onBrowserEvent(Event event) {
        // Only handle click events
        if (event.getTypeInt() != Event.ONCLICK) {
            return;
        }

        // If rendered as direct link button, open link in new tab directly instead of showing the menu popup
        if (launchPadController.renderAsDirectLink(spots)) {
            Window.open(launchPadController.getDirectLinkUrl(spots), "_blank", "");
            return;
        }

        launchPadController.showWindfinderLaunchPad(spots, this.getElement());
    }
}
