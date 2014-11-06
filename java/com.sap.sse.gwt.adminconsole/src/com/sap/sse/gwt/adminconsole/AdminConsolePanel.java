package com.sap.sse.gwt.adminconsole;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.SecurityStylesheetResources;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.gwt.client.AbstractEntryPoint;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.panels.VerticalTabLayoutPanel;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.UserStatusEventHandler;
import com.sap.sse.security.ui.loginpanel.LoginPanel;
import com.sap.sse.security.ui.shared.UserDTO;

public class AdminConsolePanel extends DockLayoutPanel {
    private final UserService userService;
    
    /**
     * The administration console's UI depends on the user's roles. When the roles change then so shall the display of
     * tabs. {@link AdminConsoleFeatures} list the roles to which they are made available. This map keeps track of the
     * dependencies and allows the UI to adjust to role changes.
     */
    private final LinkedHashMap<Triple<VerticalOrHorizontalTabLayoutPanel, Widget, String>, AdminConsoleFeatures> roleSpecificTabs;
    
    private final SelectionHandler<Integer> tabSelectionHandler;
    
    /**
     * The top-level vertical tab panel
     */
    private final VerticalTabLayoutPanel topLevelTabPanel;
    
    private final VerticalOrHorizontalTabLayoutPanel topLevelTabPanelWrapper;
    
    /**
     * Keys are the results of calling {@link RefreshableAdminConsolePanel#getWidget()} on their associated values. This
     * allows the panel to find the refresh target when a widget has been selected in a tab panel.
     */
    private final Map<Widget, RefreshableAdminConsolePanel> panelsByWidget;
    
    /**
     * Generic selection handler that forwards selected tabs to a refresher that ensures that data gets reloaded. If
     * you add a new tab then make sure to have a look at #refreshDataFor(Widget widget) to ensure that upon
     * selection your tab gets the data refreshed.
     */
    private class TabSelectionHandler implements SelectionHandler<Integer> {
        @Override
        public void onSelection(SelectionEvent<Integer> event) {
            Object source = event.getSource();
            if (source != null) {
                if (source instanceof TabLayoutPanel) {
                    final TabLayoutPanel tabPanel = ((TabLayoutPanel) source);
                    final Widget selectedPanel = tabPanel.getWidget(event.getSelectedItem());
                    refreshDataFor(selectedPanel);
                } else if (source instanceof VerticalTabLayoutPanel) {
                    final VerticalTabLayoutPanel verticalTabLayoutPanel = (VerticalTabLayoutPanel) source;
                    Widget widgetAssociatedToVerticalTab = verticalTabLayoutPanel.getWidget(verticalTabLayoutPanel
                            .getSelectedIndex());
                    if (widgetAssociatedToVerticalTab instanceof TabLayoutPanel) {
                        TabLayoutPanel selectedTabLayoutPanel = (TabLayoutPanel) widgetAssociatedToVerticalTab;
                        final int selectedIndex = selectedTabLayoutPanel.getSelectedIndex();
                        if (selectedIndex >= 0) {
                            widgetAssociatedToVerticalTab = selectedTabLayoutPanel.getWidget(selectedIndex);
                        }
                    }
                    refreshDataFor(widgetAssociatedToVerticalTab);
                }
            }
        }

        private void refreshDataFor(Widget widgetAssociatedToVerticalTab) {
            RefreshableAdminConsolePanel refreshTarget = panelsByWidget.get(widgetAssociatedToVerticalTab);
            if (refreshTarget != null) {
                refreshTarget.refreshAfterBecomingVisible();
            }
        }
    }
    
    public AdminConsolePanel(UserService userService, BuildVersionRetriever buildVersionRetriever, Label persistentAlertLabel, String releaseNotesAnchorLabel, String releaseNotesURL, ErrorReporter errorReporter) {
        super(Unit.EM);
        this.userService = userService;
        roleSpecificTabs = new LinkedHashMap<>();
        this.panelsByWidget = new HashMap<>();
        getUserService().addUserStatusEventHandler(new UserStatusEventHandler() {
            @Override
            public void onUserStatusChange(UserDTO user) {
                updateTabDisplayForCurrentUser(user);
            }
        });
        tabSelectionHandler = new TabSelectionHandler();
        topLevelTabPanel = new VerticalTabLayoutPanel(2.5, Unit.EM);
        topLevelTabPanel.addSelectionHandler(tabSelectionHandler);
        topLevelTabPanel.ensureDebugId("AdministrationTabs");
        topLevelTabPanelWrapper = new VerticalOrHorizontalTabLayoutPanel() {
            @Override
            public void add(Widget child, String text, boolean asHtml) {
                topLevelTabPanel.add(child, text, asHtml);
                topLevelTabPanel.forceLayout();
            }

            @Override
            public void remove(Widget child) {
                topLevelTabPanel.remove(child);
            }
        };
        final DockPanel informationPanel = new DockPanel();
        informationPanel.setSize("100%", "95%");
        informationPanel.setSpacing(10);
        informationPanel.add(new LoginPanel(SecurityStylesheetResources.INSTANCE.css(), getUserService()),
                DockPanel.WEST);
        informationPanel.add(persistentAlertLabel, DockPanel.CENTER);
        SystemInformationPanel sysinfoPanel = new SystemInformationPanel(buildVersionRetriever, errorReporter);
        sysinfoPanel.ensureDebugId("SystemInformation");
        final Anchor releaseNotesLink = new Anchor(new SafeHtmlBuilder().appendEscaped(releaseNotesAnchorLabel).toSafeHtml(), releaseNotesURL);
        sysinfoPanel.add(releaseNotesLink);
        informationPanel.add(sysinfoPanel, DockPanel.EAST);
        informationPanel.setCellHorizontalAlignment(sysinfoPanel, HasHorizontalAlignment.ALIGN_RIGHT);
        this.addSouth(informationPanel, 2.5);
        this.add(topLevelTabPanel);
        createUI(releaseNotesURL);
    }
    
    private UserService getUserService() {
        return userService;
    }

    private void createUI(String releaseNotesURL) {
        updateTabDisplayForCurrentUser(getUserService().getCurrentUser());
        if (topLevelTabPanel.getWidgetCount() > 0) {
            topLevelTabPanel.selectTab(0);
        }
    }

    private static interface VerticalOrHorizontalTabLayoutPanel {
        void add(Widget child, String text, boolean asHtml);

        void remove(Widget child);
    }

    /**
     * Adds a new horizontal tab panel to the top-level vertical tab panel.
     * 
     * @return the horizontal tab panel that was created and added to the top-level vertical tab panel; the panel returned can be specified
     * as argument to {@link #addToTabPanel(TabLayoutPanel, Widget, String, AdminConsoleFeatures)}.
     */
    public TabLayoutPanel addVerticalTab(String tabTitle, String tabDebugId, AdminConsoleFeatures feature) {
        final TabLayoutPanel newTabPanel = new TabLayoutPanel(2.5, Unit.EM);
        AbstractEntryPoint.setTabPanelSize(newTabPanel, "100%", "100%");
        newTabPanel.addSelectionHandler(tabSelectionHandler);
        newTabPanel.ensureDebugId(tabDebugId);
        VerticalOrHorizontalTabLayoutPanel wrapper = new VerticalOrHorizontalTabLayoutPanel() {
            @Override
            public void add(Widget child, String text, boolean asHtml) {
                newTabPanel.add(child, text, asHtml);
                newTabPanel.forceLayout();
            }

            @Override
            public void remove(Widget child) {
                newTabPanel.remove(child);
            }
        };
        remeberWidgetLocationAndFeature(wrapper, newTabPanel, tabTitle, feature);
        return newTabPanel;
    }

    /**
     * Adds an administration panel as an entry to the top-level vertical panel, without an intermediary horizontal tab panel.
     * This is useful for panels that form a top-level category of its own but don't require multiple panels to represent this
     * top-level category.
     */
    public void addToVerticalTabPanel(final RefreshableAdminConsolePanel panelToAdd, String tabTitle, AdminConsoleFeatures feature) {
        addToTabPanel(topLevelTabPanelWrapper, panelToAdd, tabTitle, feature);
    }

    private ScrollPanel wrapInScrollPanel(Widget panelToAdd) {
        ScrollPanel scrollPanel = new ScrollPanel();
        scrollPanel.add(panelToAdd);
        panelToAdd.setSize("100%", "100%");
        return scrollPanel;
    }

    public void addToTabPanel(final TabLayoutPanel tabPanel, RefreshableAdminConsolePanel panelToAdd, String tabTitle, AdminConsoleFeatures feature) {
        VerticalOrHorizontalTabLayoutPanel wrapper = new VerticalOrHorizontalTabLayoutPanel() {
            @Override
            public void add(Widget child, String text, boolean asHtml) {
                tabPanel.add(child, text, asHtml);
                tabPanel.forceLayout();
            }

            @Override
            public void remove(Widget child) {
                tabPanel.remove(child);
            }
        };
        addToTabPanel(wrapper, panelToAdd, tabTitle, feature);
    }

    /**
     * Remembers in which tab panel the <code>panelToAdd</code> is to be displayed and for which feature; additionally, remembers adds
     * a hook so that when the <code>panelToAdd</code>'s widget is selected then the {@link RefreshableAdminConsolePanel#refreshAfterBecomingVisible()}
     * method can be called.
     */
    private void addToTabPanel(VerticalOrHorizontalTabLayoutPanel tabPanel, RefreshableAdminConsolePanel panelToAdd, String tabTitle, AdminConsoleFeatures feature) {
        remeberWidgetLocationAndFeature(tabPanel, panelToAdd.getWidget(), tabTitle, feature);
        panelsByWidget.put(panelToAdd.getWidget(), panelToAdd);
    }

    /**
     * Remembers the tab panel in which the <code>widgetToAdd</code> is to be displayed and for which feature.
     */
    private void remeberWidgetLocationAndFeature(VerticalOrHorizontalTabLayoutPanel tabPanel, Widget widgetToAdd,
            String tabTitle, AdminConsoleFeatures feature) {
        roleSpecificTabs.put(new Triple<VerticalOrHorizontalTabLayoutPanel, Widget, String>(tabPanel,
                wrapInScrollPanel(widgetToAdd), tabTitle), feature);
    }

    /**
     * After initialization or whenever the user changes, the tab display is adjusted based on which roles are required
     * to see which tabs. See {@link #roleSpecificTabs}.
     */
    private void updateTabDisplayForCurrentUser(UserDTO user) {
        for (Map.Entry<Triple<VerticalOrHorizontalTabLayoutPanel, Widget, String>, AdminConsoleFeatures> e : roleSpecificTabs
                .entrySet()) {
            final Widget panelToAdd = e.getKey().getB();
            if (user != null && isUserInRole(e.getValue().getEnabledRoles())) {
                e.getKey().getA().add(panelToAdd, e.getKey().getC(), /* asHtml */false);
            } else {
                e.getKey().getA().remove(panelToAdd);
            }
        }
    }

    private boolean isUserInRole(String... roles) {
        boolean result = false;
        UserDTO user = getUserService().getCurrentUser();
        for (String enabledRole : roles) {
            if (user.hasRole(enabledRole)) {
                result = true;
                break;
            }
        }
        return result;
    }
}
