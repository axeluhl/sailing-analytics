package com.sap.sailing.gwt.ui.client;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.HeadElement;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.ScriptElement;

public class JavaScriptInjector {
    private static HeadElement head;

    public static void inject(String javascript) {
        HeadElement head = getHead();
        ScriptElement element = createScriptElement();
        element.setText(javascript);
        head.appendChild(element);
    }

    private static ScriptElement createScriptElement() {
        Document document = Document.get();
        ScriptElement script = document.createScriptElement();
        script.setAttribute("type", "text/javascript");
        
        return script;
    }

    private static HeadElement getHead() {
        if (head == null) {
            Document document = Document.get();
            NodeList<Element> nodes = document.getElementsByTagName("head");
            Element element = nodes.getItem(0);
            assert element != null : "HTML Head element required";
            HeadElement head = HeadElement.as(element);
            JavaScriptInjector.head = head;
        }
        
        return JavaScriptInjector.head;
    }
    
    private JavaScriptInjector() {
        super();
    }
}
