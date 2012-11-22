package com.sap.sailing.gwt.ui.server;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

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

    @Override
    public Enumeration<?> getAttributeNames() {
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

    @Override
    public Enumeration<?> getInitParameterNames() {
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

    @Override
    public Set<?> getResourcePaths(String arg0) {
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

    @SuppressWarnings("deprecation")
    // have to delegate; what can we do?
    @Override
    public Enumeration<?> getServletNames() {
        return delegate.getServletNames();
    }

    @SuppressWarnings("deprecation")
    // have to delegate; what can we do?
    @Override
    public Enumeration<?> getServlets() {
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
}
