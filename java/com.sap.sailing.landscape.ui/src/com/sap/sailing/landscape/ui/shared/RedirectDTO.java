package com.sap.sailing.landscape.ui.shared;

import java.util.Optional;
import java.util.function.Function;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.common.Util;

public interface RedirectDTO extends IsSerializable {
    /**
     * Each literal represents one distinct concrete subtype implementing this interface.
     * Each one provides a parser that accepts a redirect path/query combination and tries
     * to construct an instance of the respective subtype that produces an equal result
     * with its {@link RedirectDTO#getPathAndQuery} method, or {@code null} if no such
     * instance can be produced for that subtype.<p>
     * 
     * Order matters; the types are traversed in their natural order while trying to parse
     * a redirect path in {@link RedirectDTO#from(String)}. The first valid parsing result
     * will be returned.
     * 
     * @author Axel Uhl (D043530)
     *
     */
    enum Type {
        PLAIN(PlainRedirectDTO::parse), HOME(HomeRedirectDTO::parse),
        EVENT(EventRedirectDTO::parse), EVENT_SERIES(EventSeriesRedirectDTO::parse);
        
        private Function<String, RedirectDTO> parserFromPathAndQuery; 
        
        private Type(Function<String, RedirectDTO> parserFromPathAndQuery) {
            this.parserFromPathAndQuery = parserFromPathAndQuery;
        }
        
        private Function<String, RedirectDTO> getParserFromPathAndQuery() {
            return parserFromPathAndQuery;
        }
    }
    
    String getPath();
    
    default Optional<String> getQuery() {
        return Optional.of("#{query}");
    }
    
    default String getPathAndQuery() {
        return toString(getPath(), getQuery());
    }
    
    static String toString(String path, Optional<String> query) {
        return path+query.map(q->!Util.hasLength(q)?"":("?"+q)).orElse("");
    }
    
    static String getPath(String redirectPath) {
        final int queryStart = redirectPath.indexOf("?");
        return queryStart<0 ? redirectPath : redirectPath.substring(0, queryStart);
    }

    static String getQuery(String redirectPath) {
        final int queryStart = redirectPath.indexOf("?");
        return queryStart<0 ? "" : redirectPath.substring(queryStart+1);
    }

    Type getType();
    
    void accept(RedirectVisitor visitor) throws Exception;

    /**
     * From a redirect path as found on {@link SailingApplicationReplicaSetDTO#getDefaultRedirectPath()} infers the
     * specific {@link RedirectDTO} whose {@link #getPath()} and {@link #getQuery()} will produce the
     * {@code redirectPath} specified as parameter to this factory method. For this, all {@link Type}s are
     * probed for parsing the {@code redirectPath}, and the first valid redirect produced from the
     * path will be returned.
     * 
     * @return a {@link RedirectDTO} whose {@link #getPath()} and {@link #getQuery()} will produce the
     *         {@code redirectPath}, or {@code null} if no such {@link RedirectDTO} can be constructed,
     *         e.g., because the {@code redirectPath} specified has been crafted manually and couldn't have been
     *         produced by any automatic redirect specification.
     */
    static RedirectDTO from(String redirectPath) {
        for (final Type type : Type.values()) {
            final RedirectDTO redirect = type.getParserFromPathAndQuery().apply(redirectPath);
            if (redirect != null) {
                return redirect;
            }
        }
        return null;
    }

}
