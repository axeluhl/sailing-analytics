package com.sap.sailing.gwt.home.shared.partials.filter;

import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.filter.Filter;

public abstract class AbstractTextBoxFilter<T, C> extends AbstractFilterWidget<T, C> {

    public final TextBoxFilterUiBinder uiBinder = GWT.create(TextBoxFilterUiBinder.class);
    
    public interface TextBoxFilterUiBinder extends UiBinder<Widget, AbstractTextBoxFilter<?, ?>>{
    }

    @UiField TextBox textBoxUi;
    @UiField Button clearButtonUi;
    
    protected AbstractTextBoxFilter(String placeholderText) {
        initWidget(uiBinder.createAndBindUi(this));
        textBoxUi.getElement().setAttribute("placeholder", placeholderText);
        clearButtonUi.setEnabled(false);
    }
    
    @UiHandler("textBoxUi")
    void onTextBoxKeyUp(KeyUpEvent event) {
        update();
    }
    
    @UiHandler("clearButtonUi")
    void onClearButtonClick(ClickEvent event) {
        textBoxUi.setValue(null);
        update();
    }
    
    private void update() {
        clearButtonUi.setEnabled(!textBoxUi.getValue().isEmpty());
        notifyValueChangeHandlers();
    }
    
    @Override
    public final Filter<T> getFilter() {
        return getFilter(textBoxUi.getValue().trim());
    }
    
    @Override
    public void setSelectableValues(Collection<C> selectableValues) {
        // Nothing to do in a free text filter
    }

    protected abstract Filter<T> getFilter(String searchString);
    
}
