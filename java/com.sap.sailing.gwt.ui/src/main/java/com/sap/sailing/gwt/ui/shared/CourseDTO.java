package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.impl.Util.Pair;

public class CourseDTO implements IsSerializable {
    public Pair<MarkDTO, MarkDTO> startGate;

    public Pair<MarkDTO, MarkDTO> finishGate;
    
    public List<MarkDTO> buoys;
}
