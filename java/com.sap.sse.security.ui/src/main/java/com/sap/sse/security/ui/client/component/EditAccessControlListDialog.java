package com.sap.sse.security.ui.client.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.dto.AccessControlListAnnotationDTO;
import com.sap.sse.security.shared.dto.AccessControlListDTO;
import com.sap.sse.security.shared.dto.StrippedUserGroupDTO;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.component.EditAccessControlListDialog.AccessControlListData;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class EditAccessControlListDialog extends DataEntryDialog<AccessControlListData> {
    // private final StringMessages stringMessages;
    
    private final Grid grid;
    private final QualifiedObjectIdentifier accessControlledObjectId;
    private final List<Label> labels;
    private final List<TextBox> newGroups;
    private final List<TextBox> textBoxes;
    private final Button addGroupButton;
    
    public static class AccessControlListData {
        private final QualifiedObjectIdentifier accessControlledObjectId;
        private final Map<String, Set<String>> permissionStrings;
        
        protected AccessControlListData(QualifiedObjectIdentifier accessControlledObjectId, Map<String, Set<String>> permissionStrings) {
            super();
            this.accessControlledObjectId = accessControlledObjectId;
            this.permissionStrings = permissionStrings;
        }
        public QualifiedObjectIdentifier getAccessControlledObjectId() {
            return accessControlledObjectId;
        }
        public Map<String, Set<String>> getActionStrings() {
            return permissionStrings;
        }
    }
    

    public EditAccessControlListDialog(final StringMessages stringMessages, final UserManagementServiceAsync userManagementService, 
            final AccessControlListListDataProvider aclListDataProvider, AccessControlListAnnotationDTO acl) {
        this(stringMessages, stringMessages.editAnACL(), acl.getDisplayNameOfAnnotatedObject(), userManagementService, acl, new DialogCallback<AccessControlListData>() {
            @Override
            public void ok(AccessControlListData aclData) {
                        userManagementService.updateAccessControlList(aclData.getAccessControlledObjectId(),
                                aclData.getActionStrings(), new AsyncCallback<AccessControlListDTO>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        Window.alert(stringMessages.errorEditingACL());
                    }
                    @Override
                                    public void onSuccess(AccessControlListDTO result) {
                        aclListDataProvider.updateDisplays();
                    }
                });
            }
            @Override
            public void cancel() {
            }
        });
    }
    
    private EditAccessControlListDialog(final StringMessages stringMessages, final String title, final String message,
            final UserManagementServiceAsync userManagementService, final AccessControlListAnnotationDTO acl,
            final DialogCallback<AccessControlListData> callback) {
        super(title, message, stringMessages.ok(), stringMessages.cancel(),
                new DataEntryDialog.Validator<AccessControlListData>() {
                    @Override
                    public String getErrorMessage(AccessControlListData valueToValidate) {
                        return null; // TODO: Check if groups and permissions are valid
                    }
                }, callback);
        grid = new Grid(1, 2);
        newGroups = new ArrayList<>();
        labels = new ArrayList<>();
        textBoxes = new ArrayList<>();
        addGroupButton = new Button("+", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final TextBox newGroup = createTextBox("", 20);
                newGroups.add(newGroup);
                TextBox textBox = createTextBox("", 200);
                textBoxes.add(textBox);
                updateGrid();                
            }
        });
        accessControlledObjectId = acl.getIdOfAnnotatedObject();
        for (Entry<StrippedUserGroupDTO, Set<String>> entry : acl.getAnnotation().getActionsByUserGroup().entrySet()) {
            Label label = new Label(entry.getKey().getName());
            labels.add(label);
            String concatenated = "";
            for (String action : entry.getValue()) {
                concatenated += action + ", ";                
            }
            TextBox textBox = createTextBox(concatenated, 200);
            textBoxes.add(textBox);
        }
    }


    @Override
    protected FocusWidget getInitialFocusWidget() {
        if (textBoxes.size() > 0) {
            return textBoxes.get(0);
        }
        return addGroupButton;
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        updateGrid();
        return grid;
    }
    
    private void updateGrid() {
        grid.resize(textBoxes.size() + 1, 2);
        final int labelCount = labels.size();
        for (int i = 0; i < textBoxes.size(); i++) {
            if (i < labelCount) {
                grid.setWidget(i, 0, labels.get(i));
            } else {
                grid.setWidget(i, 0, newGroups.get(i - labelCount));
            }
            grid.setWidget(i, 1, textBoxes.get(i));
        }
        grid.setWidget(textBoxes.size(), 0, addGroupButton);
    }

    @Override
    protected AccessControlListData getResult() {
        Map<String, Set<String>> permissionMap = new HashMap<>();
        final int labelCount = labels.size();
        for (int i = 0; i < textBoxes.size(); i++) {
            TextBox textBox = textBoxes.get(i);
            String text = textBox.getText();
            String[] permissions = text.split("[ ]*,[ ]*");
            Set<String> permissionList = new HashSet<>(Arrays.asList(permissions));
            if (i < labelCount) {
                permissionMap.put(labels.get(i).getText(), permissionList);
            } else {
                permissionMap.put(newGroups.get(i - labelCount).getText(), permissionList);
            }
        }
        return new AccessControlListData(accessControlledObjectId, permissionMap);
    }
}
