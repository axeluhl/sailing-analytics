package com.sap.sse.gwt.client.formfactor;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.HeadElement;
import com.google.gwt.dom.client.MetaElement;
import com.sap.sse.common.Util;

/**
 * Utility class to add meta tags to the {@link Document#getHead() document head}.
 */
public abstract class MetaTagUtil {
    
    private static final String TAG_NAME_VIEWPORT = "viewport";
    
    private static final String CONTENT_SEPARATOR = ",";
    private static final String CONTENT_PROPERTY_VALUE_SEPARATOR = "=";
    
    private static final String CONTENT_PROPERTY_WIDTH = "width";
    private static final String CONTENT_PROPERTY_INITIAL_SCALE = "initial-scale";
    private static final String CONTENT_PROPERTY_MAXIMUM_SCALE = "maximum-scale";

    public static final String WIDTH_VALUE_DEVICE_WIDTH = "device-width";
    public static final String SCALE_VALUE_SMALL = "0.5";
    public static final String SCALE_VALUE_SMALLER = "0.75";
    public static final String SCALE_VALUE_NORMAL = "1";
    public static final String SCALE_VALUE_LARGE = "2";
    
    /**
     * Appends a {@code viewport} meta tag to the {@link HeadElement head} using {@code width=device-width}.  
     * 
     * @param initialScaleValue value to use for meta tag content property {@code initial-scale}
     * @param maximumScaleValue value to use for meta tag content property {@code maximum-scale}
     * 
     * @see MetaTagUtil#setViewport(String, String, String)
     */
    public static void setViewportToDeviceWidth(String initialScaleValue, String maximumScaleValue) {
        MetaTagUtil.setViewport(WIDTH_VALUE_DEVICE_WIDTH, initialScaleValue, maximumScaleValue);
    }
    
    /**
     * Appends a {@code viewport} meta tag to the {@link HeadElement head}.  
     * 
     * @param widthValue value to use for meta tag content property {@code width} 
     * @param initialScaleValue value to use for meta tag content property {@code initial-scale}
     * @param maximumScaleValue value to use for meta tag content property {@code maximum-scale}
     */
    public static void setViewport(String widthValue, String initialScaleValue, String maximumScaleValue) {
        String width = content(CONTENT_PROPERTY_WIDTH, widthValue);
        String initialScale = content(CONTENT_PROPERTY_INITIAL_SCALE, initialScaleValue);
        String maximumScale = content(CONTENT_PROPERTY_MAXIMUM_SCALE, maximumScaleValue);
        appendMetaElement(TAG_NAME_VIEWPORT, Util.join(CONTENT_SEPARATOR, width, initialScale, maximumScale));
    }
    
    private static String content(String property, String value) {
        return property + CONTENT_PROPERTY_VALUE_SEPARATOR + value;
    }

    private static void appendMetaElement(String name, String content) {
        MetaElement metaElement = Document.get().createMetaElement();
        metaElement.setName(name);
        metaElement.setContent(content);
        Document.get().getHead().appendChild(metaElement);
    }
}
