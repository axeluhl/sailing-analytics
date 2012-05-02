package com.sap.sailing.xcelsiusadapter;



import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.jdom.Document;




import com.sap.sailing.domain.base.*;

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
	
		
	
}
