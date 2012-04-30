package com.sap.sailing.xcelsiusadapter;


import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;


import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jdom.Document;
import org.jdom.Element;
import org.w3c.dom.Node;


import com.sap.sailing.domain.base.*;

import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;

import com.sap.sailing.domain.common.TimePoint;

import com.sap.sailing.domain.tracking.*;

import com.sap.sailing.server.RacingEventService;

public class EventList extends Action {
	public EventList(HttpServletRequest req, HttpServletResponse res, RacingEventService service, int maxRows) {
		super(req, res, service, maxRows);
	}

	public void perform() throws Exception {	
		 final Document table = getTable("data");
		
		
		for(Event event : getEvents().values()){
			if(event == null){ 
	        	continue; 
	        }
			
			
			addRow();
			addColumn(event.getName());
	        
		}
		say(table);// output doc to client
       
        
            
           
        
	} // function end
	
	

	private void addNamedElementWithValue(Element parent, String newChildName, Integer i) {
		if(i == null){
			addNamedElementWithValue(parent,newChildName, "0");
		}else{
			addNamedElementWithValue(parent,newChildName, i.toString());
		}
		
	}

	private void addNamedElementWithValue(Element parent, String newChildName, Double dbl) {
		if(dbl == null){
			addNamedElementWithValue(parent,newChildName, "0");
		}else{
			addNamedElementWithValue(parent,newChildName, dbl.toString());
		}
		
	}
	
	private void addNamedElementWithValue(Element parent, String newChildName, Long l) {
		if(l == null){
			addNamedElementWithValue(parent,newChildName, "0");
		}else{
			addNamedElementWithValue(parent,newChildName, l.toString());
		}
		
	}

	private Element addNamedElement(Document doc, String newChildName) {
		final Element newChild = new Element(newChildName);
		doc.addContent(newChild);
		return newChild;
	}

	private Element addNamedElementWithValue(Element parent, String newChildName, String value){
		final Element newChild = new Element(newChildName);
        newChild.addContent(value);
        parent.addContent(newChild);
        return newChild;
	}
	
	private Element addNamedElement(Element parent, String newChildName){
		 final Element newChild = new Element(newChildName);
         parent.addContent(newChild);
         return newChild;
	}
	
	
}
