package com.sap.sse.security.ui.client.component;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.view.client.SetSelectionModel;
import com.sap.sse.security.shared.dto.NamedDTO;
import com.sap.sse.security.ui.client.i18n.StringMessages;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * The button can draw the count of selected element. Optionally callback could be passed from outside and confirmation
 * window could appear
 *
 * @author Dmitry Bilyk
 *
 */

public class SelectedElementsCountingButton extends Button {

    public <T extends NamedDTO> SelectedElementsCountingButton(final String html,
            final SetSelectionModel<T> selectionModel, final ClickHandler clickHandler) {
        super(html);
        setEnabled(!selectionModel.getSelectedSet().isEmpty());
        addSelectionEventHandler(html, selectionModel);
        addClickHandler(event -> {
            if (askUserForConfirmation(selectionModel)) {
                clickHandler.onClick(event);
            }
        });
    }

    public <T extends NamedDTO> SelectedElementsCountingButton(final String html,
            final SetSelectionModel<T> selectionModel) {
        super(html);
        setEnabled(!selectionModel.getSelectedSet().isEmpty());
        addSelectionEventHandler(html, selectionModel);
    }

    public <T> SelectedElementsCountingButton(SetSelectionModel<T> selectionModel, String html, ClickHandler handler) {
        super(html, handler);
        setEnabled(!selectionModel.getSelectedSet().isEmpty());
        addSelectionEventHandler(html, selectionModel);
    }

    private <T> void addSelectionEventHandler(String html, SetSelectionModel<T> selectionModel) {
        selectionModel.addSelectionChangeHandler(event -> {
            Set<T> selectedSet = selectionModel.getSelectedSet();
            setText(selectedSet.size() <= 1 ? html : html + " (" + selectedSet.size() + ")");
            setEnabled(!selectedSet.isEmpty());
        });
    }

    private <T extends NamedDTO> boolean askUserForConfirmation(SetSelectionModel<T> selectionModel) {
        final String names = selectionModel.getSelectedSet().stream().map(NamedDTO::getName)
                .collect(Collectors.joining("\n"));
        return Window.confirm(StringMessages.INSTANCE.doYouReallyWantToRemoveSelectedElements(names));
    }
}
