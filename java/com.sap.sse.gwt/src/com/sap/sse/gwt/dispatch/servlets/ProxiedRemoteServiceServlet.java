package com.sap.sse.gwt.dispatch.servlets;

import javax.servlet.http.HttpServletRequest;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.user.server.rpc.SerializationPolicy;

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
    
}
