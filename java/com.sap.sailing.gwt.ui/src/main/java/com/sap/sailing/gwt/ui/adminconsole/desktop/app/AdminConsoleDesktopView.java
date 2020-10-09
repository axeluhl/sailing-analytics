package com.sap.sailing.gwt.ui.adminconsole.desktop.app;

import java.util.HashSet;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HeaderPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.gwt.common.authentication.FixedSailingAuthentication;
import com.sap.sailing.gwt.common.authentication.SAPSailingHeaderWithAuthentication;
import com.sap.sailing.gwt.ui.adminconsole.EventManagementPanel;
import com.sap.sailing.gwt.ui.client.LeaderboardGroupsDisplayer;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.RegattasDisplayer;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.SecurityStylesheetResources;
import com.sap.sse.gwt.adminconsole.AdminConsolePanel;
import com.sap.sse.gwt.adminconsole.DefaultRefreshableAdminConsolePanel;
import com.sap.sse.gwt.client.DefaultErrorReporter;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.ServerInfoDTO;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.mvp.TopLevelView;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.ui.authentication.decorator.WidgetFactory;
import com.sap.sse.security.ui.authentication.generic.GenericAuthentication;
import com.sap.sse.security.ui.authentication.generic.GenericAuthorizedContentDecorator;
import com.sap.sse.security.ui.client.UserService;

/**
 * This is the top-level view of the application. Every time another presenter wants to reveal itself,
 * {@link AdminConsoleDesktopView} will add its content of the target inside the {@code mainContantPanel}.
 */
public class AdminConsoleDesktopView extends Composite implements TopLevelView, RegattaRefresher {
    
    interface AdminConsoleDesktopViewUiBinder extends UiBinder<Widget, AdminConsoleDesktopView> {
    }

    private static AdminConsoleDesktopViewUiBinder uiBinder = GWT.create(AdminConsoleDesktopViewUiBinder.class);

    private static ErrorReporter errorReporter = new DefaultErrorReporter<StringMessages>(StringMessages.INSTANCE);

    final private UserService userService;
    
    @UiField(provided=true)
    HeaderPanel headerPanel;
    
    @UiField(provided=true)
    SAPSailingHeaderWithAuthentication header;
    
    @UiField(provided=true)
    GenericAuthorizedContentDecorator content;

    @UiField
    SimplePanel mainContentPanel;
    
    private AdminConsolePanel adminConsolePanel;
    
    private SailingServiceWriteAsync sailingService;
    
    private HashSet<LeaderboardGroupsDisplayer> leaderboardGroupsDisplayers;
    private HashSet<RegattasDisplayer> regattasDisplayers;

    public AdminConsoleDesktopView(final EventBus eventBus, final UserService userService, final SailingServiceWriteAsync sailingService) {
        this.userService = userService;
        leaderboardGroupsDisplayers = new HashSet<>();
        regattasDisplayers = new HashSet<>();
        headerPanel = new HeaderPanel();
        header = new SAPSailingHeaderWithAuthentication(StringMessages.INSTANCE.administration());
        GenericAuthentication genericSailingAuthentication = new FixedSailingAuthentication(userService, header.getAuthenticationMenuView());
        content = new GenericAuthorizedContentDecorator(genericSailingAuthentication);
      
        initWidget(uiBinder.createAndBindUi(this));
        
        userService.executeWithServerInfo(this::createUI);
    }
    
    private void createUI(final ServerInfoDTO serverInfo) {         
            content.setContentWidgetFactory(new WidgetFactory() {      
                @Override
                public Widget get() {
                    return createAdminConsolePanel(serverInfo);
                }
            });
    }

    private AdminConsolePanel createAdminConsolePanel(final ServerInfoDTO serverInfo) {
        adminConsolePanel = new AdminConsolePanel(userService, 
                serverInfo, StringMessages.INSTANCE.releaseNotes(), "/release_notes_admin.html", errorReporter,
                SecurityStylesheetResources.INSTANCE.css(), StringMessages.INSTANCE);
        adminConsolePanel.addStyleName("adminConsolePanel");

        
        /* EVENTS */
        final EventManagementPanel eventManagementPanel = new EventManagementPanel(sailingService,
                userService, errorReporter, this, StringMessages.INSTANCE, adminConsolePanel);
        eventManagementPanel.ensureDebugId("EventManagement");
        adminConsolePanel.addToVerticalTabPanel(new DefaultRefreshableAdminConsolePanel<EventManagementPanel>(eventManagementPanel) {
            @Override
            public void refreshAfterBecomingVisible() {
                getWidget().fillEvents();
                fillLeaderboardGroups();
                
            }
        }, StringMessages.INSTANCE.events(), SecuredDomainType.EVENT.getPermission(DefaultActions.MUTATION_ACTIONS));
        leaderboardGroupsDisplayers.add(eventManagementPanel);      
        
        fillLeaderboardGroups();
        
        return adminConsolePanel;
    }
    
    @Override
    public AcceptsOneWidget getContent() {
        return mainContentPanel;
    }
    
    @Override
    public ErrorReporter getErrorReporter() {
        return errorReporter;
    }

    private void fillLeaderboardGroups() {
        sailingService.getLeaderboardGroups(false /*withGeoLocationData*/,
                new MarkedAsyncCallback<List<LeaderboardGroupDTO>>(
                        new AsyncCallback<List<LeaderboardGroupDTO>>() {
                            @Override
                            public void onSuccess(List<LeaderboardGroupDTO> groups) {
                                for (LeaderboardGroupsDisplayer leaderboardGroupsDisplayer : leaderboardGroupsDisplayers) {
                                    leaderboardGroupsDisplayer.fillLeaderboardGroups(groups);
                                }
                            }
                            @Override
                            public void onFailure(Throwable t) {
                                errorReporter.reportError("Error trying to obtain list of leaderboard groups: " + t.getMessage());
                            }
                        }));
    }
    

    @Override
    public void fillRegattas() {
        sailingService.getRegattas(new MarkedAsyncCallback<List<RegattaDTO>>(
                new AsyncCallback<List<RegattaDTO>>() {
                    @Override
                    public void onSuccess(List<RegattaDTO> result) {
                        for (RegattasDisplayer regattaDisplayer : regattasDisplayers) {
                            regattaDisplayer.fillRegattas(result);
                        }
                    }
        
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Remote Procedure Call getRegattas() - Failure");
                    }
                }));
    }
    
}
