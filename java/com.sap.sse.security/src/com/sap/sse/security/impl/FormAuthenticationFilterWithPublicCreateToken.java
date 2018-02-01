package com.sap.sse.security.impl;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;

public class FormAuthenticationFilterWithPublicCreateToken extends FormAuthenticationFilter {

    @Override
    public AuthenticationToken createToken(ServletRequest request, ServletResponse response) {
        return super.createToken(request, response);
    }

    @Override
    public boolean isLoginSubmission(ServletRequest request, ServletResponse response) {
        return super.isLoginSubmission(request, response);
    }
}
