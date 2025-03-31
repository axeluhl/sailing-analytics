package com.sap.sse.gwt.client.panels;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.place.shared.Place;

/**
 * A panel that represents a vertical tabbed set of pages, each of which contains another widget. Its child widgets are
 * shown as the user selects the various tabs associated with them. The tabs can contain arbitrary text, HTML, or
 * widgets.
 */
public class PlacesAwareVerticalTabLayoutPanel extends VerticalTabLayoutPanel {

    final Place place;
    
    /**
     * Creates an empty vertical tab panel.
     */
    public PlacesAwareVerticalTabLayoutPanel(final Place place, double barHeight, Unit barUnit) {
        super(barHeight, barUnit);
        this.place = place;
    }

  
    public Place getPlace() {
        return place;
    }

}