package com.sap.sailing.www;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class CookieSameSiteFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // intentionally left blank
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse)response;
        Collection<String> cookiesHeaders = httpResponse.getHeaders("Set-Cookie");
        boolean firstHeader = true;
        for (String header : cookiesHeaders) {
            if (firstHeader) {
                httpResponse.setHeader("Set-Cookie",
                        String.format("%s; %s", header, "SameSite=None; Secure"));
                firstHeader = false;
                continue;
            }
            httpResponse.addHeader("Set-Cookie",
                    String.format("%s; %s", header, "SameSite=None; Secure"));
        }
        chain.doFilter(request, httpResponse);
    }

    @Override
    public void destroy() {
        // intentionally left blank
    }

}
