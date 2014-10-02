package com.sap.sse.security.ui.client.component;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.sap.sse.security.ui.shared.UserDTO;
import com.sap.sse.security.ui.shared.UserManagementServiceAsync;

public class CreateUserPanel extends FlowPanel {
    
    private List<UserCreationEventHandler> handlers = new ArrayList<>();

    public CreateUserPanel(final UserManagementServiceAsync userManagementService) {
        Label title = new Label("Create a new user");
        title.getElement().getStyle().setFontSize(25, Unit.PX);
        Label name = new Label("Username:");
        this.add(name);
        final TextBox nameBox = new TextBox();
        this.add(nameBox);
        Label email = new Label("Email:");
        this.add(email);
        final TextBox emailBox = new TextBox();
        this.add(emailBox);
        Label pw = new Label("Password:");
        this.add(pw);
        final TextBox pwBox = new PasswordTextBox();
        this.add(pwBox);
        Label pwRepeat = new Label("Repeat password:");
        this.add(pwRepeat);
        final TextBox pwRepeatBox = new PasswordTextBox();
        this.add(pwRepeatBox);
        Button create = new Button("Create", new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                String name = nameBox.getText();
                String email = emailBox.getText();
                String pw = pwBox.getText();
                String pwRepeat = pwRepeatBox.getText();
                if (name.length() >= 3 && pw.equals(pwRepeat) && pw.length() >= 5){
                    userManagementService.createSimpleUser(name, email, pw, new AsyncCallback<UserDTO>() {
                        
                        @Override
                        public void onSuccess(UserDTO result) {
                            for (UserCreationEventHandler handler : handlers){
                                handler.onUserCreation(result);
                            }
                        }
                        
                        @Override
                        public void onFailure(Throwable caught) {
                            Window.alert("Could not create user!");
                        }
                    });
                }
                else {
                    Window.alert("Invalid crendetials entered!");
                }
            }
        });
        this.add(create);
    }
    
    public void addUserCreationEventHandler(UserCreationEventHandler handler){
        this.handlers.add(handler);
    }
    
    public void removeUserCreationEventHandler(UserCreationEventHandler handler){
        this.handlers.remove(handler);
    }
    
    public static interface UserCreationEventHandler extends EventHandler {
        
        void onUserCreation(UserDTO user);
    }
}
