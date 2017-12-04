package com.sap.sailing.gwt.ui.adminconsole;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PairingListTemplateServlet extends HttpServlet{
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String fileName = (String) request.getSession().getAttribute("fileName");
        
        InputStream inputStream = null;
        OutputStream outputStream = response.getOutputStream();
        
        FileInputStream fileInputStream = new FileInputStream(fileName);
        outputStream.write(5000);
        inputStream.close();
        outputStream.close();
    }
    
}
