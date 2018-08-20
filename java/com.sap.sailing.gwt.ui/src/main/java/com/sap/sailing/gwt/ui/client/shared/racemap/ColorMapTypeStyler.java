package com.sap.sailing.gwt.ui.client.shared.racemap;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.maps.client.controls.MapTypeStyle;
import com.google.gwt.maps.client.maptypes.MapTypeStyler;

/**
 * A {@code color} styler for Google Maps that can use an RGB color code to set an absolute color. This is an
 * alternative to the rather complicated hue/saturation/lightness model that is relative to Google's base values which
 * may change without notice and which are difficult to map to from an RGB color.
 * <p>
 * 
 * To use together with {@link MapTypeStyle#setStylers(MapTypeStyler[])}, {@link JavaScriptObject#cast()} an object of
 * this type to type {@link MapTypeStyler} and place it as an element into an array of type {@link MapTypeStyler}.<p>
 * 
 * See <a href= "https://developers.google.com/maps/documentation/javascript/reference#MapTypeStyler" >MapTypeStyler API
 * Doc</a>
 * 
 * @author Axel Uhl (D043530)
 */
public class ColorMapTypeStyler extends JavaScriptObject {
    /**
     * A styler affects how a map's elements will be styled. Each ColorMapTypeStyler should contain one and only one key. If
     * more than one key is specified in a single ColorMapTypeStyler, all but one will be ignored. For example: var rule =
     * {color: '#ff0000'}. use newInstance();
     */
    protected ColorMapTypeStyler() {
    }

    public final static ColorMapTypeStyler newColorStyler(String color) {
        ColorMapTypeStyler styler = JavaScriptObject.createObject().cast();
        styler.setColor(color);
        return styler;
      }

    /**
     * sets Sets the color of the feature to match the hue of the color supplied.
     */
    private final native void setColor(String color) /*-{
      this.color = color;
    }-*/;

    public final native String getColor() /*-{
	return this.color;
    }-*/;
}
