package com.sap.sailing.gwt.ui.shared.racemap;

import com.google.gwt.ajaxloader.client.Properties;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.maps.client.events.MapEvent;
import com.google.gwt.maps.client.events.MapEventFormatter;
import com.google.gwt.maps.client.events.MapHandler;

/**
 * An utility class for the registration of native DOM events for custom map elements like a canvas.
 * To receive native DOM event you must put the custom elements into the OverlayMouseTarget layer of the google map.
 * @author Frank
 *
 */
public class DomMapHandlerRegistration {
    @SuppressWarnings("rawtypes")
    public static <E extends MapEvent> HandlerRegistration addDomHandler(JavaScriptObject jso, DomEvent.Type<?> eventType,
            MapHandler<E> handler, MapEventFormatter<E> formatter, boolean capture) {
        final JavaScriptObject listener = addDomHandlerImpl(jso, eventType.getName(), handler, formatter, capture);
        HandlerRegistration registration = new HandlerRegistration() {
            @Override
            public void removeHandler() {
                removeHandlerImpl(listener);
            }
        };
        return registration;
    }
    
    @SuppressWarnings("rawtypes")
    private static native <E extends MapEvent> JavaScriptObject addDomHandlerImpl(JavaScriptObject jso, String eventName,
        MapHandler<E> handler, MapEventFormatter<E> formatter, boolean capture) /*-{
        var callback = function(event) {
        $entry(@com.sap.sailing.gwt.ui.shared.racemap.DomMapHandlerRegistration::onCallback(Lcom/google/gwt/maps/client/events/MapHandler;Lcom/google/gwt/ajaxloader/client/Properties;Lcom/google/gwt/maps/client/events/MapEventFormatter;)(handler, event, formatter));
      };
        return $wnd.google.maps.event.addDomListener(jso, eventName, callback, capture);
    }-*/;

    @SuppressWarnings("rawtypes")
    protected static <E extends MapEvent> void onCallback(final MapHandler<E> handler, final Properties properties, final MapEventFormatter<E> formatter) {
        try {
            E event = formatter.createEvent(properties);
            handler.onEvent(event);
        } catch (Throwable x) {
            GWT.getUncaughtExceptionHandler().onUncaughtException(x);
        }
    }
    
    private static native void removeHandlerImpl(JavaScriptObject listener) /*-{
      $wnd.google.maps.event.removeListener(listener);
    }-*/;
    
//
//    public static HandlerRegistration addMouseMoveDomListener(JavaScriptObject jso, MouseMoveMapHandler handler, boolean capture) {
//        final JavaScriptObject listener = addNativeMouseMoveDomListener(jso, MouseMoveEvent.getType().getName(), handler, capture);
//        HandlerRegistration registration = new HandlerRegistration() {
//          @Override
//          public void removeHandler() {
//            removeHandlerImpl(listener);
//          }
//        };
//        return registration;
//    }
//
//    public static HandlerRegistration addMouseOutDomListener(JavaScriptObject jso, MouseOutMapHandler handler, boolean capture) {
//        final JavaScriptObject listener = addNativeMouseOutDomListener(jso, MouseOutEvent.getType().getName(), handler, capture);
//        HandlerRegistration registration = new HandlerRegistration() {
//          @Override
//          public void removeHandler() {
//            removeHandlerImpl(listener);
//          }
//        };
//        return registration;
//    }
//
//    public static HandlerRegistration addMouseOverDomListener(JavaScriptObject jso, MouseOverMapHandler handler, boolean capture) {
//        final JavaScriptObject listener = addNativeMouseOverDomListener(jso, MouseOverEvent.getType().getName(), handler, capture);
//        HandlerRegistration registration = new HandlerRegistration() {
//          @Override
//          public void removeHandler() {
//            removeHandlerImpl(listener);
//          }
//        };
//        return registration;
//    }
//
//    public static HandlerRegistration addClickDomListener(JavaScriptObject jso, ClickMapHandler handler, boolean capture) {
//        final JavaScriptObject listener = addNativeClickDomListener(jso, ClickEvent.getType().getName(), handler, capture);
//        HandlerRegistration registration = new HandlerRegistration() {
//          @Override
//          public void removeHandler() {
//            removeHandlerImpl(listener);
//          }
//        };
//        return registration;
//    }
//
//    private static native JavaScriptObject addNativeMouseMoveDomListener(JavaScriptObject object, String eventName, MouseMoveMapHandler handler, boolean capture) /*-{
//        var callback = function(event) {
//          @com.sap.sailing.gwt.ui.shared.racemap.DomMapHandlerRegistration::addMouseMoveDomListenerImpl(Lcom/google/gwt/ajaxloader/client/Properties;Lcom/google/gwt/maps/client/events/mousemove/MouseMoveMapHandler;)(event, handler)
//        };
//        return $wnd.google.maps.event.addDomListener(object, eventName, callback, capture);
//      }-*/;
//
//    private static native JavaScriptObject addNativeMouseOutDomListener(JavaScriptObject object, String eventName, MouseOutMapHandler handler, boolean capture) /*-{
//        var callback = function(event) {
//          @com.sap.sailing.gwt.ui.shared.racemap.DomMapHandlerRegistration::addMouseOutDomListenerImpl(Lcom/google/gwt/ajaxloader/client/Properties;Lcom/google/gwt/maps/client/events/mouseout/MouseOutMapHandler;)(event, handler)
//        };
//        return $wnd.google.maps.event.addDomListener(object, eventName, callback, capture);
//      }-*/;
//
//    private static native JavaScriptObject addNativeMouseOverDomListener(JavaScriptObject object, String eventName, MouseOverMapHandler handler, boolean capture) /*-{
//        var callback = function(event) {
//          @com.sap.sailing.gwt.ui.shared.racemap.DomMapHandlerRegistration::addMouseOverDomListenerImpl(Lcom/google/gwt/ajaxloader/client/Properties;Lcom/google/gwt/maps/client/events/mouseover/MouseOverMapHandler;)(event, handler)
//        };
//        return $wnd.google.maps.event.addDomListener(object, eventName, callback, capture);
//      }-*/;
//    
//    private static native JavaScriptObject addNativeClickDomListener(JavaScriptObject object, String eventName, ClickMapHandler handler, boolean capture) /*-{
//        var callback = function(event) {
//          @com.sap.sailing.gwt.ui.shared.racemap.DomMapHandlerRegistration::addClickDomListenerImpl(Lcom/google/gwt/ajaxloader/client/Properties;Lcom/google/gwt/maps/client/events/click/ClickMapHandler;)(event, handler)
//        };
//        return $wnd.google.maps.event.addDomListener(object, eventName, callback, capture);
//      }-*/;
//    
//    private static void addMouseMoveDomListenerImpl(Properties properties, MouseMoveMapHandler handler) {
//        MouseMoveEventFormatter formatter = new MouseMoveEventFormatter();
//        handler.onEvent(formatter.createEvent(properties));
//    }
//
//    private static void addMouseOutDomListenerImpl(Properties properties, MouseOutMapHandler handler) {
//        MouseOutEventFormatter formatter = new MouseOutEventFormatter();
//        handler.onEvent(formatter.createEvent(properties));
//    }
//
//    private static void addMouseOverDomListenerImpl(Properties properties, MouseOverMapHandler handler) {
//        MouseOverEventFormatter formatter = new MouseOverEventFormatter();
//        handler.onEvent(formatter.createEvent(properties));
//    }
//
//    private static void addClickDomListenerImpl(Properties properties, ClickMapHandler handler) {
//        ClickEventFormatter formatter = new ClickEventFormatter();
//        handler.onEvent(formatter.createEvent(properties));
//    }
//    

}
