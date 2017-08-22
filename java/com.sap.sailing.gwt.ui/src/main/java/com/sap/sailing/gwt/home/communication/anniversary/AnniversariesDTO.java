package com.sap.sailing.gwt.home.communication.anniversary;

import java.util.Collection;

import com.sap.sse.gwt.dispatch.shared.commands.DTO;
import com.sap.sse.gwt.dispatch.shared.commands.ListResult;

/**
 * This class is used to transmit the current anniversary state to the frontend. It is used to highlight anniversaries
 * that will happen soon, or that did happen a few days ago prominently.
 */
public class AnniversariesDTO extends ListResult<AnniversaryDTO> implements DTO {

    public AnniversariesDTO() {
    }

    public AnniversariesDTO(Collection<AnniversaryDTO> values) {
        super(values);
    }

}
