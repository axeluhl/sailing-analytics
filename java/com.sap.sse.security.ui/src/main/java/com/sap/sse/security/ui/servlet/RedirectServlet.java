package com.sap.sse.security.ui.servlet;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RedirectServlet extends HttpServlet {
    
    private static final Logger logger = Logger.getLogger(RedirectServlet.class.getName());
    
    private static final long serialVersionUID = 607209016868443972L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String target = req.getParameter("redirectTo");
        Map<String, String> parameters = new HashMap<>();
        for (Entry<String, String[]> e : req.getParameterMap().entrySet()){
            if (!e.getKey().equals("redirectTo")){
                parameters.put(e.getKey(), Arrays.toString(e.getValue()).replace("[", "").replace("]", ""));
            }
        }
        if(req.getRequestURL().toString().contains("local")){
            parameters.put("gwt.codesvr", "127.0.0.1:9997");
        }
        if (target == null){
            doHiddenRedirect(resp, resp.encodeRedirectURL(createLoginLink(parameters, req)));
        }
        else {
            switch (target){
            case "login":
                doHiddenRedirect(resp, resp.encodeRedirectURL(createLoginLink(parameters, req)));
                break;
            case "oauthlogin":
                doHiddenRedirect(resp, resp.encodeRedirectURL(createOAuthLink(parameters, req)));
                break;
            case "registration":
                doHiddenRedirect(resp, resp.encodeRedirectURL(createRegistrationLink(parameters, req)));
                break;
            case "usermanagement":
                doHiddenRedirect(resp, resp.encodeRedirectURL(createRegistrationLink(parameters, req)));
                break;
            default:
                doHiddenRedirect(resp, resp.encodeRedirectURL(createLoginLink(parameters, req)));
            }
        }
    }
    
    private void doHiddenRedirect(HttpServletResponse resp, String target) throws IOException{
        String html = "<html><head><script type='text/javascript'>";
        html += "window.location = '" + target +"';";
        html +="</script></head><body></body></html>";
        resp.setContentType("text/html");
        resp.getWriter().append(html);
        resp.getWriter().flush();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    public static String createRegistrationLink(Map<String, String> parameters, HttpServletRequest req) {
        return createEntryPointLink("/security/ui/Register.html", parameters, req);
    }
    
    public static String createLoginLink(Map<String, String> parameters, HttpServletRequest req) {
        return createEntryPointLink("/security/ui/Login.html", parameters, req);
    }
    
    public static String createOAuthLink(Map<String, String> parameters, HttpServletRequest req) {
        return createEntryPointLink("/security/ui/OAuthLogin.html", parameters, req);
    }
    
    public static String createUserManagementLink(Map<String, String> parameters, HttpServletRequest req) {
        return createEntryPointLink("/security/ui/UserManagement.html", parameters, req);
    }

    private static String createEntryPointLink(String baseLink, Map<String, String> parameters, HttpServletRequest req) {
        String debugParam = req.getParameter("gwt.codesvr");
        String localeParam = req.getParameter("locale");
        String host = "";
        String port = "";
        try {
            URL url = new URL(req.getRequestURL().toString());
            host = url.getHost();
            port = "" + url.getPort();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if (host.contains("local")){
            host = "127.0.0.1";
        }
        String link = "http://" + host + ":" + port + baseLink;
        int i = 1;
        for(Entry<String, String> entry: parameters.entrySet()) {
            link += i == 1 ? "?" : "&";
            link += entry.getKey() + "=" + entry.getValue();
            i++;
        }
        if (debugParam != null && !debugParam.isEmpty() && parameters.get("gwt.codesvr") == null) {
            link += i == 1 ? "?" : "&";
            link += "gwt.codesvr=" + debugParam;
        }
        if (localeParam != null && !localeParam.isEmpty()) {
            link += i == 1 ? "?" : "&";
            link += "locale=" + localeParam;
        }
        logger.info("Redirecting from " + req.getRequestURL().toString() +" to: " + link);
        return link;
    }
}
