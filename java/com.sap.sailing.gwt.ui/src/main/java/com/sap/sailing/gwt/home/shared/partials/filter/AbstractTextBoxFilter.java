package com.sap.sailing.gwt.home.shared.partials.filter;

import java.util.Collection;

import com.google.gwt.user.client.ui.TextBox;

public abstract class AbstractTextBoxFilter<T, C> extends AbstractTextInputFilter<T, C> {

    protected AbstractTextBoxFilter(String placeholderText) {
        initWidgets(new TextBox(), placeholderText);
    }
    
    @Override
    public void setSelectableValues(Collection<C> selectableValues) {
        // Nothing to do in a free text filter
    }

}
