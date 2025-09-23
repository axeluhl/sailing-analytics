package com.sap.sailing.gwt.home.desktop.partials.solutions;

import java.util.Optional;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.dom.client.ParagraphElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.UListElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.partials.storebadges.AppStoreBadge;
import com.sap.sailing.gwt.home.shared.partials.storebadges.PlayStoreBadge;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.shared.ClientConfiguration;

public class Solutions extends Composite {
    interface SolutionsUiBinder extends UiBinder<Widget, Solutions> {
    }
    
    private static SolutionsUiBinder uiBinder = GWT.create(SolutionsUiBinder.class);


    @UiField Anchor solution1Anchor, solution2Anchor, solution3Anchor, solution4Anchor, solution5Anchor, solution6Anchor;

    @UiField DivElement solution1Div, solution1Inner,
    solution2Div, solution2Bg,
    solution3Div, solution3Bg,
    solution4Div, solution4Bg,
    solution5Div, solution5Bg,
    solution6Div, solution6Bg;

    @UiField HeadingElement solution1Title, solution2Title, solution3Title, solution4Title, solution5Title, solution6Title;
    @UiField ParagraphElement solution1P1, solution1P2, solution2P1, solution2P2, solution3P, solution4P, solution5P, solution6P;
    @UiField UListElement solution1List1, solution1List2;
    
    @UiField Anchor solution2ReadMore, solution3ReadMore, solution4ReadMore, solution5ReadMore, solution6ReadMore;
    @UiField PlayStoreBadge solution3PlayBadge, solution4PlayBadge, solution5PlayBadge;
    @UiField AppStoreBadge  solution4AppBadge;
    
    public Solutions() {
        SolutionsResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));

        final ClientConfiguration cc = ClientConfiguration.getInstance();

        setBackgroundSafe(solution2Bg, cc.getSailingAnalyticsImageURL());
        setBackgroundSafe(solution1Inner, cc.getSolutionsInSailingImageURL());
        setBackgroundSafe(solution4Bg, cc.getSailInSightAppImageURL());
        setBackgroundSafe(solution6Bg, cc.getSailingSimulatorImageURL());
        setBackgroundSafe(solution3Bg, cc.getSailingRaceManagerAppImageURL());
        setBackgroundSafe(solution5Bg, cc.getBuoyPingerAppImageURL());
        
        populate1(cc);
        boolean show2 = populate2(cc);
        boolean show3 = populate3(cc);
        boolean show4 = populate4(cc);
        boolean show5 = populate5(cc);
        boolean show6 = populate6(cc);

        setLinkOrHide(solution1Anchor, /* none for #1 */ null); // stays hidden unless you add a link property
        setLinkOrHide(solution2Anchor, show2 ? cc.getSolution2ReadMoreLink()      : null);
        setLinkOrHide(solution3Anchor, show3 ? cc.getSolutions3ReadMoreLink()     : null);
        setLinkOrHide(solution4Anchor, show4 ? cc.getSolutions4ReadMoreLink()     : null);
        setLinkOrHide(solution5Anchor, show5 ? cc.getSolutions5ReadMoreLink()     : null);
        setLinkOrHide(solution6Anchor, show6 ? cc.getSolutions6ReadMoreLink()     : null);      
    }
    private boolean populate1(ClientConfiguration cc) {
        String head = cc.getSolutions1Headline(Optional.empty());
        String title = cc.getSolutions1Title(Optional.empty());
        if (!has(head) || !has(title)) { hide(solution1Div); return false; }
        solution1Anchor.setText(head);
        solution1Title.setInnerText(title);

        setOrHide(solution1P1, cc.getContentSolutions11(Optional.empty()));
        setListItems(solution1List1, cc.getContentSolutions12(Optional.empty()), cc.getContentSolutions13(Optional.empty()), cc.getContentSolutions14(Optional.empty()));
        setOrHide(solution1P2, cc.getContentSolutions15(Optional.empty()));
        setListItems(solution1List2,
            cc.getContentSolutions17(Optional.empty()), cc.getContentSolutions18(Optional.empty()), cc.getContentSolutions111(Optional.empty()), cc.getContentSolutions112(Optional.empty()), cc.getContentSolutions113(Optional.empty()));
        return true;
    }
    private boolean populate2(ClientConfiguration cc) {
        String head = cc.getSolutions2Headline(Optional.empty());
        String title = cc.getSolutions2Title(Optional.empty());
        if (!has(head) || !has(title)) { hide(solution2Div); return false; }
        solution2Anchor.setText(head);
        solution2Title.setInnerText(title);
    
        setOrHide(solution2P1, cc.getContentSolutions21(Optional.empty()));
        setOrHide(solution2P2, cc.getContentSolutions22(Optional.empty()));
        setCta(
                solution2ReadMore,
                cc.getSailingAnalyticsReadMoreText(Optional.empty()),
                cc.getSolution2ReadMoreLink()
              );
        return true;
    }
    private boolean populate3(ClientConfiguration cc) {
        String head = cc.getSolutions3Headline(Optional.empty());
        String title = cc.getSolutions3Title(Optional.empty());
        if (!has(head) || !has(title)) { hide(solution3Div); return false; }
        solution3Anchor.setText(head);
        solution3Title.setInnerText(title);
        setOrHide(solution3P, cc.getContentSolutions3(Optional.empty()));
        setBadgeLinkOrHide(solution3PlayBadge, cc.getSolutions3PlayStoreURL());
        setCta(
                solution3ReadMore,
                cc.getSolutions3ReadMore(Optional.empty()),
                cc.getSolutions3ReadMoreLink()
        );
        return true;
    }
    private boolean populate4(ClientConfiguration cc) {
        String head = cc.getSolutions4Headline(Optional.empty());
        String title = cc.getSolutions4Title(Optional.empty());
        if (!has(head) || !has(title)) {
            hide(solution4Div);
            return false;
        }
        solution4Anchor.setText(head);
        solution4Title.setInnerText(title);
        setOrHide(solution4P, cc.getContentSolutions4(Optional.empty()));

        setBadgeLinkOrHide(solution4AppBadge,  cc.getSolutions4AppStoreURL());
        setBadgeLinkOrHide(solution4PlayBadge, cc.getSolutions4PlayStoreURL());


        setCta(solution4ReadMore, cc.getSolutions4ReadMore(Optional.empty()), cc.getSolutions4ReadMoreLink());
        return true;
    }
    private boolean populate5(ClientConfiguration cc) {
        String head = cc.getSolutions5Headline(Optional.empty());
        String title = cc.getSolutions5Title(Optional.empty());
        if (!has(head) || !has(title)) {
            hide(solution5Div);
            return false;
        }
        solution5Anchor.setText(head);
        solution5Title.setInnerText(title);
        setOrHide(solution5P, cc.getContentSolutions5(Optional.empty()));

        setBadgeLinkOrHide(solution5PlayBadge, cc.getSolutions5PlayStoreURL());


        setCta(solution5ReadMore, cc.getSolutions5ReadMore(Optional.empty()), cc.getSolutions5ReadMoreLink());
        return true;
    }
    private boolean populate6(ClientConfiguration cc) {
        String head = cc.getSolutions6Headline(Optional.empty());
        String title = cc.getSolutions6Title(Optional.empty());
        if (!has(head) || !has(title)) {
            hide(solution6Div);
            return false;
        }
        solution6Anchor.setText(head);
        solution6Title.setInnerText(title);
        setOrHide(solution6P, cc.getContentSolutions6(Optional.empty()));
        setCta(solution6ReadMore, cc.getSolutions6ReadMore(Optional.empty()), cc.getSolutions6ReadMoreLink());
        return true;
    }
    private static boolean has(String s) {
        return Util.hasLength(s);
    }
    private static void hide(DivElement el) {
        el.getStyle().setDisplay(Display.NONE);
    }
    private static void setBackgroundSafe(DivElement el, String url) {
        if (has(url)) {
            el.getStyle().setBackgroundImage("url('" + url + "')");
        } else {
            el.getStyle().clearBackgroundImage();
        }
    }
    private static void setBadgeLinkOrHide(Widget w, final String href) {
        if (w == null) return;
        if (has(href)) {
            w.setVisible(true);
            // Avoid multiple handlers if this ever re-runs:
            w.getElement().setPropertyString("data-href", href);
            w.addDomHandler(new ClickHandler() {
                @Override public void onClick(ClickEvent event) {
                    Window.Location.assign(href);
                }
            }, ClickEvent.getType());
            w.getElement().setAttribute("role", "link");
            w.getElement().getStyle().setProperty("cursor", "pointer");
        } else {
            w.setVisible(false);
        }
    }

    private static void setOrHide(ParagraphElement p, String text) {
        if (has(text)) {
            p.setInnerText(text);
            p.getStyle().clearDisplay();
        } else {
            p.getStyle().setDisplay(com.google.gwt.dom.client.Style.Display.NONE);
        }
    }
    private static void setListItems(UListElement ul, String... items) {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        if (items != null)
            for (String it : items)
                if (has(it)) {
                    sb.append("<li>").append(escape(it)).append("</li>");
                    count++;
                }
        if (count > 0) {
            ul.setInnerHTML(sb.toString());
            ul.getStyle().clearDisplay();
        } else {
            ul.getStyle().setDisplay(com.google.gwt.dom.client.Style.Display.NONE);
        }
    }
    private static void setLinkOrHide(Anchor a, String href) {
        if (a == null)
            return;
        if (has(href)) {
            a.setHref(href);
            a.setVisible(true);
        } else {
            a.setHref("#");
            a.setVisible(false);
        }
    }
    private static void setCta(Anchor a, String text, String href) {
        if (a == null)
            return;
        if (has(text) && has(href)) {
            a.setText(text);
            a.setHref(href);
            a.setVisible(true);
        } else {
            a.setVisible(false);
            a.setHref("#");
            a.setText("");
        }
    }
    private static native String escape(String s) /*-{
    		return String(s).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
        }-*/;
    public static boolean isAnySectionConfigured(ClientConfiguration cc) {
        return has(cc.getSolutions1Headline(Optional.empty())) && has(cc.getSolutions1Title(Optional.empty()))
                || has(cc.getSolutions2Headline(Optional.empty())) && has(cc.getSolutions2Title(Optional.empty()))
                || has(cc.getSolutions3Headline(Optional.empty())) && has(cc.getSolutions3Title(Optional.empty()))
                || has(cc.getSolutions4Headline(Optional.empty())) && has(cc.getSolutions4Title(Optional.empty()))
                || has(cc.getSolutions5Headline(Optional.empty())) && has(cc.getSolutions5Title(Optional.empty()))
                || has(cc.getSolutions6Headline(Optional.empty())) && has(cc.getSolutions6Title(Optional.empty()));
    }
}    