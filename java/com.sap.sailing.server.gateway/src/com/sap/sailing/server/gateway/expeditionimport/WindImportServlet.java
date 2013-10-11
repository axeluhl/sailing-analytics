package com.sap.sailing.server.gateway.expeditionimport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.WindSourceWithAdditionalID;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.server.gateway.SailingServerHttpServlet;
import com.sap.sailing.server.gateway.expeditionimport.WindImportServlet.WindImportResult.RaceEntry;

public class WindImportServlet extends SailingServerHttpServlet {

	private static final long serialVersionUID = 1L;

	static class Upload {
		public String boatId;
		public final List<FileItem> files = new ArrayList<FileItem>();
	}

	static class WindImportResult {
		
		private Date first;
		private Date last;
		public String error;
		
		public final List<RaceEntry> raceEntries = new ArrayList<RaceEntry>();
		
		public Date getFirst() {
			return first;
		}

		public Date getLast() {
			return last;
		}
		
		public void update(Wind newWind) {
			Date newDate = newWind.getTimePoint().asDate();
			if (this.first == null || newDate.before(this.first)) {
				this.first = newDate;
			}
			if (this.last == null || newDate.after(this.last)) {
				this.last = newDate;
			}
		}

		RaceEntry addRaceEntry(String regattaName, String raceName) {
			RaceEntry raceEntry = new RaceEntry(regattaName, raceName);
			raceEntries.add(raceEntry);
			return raceEntry;
		}
		
		static class RaceEntry {

		public final String regattaName;
		public final String raceName;
		private int count;
		private Date first;
		private Date last;
		
		private RaceEntry(String regattaName, String raceName) {
			this.regattaName = regattaName;
			this.raceName = raceName;
		}

		public void update(Wind newWind) {
			count++;
			Date newDate = newWind.getTimePoint().asDate();
			if (this.first == null || newDate.before(this.first)) {
				this.first = newDate;
			}
			if (this.last == null || newDate.after(this.last)) {
				this.last = newDate;
			}
		}

		public int getCount() {
			return count;
		}

		public Date getFirst() {
			return first;
		}

		public Date getLast() {
			return last;
		}

		private JSONObject json() {
	        JSONObject result = new JSONObject();
	        result.put("regattaName", regattaName);
	        result.put("raceName", raceName);
	        result.put("count", getCount());
	        result.put("first", getFirst().getTime());
	        result.put("last", getLast().getTime());
			return result;
		}
		}

		public JSONObject json() {
	        JSONObject result = new JSONObject();

	        result.put("first", getFirst().getTime());
	        result.put("last", getLast().getTime());
	        result.put("error", error);
	        
	        JSONArray raceEntriesJson = new JSONArray();
	        for (RaceEntry raceEntry : raceEntries) {
				raceEntriesJson.add(raceEntry.json());
			}
	        result.put("raceEntries", raceEntriesJson);
	        
	        
	        return result;
	        
		}

	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (!ServletFileUpload.isMultipartContent(req)) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		WindImportResult windImportResult = new WindImportResult();
		
		try {

			Upload upload = readInput(req);
			WindSource windSource = new WindSourceWithAdditionalID(WindSourceType.EXPEDITION, upload.boatId);

			Iterable<Regatta> allRegattas = getService().getAllRegattas();

			for (FileItem file : upload.files) {
				List<Wind> windFixes = WindLogParser.importWind(file.getInputStream());
				if (windFixes.size() > 0) {
					windImportResult.update(windFixes.get(0));
					windImportResult.update(windFixes.get(windFixes.size() - 1));
					for (Regatta regatta : allRegattas) {
						DynamicTrackedRegatta trackedRegatta = getService().getTrackedRegatta(regatta);
						Iterable<RaceDefinition> allRaceDefinitions = regatta.getAllRaces();
						for (RaceDefinition raceDefinition : allRaceDefinitions) {
							DynamicTrackedRace trackedRace = trackedRegatta.getTrackedRace(raceDefinition);
							RaceEntry raceEntry = windImportResult.addRaceEntry(regatta.getName(), trackedRace.getRace().getName());
							for (Wind wind : windFixes) {
								if (trackedRace.recordWind(wind, windSource)) {
									raceEntry.update(wind);
								}
							}
						}
					}
				}
			}

			// Use text/html to prevent browsers from wrapping the response body,
			// see "Handling File Upload Responses in GWT" at http://www.artofsolving.com/node/50
			resp.setContentType("text/html;charset=UTF-8");
			resp.getWriter().append(windImportResult.json().toJSONString());
		} catch (Exception e) {
			windImportResult.error = e.toString();
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
