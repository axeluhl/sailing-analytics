package com.sap.sailing.resultimport;

import java.net.URL;

import org.apache.shiro.subject.Subject;

import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;

/**
 * A registry for result URL's of score correction providers
 * @author Frank
 */
public interface ResultUrlRegistry {
    /**
     * Registers the URL {@code url} for the result provider named {@code resultProviderName}. Unlike the
     * {@link #getReadableResultUrls(String)} method, here no security check is performed. The caller is expected to assert that
     * the calling {@link Subject} has the permission {@link SecuredDomainType#RESULT_IMPORT_URL
     * RESULT_IMPORT_URL}.{@link DefaultActions#CREATE CREATE} after setting the ownership. Implementing this
     * check properly is done best with the {@code SecurityService} which is not visible from this bundle.
     */
    void registerResultUrl(String resultProviderName, URL url);
    
    /**
     * Registers the URL {@code url} from the result provider named {@code resultProviderName}. Unlike the
     * {@link #getReadableResultUrls(String)} method, here no security check is performed. The caller is expected to assert that
     * the calling {@link Subject} has the permission {@link SecuredDomainType#RESULT_IMPORT_URL
     * RESULT_IMPORT_URL}.{@link DefaultActions#DELETE DELETE} after setting the ownership. Implementing this
     * check properly is done best with the {@code SecurityService} which is not visible from this bundle.
     */
    void unregisterResultUrl(String resultProviderName, URL url);

    /**
     * Obtains the result import URLs registered for the provider named {@code resultProviderName}. If no such URLs are
     * found, an empty iterable is returned. The URLs returned are filtered by the calling {@link Subject}'s permission
     * to The URLs returned are filtered by the calling {@link Subject}'s permission
     * to {@link SecuredDomainType#RESULT_IMPORT_URL RESULT_IMPORT_URL}.{@link DefaultActions#READ READ} them. them.
     * 
     * @return an always valid, non-{@code null} iterable
     */
    Iterable<URL> getReadableResultUrls(String resultProviderName);

    /**
     * Obtains the result import URLs registered for the provider named {@code resultProviderName}. If no such URLs are
     * found, an empty iterable is returned.
     * Opposed to {@link #getReadableResultUrls(String)} the returned URLs are not filtered in any way.
     * @return an always valid, non-{@code null} {@link Iterable}
     */
    Iterable<URL> getAllResultUrls(String resultProviderName);

    Iterable<String> getResultProviderNames();
}
