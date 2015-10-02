package com.sap.sailing.gwt.home.shared.partials.filter;

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

public abstract class AbstractTextBoxFilter<T> extends AbstractFilterWidget<T> {

    public final TextBoxFilterUiBinder uiBinder = GWT.create(TextBoxFilterUiBinder.class);
    
    public interface TextBoxFilterUiBinder extends UiBinder<Widget, AbstractTextBoxFilter<?>>{
    }

    @UiField TextBox textBoxUi;
    @UiField Button clearButtonUi;
    
    public AbstractTextBoxFilter() {
        initWidget(uiBinder.createAndBindUi(this));
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

    protected abstract Filter<T> getFilter(String searchString);
    
}
