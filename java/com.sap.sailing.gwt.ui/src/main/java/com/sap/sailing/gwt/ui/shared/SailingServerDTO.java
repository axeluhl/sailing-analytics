package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.dto.NamedDTO;

public class SailingServerDTO extends NamedDTO implements IsSerializable {
	private static final long serialVersionUID = -4209262742778693873L;
	private String url;
	private List<EventDTO> events;
	
    // for GWT
    SailingServerDTO() {}

    public SailingServerDTO(String name, String url) {
        super(name);
        this.url = url;
        this.events = null;
    }

    public SailingServerDTO(String name, String url, List<EventDTO> events) {
        super(name);
        this.url = url;
        this.events = events;
    }

	public String getUrl() {
		return url;
	}

	public List<EventDTO> getEvents() {
		return events;
	}

}
