package com.sap.sailing.gwt.home.desktop.partials.header;

import static com.google.gwt.dom.client.Style.Display.BLOCK;
import static com.google.gwt.dom.client.Style.Display.NONE;
import static com.google.gwt.dom.client.Style.Visibility.HIDDEN;
import static com.google.gwt.dom.client.Style.Visibility.VISIBLE;
import static com.sap.sse.gwt.shared.DebugConstants.DEBUG_ID_ATTRIBUTE;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Text;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.place.shared.Place;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.impl.HyperlinkImpl;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.home.client.place.event.legacy.EventPlace;
import com.sap.sailing.gwt.home.client.place.event.legacy.RegattaPlace;
import com.sap.sailing.gwt.home.desktop.app.DesktopPlacesNavigator;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.partials.header.HeaderConstants;
import com.sap.sailing.gwt.home.shared.places.event.AbstractEventPlace;
import com.sap.sailing.gwt.home.shared.places.events.EventsPlace;
import com.sap.sailing.gwt.home.shared.places.searchresult.SearchResultPlace;
import com.sap.sailing.gwt.home.shared.places.solutions.SolutionsPlace;
import com.sap.sailing.gwt.home.shared.places.solutions.SolutionsPlace.SolutionsNavigationTabs;
import com.sap.sailing.gwt.home.shared.places.start.StartPlace;
import com.sap.sailing.gwt.home.shared.utils.DropdownHandler;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.mvp.PlaceChangedEvent;
import com.sap.sse.gwt.shared.ClientConfiguration;
import com.sap.sse.gwt.shared.DebugConstants;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes.ServerActions;
import com.sap.sse.security.ui.authentication.AuthenticationContextEvent;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;
import com.sap.sse.security.ui.authentication.view.AuthenticationMenuView;
import com.sap.sse.security.ui.authentication.view.AuthenticationMenuViewImpl;

public class Header extends Composite implements HeaderConstants {
    
    private static final Logger LOG = Logger.getLogger(Header.class.getName());
    
    @UiField Anchor startPageLinkMenu;
    @UiField Anchor eventsPageLinkMenu;
    @UiField Anchor solutionsPageLinkMenu;
    @UiField Anchor adminConsolePageLinkMenu;
    @UiField Anchor dataMiningPageLinkMenu;
    
    @UiField Anchor startPageLink;
    @UiField Anchor eventsPageLink;
    @UiField Anchor solutionsPageLink;
    @UiField AnchorElement logoAnchor;
    @UiField Anchor adminConsolePageLink;
    @UiField Anchor dataMiningPageLink;
    @UiField TextBox searchText;
    @UiField Button searchButton;
    @UiField Anchor headerNavigationIcon;
    @UiField Element headerNavigationDropDownMenuContainer;
    @UiField Element rightMenuPanel;
    
    @UiField Anchor usermenu;
    
    @UiField ImageElement logoImage;

    private static final HyperlinkImpl HYPERLINK_IMPL = GWT.create(HyperlinkImpl.class);
    private final List<Anchor> links;
    private final DesktopPlacesNavigator navigator;
    private final PlaceNavigation<StartPlace> homeNavigation;
    private final PlaceNavigation<EventsPlace> eventsNavigation;
    private final PlaceNavigation<SolutionsPlace> solutionsNavigation;
    private final AuthenticationMenuView authenticationMenuView;
    
    interface HeaderUiBinder extends UiBinder<Widget, Header> {
    }
    
    private static final class MenuItemVisibilityHandler implements ResizeHandler{
        private static final int visibiltyThreshold = 40;
        private final Map<Anchor,Anchor> menuToDropDownItemMap;
        private final Anchor headerNavigationIcon;
        private final DropdownHandler dropdownHandler;
        private final Element rightMenuPanel;

        public MenuItemVisibilityHandler(Map<Anchor,Anchor> menuToDropDownItemMap, DropdownHandler dropdownHandler, Anchor headerNavigationIcon, Element rightMenuPanel) {
            this.dropdownHandler = dropdownHandler;
            this.headerNavigationIcon = headerNavigationIcon;
            this.menuToDropDownItemMap = menuToDropDownItemMap;
            this.rightMenuPanel = rightMenuPanel;
        }

        @Override
        public void onResize(ResizeEvent event) {
            refreshVisibility();
        }

        private boolean isVisibilityInMenuBar(Anchor anchor) {
            int offsetTop = anchor.getElement().getOffsetTop();
            return offsetTop < visibiltyThreshold;
        }
        

        public void refreshVisibility() {
            LOG.info("refesh visibility");
            int noOfVisibleItems = 0;
            for (Map.Entry<Anchor, Anchor> item : menuToDropDownItemMap.entrySet()) {
                Anchor menuAnchor = item.getKey();
                Element listItem = item.getValue().getElement().getParentElement();
                Display isVisible = isVisibilityInMenuBar(menuAnchor) ? NONE : BLOCK;
                listItem.getStyle().setDisplay(isVisible);
                noOfVisibleItems += isVisible == BLOCK ? 1 : 0;
            }
            this.headerNavigationIcon.getElement().getStyle()
                    .setVisibility(noOfVisibleItems == 0 ? HIDDEN : VISIBLE);
            if (noOfVisibleItems == 0) { // hide if nothing to display. Otherwise do not touch visibility
                this.dropdownHandler.setVisible(false);
            }
        }

        public void refreshVisibility(int delayMillis) {
            new Timer() {
                @Override
                public void run() {
                    refreshVisibility();
                }
            }.schedule(delayMillis);
        }

        public void refreshVisibilityDeferred() {
            Anchor anchor = (Anchor)menuToDropDownItemMap.values().toArray()[0];
            Element element = anchor.getElement();
            redraw(element);
            Scheduler.get().scheduleDeferred(this::refreshVisibility);
        }

        private void redraw(Element element) {
            Text dummyText = element.getOwnerDocument().createTextNode(" ");
            element.appendChild(dummyText);
            String strDisplay = element.getStyle().getDisplay();
            Display display = strDisplay == null || strDisplay.equals("") ? Display.INITIAL : Display.valueOf(strDisplay.toUpperCase());
            element.getStyle().setDisplay(Display.NONE);

            Scheduler.get().scheduleDeferred(() -> {
                element.removeChild(dummyText);
                element.getStyle().setDisplay(display);
            });
        }

    }

    private static HeaderUiBinder uiBinder = GWT.create(HeaderUiBinder.class);

    private MenuItemVisibilityHandler menuItemVisibilityHandler;

    public Header(final DesktopPlacesNavigator navigator, EventBus eventBus) {
        this.navigator = navigator;
        HeaderResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        Map<Anchor,Anchor> menuToDropDownItemMap = new HashMap<>();
        menuToDropDownItemMap.put(startPageLink, startPageLinkMenu);
        menuToDropDownItemMap.put(eventsPageLink, eventsPageLinkMenu);
        menuToDropDownItemMap.put(solutionsPageLink, solutionsPageLinkMenu);
        menuToDropDownItemMap.put(adminConsolePageLink, adminConsolePageLinkMenu);
        menuToDropDownItemMap.put(dataMiningPageLink, dataMiningPageLinkMenu);
        
        startPageLink.getElement().setId("startPageLink");
        eventsPageLink.getElement().setId("eventsPageLink");
        solutionsPageLink.getElement().setId("solutionsPageLink");
        adminConsolePageLink.getElement().setId("adminConsolePageLink");
        dataMiningPageLink.getElement().setId("dataMiningPageLink");
        headerNavigationDropDownMenuContainer.getStyle().setDisplay(Display.NONE);
        final DropdownHandler dropdownHandler = new DropdownHandler(headerNavigationIcon, headerNavigationDropDownMenuContainer);
        menuItemVisibilityHandler = new MenuItemVisibilityHandler(menuToDropDownItemMap, dropdownHandler, headerNavigationIcon, rightMenuPanel);
        Window.addResizeHandler(menuItemVisibilityHandler);
        links = Arrays.asList(new Anchor[] { startPageLink, eventsPageLink, solutionsPageLink, adminConsolePageLink, dataMiningPageLink });
        homeNavigation = navigator.getHomeNavigation();
        eventsNavigation = navigator.getEventsNavigation();
        solutionsNavigation = navigator.getSolutionsNavigation(SolutionsNavigationTabs.SapInSailing);
        startPageLink.setHref(homeNavigation.getTargetUrl());
        eventsPageLink.setHref(eventsNavigation.getTargetUrl());
        solutionsPageLink.setHref(solutionsNavigation.getTargetUrl());
        // make the Admin and DataMining links visible only for signed-in users
        adminConsolePageLink.getElement().getStyle().setDisplay(Display.NONE);
        dataMiningPageLink.getElement().getStyle().setDisplay(Display.NONE);
        eventBus.addHandler(AuthenticationContextEvent.TYPE, event->{
            AuthenticationContext authContext = event.getCtx();
            LOG.fine("current user:" + authContext.getCurrentUser());
            // make it point to the current server if the user has CREATE_OBJECT permission there
            if (authContext.isLoggedIn() && authContext.hasServerPermission(ServerActions.CREATE_OBJECT)) {
                adminConsolePageLinkMenu.setHref(ADMIN_CONSOLE_PATH);
                adminConsolePageLinkMenu.setTarget(ADMIN_CONSOLE_WINDOW);
                adminConsolePageLink.setHref(ADMIN_CONSOLE_PATH);
                adminConsolePageLink.setTarget(ADMIN_CONSOLE_WINDOW);
                adminConsolePageLink.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
            } else if (authContext.getCurrentUser() != null && !authContext.getCurrentUser().getName().equals("Anonymous")) {
                // make it point to the default "manage events" self-service server configured in ServerInfo otherwise
                String base = authContext.getServerInfo().getManageEventsBaseUrl();
                adminConsolePageLinkMenu.setHref(UriUtils.fromString(base + ADMIN_CONSOLE_PATH));
                adminConsolePageLinkMenu.setTarget(ADMIN_CONSOLE_WINDOW);
                adminConsolePageLink.setHref(UriUtils.fromString(base + ADMIN_CONSOLE_PATH));
                adminConsolePageLink.setTarget(ADMIN_CONSOLE_WINDOW);
                adminConsolePageLink.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
            } else {
                adminConsolePageLink.getElement().getStyle().setDisplay(Display.NONE);
            }
            if (authContext.hasServerPermission(ServerActions.DATA_MINING)) {
                dataMiningPageLinkMenu.setHref(DATA_MINING_PATH);
                dataMiningPageLinkMenu.setTarget(DATA_MINING_WINDOW);
                dataMiningPageLink.setHref(DATA_MINING_PATH);
                dataMiningPageLink.setTarget(DATA_MINING_WINDOW);
                dataMiningPageLink.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
            } else {
                dataMiningPageLink.getElement().getStyle().setDisplay(Display.NONE);
            }
            menuItemVisibilityHandler.refreshVisibilityDeferred();
        });
        searchText.getElement().setAttribute("placeholder", StringMessages.INSTANCE.headerSearchPlaceholder());
        searchText.addFocusHandler((focusEvent) -> menuItemVisibilityHandler.refreshVisibility(370));
        searchText.addBlurHandler(blurEvent -> {
            menuItemVisibilityHandler.refreshVisibility(370);
        });
        searchText.addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
                    searchButton.click();
                }
            }
        });
        eventBus.addHandler(PlaceChangedEvent.TYPE, new PlaceChangedEvent.Handler() {
            @Override
            public void onPlaceChanged(PlaceChangedEvent event) {
                updateActiveLink(event.getNewPlace());
            }
        });
        authenticationMenuView = new AuthenticationMenuViewImpl(usermenu, HeaderResources.INSTANCE.css().loggedin(), HeaderResources.INSTANCE.css().open());
        if (!ClientConfiguration.getInstance().isBrandingActive()) {
            logoImage.getStyle().setDisplay(Display.NONE);
            solutionsPageLink.getElement().getStyle().setDisplay(Display.NONE);
            logoAnchor.setHref("");
        }
        logoImage.setAttribute(DebugConstants.DEBUG_ID_ATTRIBUTE, "logoImage");
        solutionsPageLink.getElement().setAttribute(DEBUG_ID_ATTRIBUTE, "solutionsPageLink");
        logoAnchor.setAttribute(DEBUG_ID_ATTRIBUTE, "logoAnchor");
        eventsPageLink.getElement().setAttribute(DEBUG_ID_ATTRIBUTE, "eventsPage");

        // register event handler for dropdown items
        startPageLinkMenu.addClickHandler(this::goToHome);
        eventsPageLinkMenu.addClickHandler(this::goToEvents);
        solutionsPageLinkMenu.addClickHandler(this::goToSolutions);
        //remaining entries please see at registration for AuthenticationContextEvent 
        // refresh after UI has been put into place
        menuItemVisibilityHandler.refreshVisibilityDeferred();
    }

    @UiHandler("startPageLink")
    public void goToHome(ClickEvent e) {
        handleClickEvent(e, homeNavigation, startPageLink);
    }

    @UiHandler("eventsPageLink")
    public void goToEvents(ClickEvent e) {
        handleClickEvent(e, eventsNavigation, eventsPageLink);
    }

    @UiHandler("solutionsPageLink")
    public void goToSolutions(ClickEvent e) {
        handleClickEvent(e, solutionsNavigation, solutionsPageLink);
    }

    @UiHandler("searchButton")
    void searchButtonClick(ClickEvent event) {
        PlaceNavigation<SearchResultPlace> searchResultNavigation = navigator.getSearchResultNavigation(searchText
                .getText());
        navigator.goToPlace(searchResultNavigation);
    }
    
    private void updateActiveLink(Place place) {
        if(place instanceof EventsPlace
                || place instanceof AbstractEventPlace
                || place instanceof EventPlace
                || place instanceof RegattaPlace) {
            setActiveLink(eventsPageLink);
        } else if(place instanceof StartPlace) {
            setActiveLink(startPageLink);
        } else if(place instanceof SolutionsPlace) {
            setActiveLink(solutionsPageLink);
        } else {
            setActiveLink(null);
        }
    }
    
    private void setActiveLink(Anchor link) {
        final String activeStyle = HeaderResources.INSTANCE.css().sitenavigation_linkactive();
        for (Anchor l : links) {
            if (l == link) {
                l.addStyleName(activeStyle);
            } else {
                l.removeStyleName(activeStyle);
            }
        }
    }

    private void handleClickEvent(ClickEvent e, PlaceNavigation<?> placeNavigation, Anchor activeLink) {
        if (HYPERLINK_IMPL.handleAsClick((Event) e.getNativeEvent())) {
            navigator.goToPlace(placeNavigation);
            e.preventDefault();
            setActiveLink(activeLink);
         }
    }

    public AuthenticationMenuView getAuthenticationMenuView() {
        return authenticationMenuView;
    }
}
