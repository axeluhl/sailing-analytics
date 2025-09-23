package com.sap.sailing.gwt.home.mobile.partials.solutions;

import java.util.Optional;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ParagraphElement;
import com.google.gwt.dom.client.UListElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.sap.sailing.gwt.home.shared.partials.storebadges.AppStoreBadge;
import com.sap.sailing.gwt.home.shared.partials.storebadges.PlayStoreBadge;
import com.sap.sse.gwt.shared.ClientConfiguration;

public class Solutions extends Composite {

  interface MyUiBinder extends UiBinder<Widget, Solutions> {}
  private static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  // Accordion items
  @UiField SolutionsItem item1;
  @UiField SolutionsItem item2;
  @UiField SolutionsItem item3;
  @UiField SolutionsItem item4;
  @UiField SolutionsItem item5;
  @UiField SolutionsItem item6;

  // Text fields
  @UiField ParagraphElement m1P1, m1P2, m2P1, m2P2, m3P, m4P, m5P, m6P;
  @UiField UListElement m1List1, m1List2;

  // Read-more buttons
  @UiField Anchor m2ReadMore, m3ReadMore, m4ReadMore, m5ReadMore, m6ReadMore;

  // Store badges
  @UiField PlayStoreBadge m3PlayBadge, m4PlayBadge, m5PlayBadge;
  @UiField AppStoreBadge  m4AppBadge;

  public Solutions(/* MobilePlacesNavigator placesNavigator (unused now) */) {
    initWidget(uiBinder.createAndBindUi(this));

    ClientConfiguration cc = ClientConfiguration.getInstance();

    boolean show1 = has(cc.getSolutions1Headline(Optional.empty())) && has(cc.getSolutions1Title(Optional.empty()));
    if (show1) {
      item1.setImageUrl(cc.getSolutionsInSailingImageURL());
      item1.setHeaderText(cc.getSolutions1Headline(Optional.empty()));
      setOrHide(m1P1, cc.getContentSolutions11(Optional.empty()));
      setListItems(m1List1,
          cc.getContentSolutions12(Optional.empty()),
          cc.getContentSolutions13(Optional.empty()),
          cc.getContentSolutions14(Optional.empty()));
      setOrHide(m1P2, cc.getContentSolutions15(Optional.empty()));
      setListItems(m1List2,
          cc.getContentSolutions17(Optional.empty()),
          cc.getContentSolutions110(Optional.empty()),
          cc.getContentSolutions111(Optional.empty()),
          cc.getContentSolutions112(Optional.empty()),
          cc.getContentSolutions113(Optional.empty()));
    } else {
      item1.setVisible(false);
    }

    boolean show2 = has(cc.getSolutions2Headline(Optional.empty())) && has(cc.getSolutions2Title(Optional.empty()));
    if (show2) {
      item2.setHeaderText(cc.getSolutions2Headline(Optional.empty()));
      item2.setImageUrl(cc.getSoutionsInSailingTrimmedImageURL());
      setOrHide(m2P1, cc.getContentSolutions21(Optional.empty()));
      setOrHide(m2P2, cc.getContentSolutions22(Optional.empty()));
      setCta(m2ReadMore, cc.getSailingAnalyticsReadMoreText(Optional.empty()), cc.getSolution2ReadMoreLink());
    } else {
      item2.setVisible(false);
    }

    boolean show3 = has(cc.getSolutions3Headline(Optional.empty())) && has(cc.getSolutions3Title(Optional.empty()));
    if (show3) {
      item3.setHeaderText(cc.getSolutions3Headline(Optional.empty()));
      item3.setImageUrl(cc.getSailingRaceManagerAppTrimmedImageURL());
      setOrHide(m3P, cc.getContentSolutions3(Optional.empty()));
      setBadgeLinkOrHide(m3PlayBadge, cc.getSolutions3PlayStoreURL());
      setCta(m3ReadMore, cc.getSolutions3ReadMore(Optional.empty()), cc.getSolutions3ReadMoreLink());
    } else {
      item3.setVisible(false);
    }

    boolean show4 = has(cc.getSolutions4Headline(Optional.empty())) && has(cc.getSolutions4Title(Optional.empty()));
    if (show4) {
      item4.setHeaderText(cc.getSolutions4Headline(Optional.empty()));
      item4.setImageUrl(cc.getSailInSightAppImageURL());
      setOrHide(m4P, cc.getContentSolutions4(Optional.empty()));
      setBadgeLinkOrHide(m4AppBadge,  cc.getSolutions4AppStoreURL());
      setBadgeLinkOrHide(m4PlayBadge, cc.getSolutions4PlayStoreURL());
      setCta(m4ReadMore, cc.getSolutions4ReadMore(Optional.empty()), cc.getSolutions4ReadMoreLink());
    } else {
      item4.setVisible(false);
    }

    boolean show5 = has(cc.getSolutions5Headline(Optional.empty())) && has(cc.getSolutions5Title(Optional.empty()));
    if (show5) {
      item5.setHeaderText(cc.getSolutions5Headline(Optional.empty()));
      item5.setImageUrl(cc.getBuoyPingerAppImageURL());
      setOrHide(m5P, cc.getContentSolutions5(Optional.empty()));
      setBadgeLinkOrHide(m5PlayBadge, cc.getSolutions5PlayStoreURL());
      setCta(m5ReadMore, cc.getSolutions5ReadMore(Optional.empty()), cc.getSolutions5ReadMoreLink());
    } else {
      item5.setVisible(false);
    }

    boolean show6 = has(cc.getSolutions6Headline(Optional.empty())) && has(cc.getSolutions6Title(Optional.empty()));
    if (show6) {
      item6.setHeaderText(cc.getSolutions6Headline(Optional.empty()));
      item6.setImageUrl(cc.getSailingSimulatorTrimmedImageURL());
      setOrHide(m6P, cc.getContentSolutions6(Optional.empty()));
      setCta(m6ReadMore, cc.getSolutions6ReadMore(Optional.empty()), cc.getSolutions6ReadMoreLink());
    } else {
      item6.setVisible(false);
    }
  }
  private static boolean has(String s) {
    return com.sap.sse.common.Util.hasLength(s);
  }
  private static void setOrHide(ParagraphElement p, String text) {
    if (has(text)) { p.setInnerText(text); p.getStyle().clearDisplay(); }
    else { p.getStyle().setProperty("display", "none"); }
  }
  private static void setCta(Anchor a, String text, String href) {
    if (a == null) return;
    if (has(text) && has(href)) { a.setText(text); a.setHref(href); a.setVisible(true); }
    else { a.setVisible(false); a.setHref("#"); a.setText(""); }
  }
  private static void setBadgeLinkOrHide(Widget w, final String href) {
    if (w == null) return;
    if (has(href)) {
      w.setVisible(true);
      w.addDomHandler(new ClickHandler() {
        @Override public void onClick(ClickEvent event) { Window.Location.assign(href); }
      }, ClickEvent.getType());
      w.getElement().setAttribute("role", "link");
      w.getElement().getStyle().setProperty("cursor", "pointer");
    } else {
      w.setVisible(false);
    }
  }
  private static native String escape(String s) /*-{
    return String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
  }-*/;
  private static void setListItems(UListElement ul, String... items) {
    StringBuilder sb = new StringBuilder(); int count = 0;
    if (items != null) for (String it : items)
      if (has(it)) { sb.append("<li>").append(escape(it)).append("</li>"); count++; }
    if (count > 0) { ul.setInnerHTML(sb.toString()); ul.getStyle().clearDisplay(); }
    else { ul.getStyle().setProperty("display", "none"); }
  }
}
