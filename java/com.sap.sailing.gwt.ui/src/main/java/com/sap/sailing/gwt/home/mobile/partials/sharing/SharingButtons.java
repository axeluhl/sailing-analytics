package com.sap.sailing.gwt.home.mobile.partials.sharing;

import static com.google.gwt.dom.client.Style.Display.NONE;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ScrollEvent;
import com.google.gwt.user.client.Window.ScrollHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.mobile.partials.sharing.SharingButtonsResources.LocalCss;
import com.sap.sailing.gwt.home.shared.partials.shared.SharingMetadataProvider;
import com.sap.sailing.gwt.home.shared.places.ShareablePlaceContext;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.shared.ClientConfiguration;

public class SharingButtons extends Composite {

    private static SharingButtonsUiBinder uiBinder = GWT.create(SharingButtonsUiBinder.class);

    private static String SHARING_URL_PREFIX = "/gwt/shared";

    interface SharingButtonsUiBinder extends UiBinder<Widget, SharingButtons> {
    }

    @UiField
    HTMLPanel htmlPanel;
    @UiField
    Button shareButton;
    @UiField
    Button copyToClipBoard;

    public SharingButtons() {
        SharingButtonsResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        if (!ClientConfiguration.getInstance().isBrandingActive()) {
            htmlPanel.getElement().getStyle().setDisplay(NONE);
        }
    }

    public void setUp(SharingMetadataProvider provider) {
        if (!ClientConfiguration.getInstance().isBrandingActive()) {
            return;
        }
        final String hostName = Window.Location.getHost();
        final ShareablePlaceContext context = provider.getContext();
        final String urlToShare = "http://" + hostName + SHARING_URL_PREFIX + context.getContextAsPathParameters();
        if (clientHasNavigatorShareSupport()) {
            copyToClipBoard.setVisible(false);
            shareButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    share(urlToShare, provider.getShortText());
                }
            });
        } else if (clientHasNavigatorCopyToClipboardSupport()) {
            shareButton.setVisible(false);
            copyToClipBoard.removeStyleDependentName("gwt-button");
            copyToClipBoard.removeStyleDependentName("gwt-Button:visited");
            copyToClipBoard.removeStyleName("button");
            copyToClipBoard.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    copyToClipboard(urlToShare);
                    Notification.notify(StringMessages.INSTANCE.sharingLinkCopied(), NotificationType.INFO);
                }
            });
        } else {
            shareButton.setVisible(false);
            copyToClipBoard.setVisible(false);
        }
        final LocalCss css = SharingButtonsResources.INSTANCE.css();
        Timer fadeOutSharingButtonsTimer = new Timer() {
            @Override
            public void run() {
                htmlPanel.removeStyleName(css.eventheader_sharing_faded_in());
                htmlPanel.addStyleName(css.eventheader_sharing_faded_out());
            }
        };
        Window.addWindowScrollHandler(new ScrollHandler() {
            @Override
            public void onWindowScroll(ScrollEvent event) {
                htmlPanel.removeStyleName(css.eventheader_sharing_faded_out());
                htmlPanel.addStyleName(css.eventheader_sharing_faded_in());
                fadeOutSharingButtonsTimer.schedule(1500);
            }
        });
    }

    public static native void copyToClipboard(String text) /*-{
        window.focus();
        navigator.clipboard.writeText(text);
    }-*/;

    public static native void share(String url, String text) /*-{
        window.focus();
        navigator.share({
            url: url,
            text: text
        });
    }-*/;

    public static native boolean clientHasNavigatorShareSupport() /*-{
        window.focus();
        if (navigator.share) {
            return true;
        } else {
            return false;
        }
    }-*/;

    public static native boolean clientHasNavigatorCopyToClipboardSupport() /*-{
        window.focus();
        if (navigator && navigator.clipboard && navigator.clipboard.writeText) {
            return true;
        } else {
            return false;
        }
    }-*/;
}
