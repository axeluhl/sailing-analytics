package com.sap.sse.security.ui.client.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ImageResourceRenderer;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sse.security.ui.client.Resources;
import com.sap.sse.security.ui.client.StringMessages;
import com.sap.sse.security.ui.client.UserChangeEventHandler;
import com.sap.sse.security.ui.oauth.client.SocialUserDTO;
import com.sap.sse.security.ui.shared.AccountDTO;
import com.sap.sse.security.ui.shared.SuccessInfo;
import com.sap.sse.security.ui.shared.UserDTO;
import com.sap.sse.security.ui.shared.UserManagementServiceAsync;
import com.sap.sse.security.ui.shared.UsernamePasswordAccountDTO;

public class UserDetailsView extends FlowPanel {
    private UserManagementServiceAsync userManagementService;

    private List<UserChangeEventHandler> handlers = new ArrayList<>();

    private final StringMessages stringMessages;

    public UserDetailsView(UserManagementServiceAsync userManagementService, UserDTO user, StringMessages stringMessages) {
        this.stringMessages = stringMessages;
        this.userManagementService = userManagementService;
        addStyleName("userDetailsView");
        updateUser(user);
    }

    public void updateUser(final UserDTO user) {
        this.clear();
        Label title = new Label(stringMessages.userDetails());
        if (user == null) {
            return;
        }
        title.getElement().getStyle().setFontSize(25, Unit.PX);
        this.add(title);

        DecoratorPanel decoratorPanel = new DecoratorPanel();
        FlowPanel fp = new FlowPanel();
        fp.setWidth("100%");
        ImageResourceRenderer renderer = new ImageResourceRenderer();
        final ImageResource userImageResource = Resources.INSTANCE.userSmall();
        fp.add(new HTML(renderer.render(userImageResource)));
        Label name = new Label(stringMessages.name() + ": " + user.getName());
        fp.add(name);
        Label email = new Label(stringMessages.email() + ": " + user.getEmail());
        fp.add(email);
        
        for (AccountDTO a : user.getAccounts()){
            DecoratorPanel accountPanelDecorator = new DecoratorPanel();
            FlowPanel accountPanelContent = new FlowPanel();
            accountPanelDecorator.setWidget(accountPanelContent);
            accountPanelContent.add(new Label(a.getAccountType() + "-Account"));
            if (a instanceof UsernamePasswordAccountDTO){
                accountPanelContent.add(new Label(stringMessages.changePassword()));
            }
            else if (a instanceof SocialUserDTO){
                SocialUserDTO sua = (SocialUserDTO) a;
                FlexTable table = new FlexTable();
                int i = 0;
                for (Entry<String, String> e : sua.getProperties().entrySet()){
                    if (e.getValue() != null){
                        table.setText(i, 0, e.getKey().toLowerCase().replace('_', ' '));
                        table.setText(i, 1, e.getValue());
                        i++;
                    }
                }
                accountPanelContent.add(table);
            }
            fp.add(accountPanelDecorator);
        }
        
        decoratorPanel.setWidget(fp);
        this.add(decoratorPanel);

        RolesList rolesList = new RolesList(userManagementService, user);
        rolesList.addUserChangeEventHandler(new UserChangeEventHandler() {
            @Override
            public void onUserChange(UserDTO user) {
                for (UserChangeEventHandler handler : handlers){
                    handler.onUserChange(user);
                }
            }
        });
        this.add(rolesList);
        Button delete = new Button(stringMessages.deleteUser(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final DeleteUserConfimDialog deleteUserConfimDialog = new DeleteUserConfimDialog(userManagementService,
                        user.getName(), stringMessages);
                Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                    public void execute() {
                        deleteUserConfimDialog.center();
                    }
                });
                deleteUserConfimDialog.show();
                for (UserChangeEventHandler handler : handlers){
                    handler.onUserChange(user);
                }
            }
        });
        this.add(delete);
    }

    private static class DeleteUserConfimDialog extends DialogBox {
        public DeleteUserConfimDialog(final UserManagementServiceAsync userManagementService, final String username, final StringMessages stringMessages) {
            setText(stringMessages.deleteUserQuestion());
            setAnimationEnabled(true);
            setGlassEnabled(true);
            VerticalPanel vp = new VerticalPanel();
            vp.add(new Label(stringMessages.doYouReallyWantToDeleteUser(username)));
            HorizontalPanel hp = new HorizontalPanel();
            Button ok = new Button(stringMessages.ok());
            ok.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    userManagementService.deleteUser(username, new AsyncCallback<SuccessInfo>() {
                        @Override
                        public void onSuccess(SuccessInfo result) {
                            Window.alert(result.getMessage());
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            Window.alert(stringMessages.errorDeletingUser());
                        }
                    });
                    DeleteUserConfimDialog.this.hide();
                }
            });
            hp.add(ok);
            Button cancel = new Button(stringMessages.cancel(), new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    DeleteUserConfimDialog.this.hide();
                }
            });
            hp.add(cancel);
            vp.add(hp);
            setWidget(vp);
        }
    }

    public void addUserChangeEventHandler(UserChangeEventHandler handler) {
        this.handlers.add(handler);
    }

    public void removeUserChangeEventHandler(UserChangeEventHandler handler) {
        this.handlers.remove(handler);
    }
}
