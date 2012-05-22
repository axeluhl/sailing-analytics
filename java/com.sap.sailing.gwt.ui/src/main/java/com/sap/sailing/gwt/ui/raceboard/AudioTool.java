import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mongodb.DB;
import com.mongodb.gridfs.GridFS;



/**
 *
 * @author wanbar
 */
public class AudioTool extends HttpServlet {
   
    /** 
    * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
    * @param request servlet request
    * @param response servlet response
    */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        
    } 

    /** 
    * Handles the HTTP <code>GET</code> method.
    * @param request servlet request
    * @param response servlet response
    */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    } 

    /** 
    * Handles the HTTP <code>POST</code> method.
    * @param request servlet request
    * @param response servlet response
    */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        
        PrintWriter out = response.getWriter();
                 
        try {           

            /* STEP 1. 
               Handle the incoming request from the client
             */
            
            // Request parsing using the FileUpload lib from Jakarta Commons
            // http://commons.apache.org/fileupload/
            
           
			// Create a factory for disk-based file items
            GridFS factory = new GridFS(null, "test");
            
            // Create a new file upload handler
//            ServletFileUpload upload = new ServletFileUpload(factory);
//            upload.setSizeMax(1000000000);
            
            // Parse the request into a list of DiskFileItems
 //           List items = upload.parseRequest(request);
            
            // The fields we will need for the API request
            String audioName = "";
            String audioDescription = "";
            File audioFile = null;
            String audioFilename = "";
            long audioMaxSize = 0;

            // Iterate through the list of DiskFileItems
   /*         Iterator iter = items.iterator();  
            while (iter.hasNext()) {
                DiskFileItem item = (DiskFileItem) iter.next();

                if (item.isFormField()) {
                    
                    if (item.getFieldName().equals("name")) {
                        audioName = item.getString();
                    } else if (item.getFieldName().equals("desc")) {
                        audioDescription = item.getString();                        
                    }

                } else {
                    audioFile = item.getStoreLocation();
                    String fileName = item.getName();
                    audioMaxSize = item.getSize();

                }
            }
           
*/            out.print(audioFilename);
           
            
            
            /* STEP 2. 
			Assemble the JSON params
             */

            String json = "{\"method\":\"create_video\"" +
            ", \"params\":{" +
            "\"token\":" + "W3BbfN5Qty11tzdyBxUAUIAOPldj1FsI8." + ", " +
            "\"video\":" + 
            "{\"name\":\"" +  audioName + "\", " +
            "\"shortDescription\":\"" + audioDescription + "\"}, " +
          	"\"filename\":\"" + audioFilename + "\", " +
          	"\"maxsize\":\"" + audioMaxSize + "\", " +
          	"}}";

           
            /* STEP 3. 
               Send the request to the Media API
             */
            
            // Define the url to the api
            String targetURL = "127.0.0.1/Audio_Tool_Uploads";
            
            // Create the params object required by...
            Object[] params;
            if(audioFile == null) {
              params = new Object[] { "JSON-RPC", json };
            } else {
              params = new Object[] {
                    "JSON-RPC", json,
                    audioFilename, audioFile
                 }; 
            }              
            
            // ... the ClientHTTPRequest helper class from the ClientHTTP library by Vlad Patryshev
            // http://www.devx.com/Java/Article/17679/1954?pf=true
            
            InputStream in = ClientHttpRequest.post( new java.net.URL(targetURL), params );
            
            // Turn the input stream into a string
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream ret = new ByteArrayOutputStream();
            while ((bytesRead = in.read(buffer)) > 0)
            {
              ret.write(buffer, 0, bytesRead);
            }
            
            // print the response from the API
            /*           String resp = new String(ret.toByteArray());
            out.print(resp);
            
            
        	} 
  */           catch (FileUploadException ex) {
            Logger.getLogger(AudioTool.class.getName()).log(Level.SEVERE, null, ex);
            
        }         
       
    }

    /** 
    * Returns a short description of the servlet.
    */
    public String getServletInfo() {
        return "Short description";
    }
}