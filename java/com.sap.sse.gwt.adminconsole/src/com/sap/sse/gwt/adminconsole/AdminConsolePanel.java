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
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.gwt.client.AbstractEntryPoint;
import com.sap.sse.gwt.client.BuildVersionRetriever;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.panels.VerticalTabLayoutPanel;
import com.sap.sse.security.shared.Permission;
import com.sap.sse.security.shared.PermissionsForRoleProvider;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.UserStatusEventHandler;
import com.sap.sse.security.ui.loginpanel.LoginPanel;
import com.sap.sse.security.ui.loginpanel.LoginPanelCss;
import com.sap.sse.security.ui.shared.UserDTO;

/**
 * A panel that can be used to implement an administration console. Widgets can be arranged in vertical and horizontal tabs ("L-shape").
 * The top-level element is the vertical tab panel. Widgets may either be added directly as the content of one vertical tab, or a horizontal
 * tab panel can be added as the content widget of a vertical tab, in turn holding widgets in horizontal tabs.<p>
 * 
 * After constructing an instance of this class, there are three ways for adding widgets:<ul>
 *   <li>{@link #addToVerticalTabPanel(RefreshableAdminConsolePanel, String, AdminConsoleFeatures)} adds a widget as a content element
 *   of a vertical tab</li>
 *   <li>{@link #addVerticalTab(String, String, AdminConsoleFeatures)} creates a horizontal tab panel and adds it as a content element of
 *   a vertical tab</li>
 *   <li>{@link #addToTabPanel(TabLayoutPanel, RefreshableAdminConsolePanel, String, AdminConsoleFeatures)} adds a widget as a content element
 *   of a horizontal tab</li>
 * </ul>
 * 
 * Widgets to be added need to be wrapped as {@link RefreshableAdminConsolePanel} holding the widget and receiving the refresh call when
 * the widget is shown because the user has selected the tab. If the component doesn't require any refresh logic, an instance of
 * {@link DefaultRefreshableAdminConsolePanel} can be used to wrap the widget.<p>
 * 
 * After the widgets have been added, {@link #initUI()} must be called to assemble all tabs for the current user's roles. The {@link #initUI()}
 * method must be called each time more widgets have been added dynamically.<p>
 * 
 * For each widget added, a {@link AdminConsoleFeatures feature} needs to be specified. The feature tells the roles of which the logged-in user
 * needs to have at least one in order to see the tab. When the user changes or has his/her roles updated the set of tabs visible will be
 * adjusted according to the new roles available for the logged-in user.<p> 
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class AdminConsolePanel extends DockLayoutPanel {
    private final UserService userService;
    
    /**
     * The administration console's UI depends on the user's roles. When the roles change then so shall the display of
     * tabs. {@link AdminConsoleFeatures} list the roles to which they are made available. This map keeps track of the
     * dependencies and allows the UI to adjust to role changes.
     */
    private final LinkedHashMap<Triple<VerticalOrHorizontalTabLayoutPanel, Widget, String>, Permission> roleSpecificTabs;
    
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
    
    private final PermissionsForRoleProvider permissionsForRoleProvider;
    
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
                    Widget widgetAssociatedToVerticalTab = verticalTabLayoutPanel.getWidget(verticalTabLayoutPanel.getSelectedIndex());
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

        private void refreshDataFor(Widget target) {
            RefreshableAdminConsolePanel refreshTarget = panelsByWidget.get(unwrapScrollPanel(target));
            if (refreshTarget != null) {
                refreshTarget.refreshAfterBecomingVisible();
            }
        }

        /**
         * If the <code>widgetMaybeWrappedByScrollPanel</code> is a scroll panel, returns the content widget,
         * otherwise <code>widgetMaybeWrappedByScrollPanel</code> is returned.
         */
        private Widget unwrapScrollPanel(Widget widgetMaybeWrappedByScrollPanel) {
            final Widget target;
            if (widgetMaybeWrappedByScrollPanel instanceof ScrollPanel) {
                target = ((ScrollPanel) widgetMaybeWrappedByScrollPanel).getWidget();
            } else {
                target = widgetMaybeWrappedByScrollPanel;
            }
            return target;
        }
    }
    
    public AdminConsolePanel(UserService userService, PermissionsForRoleProvider permissionsForRoleProvider,
            BuildVersionRetriever buildVersionRetriever, String releaseNotesAnchorLabel,
            String releaseNotesURL, ErrorReporter errorReporter, LoginPanelCss loginPanelCss) {
        super(Unit.EM);
        this.permissionsForRoleProvider = permissionsForRoleProvider;
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
        informationPanel.add(new LoginPanel(loginPanelCss, getUserService()), DockPanel.WEST);
        informationPanel.add(errorReporter.getPersistentInformationWidget(), DockPanel.CENTER);
        SystemInformationPanel sysinfoPanel = new SystemInformationPanel(buildVersionRetriever, errorReporter);
        sysinfoPanel.ensureDebugId("SystemInformation");
        final Anchor releaseNotesLink = new Anchor(new SafeHtmlBuilder().appendEscaped(releaseNotesAnchorLabel).toSafeHtml(), releaseNotesURL);
        sysinfoPanel.add(releaseNotesLink);
        informationPanel.add(sysinfoPanel, DockPanel.EAST);
        informationPanel.setCellHorizontalAlignment(sysinfoPanel, HasHorizontalAlignment.ALIGN_RIGHT);
        this.addSouth(informationPanel, 2.5);
        this.add(topLevelTabPanel);
    }

    /**
     * Invoke this method after having added all panels using
     * {@link #addToTabPanel(TabLayoutPanel, RefreshableAdminConsolePanel, String, AdminConsoleFeatures)} or
     * {@link #addToVerticalTabPanel(RefreshableAdminConsolePanel, String, AdminConsoleFeatures)} or
     * {@link #addVerticalTab(String, String, AdminConsoleFeatures)}. Tabs can also dynamically be added after calling
     * this method, but then this method needs to be invoked again to ensure that all all tabs are properly displayed
     * for the current panel's state.
     */
    public void initUI() {
        updateTabDisplayForCurrentUser(getUserService().getCurrentUser());
        if (topLevelTabPanel.getWidgetCount() > 0) {
            topLevelTabPanel.selectTab(0);
        }
    }

    private UserService getUserService() {
        return userService;
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
    public TabLayoutPanel addVerticalTab(String tabTitle, String tabDebugId, Permission feature) {
        final TabLayoutPanel newTabPanel = new TabLayoutPanel(2.5, Unit.EM);
        AbstractEntryPoint.setTabPanelSize(newTabPanel, "100%", "100%");
        newTabPanel.addSelectionHandler(tabSelectionHandler);
        newTabPanel.ensureDebugId(tabDebugId);
        remeberWidgetLocationAndFeature(topLevelTabPanelWrapper, newTabPanel, tabTitle, feature);
        return newTabPanel;
    }

    /**
     * Adds an administration panel as an entry to the top-level vertical panel, without an intermediary horizontal tab panel.
     * This is useful for panels that form a top-level category of its own but don't require multiple panels to represent this
     * top-level category.
     */
    public void addToVerticalTabPanel(final RefreshableAdminConsolePanel panelToAdd, String tabTitle, Permission feature) {
        addToTabPanel(topLevelTabPanelWrapper, panelToAdd, tabTitle, feature);
    }

    private ScrollPanel wrapInScrollPanel(Widget panelToAdd) {
        ScrollPanel scrollPanel = new ScrollPanel();
        scrollPanel.add(panelToAdd);
        panelToAdd.setSize("100%", "100%");
        return scrollPanel;
    }

    public void addToTabPanel(final TabLayoutPanel tabPanel, RefreshableAdminConsolePanel panelToAdd, String tabTitle, Permission feature) {
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
    private void addToTabPanel(VerticalOrHorizontalTabLayoutPanel tabPanel, RefreshableAdminConsolePanel panelToAdd, String tabTitle, Permission feature) {
        remeberWidgetLocationAndFeature(tabPanel, wrapInScrollPanel(panelToAdd.getWidget()), tabTitle, feature);
        panelsByWidget.put(panelToAdd.getWidget(), panelToAdd);
    }

    /**
     * Remembers the tab panel in which the <code>widgetToAdd</code> is to be displayed and for which feature.
     */
    private void remeberWidgetLocationAndFeature(VerticalOrHorizontalTabLayoutPanel tabPanel, Widget widgetToAdd,
            String tabTitle, Permission feature) {
        roleSpecificTabs.put(new Triple<VerticalOrHorizontalTabLayoutPanel, Widget, String>(tabPanel,
                widgetToAdd, tabTitle), feature);
    }

    /**
     * After initialization or whenever the user changes, the tab display is adjusted based on which roles are required
     * to see which tabs. See {@link #roleSpecificTabs}.
     */
    private void updateTabDisplayForCurrentUser(UserDTO user) {
        for (Map.Entry<Triple<VerticalOrHorizontalTabLayoutPanel, Widget, String>, Permission> e : roleSpecificTabs
                .entrySet()) {
            final Widget panelToAdd = e.getKey().getB();
            if (user != null && user.hasPermission(e.getValue().getStringPermission(), permissionsForRoleProvider)) {
                e.getKey().getA().add(panelToAdd, e.getKey().getC(), /* asHtml */false);
            } else {
                e.getKey().getA().remove(panelToAdd);
            }
        }
    }
}
