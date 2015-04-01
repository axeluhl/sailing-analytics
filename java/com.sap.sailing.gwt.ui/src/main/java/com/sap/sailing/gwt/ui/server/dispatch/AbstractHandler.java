package com.sap.sailing.gwt.ui.server.dispatch;

import java.lang.reflect.ParameterizedType;

import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.Result;

public abstract class AbstractHandler<R extends Result, A extends Action<R>> implements Handler<R, A> {
    private Class<A> type;

    @SuppressWarnings("unchecked")
    protected AbstractHandler() {
        type = (Class<A>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];
    }

    @Override
    public final Class<A> getType() {
        return type;
    }
}
