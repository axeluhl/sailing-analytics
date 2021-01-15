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
import com.sap.sailing.gwt.common.client.NavigatorUtil;
import com.sap.sailing.gwt.common.client.sharing.FloatingSharingButtonsResources;
import com.sap.sailing.gwt.common.client.sharing.FloatingSharingButtonsResources.LocalCss;
import com.sap.sailing.gwt.home.shared.partials.shared.SharingMetadataProvider;
import com.sap.sailing.gwt.home.shared.places.ShareablePlaceContext;
import com.sap.sse.gwt.shared.ClientConfiguration;

public class SharingButtons extends Composite {

    private static SharingButtonsUiBinder uiBinder = GWT.create(SharingButtonsUiBinder.class);

    private static String SHARING_URL_PREFIX = "/sailingserver/shared/home";
    

    interface SharingButtonsUiBinder extends UiBinder<Widget, SharingButtons> {
    }

    @UiField
    HTMLPanel htmlPanel;
    @UiField
    Button shareButton;
    @UiField
    Button copyToClipBoard;

    public SharingButtons() {
        FloatingSharingButtonsResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        if (!ClientConfiguration.getInstance().isBrandingActive()) {
            htmlPanel.getElement().getStyle().setDisplay(NONE);
        }
    }

    public void setUp(SharingMetadataProvider provider) {
        if (!ClientConfiguration.getInstance().isBrandingActive()) {
            return;
        }
        final ShareablePlaceContext context = provider.getContext();
        String urlToShare = Window.Location.createUrlBuilder()
                .setPath(SHARING_URL_PREFIX + context.getContextAsPathParameters())
                .setHash(null)
                .buildString();
        if (NavigatorUtil.clientHasNavigatorShareSupport()) {
            copyToClipBoard.setVisible(false);
            shareButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    NavigatorUtil.shareUrlAndText(urlToShare, provider.getShortText());
                }
            });
        } else if (NavigatorUtil.clientHasNavigatorCopyToClipboardSupport()) {
            shareButton.setVisible(false);
            copyToClipBoard.removeStyleDependentName("gwt-button");
            copyToClipBoard.removeStyleDependentName("gwt-Button:visited");
            copyToClipBoard.removeStyleName("button");
            copyToClipBoard.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    NavigatorUtil.copyToClipboard(urlToShare);
                }
            });
        } else {
            shareButton.setVisible(false);
            copyToClipBoard.setVisible(false);
        }
        final LocalCss css = FloatingSharingButtonsResources.INSTANCE.css();
        Timer fadeOutSharingButtonsTimer = new Timer() {
            @Override
            public void run() {
                htmlPanel.removeStyleName(css.sharing_faded_in());
                htmlPanel.addStyleName(css.sharing_faded_out());
            }
        };
        Window.addWindowScrollHandler(new ScrollHandler() {
            @Override
            public void onWindowScroll(ScrollEvent event) {
                htmlPanel.removeStyleName(css.sharing_faded_out());
                htmlPanel.addStyleName(css.sharing_faded_in());
                fadeOutSharingButtonsTimer.schedule(1500);
            }
        });
        fadeOutSharingButtonsTimer.schedule(2000);
    }
}
