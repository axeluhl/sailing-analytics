package com.sap.sse.security.ui.client.component;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.view.client.SelectionModel;
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
            final ClickHandler clickHandler ) {
        this(html, selectionModel, null, clickHandler);
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
            setText(selectedSet.isEmpty() ? html : html + " (" + selectedSet.size() + ")");
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
    
    /**
     * Helper method for creating asker for remove action using objects implementing {@link Named} interface
     * 
     * @param selectionModel    {@link SelectionModel} model for collecting removing element names
     * @return
     */
    public static <T extends Named> Supplier<Boolean> createRemoveAsker(SetSelectionModel<T> selectionModel) {
        return createRemoveAsker(selectionModel, Named:: getName);
    }

    /**
     * Helper method for creating asker for remove action
     * 
     * @param selectionModel    {@link SelectionModel} model for collecting removing element names
     * @param mapper            {@link Function} mapper for extracting element names
     * @return
     */
    public static <T> Supplier<Boolean> createRemoveAsker(SetSelectionModel<T> selectionModel, Function<T, String> mapper) {
        return () -> {
            final String names = selectionModel.getSelectedSet().stream().map(mapper).collect(Collectors.joining("\n"));
            return Window.confirm(StringMessages.INSTANCE.doYouReallyWantToRemoveSelectedElements(names));
        };
    }
}
