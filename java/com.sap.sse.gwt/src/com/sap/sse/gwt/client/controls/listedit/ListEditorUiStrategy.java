package com.sap.sse.gwt.client.controls.listedit;

import com.google.gwt.user.client.ui.Widget;

/**
 * Describes what a UI strategy for a {@link ListEditorComposite value list editor} must provide and which callbacks it
 * will receive. The {@link #initWidget()} method provides the core UI element used to display an editable or uneditable
 * list of values of type <code>ValueType</code>. When the {@link #refresh()} method is invoked by the framework, the
 * implementing class needs to update the contents of the widget returned from {@link #initWidget} based on the
 * {@link ListEditorComposite#getValue() values} of the context editor that was passed to
 * {@link #setContext(ListEditorComposite)} during editor initialization.<p>
 * 
 * Objects will receive calls to {@link #onRowAdded()} when a value was added to the list, and {@link #onRowRemoved(int)} when
 * a value was removed from the list.<p>
 * 
 * There are two default implementations of this interface: {@link CollapsedListEditorUi} and {@link ExpandedListEditorUi}.
 * 
 * @author Lukas Niemeier
 * @author Axel Uhl (D043530)
 * 
 * @param <ValueType>
 */
public interface ListEditorUiStrategy<ValueType> {
    /**
     * Called by the framework before any other operations of this object are called.
     * 
     * @param context
     *            the editor using this strategy to display and/or edit the value list. The
     *            {@link ListEditorComposite#getValue()} method can be used to obtain the current
     *            list of values from the editor.
     */
    void setContext(ListEditorComposite<ValueType> context);

    /**
     * Called by the framework to obtain the UI widget to display to the user to show and/or make
     * editable the list of values managed by the context {@link ListEditorComposite editor}.
     */
    Widget initWidget();

    /**
     * Instructs this object to refresh the contents of the widget returned from {@link #initWidget()}
     * based on the {@link ListEditorComposite#getValue() values} managed by the editor. This method
     * is called when the entire value list has changed or has been set for the first time.
     */
    void refresh();

    /**
     * Called by the framework after a value was added to the list. Note that this is not the place
     * for this UI strategy to actually implement the UI changes. Those are expected to already have
     * happened before.
     */
    void onRowAdded();
    
    /**
     * Called by the framework after a value was removed from the list. Note that this is not the place for this UI
     * strategy to actually implement the UI changes. Those are expected to already have happened before.
     * @param rowIndex The index of the removed row
     */
    void onRowRemoved(int rowIndex);
}