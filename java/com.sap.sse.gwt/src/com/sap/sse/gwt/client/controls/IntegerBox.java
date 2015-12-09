package com.sap.sse.gwt.client.controls;

import java.io.IOException;

import com.google.gwt.dom.client.Document;
import com.google.gwt.text.client.IntegerParser;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ValueBox;

/**
 * Renders integers in their box without any separators, non-localized, plain. 
 */
public class IntegerBox extends ValueBox<Integer> {
    private static final Renderer<Integer> RENDERER = new Renderer<Integer>() {
        public String render(Integer object) {
          if (object == null) {
            return null;
          }
          StringBuilder sb = new StringBuilder(String.valueOf(object));
          return sb.toString();
        }

        public void render(Integer object, Appendable appendable) throws IOException {
          appendable.append(render(object));
        }
     };
     
     public IntegerBox() {
         super(Document.get().createTextInputElement(), RENDERER, IntegerParser.instance());
     }
}
