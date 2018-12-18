package com.sap.sse.security.ui.client.usermanagement;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.gwt.client.IconResources;
import com.sap.sse.gwt.client.controls.listedit.ExpandedListEditorUi;
import com.sap.sse.gwt.client.controls.listedit.ListEditorComposite;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class UserRolesListEditorComposite extends ListEditorComposite<Triple<String, String, String>> {

    UserRolesListEditorComposite(final Iterable<Triple<String, String, String>> initialValues,
            final StringMessages stringMessages, final Iterable<String> suggestValues) {
        super(initialValues, new ExpandedUi(suggestValues, stringMessages));
    }

    private static class ExpandedUi extends ExpandedListEditorUi<Triple<String, String, String>> {

        private final StringMessages stringMessages;
        private final MultiWordSuggestOracle suggestOracle = new MultiWordSuggestOracle();

        public ExpandedUi(final Iterable<String> suggestValues, final StringMessages stringMessages) {
            super(stringMessages, IconResources.INSTANCE.removeIcon(), true);
            this.stringMessages = stringMessages;
            suggestValues.forEach(suggestOracle::add);
            suggestOracle.setDefaultSuggestionsFromText(Util.asList(suggestValues));
        }

        @Override
        protected Widget createAddWidget() {
            final HorizontalPanel panel = new HorizontalPanel();
            final SuggestBox roleInput = new SuggestBox(suggestOracle);
            this.initPlaceholder(roleInput, stringMessages.enterRoleName());
            roleInput.ensureDebugId("InputSuggestBox");
            final TextBox tenantInput = new TextBox();
            this.initPlaceholder(tenantInput, stringMessages.groupName());
            final TextBox userInput = new TextBox();
            this.initPlaceholder(userInput, stringMessages.username());
            panel.add(roleInput);
            panel.add(tenantInput);
            panel.add(userInput);
            final Button addRoleButton = new Button(stringMessages.add(), (ClickHandler) event -> {
                addValue(new Triple<>(roleInput.getValue(), tenantInput.getValue(), userInput.getValue()));
                roleInput.setText("");
                tenantInput.setText("");
                userInput.setText("");
            });
            addRoleButton.ensureDebugId("AddButton");
            final Command addRoleButtonUpdater = () -> addRoleButton.setEnabled(!roleInput.getValue().isEmpty());
            roleInput.addKeyUpHandler(event -> addRoleButtonUpdater.execute());
            roleInput.addSelectionHandler(event -> addRoleButtonUpdater.execute());
            panel.add(addRoleButton);
            return panel;
        }

        @Override
        protected Widget createValueWidget(int rowIndex, Triple<String, String, String> value) {
            final boolean hasTenantQualifier = value.getB() != null && !value.getB().isEmpty();
            final boolean hasUserQualifier = value.getC() != null && !value.getC().isEmpty();
            final boolean hasQualifier = hasTenantQualifier || hasUserQualifier;
            final String seperator = WildcardPermission.PART_DIVIDER_TOKEN;
            final String tenantQualifier = (hasQualifier ? seperator : "") + (hasTenantQualifier ? value.getB() : "");
            final String userQualifier = hasUserQualifier ? (seperator + value.getC()) : "";
            return new Label(value.getA() + tenantQualifier + userQualifier);
        }

        private void initPlaceholder(final UIObject target, final String placeholder) {
            target.getElement().setAttribute("placeholder", placeholder);
        }

    }

}
