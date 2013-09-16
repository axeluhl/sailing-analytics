package com.sap.sailing.www.events;

import java.io.IOException;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RedirectAndForwardServlet extends HttpServlet {
    private static final long serialVersionUID = 7755924354907488543L;

    public RedirectAndForwardServlet() {
    }

    protected void redirect(String destination, HttpServletResponse response, Map<String, String> parameters)
            throws IOException, ServletException {
        String encodedParameters = encodeParameters(parameters);
        if (encodedParameters != null)
            destination += encodedParameters;

        String url = response.encodeRedirectURL(destination);
        response.sendRedirect(url);
    }

    protected String encodeParameters(Map<String, String> parameters) throws ServletException {
        if (parameters == null || parameters.size() == 0)
            return "";

        // check for invalid parameters
        for (String paramKey : parameters.keySet()) {
            String param = parameters.get(paramKey);
            if (param == null)
                throw new ServletException("Try to set an empty value for parameter '" + paramKey + "'");
        }

        StringBuffer buffer = new StringBuffer();

        int i = 0;
        buffer.append('?');

        for (String paramKey : parameters.keySet()) {
            if (i > 0)
                buffer.append('&');

            buffer.append(paramKey);
            buffer.append('=');
            buffer.append(parameters.get(paramKey));

            i++;
        }

        return buffer.toString();
    }

    protected void forward(String destination, HttpServletRequest request, HttpServletResponse response,
            Map<String, String> parameters) throws ServletException, IOException {
        String encodedParameters = encodeParameters(parameters);
        if (encodedParameters != null)
            destination += encodedParameters;

        RequestDispatcher dispatcher = request.getRequestDispatcher(destination);
        dispatcher.forward(request, response);
    }

}
