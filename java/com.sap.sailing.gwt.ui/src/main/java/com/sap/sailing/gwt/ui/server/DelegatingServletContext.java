package com.sap.sailing.gwt.ui.server;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;

public class DelegatingServletContext implements ServletContext {
    private static final Logger logger = Logger.getLogger(DelegatingServletContext.class.getName());

    private final ServletContext delegate;

    private static final String PREFIX = "/war";

    public DelegatingServletContext(ServletContext delegate) {
        this.delegate = delegate;
    }

    @Override
    public Object getAttribute(String arg0) {
        return delegate.getAttribute(arg0);
    }

    @SuppressWarnings({"unchecked", "rawtypes" }) // need to do this because the base implementation in javax.servlet 3.0 requires this
    @Override
    public Enumeration getAttributeNames() {
        return delegate.getAttributeNames();
    }

    @Override
    public ServletContext getContext(String arg0) {
        return delegate.getContext(arg0);
    }

    @Override
    public String getContextPath() {
        return delegate.getContextPath();
    }

    @Override
    public String getInitParameter(String arg0) {
        return delegate.getInitParameter(arg0);
    }

    @SuppressWarnings({"unchecked", "rawtypes" }) // need to do this because the base implementation in javax.servlet 3.0 requires this
    @Override
    public Enumeration getInitParameterNames() {
        return delegate.getInitParameterNames();
    }

    @Override
    public int getMajorVersion() {
        return delegate.getMajorVersion();
    }

    @Override
    public String getMimeType(String arg0) {
        return delegate.getMimeType(arg0);
    }

    @Override
    public int getMinorVersion() {
        return delegate.getMinorVersion();
    }

    @Override
    public RequestDispatcher getNamedDispatcher(String arg0) {
        return delegate.getNamedDispatcher(arg0);
    }

    @Override
    public String getRealPath(String arg0) {
        return delegate.getRealPath(arg0);
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String arg0) {
        return delegate.getRequestDispatcher(arg0);
    }

    @Override
    public URL getResource(String arg0) throws MalformedURLException {
        URL result = delegate.getResource(arg0);
        if (result == null) {
            logger.fine("Couldn't find " + arg0 + ". Trying " + prependPrefix(arg0));
            result = delegate.getResource(prependPrefix(arg0));
            logger.fine("Found " + result);
        }
        return result;
    }

    private String prependPrefix(String arg0) {
        if (arg0.startsWith("/")) {
            return PREFIX + arg0;
        } else {
            return PREFIX + "/" + arg0;
        }
    }

    @Override
    public InputStream getResourceAsStream(String arg0) {
        InputStream result = delegate.getResourceAsStream(arg0);
        if (result == null) {
            logger.fine("Couldn't find " + arg0 + ". Trying " + prependPrefix(arg0));
            result = delegate.getResourceAsStream(prependPrefix(arg0));
            logger.fine("Found " + result);
        }
        return result;
    }

    @SuppressWarnings({"unchecked", "rawtypes" }) // need to do this because the base implementation in javax.servlet 3.0 requires this
    @Override
    public Set getResourcePaths(String arg0) {
        return delegate.getResourcePaths(arg0);
    }

    @Override
    public String getServerInfo() {
        return delegate.getServerInfo();
    }

    @SuppressWarnings("deprecation")
    // have to delegate; what can we do?
    @Override
    public Servlet getServlet(String arg0) throws ServletException {
        return delegate.getServlet(arg0);
    }

    @Override
    public String getServletContextName() {
        return delegate.getServletContextName();
    }

    @SuppressWarnings({ "deprecation", "rawtypes", "unchecked" }) // need to do this because the base implementation in javax.servlet 3.0 requires this
    // have to delegate; what can we do?
    @Override
    public Enumeration getServletNames() {
        return delegate.getServletNames();
    }

    @SuppressWarnings({"deprecation", "rawtypes", "unchecked"}) // need to do this because the base implementation in javax.servlet 3.0 requires this
    // have to delegate; what can we do?
    @Override
    public Enumeration getServlets() {
        return delegate.getServlets();
    }

    @Override
    public void log(String arg0) {
        delegate.log(arg0);
    }

    @SuppressWarnings("deprecation")
    // have to delegate; what can we do?
    @Override
    public void log(Exception arg0, String arg1) {
        delegate.log(arg0, arg1);
    }

    @Override
    public void log(String arg0, Throwable arg1) {
        delegate.log(arg0, arg1);
    }

    @Override
    public void removeAttribute(String arg0) {
        delegate.removeAttribute(arg0);
    }

    @Override
    public void setAttribute(String arg0, Object arg1) {
        delegate.setAttribute(arg0, arg1);
    }

    @Override
    public int getEffectiveMajorVersion() {
        return delegate.getEffectiveMajorVersion();
    }

    @Override
    public int getEffectiveMinorVersion() {
        return delegate.getEffectiveMinorVersion();
    }

    @Override
    public boolean setInitParameter(String name, String value) {
        return delegate.setInitParameter(name, value);
    }

    @Override
    public Dynamic addServlet(String servletName, String className) {
        return delegate.addServlet(servletName, className);
    }

    @Override
    public Dynamic addServlet(String servletName, Servlet servlet) {
        return delegate.addServlet(servletName, servlet);
    }

    @Override
    public Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
        return addServlet(servletName, servletClass);
    }

    @Override
    public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
        return delegate.createServlet(clazz);
    }

    @Override
    public ServletRegistration getServletRegistration(String servletName) {
        return delegate.getServletRegistration(servletName);
    }

    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        return delegate.getServletRegistrations();
    }

    @Override
    public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, String className) {
        return delegate.addFilter(filterName, className);
    }

    @Override
    public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
        return delegate.addFilter(filterName, filter);
    }

    @Override
    public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
        return delegate.addFilter(filterName, filterClass);
    }

    @Override
    public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {
        return delegate.createFilter(clazz);
    }

    @Override
    public FilterRegistration getFilterRegistration(String filterName) {
        return delegate.getFilterRegistration(filterName);
    }

    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        return delegate.getFilterRegistrations();
    }

    @Override
    public SessionCookieConfig getSessionCookieConfig() {
        return delegate.getSessionCookieConfig();
    }

    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
        delegate.setSessionTrackingModes(sessionTrackingModes);
    }

    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        return delegate.getDefaultSessionTrackingModes();
    }

    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        return delegate.getEffectiveSessionTrackingModes();
    }

    @Override
    public void addListener(String className) {
        delegate.addListener(className);
    }

    @Override
    public <T extends EventListener> void addListener(T t) {
        delegate.addListener(t);
    }

    @Override
    public void addListener(Class<? extends EventListener> listenerClass) {
        delegate.addListener(listenerClass);
    }

    @Override
    public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
        return delegate.createListener(clazz);
    }

    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        return delegate.getJspConfigDescriptor();
    }

    @Override
    public ClassLoader getClassLoader() {
        return delegate.getClassLoader();
    }

    @Override
    public void declareRoles(String... roleNames) {
        delegate.declareRoles(roleNames);
    }

    @Override
    public String getVirtualServerName() {
        return delegate.getVirtualServerName();
    }
}
