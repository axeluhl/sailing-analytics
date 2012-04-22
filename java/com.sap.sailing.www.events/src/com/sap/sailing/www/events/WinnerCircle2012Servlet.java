package com.sap.sailing.www.events;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class WinnerCircle2012Servlet extends RedirectAndForwardServlet {
    private static final long serialVersionUID = -7596583866699930431L;

    public WinnerCircle2012Servlet() {
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        forward("winnercircle2012/index.html", request, response, null);
    }

}
