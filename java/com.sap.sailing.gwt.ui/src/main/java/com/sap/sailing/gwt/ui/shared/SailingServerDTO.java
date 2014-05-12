package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.dto.NamedDTO;

public class SailingServerDTO extends NamedDTO implements IsSerializable {
	private static final long serialVersionUID = -4209262742778693873L;
	private String url;

    // for GWT
    SailingServerDTO() {}

    public SailingServerDTO(String name, String url) {
        super(name);
        this.url = url;
    }

	public String getUrl() {
		return url;
	}

}
