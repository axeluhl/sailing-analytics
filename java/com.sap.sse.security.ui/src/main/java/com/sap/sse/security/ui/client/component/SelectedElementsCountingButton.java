package com.sap.sse.security.ui.client.component;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.view.client.SetSelectionModel;
import com.sap.sse.common.Named;
import com.sap.sse.security.ui.client.i18n.StringMessages;

import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * The button which shows the count of selected element(s). Optionally callback could be passed from outside and
 * confirmation window could appear depending on the flag.
 *
 * @author Dmitry Bilyk
 *
 */

public class SelectedElementsCountingButton<T extends Named> extends Button {

    public SelectedElementsCountingButton(final String html, final SetSelectionModel<T> selectionModel,
            boolean withConfirmation, final ClickHandler clickHandler) {
        this(html, selectionModel, withConfirmation ? Named::getName : null, clickHandler);
    }

    public SelectedElementsCountingButton(final String html, final SetSelectionModel<T> selectionModel,
            final Function<T, String> nameMapper, final ClickHandler clickHandler) {
        this(html, selectionModel, nameMapper == null ? null : createAsker(selectionModel, nameMapper), clickHandler);
    }

    public SelectedElementsCountingButton(final String html, final SetSelectionModel<T> selectionModel,
            final Supplier<Boolean> asker, final ClickHandler clickHandler) {
        super(html);
        setEnabled(!selectionModel.getSelectedSet().isEmpty());
        addSelectionEventHandler(html, selectionModel);
        addClickHandler(asker, selectionModel, clickHandler);
    }

    private void addSelectionEventHandler(final String html, final SetSelectionModel<T> selectionModel) {
        selectionModel.addSelectionChangeHandler(event -> {
            Set<T> selectedSet = selectionModel.getSelectedSet();
            setText(selectedSet.size() <= 1 ? html : html + " (" + selectedSet.size() + ")");
            setEnabled(!selectedSet.isEmpty());
        });
    }

    private void addClickHandler(Supplier<Boolean> asker, SetSelectionModel<T> selectionModel,
            ClickHandler clickHandler) {
        if (clickHandler != null) {
            addClickHandler(event -> {
                if (asker == null || asker.get()) {
                    clickHandler.onClick(event);
                }
            });
        }
    }

    private static <T> Supplier<Boolean> createAsker(SetSelectionModel<T> selectionModel, Function<T, String> mapper) {
        return () -> {
            final String names = selectionModel.getSelectedSet().stream().map(mapper).collect(Collectors.joining("\n"));
            return Window.confirm(StringMessages.INSTANCE.doYouReallyWantToRemoveSelectedElements(names));
        };
    }
}
