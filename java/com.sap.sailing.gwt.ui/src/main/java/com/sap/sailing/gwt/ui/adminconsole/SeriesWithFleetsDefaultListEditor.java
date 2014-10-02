package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.listedit.ListEditorUiStrategy;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;

public class SeriesWithFleetsDefaultListEditor extends
		SeriesWithFleetsListEditor {
	private final Map<String, Map<String, Set<SeriesDTO>>> seriesStructure;

	public SeriesWithFleetsDefaultListEditor(List<SeriesDTO> series,
			StringMessages stringMessages, ImageResource removeImage,
			boolean enableFleetRemoval) {
		super(series, stringMessages, removeImage, enableFleetRemoval);

		seriesStructure = new HashMap<String, Map<String, Set<SeriesDTO>>>();

		analyzeSeriesStructure(series);
		List<SeriesDTO> seriesCompact = new ArrayList<SeriesDTO>();
		for (String string : seriesStructure.keySet()) {
			for (String seriesName : seriesStructure.get(string).keySet()) {
				for (SeriesDTO seriesDTO : seriesStructure.get(string).get(seriesName)) {
					seriesCompact.add(seriesDTO);
					break;
				}
			}
		}
		super.setValue(seriesCompact);
	}

	private void analyzeSeriesStructure(List<SeriesDTO> series) {
		for (SeriesDTO seriesDTO : series) {
			if (seriesStructure.get(seriesDTO.getName()) == null) {
				seriesStructure.put(seriesDTO.getName(),
						new HashMap<String, Set<SeriesDTO>>());
			}
			String temp = "";
			for (FleetDTO fleet : seriesDTO.getFleets()) {
				temp += fleet.getName();
			}
			if (!seriesStructure.get(seriesDTO.getName()).containsKey(temp)) {
				seriesStructure.get(seriesDTO.getName()).put(temp,
						new HashSet<SeriesDTO>());
			}
			seriesStructure.get(seriesDTO.getName()).get(temp).add(seriesDTO);
		}
	}

	protected ListEditorUiStrategy<SeriesDTO> createExpandedUi(
			StringMessages stringMessages, ImageResource removeImage,
			boolean enableFleetRemoval) {
		return new ExpandedUiDefault(stringMessages, removeImage,
				enableFleetRemoval);
	}

	private static class ExpandedUiDefault extends ExpandedUi {

		public ExpandedUiDefault(StringMessages stringMessages,
				ImageResource removeImage, boolean canRemoveItems) {
			super(stringMessages, removeImage, canRemoveItems);
		}

		protected Widget createAddWidget() {
			Button addSeriesButton = new Button(
					stringMessages.setDefaultSeries());
			addSeriesButton.ensureDebugId("SetDefaultSeriesButton");
			addSeriesButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					SeriesWithFleetsDefaultCreateDialog dialog = new SeriesWithFleetsDefaultCreateDialog(
							Collections.unmodifiableCollection(context
									.getValue()), stringMessages,
							new DialogCallback<SeriesDTO>() {
								@Override
								public void cancel() {
								}

								@Override
								public void ok(SeriesDTO newSeries) {
									addValue(newSeries);
								}
							});
					dialog.ensureDebugId("DefaultSeriesCreateDialog");
					dialog.show();
				}
			});
			return addSeriesButton;
		}

		@Override
		protected Widget createValueWidget(int rowIndex, SeriesDTO seriesDTO) {
			HorizontalPanel hPanel = new HorizontalPanel();
			hPanel.setSpacing(5);
			Label seriesLabel = new Label(stringMessages.series() + " '"
					+ seriesDTO.getName() + "' :");
			seriesLabel.setWordWrap(false);
			seriesLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
			hPanel.add(seriesLabel);

			String fleetText = seriesDTO.getFleets().size() + " ";

			if (seriesDTO.getFleets() != null
					&& seriesDTO.getFleets().size() > 0) {
				if (seriesDTO.getFleets().size() == 1) {
					fleetText = "1 " + stringMessages.fleet();
				} else {
					fleetText = seriesDTO.getFleets().size() + " "
							+ stringMessages.fleets();
				}
				fleetText += ": " + seriesDTO.getFleets().toString();
			} else {
				fleetText = "No fleets defined.";
			}
			hPanel.add(new Label(fleetText));

			return hPanel;
		}

	}

}
