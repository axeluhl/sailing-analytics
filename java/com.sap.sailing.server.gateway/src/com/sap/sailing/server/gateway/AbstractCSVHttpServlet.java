package com.sap.sailing.server.gateway;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

/**
 * Base servlet for CSV (comma separated file) exports
 * @author Frank
 */
@SuppressWarnings("serial")
public abstract class AbstractCSVHttpServlet extends SailingServerHttpServlet {

    protected void setCSVResponseHeader(HttpServletResponse resp, String filename) {
        resp.setContentType("text/csv");
        resp.setHeader("Content-Disposition", "attachment;filename=\"" + filename + "\"");
        resp.setCharacterEncoding("UTF-8");
    }
    
    protected <T> void writeCsv (List<List<T>> csv, char separator, boolean quoteStrings, OutputStream output) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, "UTF-8"));
        for (List<T> row : csv) {
            StringBuilder line = new StringBuilder();
            for (Iterator<T> iter = row.iterator(); iter.hasNext();) {
                T fieldObject = iter.next();
                String field = String.valueOf(fieldObject).replace("\"", "\"\"");
                if(fieldObject instanceof String && quoteStrings) {
                    field = '"' + field + '"';
                }
                line.append(field);
                if (iter.hasNext()) {
                    line.append(separator);
                }
            }
            writer.write(line.toString());
            writer.newLine();
        }
        writer.flush();
    }
}
