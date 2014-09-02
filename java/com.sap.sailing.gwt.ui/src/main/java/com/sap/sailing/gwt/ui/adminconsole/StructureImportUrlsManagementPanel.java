package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.github.gwtbootstrap.client.ui.base.ProgressBarBase.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.RegattaSelectionModel;
import com.sap.sailing.gwt.ui.client.RegattaSelectionProvider;
import com.sap.sailing.gwt.ui.client.RegattasDisplayer;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.TextfieldEntryDialog;
import com.sap.sailing.gwt.ui.shared.CourseAreaDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;

public class StructureImportUrlsManagementPanel extends FlowPanel implements
		RegattasDisplayer {
	private final ListBox urlProviderSelectionListBox;
	private final ListBox urlListBox;

	private final SailingServiceAsync sailingService;
	private final ErrorReporter errorReporter;
	private final StringMessages stringMessages;
	private final RegattaRefresher regattaRefresher;
	private final EventManagementPanel eventManagementPanel;
	private RegattaListComposite regattaListComposite;
	private RegattaSelectionProvider regattaSelectionProvider;
	private Panel progressPanel;

	private final Button addButton;
	private final Button removeButton;
	private final Button importDetailsButton;

	private StructureImportProgressBar progressBar;
	private Label overallName;
	private int parsed;

	public StructureImportUrlsManagementPanel(
			SailingServiceAsync sailingService, ErrorReporter errorReporter,
			StringMessages stringMessages, RegattaRefresher regattaRefresher,
			EventManagementPanel eventManagementPanel) {
		this.eventManagementPanel = eventManagementPanel;
		this.sailingService = sailingService;
		this.errorReporter = errorReporter;
		this.stringMessages = stringMessages;
		this.regattaRefresher = regattaRefresher;
		progressPanel = new FlowPanel();
		urlListBox = new ListBox(/* multiple select */true);
		urlProviderSelectionListBox = new ListBox(false);
		urlProviderSelectionListBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				int selectedIndex = urlProviderSelectionListBox
						.getSelectedIndex();
				boolean buttonsEnabled = selectedIndex > 0;
				addButton.setEnabled(buttonsEnabled);
				removeButton.setEnabled(buttonsEnabled);
				refreshUrlList();
			}
		});

		addButton = new Button(stringMessages.add());
		removeButton = new Button(stringMessages.remove());
		importDetailsButton = new Button(stringMessages.importRegatta());
		importDetailsButton.setEnabled(false);
		importDetailsButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				List<RegattaDTO> regattaDTOs = new ArrayList<RegattaDTO>();
				for (RegattaIdentifier selectedRegatta : regattaSelectionProvider
						.getSelectedRegattas()) {
					for (RegattaDTO regattaDTO : regattaListComposite
							.getAllRegattas()) {
						if (regattaDTO.getRegattaIdentifier().equals(
								selectedRegatta)) {
							regattaDTOs.add(regattaDTO);
						}
					}
				}
				createEventDetails(regattaDTOs);
			}
		});
		addButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				addUrl();
			}
		});
		removeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				List<RegattaDTO> regattaDTOs = regattaListComposite
						.getAllRegattas();
				for (RegattaIdentifier selectedRegatta : regattaSelectionProvider
						.getSelectedRegattas()) {
					for (RegattaDTO regattaDTO : regattaListComposite
							.getAllRegattas()) {
						if (regattaDTO.getRegattaIdentifier().equals(
								selectedRegatta)) {
							regattaDTOs.remove(regattaDTO);
							break;
						}
					}
				}
				fillRegattas(regattaDTOs);
				regattaDTOs.clear();
			}
		});
		VerticalPanel vp = new VerticalPanel();

		HorizontalPanel buttonPanel = new HorizontalPanel();
		vp.add(buttonPanel);
		buttonPanel.add(addButton);
		buttonPanel.add(importDetailsButton);
		buttonPanel.add(removeButton);
		Grid grid = new Grid(1, 1);
		vp.add(grid);

		regattaSelectionProvider = new RegattaSelectionModel(true);

		regattaListComposite = new RegattaListComposite(sailingService,
				regattaSelectionProvider, regattaRefresher, errorReporter,
				stringMessages, "test");
		regattaListComposite.ensureDebugId("RegattaListComposite");
		grid.setWidget(0, 0, regattaListComposite);
		grid.getRowFormatter().setVerticalAlign(0,
				HasVerticalAlignment.ALIGN_TOP);
		grid.getColumnFormatter().getElement(0/** 1 */
		).getStyle().setPaddingTop(2.0, Unit.EM);

		add(progressPanel);
		add(vp);
		refreshUrlList();

	}

	private String getSelectedProviderName() {
		String result = null;
		int selectedIndex = urlProviderSelectionListBox.getSelectedIndex();
		if (selectedIndex > 0) {
			result = urlProviderSelectionListBox.getItemText(selectedIndex);
		}
		return result;
	}

	private void refreshUrlList() {
		String selectedProviderName = getSelectedProviderName();
		if (selectedProviderName != null) {
			sailingService.getResultImportUrls(selectedProviderName,
					new AsyncCallback<List<String>>() {
						@Override
						public void onFailure(Throwable caught) {
							errorReporter.reportError(stringMessages
									.errorRefreshingResultImportUrlList(caught
											.getMessage()));
						}

						@Override
						public void onSuccess(List<String> result) {
							urlListBox.clear();
							for (String s : result) {
								urlListBox.addItem(s);
							}
						}
					});
		} else {
			urlListBox.clear();
		}
	}

	private void createEventDetails(List<RegattaDTO> regattas) {
		List<String> regattaNamesTemp = new ArrayList<String>();
		for (RegattaDTO regatta : regattas) {
			regattaNamesTemp.add(regatta.getName());
		}

		final List<String> regattaNames = regattaNamesTemp;
		sailingService.getEvents(new AsyncCallback<List<EventDTO>>() {
			@Override
			public void onFailure(Throwable caught) {
				errorReporter.reportError("Error trying to get events");
			}

			@Override
			public void onSuccess(List<EventDTO> events) {
				List<EventDTO> existingEvents = events;
				EventCreateDialog dialog = new EventCreateDialog(Collections
						.unmodifiableCollection(existingEvents),
						stringMessages, new DialogCallback<EventDTO>() {
							@Override
							public void cancel() {
							}

							@Override
							public void ok(final EventDTO newEvent) {
								List<String> courseAreaNames = new ArrayList<String>();
								for (CourseAreaDTO courseAreaDTO : newEvent.venue
										.getCourseAreas()) {
									courseAreaNames.add(courseAreaDTO.getName());
								}
								sailingService.createEvent(newEvent.getName(),
										newEvent.startDate, newEvent.endDate,
										newEvent.venue.getName(),
										newEvent.isPublic, courseAreaNames,
										newEvent.getImageURLs(),
										newEvent.getVideoURLs(),
										newEvent.getSponsorImageURLs(),
										newEvent.getLogoImageURL(),
										newEvent.getOfficialWebsiteURL(),
										new AsyncCallback<EventDTO>() {

											@Override
											public void onFailure(Throwable t) {
												errorReporter.reportError("Error trying to create new event "
														+ newEvent.getName()
														+ ": " + t.getMessage());
											}

											@Override
											public void onSuccess(
													EventDTO newEvent) {
												setDefaultRegatta(regattaNames,
														newEvent);
											}
										});

							}
						});
				dialog.show();
			}
		});
	}

	private void addUrl() {
		final TextfieldEntryDialog dialog = new TextfieldEntryDialog(
				stringMessages.addStructureImportUrl(),
				stringMessages.addStructureImportUrl(), stringMessages.add(),
				stringMessages.cancel(), "http://",
				new DataEntryDialog.Validator<String>() {
					@Override
					public String getErrorMessage(String valueToValidate) {
						String result = null;
						if (valueToValidate == null
								|| valueToValidate.length() == 0) {
							result = stringMessages.pleaseEnterNonEmptyUrl();
						}
						return result;
					}
				}, new DialogCallback<String>() {
					@Override
					public void cancel() {
						// user cancelled; just don't add
					}

					@Override
					public void ok(final String url) {
						sailingService.getRegattas(url,
								new AsyncCallback<List<RegattaDTO>>() {
									@Override
									public void onFailure(Throwable caught) {
										errorReporter
												.reportError("Error trying to load regattas");
									}

									@Override
									public void onSuccess(
											List<RegattaDTO> regattas) {
										fillRegattas(regattas);
										importDetailsButton.setEnabled(true);
										removeButton.setEnabled(true);
									}
								});

					}
				});
		dialog.getEntryField().setVisibleLength(100);
		dialog.show();
	}

	@Override
	public void fillRegattas(List<RegattaDTO> regattas) {
		regattaListComposite.fillRegattas(regattas);

	}

	private void createBarAndLabel(int amountOfRegattas) {
		overallName = new Label(stringMessages.overallProgress() + ":");
		progressPanel.add(overallName);
		progressBar = new StructureImportProgressBar(amountOfRegattas + 1,
				Style.ANIMATED);
		progressPanel.add(progressBar);

	}

	private void setProgressBar(final int amountOfRegattas) {
		createBarAndLabel(amountOfRegattas);

		final Timer timer = new Timer() {
			@Override
			public void run() {
				sailingService
						.getStructureImportOperationProgress(new AsyncCallback<Integer>() {
							@Override
							public void onFailure(Throwable caught) {
								errorReporter.reportError(stringMessages
										.errorAddingResultImportUrl(caught
												.getMessage()));
							}

							@Override
							public void onSuccess(Integer result) {
								parsed = result;
								progressBar.setPercent(result);
							}

						});
				if (parsed >= amountOfRegattas) {
					regattaRefresher.fillRegattas();
					fillRegattas(new ArrayList<RegattaDTO>());
					progressBar.removeFromParent();
					overallName.removeFromParent();
					this.cancel();
				}
			}
		};
		timer.scheduleRepeating(2000);
	}

	private void setDefaultRegatta(final List<String> regattaNames,
			final EventDTO newEvent) {
		Collection<RegattaDTO> existingRegattas = Collections.emptyList();
		List<EventDTO> existingEvents = new ArrayList<EventDTO>();
		existingEvents.add(newEvent);

		DefaultRegattaCreateDialog dialog = new DefaultRegattaCreateDialog(
				existingRegattas, existingEvents, stringMessages,
				new DialogCallback<RegattaDTO>() {
					@Override
					public void cancel() {
					}

					@Override
					public void ok(RegattaDTO newRegatta) {
						createRegattas(regattaNames, newEvent, newRegatta);
					}
				});
		dialog.ensureDebugId("DefaultRegattaCreateDialog");
		dialog.show();

	}

	private void createRegattas(final List<String> regattaNames,
			EventDTO newEvent, RegattaDTO defaultRegatta) {
		eventManagementPanel.fillEvents();
		sailingService.addEventImportUrl(regattaNames, newEvent,
				defaultRegatta, new AsyncCallback<Void>() {
					@Override
					public void onFailure(Throwable caught) {
						errorReporter.reportError(stringMessages
								.errorAddingResultImportUrl(caught.getMessage()));
					}

					@Override
					public void onSuccess(Void result) {
						setProgressBar(regattaNames.size());
					}
				});
	}

}