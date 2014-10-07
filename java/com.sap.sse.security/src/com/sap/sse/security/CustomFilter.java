package com.sap.sse.security;

import java.io.IOException;
import java.util.Map.Entry;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.web.filter.authc.PassThruAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;

public class CustomFilter extends PassThruAuthenticationFilter {
    
    @Override
    protected void redirectToLogin(ServletRequest request, ServletResponse response) throws IOException {
        String loginUrl = "http://" + request.getServerName() + ":" + request.getServerPort() + getLoginUrl();
        boolean first = true;
        for (Entry<String, String[]> e : request.getParameterMap().entrySet()){
            if (first){
                loginUrl += "?"+ e.getKey()+"=";
            }
            else {
                loginUrl += "&"+ e.getKey()+"=";
            }
            for(int i = 0; i < e.getValue().length; i++){
                loginUrl+=e.getValue()[i];
                if (i < e.getValue().length - 2){
                    loginUrl += ",";
                }
                
            }
        }
        
        WebUtils.issueRedirect(request, response, loginUrl);
    }
}
