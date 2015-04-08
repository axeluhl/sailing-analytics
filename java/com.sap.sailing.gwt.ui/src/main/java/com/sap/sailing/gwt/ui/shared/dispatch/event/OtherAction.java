package com.sap.sailing.gwt.ui.shared.dispatch.event;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;

public class OtherAction implements Action<ResultWithTTL<OtherDTO>> {

    @Override
    @GwtIncompatible
    public ResultWithTTL<OtherDTO> execute(DispatchContext context) {
        return new ResultWithTTL<>(3000, new OtherDTO());
    }

}
