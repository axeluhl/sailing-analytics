package com.sap.sailing.dashboards.gwt.client.popups;

import java.util.Iterator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class PopupWithMessageAndImage extends Composite implements HasWidgets {

    interface PopupWithMessageAndImageStyle extends CssResource {
        String popup();

        String popup_shown();

        String popup_hidden();

        String popup_loadingindicator_visible();

        String popup_loadingindicator_hidden();

        String blurred();

        String not_blurred();
    }

    @UiField
    PopupWithMessageAndImageStyle style;

    private static PopupWithMessageAndImageUiBinder uiBinder = GWT.create(PopupWithMessageAndImageUiBinder.class);

    interface PopupWithMessageAndImageUiBinder extends UiBinder<Widget, PopupWithMessageAndImage> {
    }

    @UiField
    VerticalPanel popup;

    @UiField
    Button button;

    @UiField
    Image icon;

    @UiField
    Image loadingindicator;

    @UiField
    HTML message;

    public PopupWithMessageAndImage() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void showWithMessageAndImageAndButtonText(String message, ImageResource image, String buttontext) {
        RootPanel.get().add(this);
        RootLayoutPanel.get().addStyleName(style.blurred());
        this.message.getElement().setInnerText(message);
        this.icon.setResource(image);
        this.button.setText(buttontext);
        popup.getElement().addClassName(style.popup_shown());
        popup.getElement().removeClassName(style.popup_hidden());
    }

    public void hide() {
        this.getElement().addClassName(style.popup_hidden());
        this.getElement().removeClassName(style.popup_shown());
        RootLayoutPanel.get().addStyleName(style.not_blurred());
        RootPanel.get().remove(this);
    }

    @UiHandler("button")
    void handleClick(ClickEvent e) {
        reactToButtonClickEvent();
    }

    protected void reactToButtonClickEvent() {
        hide();
    }

    @SuppressWarnings("unused")
    private void stopPopupLoading() {
        loadingindicator.getElement().addClassName(style.popup_loadingindicator_hidden());
        loadingindicator.getElement().removeClassName(style.popup_loadingindicator_visible());
    }

    @Override
    public void add(Widget w) {
        throw new UnsupportedOperationException("The method add(Widget w) is not supported.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("The method clear() is not supported.");
    }

    @Override
    public Iterator<Widget> iterator() {
        return null;
    }

    @Override
    public boolean remove(Widget w) {
        return false;
    }
}
