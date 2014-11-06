package com.sap.sailing.dashboards.gwt.client.popups;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;

public class RacingNotYetStartedPopup extends PopupWithMessageAndImage {

    //private static final Logger logger = Logger.getLogger(RibDashboardEntryPoint.class.getName());

    private List<RacingNotYetStartedPopupListener> listener;

    public RacingNotYetStartedPopup() {
        listener = new ArrayList<RacingNotYetStartedPopupListener>();
        initLoadingIndicator();
    }

    private void initLoadingIndicator() {
        loadingindicator.setUrl("gifs/loader.gif");
        loadingindicator.getElement().addClassName(style.popup_loadingindicator_hidden());
    }

    private void showLoadingIndicator() {
        loadingindicator.getElement().addClassName(style.popup_loadingindicator_visible());
        loadingindicator.getElement().removeClassName(style.popup_loadingindicator_hidden());
    }

    private void hideLoadingIndicator() {
        loadingindicator.getElement().removeClassName(style.popup_loadingindicator_visible());
        loadingindicator.getElement().addClassName(style.popup_loadingindicator_hidden());
    }

    public void showWithMessageAndImageAndButtonText(String message, ImageResource image, String buttontext) {
        // if index is -1 it does not contain widget
        if (RootPanel.get().getWidgetIndex(this) == -1) {
            RootPanel.get().add(this);
            RootLayoutPanel.get().addStyleName(style.blurred());
            this.message.getElement().setInnerText(message);
            this.icon.setResource(image);
            this.button.setText(buttontext);
            popup.getElement().addClassName(style.popup_shown());
            popup.getElement().removeClassName(style.popup_hidden());
        } else {
            hideLoadingIndicator();
        }
    }

    public void hide(boolean shouldHoldBlurrEffectForOtherPopup) {
        hideLoadingIndicator();
        this.getElement().addClassName(style.popup_hidden());
        this.getElement().removeClassName(style.popup_shown());
        if (shouldHoldBlurrEffectForOtherPopup) {
            RootLayoutPanel.get().addStyleName(style.not_blurred());
        }
        RootPanel.get().remove(this);
    }

    @Override
    protected void reactToButtonClickEvent() {
        showLoadingIndicator();
        notifyListenerAboutButtonClicked();
    }

    public void addListener(RacingNotYetStartedPopupListener o) {
        if (o != null && !listener.contains(o)) {
            this.listener.add(o);
        }
    }

    public void removeListener(RacingNotYetStartedPopupListener o) {
        this.listener.remove(o);
    }

    public void notifyListenerAboutButtonClicked() {
        for (RacingNotYetStartedPopupListener newStartAnalysisListener : listener) {
            newStartAnalysisListener.popupButtonClicked();
        }
    }
}
