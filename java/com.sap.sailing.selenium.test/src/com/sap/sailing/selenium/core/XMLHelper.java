package com.sap.sailing.selenium.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <p>Utility class for working with the Java representation of the Document Object Model.</p>
 * 
 * @author
 *   D049941
 */
public class XMLHelper {
    /**
     * <p></p>
     * 
     * @param element
     *   
     * @param name
     *   
     * @param uri
     *   
     * @return
     *   
     */
    public static String getAttributeValueNS(Element element, String name, String uri) {
        return getAttributeValueNS(element, name, uri, null);
    }
    
    /**
     * <p></p>
     * 
     * @param element
     * @param name
     * @param uri
     * @param defaultValue
     * @return
     */
    public static String getAttributeValueNS(Element element, String name, String uri, String defaultValue) {
        Attr attribute = element.getAttributeNodeNS(uri, name);
        
        if(attribute != null)
            return attribute.getValue();
        
        return defaultValue;
    }

    /**
     * <p></p>
     * 
     * @param element
     *   
     * @return
     *   
     */
    public static String getContentText(Element element) {
        if(element == null)
            return null;
                
        return element.getTextContent();
    }
    
    /**
     * <p></p>
     * 
     * @param element
     *   
     * @param name
     *   
     * @param uri
     *   
     * @return
     *   
     */
    public static String getContentTextNS(Element element, String name, String uri) {
        return getContentTextNS(element, name, uri, null);
    }
    
    /**
     * <p></p>
     * 
     * @param element
     *   
     * @param name
     *   
     * @param uri
     *   
     * @param defaultValue
     *   
     * @return
     *   
     */
    public static String getContentTextNS(Element element, String name, String uri, String defaultValue) {
        Element child = getElementNS(element, name, uri);
        
        if(child != null)
            return getContentText(child);
        
        return defaultValue;
    }
    
    /**
     * <p></p>
     * 
     * @param element
     *   
     * @param name
     *   
     * @param uri
     *   
     * @return
     *   
     */
    public static Element getElementNS(Element element, String name, String uri) {
        if(element == null || !element.hasChildNodes())
            return null;
        
        for(Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
            if(child.getNodeType() != Node.ELEMENT_NODE)
                continue;
            
            if(matches(child, name, uri))
                return (Element) child;
        }
        
        return null;
    }
    
    /**
     * <p></p>
     * 
     * @param element
     *   
     * @param name
     *   
     * @return
     *   
     */
    public static List<Element> getElements(Element element, String name) {
        return getElementsNS(element, name, null);
    }
    
    /**
     * <p></p>
     * 
     * @param element
     *   
     * @param name
     *   
     * @param uri
     *   
     * @return
     *   
     */
    public static List<Element> getElementsNS(Element element, String name, String uri) {
        if(element == null || !element.hasChildNodes())
            return Collections.emptyList();

        List<Element> elements = new ArrayList<>();
        
        for(Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
            if(child.getNodeType() == Node.ELEMENT_NODE) {
                if(matches(child, name, uri))
                    elements.add((Element) child);
            }
        }
        
        return elements;
    }
    
    private static boolean matches(Node node, String name, String uri) {
        String tagName = node.getLocalName();
        String namespaceUri = node.getNamespaceURI();
        
        if(!name.equals(tagName))
            return false;
        
        if(uri == null)
            return true;
        
        if(!uri.equals(namespaceUri))
            return false;
        
        return true;
    }
}
