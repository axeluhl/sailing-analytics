package com.sap.sse.i18n;

import java.util.Locale;
import java.util.ResourceBundle.Control;

import com.sap.sse.i18n.impl.NullResourceBundleStringMessages;

/**
 * Allow server-side internationalization similar to GWT client-side by using property files.
 * 
 * Get Locale in GWT-Context by calling
 * <pre>
 * LocaleInfo.getCurrentLocale().getLocaleName();
 * </pre>
 * 
 * Then transform back to {@link Locale} on server by calling
 * <pre>
 * ResourceBundleStringMessages.Util.getLocaleFor(localeInfoName);
 * </pre>
 */
public interface ResourceBundleStringMessages {
    public static final ResourceBundleStringMessages NULL = new NullResourceBundleStringMessages();

    public String getResourceBaseName();

    public String get(Locale locale, String messageKey);
    public String get(Locale locale, String messageKey, String... parameters);
    
    public static final class Util {
        private static final Locale FALLBACK_LOCALE = Locale.ROOT;
        public static final Control CONTROL = new Control() {
            @Override
            public Locale getFallbackLocale(String baseName, Locale locale) {
                if (baseName == null) throw new NullPointerException();
                return locale.equals(FALLBACK_LOCALE) ? null : FALLBACK_LOCALE;
            }
        };
        
        public static Locale getLocaleFor(String localeInfoName) {
            return Locale.forLanguageTag(localeInfoName);
        }
        
        private Util () {
        }
    }
}
