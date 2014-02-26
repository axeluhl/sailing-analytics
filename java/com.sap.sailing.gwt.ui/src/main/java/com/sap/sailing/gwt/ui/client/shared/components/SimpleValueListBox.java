package com.sap.sailing.gwt.ui.client.shared.components;

import java.io.IOException;

import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ValueListBox;

/**
 * A simple implementation of {@link ValueListBox}, that uses the <code>toString()</code>-Method to render it's content.
 * 
 * @author Lennart
 * @see Renderer
 */
public class SimpleValueListBox<T> extends ValueListBox<T> {
    
    public SimpleValueListBox() {
        super(new Renderer<T>() {
            @Override
            public String render(T object) {
                if (object == null) {
                    return "";
                }
                return object.toString();
            }
            @Override
            public void render(T object, Appendable appendable) throws IOException {
                appendable.append(render(object));
            }
        });
    }

}
