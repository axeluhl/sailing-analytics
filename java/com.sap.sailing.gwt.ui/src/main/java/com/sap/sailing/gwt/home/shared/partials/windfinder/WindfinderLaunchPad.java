package com.sap.sailing.gwt.home.shared.partials.windfinder;

import java.util.function.BiFunction;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.windfinder.SpotDTO;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

class WindfinderLaunchPad extends Widget {

    private final DivElement container = Document.get().createDivElement();
    private final BiFunction<SpotDTO, TimePoint, String> windfinderUrlFactory;
    private final PopupPanel parent;

    WindfinderLaunchPad(Iterable<SpotDTO> windfinderSpots,
            BiFunction<SpotDTO, TimePoint, String> windfinderUrlFactory, PopupPanel parent) {
        this.container.addClassName(WindfinderResources.INSTANCE.css().windfinderlaunchpad_content());
        this.windfinderUrlFactory = windfinderUrlFactory;
        this.parent = parent;
        windfinderSpots.forEach(this::addItem);
        setElement(container);
        sinkEvents(Event.ONCLICK);
    }

    @Override
    public void onBrowserEvent(final Event event) {
        if (event.getTypeInt() == Event.ONCLICK) {
            parent.hide();
        }
        super.onBrowserEvent(event);
    }

    private void addItem(SpotDTO windfinderSpot) {
        final String url = windfinderUrlFactory.apply(windfinderSpot, MillisecondsTimePoint.now());
        this.container.appendChild(new WindfinderLaunchPadItem(windfinderSpot.getName(), url).getElement());
    }

}
