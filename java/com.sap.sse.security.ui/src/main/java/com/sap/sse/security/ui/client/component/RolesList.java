package com.sap.sse.security.ui.client.component;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sse.security.ui.client.UserChangeEventHandler;
import com.sap.sse.security.ui.shared.SuccessInfo;
import com.sap.sse.security.ui.shared.UserDTO;
import com.sap.sse.security.ui.shared.UserManagementServiceAsync;

public class RolesList extends DecoratorPanel {
    
    private UserManagementServiceAsync userManagementService;
    private UserDTO userDTO;
    
    private List<UserChangeEventHandler> handlers = new ArrayList<>();
    
    ProvidesKey<String> keyProvider = new ProvidesKey<String>() {
        public Object getKey(String item) {
            // Always do a null check.
            return (item == null) ? null : item;
        }
    };
    
    private ListDataProvider<String> rolesDataProvider;
    
    SingleSelectionModel<String>  singleSelectionModel = new SingleSelectionModel<>(keyProvider);
    
    private CellList<String> roleList;
    private TextBox roleBox;
    
    public RolesList(UserManagementServiceAsync userManagementService, UserDTO userDTO) {
        super();
        this.userManagementService = userManagementService;
        this.userDTO = userDTO;
        FlowPanel fp = new FlowPanel();
        fp.add(new Label("Roles:"));
        HorizontalPanel hp = new HorizontalPanel();
        roleBox = new TextBox();
        roleBox.getElement().setPropertyString("placeholder", "Add role...");
        hp.add(roleBox);
        Button addRole = new Button("Add role", new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                RolesList.this.userManagementService.addRoleForUser(RolesList.this.userDTO.getName(), roleBox.getText(), new AsyncCallback<SuccessInfo>() {
                    
                    @Override
                    public void onSuccess(SuccessInfo result) {
                        if (result.isSuccessful()){
                            Window.alert("Success: "+result.getMessage());
                            for (UserChangeEventHandler handler : handlers){
                                handler.onUserChange(RolesList.this.userDTO);
                            }
                        }
                        else {
                            Window.alert("Failed: "+result.getMessage());
                        }
                    }
                    
                    @Override
                    public void onFailure(Throwable caught) {
                        Window.alert("An error occured while adding the role: " + caught);
                    }
                });
            }
        });
        hp.add(addRole);
        fp.add(hp);
        
        rolesDataProvider = new ListDataProvider<>(userDTO.getRoles(), keyProvider);
        roleList = new CellList<>(new TextCell(), keyProvider);
        rolesDataProvider.addDataDisplay(roleList);
        
        fp.add(roleList);
        
        fp.setWidth("400px");
        this.setWidget(fp);
    }

    public void addUserChangeEventHandler(UserChangeEventHandler handler){
        this.handlers.add(handler);
    }
    
    public void removeUserChangeEventHandler(UserChangeEventHandler handler){
        this.handlers.remove(handler);
    }
}
