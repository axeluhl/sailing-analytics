package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.sap.sse.security.shared.dto.NamedDTO;

@SuppressWarnings("serial")
public class SidelineDTO extends NamedDTO {
    private List<MarkDTO> marks;

    @Deprecated
    SidelineDTO() {} // for GWT RPC serialization only

    public SidelineDTO(String name, List<MarkDTO> marks) {
        super(name);
        this.marks = marks;
    }

    public List<MarkDTO> getMarks() {
        return marks;
    }
}
