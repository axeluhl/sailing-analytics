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
import com.sap.sse.security.shared.UserGroupDTO;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;

public class UserGroupListDataProvider extends AbstractDataProvider<UserGroupDTO> {
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
    protected void onRangeChanged(final HasData<UserGroupDTO> display) {
        final Range range = display.getVisibleRange();
        userManagementService.getUserGroups(new AsyncCallback<Collection<UserGroupDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert(caught.getMessage());
            }
   
            @Override
            public void onSuccess(Collection<UserGroupDTO> result) {
                List<UserGroupDTO> resultList = new ArrayList<>();
                for (UserGroupDTO userGroup : result) {
                    if (userGroup.getName().contains(filterBox.getText())) {
                        resultList.add(userGroup);
                    }
                }
                List<UserGroupDTO> show = new ArrayList<>();
                int start = range.getStart();
                int end = range.getStart() + range.getLength();
                for (int i = start; show.size() < end && i < resultList.size(); i++) {
                    final UserGroupDTO e = resultList.get(i);
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
        for (HasData<UserGroupDTO> hd : getDataDisplays()) {
            onRangeChanged(hd);
        }
    }
    
    public void addChangeHandler(UserGroupListDataProviderChangeHandler handler) {
        handlers.add(handler);
    }
}
