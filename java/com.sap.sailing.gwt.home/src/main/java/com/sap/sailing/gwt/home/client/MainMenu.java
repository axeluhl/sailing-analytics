package com.sap.sailing.gwt.home.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * The main menu for the homepage.
*/
public class MainMenu extends Composite {
  interface MainMenuUiBinder extends UiBinder<Widget, MainMenu> {
  }

  private static MainMenuUiBinder uiBinder = GWT.create(MainMenuUiBinder.class);

  public MainMenu() {
    initWidget(uiBinder.createAndBindUi(this));
  }
}

