package com.sap.sse.security.ui.client.component;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ImageResourceRenderer;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sse.security.ui.client.UserManagementImageResources;
import com.sap.sse.security.ui.shared.SuccessInfo;
import com.sap.sse.security.ui.shared.UserDTO;
import com.sap.sse.security.ui.shared.UserManagementServiceAsync;

public class UserDetailsView extends FlowPanel {
    private UserManagementServiceAsync userManagementService;

    private List<UserChangeEventHandler> handlers = new ArrayList<>();

    public UserDetailsView(UserManagementServiceAsync userManagementService, UserDTO user) {
        this.userManagementService = userManagementService;
        updateUser(user);
    }

    public void updateUser(final UserDTO user) {
        this.clear();
        Label title = new Label("User details");
        if (user == null) {
            return;
        }
        title.getElement().getStyle().setFontSize(25, Unit.PX);
        this.add(title);

        DecoratorPanel decoratorPanel = new DecoratorPanel();
        FlowPanel fp = new FlowPanel();
        fp.setWidth("400px");
        ImageResourceRenderer renderer = new ImageResourceRenderer();
        final ImageResource userImageResource = UserManagementImageResources.INSTANCE.userSmall();
        fp.add(new HTML(renderer.render(userImageResource)));
        Label name = new Label("Name: " + user.getName());
        fp.add(name);
        Label type = new Label("Account type: " + user.getAccountType());
        fp.add(type);
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
        Button delete = new Button("Delete user", new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                final DeleteUserConfimDialog deleteUserConfimDialog = new DeleteUserConfimDialog(userManagementService,
                        user.getName());
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

        public DeleteUserConfimDialog(final UserManagementServiceAsync userManagementService, final String username) {
            setText("Delete user?");

            setAnimationEnabled(true);
            setGlassEnabled(true);
            VerticalPanel vp = new VerticalPanel();
            vp.add(new Label("Do you really want to delete \"" + username + "\"?"));
            HorizontalPanel hp = new HorizontalPanel();
            Button ok = new Button("OK");
            ok.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    userManagementService.deleteUser(username, new AsyncCallback<SuccessInfo>() {

                        @Override
                        public void onSuccess(SuccessInfo result) {
                            Window.alert(result.getMessage());
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            Window.alert("An error happened while deleting the user.");
                        }
                    });
                    DeleteUserConfimDialog.this.hide();
                }
            });
            hp.add(ok);
            Button cancel = new Button("cancel", new ClickHandler() {

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

    public static interface UserChangeEventHandler extends EventHandler {

        void onUserChange(UserDTO user);
    }
}
