package com.sap.sse.gwt.dispatch.servlets;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPCServletUtils;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.sap.sse.gwt.shared.RpcConstants;

/**
 * Using GWT in a proxyfied environment can be tricky and leads to strange errors
 * when using GWT-RPC. This is due to the fact that GWT doesn't find the correct
 * serialization rule file. For non proxyfied environments you do not need to change
 * anything. In an Apache based env you need to add a line like the following to your
 * configuration: RequestHeader edit X-GWT-Module-Base sapsailing.com/(gwt/)? sapsailing.com/gwt/
 * 
 * @author Simon Pamies (info@pamies.de)
 * @since Oct 10, 2012
 */
public abstract class ProxiedRemoteServiceServlet extends RemoteServiceServlet {

    private static final long serialVersionUID = 5379097888921157936L;

    @Override
    protected SerializationPolicy doGetSerializationPolicy(
            HttpServletRequest request, String moduleBaseURL, String strongName) {
        String moduleBaseURLHdr = request.getHeader("X-GWT-Module-Base");

        if(moduleBaseURLHdr != null){
            moduleBaseURL = moduleBaseURLHdr;
        }

        return super.doGetSerializationPolicy(request, moduleBaseURL, strongName);
    }

    @Override
    protected void doUnexpectedFailure(Throwable e) {
        if ((e.getCause() != null && e.getCause().getClass().getName().matches("org\\.apache\\.shiro\\.authz\\..*[Aa]uth.*Exception")) ||
                (e instanceof SerializationException && e.getMessage().matches("Type 'org\\.apache\\.shiro\\.authz\\..*[Aa]uth.*Exception' was not included in the set of types which can be serialized.*"))) {
            final HttpServletResponse servletResponse = getThreadLocalResponse();
            try {
                servletResponse.reset();
            } catch (IllegalStateException ex) {
                throw new RuntimeException("Unable to report failure", e);
            }
            ServletContext servletContext = getServletContext();
            RPCServletUtils.writeResponseForUnexpectedFailure(servletContext, servletResponse, e);
            servletContext.log("Exception while dispatching incoming RPC call", e.getCause()==null?e:e.getCause());
            try {
                servletResponse.setContentType("text/plain");
                servletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                try {
                    servletResponse.getOutputStream().write((e.getCause()==null?e:e.getCause()).getLocalizedMessage().getBytes("UTF-8"));
                } catch (IllegalStateException ex) {
                    // Handle the (unexpected) case where getWriter() was previously used
                    servletResponse.getWriter().write("Couldn't write exception");
                }
            } catch (IOException ex) {
                servletContext.log(
                        "respondWithUnexpectedFailure failed while sending the previous failure to the client", ex);
            }
        } else {
            super.doUnexpectedFailure(e);
        }
    }
    
    protected Locale getClientLocale() {
        final HttpServletRequest request = getThreadLocalRequest();
        if (request != null) {
            final String localeString = request.getHeader(RpcConstants.HEADER_LOCALE);
            if (localeString != null && ! localeString.isEmpty()) {
                try {
                    return Locale.forLanguageTag(localeString);
                } catch (Exception e) {
                    // non-parseable locales are ignored
                }
            }
        }
        // Using default locale if the client locale could not be determined
        return Locale.ENGLISH;
    }
}
