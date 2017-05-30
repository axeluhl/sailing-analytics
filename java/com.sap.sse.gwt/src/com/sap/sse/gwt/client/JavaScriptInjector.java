package com.sap.sse.gwt.client;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.HeadElement;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.ScriptElement;

/**
 * <p>Utility class to inject JavaScript in the default document.</p>
 * 
 * @author
 *   D049941
 */
public class JavaScriptInjector {
    // TODO: why not simply use com.google.gwt.core.client.ScriptInjector? Is there specific functionality that isn't provided by the GWT class?
    private static HeadElement head;

    /**
     * <p>Injects the given JavaScript code in the head of the default document. To inject the code a script element is
     *   created and appended to the head element of the document.</p>
     * 
     * @param javascript
     *   The JavaScript code to inject.
     */
    public static void inject(String javascript) {
        HeadElement head = getHead();
        ScriptElement element = createScriptElement();
        element.setText(javascript);
        head.appendChild(element);
    }

    private static ScriptElement createScriptElement() {
        Document document = Document.get();
        ScriptElement script = document.createScriptElement();
        script.setAttribute("type", "text/javascript"); //$NON-NLS-1$ //$NON-NLS-2$
        
        return script;
    }

    private static HeadElement getHead() {
        if (JavaScriptInjector.head == null) {
            Document document = Document.get();
            NodeList<Element> nodes = document.getElementsByTagName("head"); //$NON-NLS-1$
            Element element = nodes.getItem(0);
            assert element != null : "HTML Head element required"; //$NON-NLS-1$
            HeadElement head = HeadElement.as(element);
            JavaScriptInjector.head = head;
        }
        
        return JavaScriptInjector.head;
    }
    
    private JavaScriptInjector() {
        super();
    }
}
