package com.sap.sailing.gwt.home.desktop.partials.regattanavigation;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class TextBoxFilter extends Composite {

    public final TextBoxFilterUiBinder uiBinder = GWT.create(TextBoxFilterUiBinder.class);
    
    public interface TextBoxFilterUiBinder extends UiBinder<Widget, TextBoxFilter>{
    }

    @UiField TextBox textBoxUi;
    @UiField Button clearButtonUi;
    
    private final List<TextBoxFilterChangeHandler> valueChangeHandlers = new ArrayList<>();

    public TextBoxFilter() {
        initWidget(uiBinder.createAndBindUi(this));
        clearButtonUi.setVisible(false);
    }
    
    public HandlerRegistration addValueChangeHandler(final TextBoxFilterChangeHandler changeHandler) {
        valueChangeHandlers.add(changeHandler);
        return new HandlerRegistration() {
            @Override
            public void removeHandler() {
                valueChangeHandlers.remove(changeHandler);
            }
        };
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
        clearButtonUi.setVisible(textBoxUi.getValue() != null && !textBoxUi.getValue().isEmpty());
        String searchString = textBoxUi.getValue().trim();
        for (TextBoxFilterChangeHandler handler : valueChangeHandlers) {
            handler.onFilterChanged(searchString);
        }
    }

    public interface TextBoxFilterChangeHandler {
        void onFilterChanged(String searchString);
    }
    
}
