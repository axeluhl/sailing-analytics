package com.sap.sse.gwt.client.dialog;

import java.text.ParseException;

import com.google.gwt.dom.client.Document;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.user.client.ui.ValueBox;

public class DoubleBox extends ValueBox<Double> {
    private static class PreciseDoubleRenderer extends AbstractRenderer<Double> {
        private final NumberFormat format;
        
        public PreciseDoubleRenderer(NumberFormat format) {
            this.format = format;
        }

        @Override
        public String render(Double object) {
            final String result;
            if (object == null) {
                result = "";
            } else {
                result = format.format(object);
            }
            return result;
        }
    }

    public DoubleBox() {
        this(NumberFormat.getDecimalFormat().overrideFractionDigits(0, 1000));
    }
    
    private DoubleBox(NumberFormat format) {
        super(Document.get().createTextInputElement(), new PreciseDoubleRenderer(format), new Parser<Double>() {
            @Override
            public Double parse(CharSequence text) throws ParseException {
                final String s = text.toString();
                final Double result;
                if (s.trim().equals("")) {
                    result = null;
                } else {
                    result = format.parse(text.toString());
                }
                return result;
            }
        });
    }
}
