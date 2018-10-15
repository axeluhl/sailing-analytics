package com.sap.sse.gwt.adminconsole;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HeaderPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.gwt.client.AbstractEntryPoint;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.ServerInfoDTO;
import com.sap.sse.gwt.client.panels.AbstractTabLayoutPanel;
import com.sap.sse.gwt.client.panels.HorizontalTabLayoutPanel;
import com.sap.sse.gwt.client.panels.VerticalTabLayoutPanel;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.PermissionChecker;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.UserStatusEventHandler;
import com.sap.sse.security.ui.loginpanel.LoginPanelCss;
import com.sap.sse.security.ui.shared.UserDTO;

/**
 * A panel that can be used to implement an administration console. Widgets can be arranged in vertical and horizontal
 * tabs ("L-shape"). The top-level element is the vertical tab panel. Widgets may either be added directly as the
 * content of one vertical tab, or a horizontal tab panel can be added as the content widget of a vertical tab, in turn
 * holding widgets in horizontal tabs.
 * <p>
 * 
 * After constructing an instance of this class, there are three ways for adding widgets:
 * <ul>
 * <li>{@link #addToVerticalTabPanel(RefreshableAdminConsolePanel, String, HasPermissions)} adds a widget as a content
 * element of a vertical tab</li>
 * <li>{@link #addVerticalTab(String, String, HasPermissions)} creates a horizontal tab panel and adds it as a content
 * element of a vertical tab</li>
 * <li>{@link #addToTabPanel(TabLayoutPanel, RefreshableAdminConsolePanel, String, HasPermissions)} adds a widget as a
 * content element of a horizontal tab</li>
 * </ul>
 * 
 * Widgets to be added need to be wrapped as {@link RefreshableAdminConsolePanel} holding the widget and receiving the
 * refresh call when the widget is shown because the user has selected the tab. If the component doesn't require any
 * refresh logic, an instance of {@link DefaultRefreshableAdminConsolePanel} can be used to wrap the widget.
 * <p>
 * 
 * After the widgets have been added, {@link #initUI()} must be called to assemble all tabs for the current user's
 * roles. The {@link #initUI()} method must be called each time more widgets have been added dynamically.
 * <p>
 * 
 * For each widget added, a {@link HasPermissions set of permissions} needs to be specified, any of which is sufficient to
 * get to see the widget. When the user changes or has his/her permissions updated the set of tabs visible will be
 * adjusted according to the new roles available for the logged-in user.
 * <p>
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class AdminConsolePanel extends HeaderPanel implements HandleTabSelectable {
    private final UserService userService;
    
    /**
     * The administration console's UI depends on the user's roles. When the roles change then so shall the display of
     * tabs. Required {@link HasPermissions}s tell when they are to be made available based on the user's actual
     * permissions. This map keeps track of the dependencies and allows the UI to adjust to role changes.<p>
     * 
     * The values are the permissions, at least one of which is required from the user to be able to see the widget.
     */
    private final LinkedHashSet<Triple<VerticalOrHorizontalTabLayoutPanel, Widget, String>> roleSpecificTabs;
    
    private final Map<Widget, Set<WildcardPermission>> permissionsAnyOfWhichIsRequiredToSeeWidget;
    
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
     * If {@code null}, any permission will be accepted by
     * {@link #remeberWidgetLocationAndPermissions(VerticalOrHorizontalTabLayoutPanel, Widget, String, HasPermissions...)}
     * which is used by all methods that add a panel and optionally specify permissions required to see that panel.
     * If this field holds a valid permission collection, only permissions from this collection will be accepted.
     * This can be used to keep a central repository of all such permissions which in turn may be used to
     * automatically create a role that implies all those permissions for users to have full access to the
     * admin console and its panels.
     */
    private final Iterable<? extends WildcardPermission> acceptablePermissionsRequiredToSeeWidgets;

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
                if (source instanceof HorizontalTabLayoutPanel) {
                    final HorizontalTabLayoutPanel tabPanel = ((HorizontalTabLayoutPanel) source);
                    final Widget selectedPanel = tabPanel.getWidget(event.getSelectedItem());
                    refreshDataFor(selectedPanel);
                } else if (source instanceof VerticalTabLayoutPanel) {
                    final VerticalTabLayoutPanel verticalTabLayoutPanel = (VerticalTabLayoutPanel) source;
                    Widget widgetAssociatedToVerticalTab = verticalTabLayoutPanel.getWidget(verticalTabLayoutPanel.getSelectedIndex());
                    if (widgetAssociatedToVerticalTab instanceof HorizontalTabLayoutPanel) {
                        HorizontalTabLayoutPanel selectedTabLayoutPanel = (HorizontalTabLayoutPanel) widgetAssociatedToVerticalTab;
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

    }
    
    /**
     * If the <code>widgetMaybeWrappedByScrollPanel</code> is a scroll panel, returns the content widget,
     * otherwise <code>widgetMaybeWrappedByScrollPanel</code> is returned.
     */
    private static Widget unwrapScrollPanel(Widget widgetMaybeWrappedByScrollPanel) {
        final Widget target;
        if (widgetMaybeWrappedByScrollPanel instanceof ScrollPanel) {
            target = ((ScrollPanel) widgetMaybeWrappedByScrollPanel).getWidget();
        } else {
            target = widgetMaybeWrappedByScrollPanel;
        }
        return target;
    }
    
    public AdminConsolePanel(UserService userService, ServerInfoDTO serverInfo,
            String releaseNotesAnchorLabel, String releaseNotesURL, ErrorReporter errorReporter,
            LoginPanelCss loginPanelCss, StringMessages stringMessages) {
        this(userService, serverInfo, releaseNotesAnchorLabel, releaseNotesURL, errorReporter,
                loginPanelCss, stringMessages,
                /* acceptablePermissionsRequiredToSeeWidgets==null means accept any permission */ null);
    }

    public AdminConsolePanel(UserService userService,
            ServerInfoDTO serverInfo, String releaseNotesAnchorLabel,
            String releaseNotesURL, ErrorReporter errorReporter, LoginPanelCss loginPanelCss, StringMessages stringMessages,
            Iterable<? extends WildcardPermission> acceptablePermissionsRequiredToSeeWidgets) {
        this.acceptablePermissionsRequiredToSeeWidgets = acceptablePermissionsRequiredToSeeWidgets;
        this.permissionsAnyOfWhichIsRequiredToSeeWidget = new HashMap<>();
        this.userService = userService;
        roleSpecificTabs = new LinkedHashSet<>();
        this.panelsByWidget = new HashMap<>();
        getUserService().addUserStatusEventHandler(new UserStatusEventHandler() {
            @Override
            public void onUserStatusChange(UserDTO user, boolean preAuthenticated) {
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
            public boolean remove(Widget child) {
                return topLevelTabPanel.remove(child);
            }

            @Override
            public boolean remove(Widget child, boolean fireEvents) {
                return topLevelTabPanel.remove(topLevelTabPanel.getWidgetIndex(child), fireEvents);
            }

            @Override
            public Widget getPanel() {
                return topLevelTabPanel;
            }

            @Override
            public void selectTab(int index) {
                topLevelTabPanel.selectTab(index);
                
            }

            @Override
            public int getWidgetIndex(Widget child) {
                return topLevelTabPanel.getWidgetIndex(child);
            }
        };
        final DockPanel informationPanel = new DockPanel();
        informationPanel.setWidth("100%");
        informationPanel.setSpacing(10);
        informationPanel.add(errorReporter.getPersistentInformationWidget(), DockPanel.CENTER);
        SystemInformationPanel sysinfoPanel = new SystemInformationPanel(serverInfo, errorReporter, stringMessages);
        sysinfoPanel.ensureDebugId("SystemInformation");
        final Anchor releaseNotesLink = new Anchor(new SafeHtmlBuilder().appendEscaped(releaseNotesAnchorLabel).toSafeHtml(), releaseNotesURL);
        sysinfoPanel.add(releaseNotesLink);
        informationPanel.add(sysinfoPanel, DockPanel.EAST);
        informationPanel.setCellHorizontalAlignment(sysinfoPanel, HasHorizontalAlignment.ALIGN_RIGHT);
        this.setFooterWidget(informationPanel);
        topLevelTabPanel.setSize("100%", "100%");
        this.setContentWidget(topLevelTabPanel);
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

        boolean remove(Widget child);
        
        boolean remove(Widget child, boolean fireEvents);
        
        Widget getPanel();
        
        void selectTab(int index);
        
        int getWidgetIndex(Widget child);
    }

    /**
     * Adds a new horizontal tab panel to the top-level vertical tab panel.
     * 
     * @return the horizontal tab panel that was created and added to the top-level vertical tab panel; the panel returned can be specified
     * as argument to {@link #addToTabPanel(TabLayoutPanel, Widget, String, AdminConsoleFeatures)}.
     */
    public HorizontalTabLayoutPanel addVerticalTab(String tabTitle, String tabDebugId, WildcardPermission... requiresAnyOfThesePermissions) {
        final HorizontalTabLayoutPanel newTabPanel = new HorizontalTabLayoutPanel(2.5, Unit.EM);
        AbstractEntryPoint.setTabPanelSize(newTabPanel, "100%", "100%");
        newTabPanel.addSelectionHandler(tabSelectionHandler);
        newTabPanel.ensureDebugId(tabDebugId);
        remeberWidgetLocationAndPermissions(topLevelTabPanelWrapper, newTabPanel, tabTitle, requiresAnyOfThesePermissions);
        return newTabPanel;
    }

    /**
     * Adds an administration panel as an entry to the top-level vertical panel, without an intermediary horizontal tab panel.
     * This is useful for panels that form a top-level category of its own but don't require multiple panels to represent this
     * top-level category.
     */
    public void addToVerticalTabPanel(final RefreshableAdminConsolePanel panelToAdd, String tabTitle, WildcardPermission... requiresAnyOfThesePermissions) {
        addToTabPanel(topLevelTabPanelWrapper, panelToAdd, tabTitle, requiresAnyOfThesePermissions);
    }

    private ScrollPanel wrapInScrollPanel(Widget panelToAdd) {
        ScrollPanel scrollPanel = new ScrollPanel();
        scrollPanel.add(panelToAdd);
        panelToAdd.setSize("100%", "100%");
        return scrollPanel;
    }

    public void addToTabPanel(final HorizontalTabLayoutPanel tabPanel, RefreshableAdminConsolePanel panelToAdd, String tabTitle, WildcardPermission... requiresAnyOfThesePermissions) {
        VerticalOrHorizontalTabLayoutPanel wrapper = new VerticalOrHorizontalTabLayoutPanel() {
            @Override
            public void add(Widget child, String text, boolean asHtml) {
                tabPanel.add(child, text, asHtml);
                tabPanel.forceLayout();
            }

            @Override
            public boolean remove(Widget child) {
                return tabPanel.remove(child);
            }
            
            @Override
            public boolean remove(Widget child, boolean fireEvents) {
                return tabPanel.remove(tabPanel.getWidgetIndex(child), fireEvents);
            }

            @Override
            public Widget getPanel() {
                return tabPanel;
            }

            @Override
            public void selectTab(int index) {
               tabPanel.selectTab(index);
                
            }

            @Override
            public int getWidgetIndex(Widget child) {
                return tabPanel.getWidgetIndex(child);
            }
        };
        addToTabPanel(wrapper, panelToAdd, tabTitle, requiresAnyOfThesePermissions);
    }

    /**
     * Remembers in which tab panel the <code>panelToAdd</code> is to be displayed and for which feature; additionally, remembers adds
     * a hook so that when the <code>panelToAdd</code>'s widget is selected then the {@link RefreshableAdminConsolePanel#refreshAfterBecomingVisible()}
     * method can be called.
     */
    private void addToTabPanel(VerticalOrHorizontalTabLayoutPanel tabPanel, RefreshableAdminConsolePanel panelToAdd, String tabTitle, WildcardPermission... requiresAnyOfThesePermissions) {
        remeberWidgetLocationAndPermissions(tabPanel, wrapInScrollPanel(panelToAdd.getWidget()), tabTitle, requiresAnyOfThesePermissions);
        panelsByWidget.put(panelToAdd.getWidget(), panelToAdd);
    }

    /**
     * Remembers the tab panel in which the <code>widgetToAdd</code> is to be displayed and which permissions are
     * sufficient to see the widget. For the <code>tabPanel</code>, all permissions provided here are added to the tab
     * panel's permissions so that the user will see the tab panel as soon as the user may see any of the widgets inside
     * that panel
     * 
     * @param requiresAnyOfThesePermissions
     *            zero or more permissions; if no permissions are provided, the user will always be able to see the
     *            widget. Otherwise, if any of these permissions implies any of the permissions the user has, the user
     *            will be shown the widget.
     */
    private void remeberWidgetLocationAndPermissions(VerticalOrHorizontalTabLayoutPanel tabPanel, Widget widgetToAdd,
            String tabTitle, WildcardPermission... requiresAnyOfThesePermissions) {
        if (acceptablePermissionsRequiredToSeeWidgets != null) {
            for (final WildcardPermission requiredPermission : requiresAnyOfThesePermissions) {
                if (!Util.contains(acceptablePermissionsRequiredToSeeWidgets, requiredPermission)) {
                    throw new RuntimeException("Internal error: permission "+requiredPermission+
                            " missing from the set of acceptable admin console permissions "+acceptablePermissionsRequiredToSeeWidgets);
                }
            }
        }
        roleSpecificTabs.add(new Triple<VerticalOrHorizontalTabLayoutPanel, Widget, String>(tabPanel, widgetToAdd, tabTitle));
        final Set<WildcardPermission> permissionsAsSet = new HashSet<>(Arrays.asList(requiresAnyOfThesePermissions));
        permissionsAnyOfWhichIsRequiredToSeeWidget.put(widgetToAdd, permissionsAsSet);
        Set<WildcardPermission> permissionsForTabPanel = permissionsAnyOfWhichIsRequiredToSeeWidget.get(tabPanel.getPanel());
        if (permissionsForTabPanel == null) {
            permissionsForTabPanel = new HashSet<>();
            permissionsAnyOfWhichIsRequiredToSeeWidget.put(tabPanel.getPanel(), permissionsForTabPanel);
        }
        permissionsForTabPanel.addAll(permissionsAsSet);
    }

    /**
     * After initialization or whenever the user changes, the tab display is adjusted based on which roles are required
     * to see which tabs. See {@link #roleSpecificTabs}. A selection event is fired when the tab currently selected
     * was removed and another tab was therefore selected.
     */
    private void updateTabDisplayForCurrentUser(UserDTO user) {
        final Widget selectedPanel = getSelectedTab(null);
        for (Triple<VerticalOrHorizontalTabLayoutPanel, Widget, String> e : roleSpecificTabs) {
            final Widget widgetToAddOrRemove = e.getB();
            if (user != null && userHasPermissionsToSeeWidget(user, e.getB())) {
                if (e.getA().getWidgetIndex(widgetToAddOrRemove) == -1) {
                    e.getA().add(widgetToAddOrRemove, e.getC(), /* asHtml */false);
                }
            } else {
                e.getA().remove(widgetToAddOrRemove, /* fireEvents */ false);
            }
        }
        getSelectedTab(selectedPanel);
    }

    /**
     * If the top-level selected tab is a horizontal tab panel, its selected panel is returned; otherwise, the selected
     * top-level panel is returned. If no top-level panel exists or is selected, {@code null} is returned.
     * 
     * @param reselectCurrentSelectionIfNotSameAsThis
     *            if not {@code null}, the selected panel is {@link AbstractTabLayoutPanel#selectTab(Widget) selected}
     *            again in case it isn't the same as {@code reselectCurrentSelectionIfNotSameAsThis}, firing a selection
     *            event
     */
    private Widget getSelectedTab(Widget reselectCurrentSelectionIfNotSameAsThis) {
        final Widget topLevelSelectedTab;
        Widget selectedTabInHorizontalTabPanel = null;
        if (topLevelTabPanel.getSelectedIndex() != -1) {
            topLevelSelectedTab = unwrapScrollPanel(topLevelTabPanel.getWidget(topLevelTabPanel.getSelectedIndex()));
            if (topLevelSelectedTab instanceof AbstractTabLayoutPanel) {
                AbstractTabLayoutPanel p = (AbstractTabLayoutPanel) topLevelSelectedTab;
                if (p.getSelectedIndex() != -1) {
                    selectedTabInHorizontalTabPanel = unwrapScrollPanel(p.getWidget(p.getSelectedIndex()));
                    if (reselectCurrentSelectionIfNotSameAsThis != null && selectedTabInHorizontalTabPanel != reselectCurrentSelectionIfNotSameAsThis) {
                        SelectionEvent.fire(p, p.getSelectedIndex());
                    }
                } else {
                    selectedTabInHorizontalTabPanel = null;
                }
            } else {
                if (reselectCurrentSelectionIfNotSameAsThis != null && topLevelSelectedTab != reselectCurrentSelectionIfNotSameAsThis) {
                    SelectionEvent.fire(topLevelTabPanel, topLevelTabPanel.getSelectedIndex());
                }
            }
        } else {
            topLevelSelectedTab = null;
        }
        return selectedTabInHorizontalTabPanel != null ? selectedTabInHorizontalTabPanel : topLevelSelectedTab;
    }

    @Override
    public void selectTabByNames(String verticalTabName, String horizontalTabName, Map<String, String> params) {
        if (verticalTabName == null) {
            return;
        }

        Widget widgetForSetup = null; //Remember widget for set up
        for (Triple<VerticalOrHorizontalTabLayoutPanel, Widget, String> e : roleSpecificTabs) {
            VerticalOrHorizontalTabLayoutPanel panel = e.getA();
            Widget currentWidget = e.getB();
            if (panel == topLevelTabPanelWrapper && verticalTabName.equals(e.getC())) { // for vertical panel
                int index = panel.getWidgetIndex(currentWidget);
                panel.selectTab(index);
                if (horizontalTabName == null) {//If we don't have horizontal tab will setup vertical tab.
                    widgetForSetup = currentWidget;
                }
            } else if (horizontalTabName != null && horizontalTabName.equals(e.getC())) { // for horizontal panel
                int index = panel.getWidgetIndex(currentWidget);
                panel.selectTab(index);
                widgetForSetup = currentWidget;
            }
        }
        panelsByWidget.get(unwrapScrollPanel(widgetForSetup)).setupWidgetByParams(params);
    }

    /**
     * A user is defined to have permission to see a widget if the widget's required permissions imply any of the permissions the
     * user has. This may at first seem the wrong way around. However, the problem is that a widget cannot express a general permission
     * that is implied by any detailed permissions. Wildcard permissions don't work this way. Instead, a wildcard permission implies
     * detailed permissions. This way, if the widget requires, say, "event:*:*" (or "event" for short), this permission implies
     * all more detailed event permissions such as "event:write:9456192873". Therefore, the permissions provided for widgets
     * must imply a permission the user has in order for the user to see the tab. More detailed permissions checks can then be
     * applied at a more detailed level of the UI and, of course, in the back end. Additionally, if any of the user's permissions
     * implies the required permission (e.g., the user having "*" as the administrator's permission), permission to see the widget
     * is also implied.
     */
    private boolean userHasPermissionsToSeeWidget(UserDTO user, Widget widget) {
        final Set<WildcardPermission> permissionsRequired = permissionsAnyOfWhichIsRequiredToSeeWidget.get(widget);
        boolean hasPermission;
        if (permissionsRequired.isEmpty()) {
            hasPermission = true;
        } else {
            hasPermission = false;
            for (WildcardPermission requiredPermission : permissionsRequired) {
                // TODO bug4763: obtain ownership and ACL through a provider pattern; providers may be passed to this panel's constructor
                UserDTO anonymous = userService.getAnonymousUser();
                if (PermissionChecker.isPermitted(requiredPermission, user, user.getUserGroups(), anonymous,
                        anonymous.getUserGroups(), null, null)) {
                    hasPermission = true;
                    break;
                }
            }
        }
        return hasPermission;
    }
}
