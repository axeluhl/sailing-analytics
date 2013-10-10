package com.sap.sailing.server.gateway.expeditionimport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.WindSourceWithAdditionalID;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.server.gateway.SailingServerHttpServlet;

public class WindImportServlet extends SailingServerHttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	static class Upload {
		public String boatId;
		public final List<FileItem> files = new ArrayList<FileItem>();
	}
	
	static class WindImportResult {
		
		private final RegattaIdentifier regattaIdentifier;
		private final RegattaAndRaceIdentifier raceIdentifier;
		private int count;
		private Wind firstWind;
		private Wind lastWind;

		public void update(Wind newWind) {
			count++;
			if (firstWind == null || newWind.getTimePoint().before(firstWind.getTimePoint())) {
				firstWind = newWind;
			}
			if (lastWind == null || newWind.getTimePoint().after(lastWind.getTimePoint())) {
				lastWind = newWind;
			}
		}

		public RegattaIdentifier getRegattaIdentifier() {
			return regattaIdentifier;
		}

		public RegattaAndRaceIdentifier getRaceIdentifier() {
			return raceIdentifier;
		}

		public WindImportResult(RegattaIdentifier regattaIdentifier, RegattaAndRaceIdentifier raceIdentifier) {
			this.regattaIdentifier = regattaIdentifier;
			this.raceIdentifier = raceIdentifier;
		}

		public int getCount() {
			return count;
		}

		public Wind getFirstWind() {
			return firstWind;
		}

		public Wind getLastWind() {
			return lastWind;
		}
		
	}
	
	@Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!ServletFileUpload.isMultipartContent(req)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        
		try {
			
			Upload upload = readInput(req);
			WindSource windSource = new WindSourceWithAdditionalID(WindSourceType.EXPEDITION, upload.boatId);
			
			List<WindImportResult> windImportResults = new ArrayList<WindImportResult>();
			
			Iterable<Regatta> allRegattas = getService().getAllRegattas();

			for (FileItem file : upload.files) {
				List<Wind> windFixes = WindLogParser.importWind(file.getInputStream());
				for (Regatta regatta : allRegattas) {
					DynamicTrackedRegatta trackedRegatta = getService().getTrackedRegatta(regatta);
					Iterable<RaceDefinition> allRaceDefinitions = regatta.getAllRaces();
					for (RaceDefinition raceDefinition : allRaceDefinitions) {
						DynamicTrackedRace trackedRace = trackedRegatta.getTrackedRace(raceDefinition);
						WindImportResult windImportResult = new WindImportResult(regatta.getRegattaIdentifier(), trackedRace.getRaceIdentifier());
						windImportResults.add(windImportResult);
						for (Wind wind : windFixes) {
							if (trackedRace.recordWind(wind, windSource)) {
								windImportResult.update(wind);
							}
						}
					}
				}
			}
			
			// Use text/html to prevent browsers from wrapping the response body,
			// see "Handling File Upload Responses in GWT" at http://www.artofsolving.com/node/50
			resp.setContentType("text/html;charset=UTF-8"); 
			resp.getWriter().append(windImportResults.toString());
		} catch (FileUploadException e) {
			throw new IOException(e);
		}
    }

    private Upload readInput(HttpServletRequest req) throws FileUploadException {
    	Upload result = new Upload();
        // http://commons.apache.org/fileupload/using.html
            FileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            @SuppressWarnings("unchecked")
            List<FileItem> items = upload.parseRequest(req);
            for (FileItem item : items) {
                if (item.isFormField()) {
                	if ("boatId".equals(item.getFieldName())) {
                		result.boatId = item.getString();
                	}
                } else {
                	result.files.add(item);
                } 
            }
            return result;
    }
}
