package com.sap.sailing.gwt.home.shared.partials.filter;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.filter.Filter;

public abstract class AbstractTextInputFilter<T, C> extends AbstractFilterWidget<T, C> {
    
    private final FlowPanel containerUi = new FlowPanel();
    private final Button clearButtonUi = new Button("X");
    private HasValue<String> textInputUi;

    protected <W extends IsWidget & HasValue<String>> AbstractTextInputFilter() {
        FilterWidgetResources.INSTANCE.css().ensureInjected();
        containerUi.addStyleName(FilterWidgetResources.INSTANCE.css().input_filter_container());
        initWidget(containerUi);
    }
    
    private void initTextInputWidget(Widget textInputWidget, String placeholderText, KeyUpHandler handler) {
        textInputWidget.getElement().setAttribute("placeholder", placeholderText);
        textInputWidget.addStyleName(FilterWidgetResources.INSTANCE.css().input_filter_text_input());
        textInputWidget.addDomHandler(handler, KeyUpEvent.getType());
        containerUi.add(textInputWidget);
    }
    
    private void initClearButtonWidget(ClickHandler handler) {
        clearButtonUi.addStyleName(FilterWidgetResources.INSTANCE.css().input_filter_clear_button());
        clearButtonUi.addClickHandler(handler);
        containerUi.add(clearButtonUi);
        clearButtonUi.setEnabled(false);
    }
    
    private String getTextInputValue() {
        return textInputUi == null ? "" : textInputUi.getValue();
    }
    
    protected <W extends IsWidget & HasValue<String>> void initWidgets(W textInputWidget, String placeholderText) {
        this.textInputUi = textInputWidget;
        TextInputFilterHandler handler = new TextInputFilterHandler();
        initTextInputWidget(textInputWidget.asWidget(), placeholderText, handler);
        initClearButtonWidget(handler);
    }
    
    @Override
    public final Filter<T> getFilter() {
        return getFilter(getTextInputValue().trim());
    }

    protected void update() {
        clearButtonUi.setEnabled(!getTextInputValue().isEmpty());
        notifyValueChangeHandlers();
    }
    
    protected void clear() {
        textInputUi.setValue(null);
        update();
    }
    
    protected abstract Filter<T> getFilter(String searchString);
    
    private class TextInputFilterHandler implements ClickHandler, KeyUpHandler {
        @Override
        public void onKeyUp(KeyUpEvent event) {
            AbstractTextInputFilter.this.update();
        }
        @Override
        public void onClick(ClickEvent event) {
            AbstractTextInputFilter.this.clear();
        }
    }

}
