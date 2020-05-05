package com.sap.sse.security.ui.client.component;

import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.view.client.SetSelectionModel;
import com.sap.sse.common.Named;

/**
 * The button which shows the count of selected element(s). Optionally, a confirm callback can be passed that will be
 * invoked when not {@code null} when the button has been clicked. Constructor flavors exist that provide confirmation
 * dialog behavior, defaulting the display of {@link Named} elements by displaying their {@link Named#getName() name}.
 *
 * @author Dmitry Bilyk
 *
 */

public class SelectedElementsCountingButton<T extends Named> extends Button {
    /**
     * Constructs the button without a confirmation callback installed. The {@code clickHandler}
     * will be invoked immediately with the selection when the button is clicked.
     */
    public SelectedElementsCountingButton(final String html, final SetSelectionModel<T> selectionModel,
            final ClickHandler clickHandler) {
        this(html, selectionModel, /* no confirmation dialog */ (Supplier<Boolean>) null, clickHandler);
    }
    
    /**
     * Constructs the button so that when the button is clicked, a confirmation dialog is {@link Window#confirm(String)
     * shown} with an explicit {@code mapper} function for how to map the elements selected to strings to display in the
     * confirmation message which undergoes internationalization through the {@code messageToDisplay} message
     * provisioning function. Use this flavor if you don't simply want the element's {@link Named#getName() name} to be
     * used. Otherwise, consider using
     * {@link #SelectedElementsCountingButton(String, SetSelectionModel, Function, ClickHandler)} which simply uses the
     * element name for confirmation message display.
     */
    public SelectedElementsCountingButton(final String html, final SetSelectionModel<T> selectionModel,
            Function<T, String> mapper, Function<String, String> messageToDisplay, final ClickHandler clickHandler) {
        this(html, selectionModel, createMessageBasedAsker(selectionModel, mapper, messageToDisplay), clickHandler);
    }

    /**
     * Like {@link #SelectedElementsCountingButton(String, SetSelectionModel, Function, Function, ClickHandler)}, but
     * the elements are represented in the confirmation message by their {@link Named#getName() name}.
     */
    public SelectedElementsCountingButton(final String html, final SetSelectionModel<T> selectionModel,
            Function<String, String> messageToDisplay, final ClickHandler clickHandler) {
        this(html, selectionModel, createMessageBasedAsker(selectionModel, messageToDisplay), clickHandler);
    }

    /**
     * Allows callers to implement specific behavior for the confirmation step if a non-{@code null} {@code asker} is
     * passed. A non-{@code null} {@code asker} will be invoked when the button is clicked. The {@code clickHandler}
     * will be invoked after the button has been clicked and either {@code asker} was {@code null} or
     * {@link Supplier#get() invoking} the {@code asker} has returned a {@code true} result.
     */
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
     * Creates an "asker" callback that {@link Window#confirm(String) shows a confirmation dialog} whose message is
     * assembled by mapping each element selected through the name mapping function, separated by newlines, and then
     * passing it to the i18n message function {@code messageToDisplay}.
     * 
     * @param mapper
     *            maps the element of type {@code T} to a {@link String} that is to be used for representing the element
     *            in the confirmation message, separated by newlines in case several elements are selected, before
     *            passing it to {@code messageToDisplay} for i18n support
     * @param messageToDisplay
     *            takes the newline-separated string representations of the elements selected (as obtaine by passing
     *            them through the {@code mapper} function) as parameter and returns an internationalized message to
     *            display
     */
    private static <T> Supplier<Boolean> createMessageBasedAsker(SetSelectionModel<T> selectionModel, Function<T, String> mapper, Function<String, String> messageToDisplay) {
        return () -> {
            final String names = selectionModel.getSelectedSet().stream().map(mapper).collect(Collectors.joining("\n"));
            return Window.confirm(messageToDisplay.apply(names));
        };
    }
    
    /**
     * Creates an "asker" callback that {@link Window#confirm(String) shows a confirmation dialog} whose message is
     * assembled by mapping each of the {@link Named} elements selected to their {@link Named#getName() name}, separated
     * by newlines, and then passing it to the i18n message function {@code messageToDisplay}.
     * 
     * @param messageToDisplay
     *            takes the newline-separated string representations of the elements selected (as obtaine by passing
     *            them through the {@code mapper} function) as parameter and returns an internationalized message to
     *            display
     */
    private static <T extends Named> Supplier<Boolean> createMessageBasedAsker(SetSelectionModel<T> selectionModel, Function<String, String> messageToDisplay) {
        return createMessageBasedAsker(selectionModel, Named::getName, messageToDisplay);
    }
}
