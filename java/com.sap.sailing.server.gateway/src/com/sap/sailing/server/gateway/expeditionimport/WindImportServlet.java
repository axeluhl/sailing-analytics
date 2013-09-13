package com.sap.sailing.server.gateway.expeditionimport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.server.gateway.SailingServerHttpServlet;

public class WindImportServlet extends SailingServerHttpServlet {

	private static final long serialVersionUID = 1L;
	
	private static final WindSource EXPEDITION_WIND_SOURCE = new WindSourceImpl(WindSourceType.EXPEDITION);

	@Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!ServletFileUpload.isMultipartContent(req)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        
		try {
			
			Iterable<Regatta> allRegattas = getService().getAllRegattas();

			Collection<FileItem> files = readInput(req);
			for (FileItem file : files) {
				List<Wind> windFixes = WindLogParser.importWind(file.getInputStream());
				for (Regatta regatta : allRegattas) {
					DynamicTrackedRegatta trackedRegatta = getService().getTrackedRegatta(regatta);
					Iterable<RaceDefinition> allRaceDefinitions = regatta.getAllRaces();
					for (RaceDefinition raceDefinition : allRaceDefinitions) {
						DynamicTrackedRace trackedRace = trackedRegatta.getTrackedRace(raceDefinition);
						for (Wind wind : windFixes) {
							trackedRace.recordWind(wind, EXPEDITION_WIND_SOURCE);
						}
					}
				}
			}
		} catch (FileUploadException e) {
			throw new IOException(e);
		}
    }

    private Collection<FileItem> readInput(HttpServletRequest req) throws FileUploadException {
    	Collection<FileItem> files = new ArrayList<FileItem>();
        // http://commons.apache.org/fileupload/using.html
            FileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            @SuppressWarnings("unchecked")
            List<FileItem> items = upload.parseRequest(req);
            for (FileItem item : items) {
                if (!item.isFormField())
                    files.add(item);
            }
            return files;
    }
}
