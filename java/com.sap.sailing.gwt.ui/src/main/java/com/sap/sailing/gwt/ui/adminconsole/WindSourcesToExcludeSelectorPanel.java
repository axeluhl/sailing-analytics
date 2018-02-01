package com.sap.sailing.gwt.ui.adminconsole;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.WindSourceTypeFormatter;
import com.sap.sse.gwt.client.ErrorReporter;

/**
 * A panel that allows users to select one or more wind sources from a list of wind sources which shall be
 * excluded from default wind-based operations for a tracked race. For each of the available wind sources,
 * a check box is displayed. Check mark set means the wind source is selected and will be included. The user
 * has to un-check the tick mark to exclude the wind source.
 * 
 * @author Axel Uhl (d043530)
 */
public class WindSourcesToExcludeSelectorPanel extends VerticalPanel {
    private final LinkedHashMap<WindSource, CheckBox> checkboxesByWindSource;
    private final StringMessages stringMessages;
    private final SailingServiceAsync service;
    private final ErrorReporter errorReporter;
    private RegattaAndRaceIdentifier raceIdentifier;

    public WindSourcesToExcludeSelectorPanel(SailingServiceAsync service,
            StringMessages stringMessages, ErrorReporter errorReporter) {
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        this.service = service;
        this.checkboxesByWindSource = new LinkedHashMap<WindSource, CheckBox>();
        add(new Label(stringMessages.windSourcesUsed()));
    }
    
    public void update(RegattaAndRaceIdentifier raceIdentifier, Iterable<WindSource> allWindSources, Iterable<WindSource> windSourcesToExclude) {
        this.raceIdentifier = raceIdentifier;
        Set<WindSource> windSourcesToRemove = new HashSet<WindSource>(checkboxesByWindSource.keySet());
        for (WindSource windSource : allWindSources) {
            windSourcesToRemove.remove(windSource);
            CheckBox checkbox = checkboxesByWindSource.get(windSource);
            if (checkbox == null) {
                checkbox = new CheckBox(WindSourceTypeFormatter.format(windSource, stringMessages));
                checkbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                    @Override
                    public void onValueChange(ValueChangeEvent<Boolean> selected) {
                        onWindSourcesToExcludeChanged(WindSourcesToExcludeSelectorPanel.this.raceIdentifier);
                    }
                });
                checkboxesByWindSource.put(windSource, checkbox);
                checkbox.setValue(true);
                add(checkbox);
            } else {
                checkbox.setValue(true);
            }
        }
        for (WindSource windSourceToExclude : windSourcesToExclude) {
            assert checkboxesByWindSource.containsKey(windSourceToExclude);
            checkboxesByWindSource.get(windSourceToExclude).setValue(false); // deselect those excluded
        }
        for (WindSource windSourceToRemove : windSourcesToRemove) {
            CheckBox checkboxToRemove = checkboxesByWindSource.remove(windSourceToRemove);
            remove(checkboxToRemove);
        }
    }

    private void onWindSourcesToExcludeChanged(final RegattaAndRaceIdentifier raceIdentifier) {
        service.setWindSourcesToExclude(raceIdentifier, getWindSourcesToExclude(), new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable e) {
                errorReporter.reportError(stringMessages.errorTryingToUpdateWindSourcesToExclude(raceIdentifier.getRaceName(), e.getMessage()));
            }

            @Override
            public void onSuccess(Void arg0) {
            }
        });
    }

    private Iterable<WindSource> getWindSourcesToExclude() {
        Set<WindSource> result = new HashSet<WindSource>();
        for (Map.Entry<WindSource, CheckBox> e : checkboxesByWindSource.entrySet()) {
            if (!e.getValue().getValue()) {
                result.add(e.getKey());
            }
        }
        return result;
    }

}
