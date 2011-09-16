package com.sap.sailing.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Document;

public class XcelsiusApp extends Servlet {
    private static final long serialVersionUID = -6849138354941569249L;
    public XcelsiusApp() {
        @SuppressWarnings("unused")
        Document doc;
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            resp.getWriter().println("Hello Excelsius!");
        } catch (Throwable e) {
            resp.getWriter().println("Error processing request:");
            e.printStackTrace(resp.getWriter());
        }
    }
    
}
