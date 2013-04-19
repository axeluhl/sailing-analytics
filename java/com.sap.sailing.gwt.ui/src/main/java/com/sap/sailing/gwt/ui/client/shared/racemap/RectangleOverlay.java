package com.sap.sailing.gwt.ui.client.shared.racemap;

import com.google.gwt.maps.client.MapPane;
import com.google.gwt.maps.client.MapPaneType;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.overlay.Overlay;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.AbsolutePanel;

public class RectangleOverlay extends Overlay {

    private final LatLngBounds bounds;

    private final Rectangle rectangle;

    private final int weight;

    private MapWidget map;

    private MapPane pane;

    private static class Rectangle extends AbsolutePanel {
        public Rectangle() {
          super(DOM.createDiv());
          DOM.setStyleAttribute(getElement(), "borderStyle", "solid");
        }

        public void setBorderColor(String color) {
          DOM.setStyleAttribute(getElement(), "borderColor", color);
        }

        public void setBorderWidth(String width) {
          DOM.setStyleAttribute(getElement(), "borderWidth", width);
        }
      }

    public RectangleOverlay(LatLngBounds bounds, int weight) {
      this.bounds = bounds;
      this.weight = weight;
      rectangle = new Rectangle();
      rectangle.setBorderWidth(weight + "px");
      rectangle.setBorderColor("#00ff00");
    }

    @Override
    protected Overlay copy() {
      return new RectangleOverlay(bounds, weight);
    }

    @Override
    protected void initialize(MapWidget map) {
      this.map = map;
      pane = map.getPane(MapPaneType.MAP_PANE);
      pane.add(rectangle);
    }

    @Override
    protected void redraw(boolean force) {
      // Only set the rectangle's size if the map's size has changed
      if (!force) {
        return;
      }

      Point sw = map.convertLatLngToDivPixel(bounds.getSouthWest());
      Point ne = map.convertLatLngToDivPixel(bounds.getNorthEast());
      pane.setWidgetPosition(rectangle, Math.min(sw.getX(), ne.getX()),
          Math.min(sw.getY(), ne.getY()));

      int width = Math.abs(ne.getX() - sw.getX()) - weight;
      int height = Math.abs(ne.getY() - sw.getY()) - weight;
      rectangle.setSize(width + "px", height + "px");
    }

    @Override
    protected void remove() {
      rectangle.removeFromParent();
    }
  }
