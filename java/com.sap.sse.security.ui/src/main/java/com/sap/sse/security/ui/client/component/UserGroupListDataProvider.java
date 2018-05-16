package com.sap.sse.security.ui.client.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.sap.sse.security.shared.UserGroup;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;

public class UserGroupListDataProvider extends AbstractDataProvider<UserGroup> {
    private UserManagementServiceAsync userManagementService;
    private TextBox filterBox;
    
    public interface UserGroupListDataProviderChangeHandler {
        void onChange();
    }
    
    private final List<UserGroupListDataProviderChangeHandler> handlers;
    
    public UserGroupListDataProvider(UserManagementServiceAsync userManagementService, TextBox filterBox) {
       this.userManagementService = userManagementService;
       this.filterBox = filterBox;
       this.handlers = new ArrayList<>();
       
       filterBox.addChangeHandler(new ChangeHandler() {
           @Override
           public void onChange(ChangeEvent event) {
               UserGroupListDataProvider.this.updateDisplays();
           }
       });
       filterBox.addKeyUpHandler(new KeyUpHandler() {
           @Override
           public void onKeyUp(KeyUpEvent event) {
               UserGroupListDataProvider.this.updateDisplays();
           }
       });
    }
   
    @Override
    protected void onRangeChanged(final HasData<UserGroup> display) {
        final Range range = display.getVisibleRange();
        userManagementService.getUserGroups(new AsyncCallback<Collection<UserGroup>>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert(caught.getMessage());
            }
   
            @Override
            public void onSuccess(Collection<UserGroup> result) {
                List<UserGroup> resultList = new ArrayList<>();
                for (UserGroup userGroup : result) {
                    if (userGroup.getName().contains(filterBox.getText())) {
                        resultList.add(userGroup);
                    }
                }
                List<UserGroup> show = new ArrayList<>();
                int start = range.getStart();
                int end = range.getStart() + range.getLength();
                for (int i = start; show.size() < end && i < resultList.size(); i++) {
                    final UserGroup e = resultList.get(i);
                    show.add(e);
                }
                updateRowData(start, show);
                updateRowCount(resultList.size(), true);
                for (UserGroupListDataProviderChangeHandler handler : handlers) {
                    handler.onChange();
                }
            }
        });
    }
   
    public void updateDisplays() {
        for (HasData<UserGroup> hd : getDataDisplays()) {
            onRangeChanged(hd);
        }
    }
    
    public void addChangeHandler(UserGroupListDataProviderChangeHandler handler) {
        handlers.add(handler);
    }
}
