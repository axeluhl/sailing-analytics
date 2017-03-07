package com.sap.sse.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

import com.sap.sse.i18n.impl.NullResourceBundleStringMessages;

/**
 * Allow server-side internationalization similar to GWT client-side by using property files.
 * 
 * Get Locale in GWT-Context by calling
 * 
 * <pre>
 * LocaleInfo.getCurrentLocale().getLocaleName();
 * </pre>
 * 
 * Then transform back to {@link Locale} on server by calling
 * 
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

        public static Control createControl(String encoding) {
            return new Control() {
                @Override
                public Locale getFallbackLocale(String baseName, Locale locale) {
                    if (baseName == null)
                        throw new NullPointerException();
                    return locale.equals(FALLBACK_LOCALE) ? null : FALLBACK_LOCALE;
                }

                // FIXME replace with java9 logic, as soon as possible
                public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader,
                        boolean reload) throws IllegalAccessException, InstantiationException, IOException {
                    String bundleName = toBundleName(baseName, locale);
                    ResourceBundle bundle = null;
                    if (format.equals("java.class")) {
                        try {
                            @SuppressWarnings("unchecked")
                            Class<? extends ResourceBundle> bundleClass = (Class<? extends ResourceBundle>) loader
                                    .loadClass(bundleName);

                            // If the class isn't a ResourceBundle subclass, throw a
                            // ClassCastException.
                            if (ResourceBundle.class.isAssignableFrom(bundleClass)) {
                                bundle = bundleClass.newInstance();
                            } else {
                                throw new ClassCastException(
                                        bundleClass.getName() + " cannot be cast to ResourceBundle");
                            }
                        } catch (ClassNotFoundException e) {
                        }
                    } else if (format.equals("java.properties")) {
                        final String resourceName = toResourceName0(bundleName, "properties");
                        if (resourceName == null) {
                            return bundle;
                        }
                        final ClassLoader classLoader = loader;
                        final boolean reloadFlag = reload;
                        InputStream stream = null;
                        try {
                            stream = AccessController.doPrivileged(new PrivilegedExceptionAction<InputStream>() {
                                public InputStream run() throws IOException {
                                    InputStream is = null;
                                    if (reloadFlag) {
                                        URL url = classLoader.getResource(resourceName);
                                        if (url != null) {
                                            URLConnection connection = url.openConnection();
                                            if (connection != null) {
                                                // Disable caches to get fresh data for
                                                // reloading.
                                                connection.setUseCaches(false);
                                                is = connection.getInputStream();
                                            }
                                        }
                                    } else {
                                        is = classLoader.getResourceAsStream(resourceName);
                                    }
                                    return is;
                                }
                            });
                        } catch (PrivilegedActionException e) {
                            throw (IOException) e.getException();
                        }
                        if (stream != null) {
                            try {
                                bundle = new PropertyResourceBundle(new InputStreamReader(stream, encoding));
                            } finally {
                                stream.close();
                            }
                        }
                    } else {
                        throw new IllegalArgumentException("unknown format: " + format);
                    }
                    return bundle;
                }

                private String toResourceName0(String bundleName, String suffix) {
                    // application protocol check
                    if (bundleName.contains("://")) {
                        return null;
                    } else {
                        return toResourceName(bundleName, suffix);
                    }
                }
            };
        }

        public static Locale getLocaleFor(String localeInfoName) {
            return Locale.forLanguageTag(localeInfoName);
        }

        private Util() {
        }
    }
}
