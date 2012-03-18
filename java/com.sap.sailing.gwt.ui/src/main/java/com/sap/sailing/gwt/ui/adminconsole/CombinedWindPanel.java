package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.gwt.ui.actions.AsyncActionsExecutor;
import com.sap.sailing.gwt.ui.actions.GetWindInfoAction;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.TimeListener;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.client.WindSourceTypeFormatter;
import com.sap.sailing.gwt.ui.client.Timer.PlayModes;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDTO;
import com.sap.sailing.gwt.ui.shared.WindTrackInfoDTO;

public class CombinedWindPanel extends FlowPanel implements TimeListener, RaceSelectionChangeListener {
    private ImageTransformer transformer;
    private RaceMapResources imageResources;
    
    private final SailingServiceAsync sailingService;
    private final AsyncActionsExecutor asyncActionsExecutor;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;
    
    private final Timer timer;

    private List<String> windSourceTypeNames;
    
    private List<RaceIdentifier> selectedRaces;

    private final Image windSymbolImage;
    private final Label textLabel;
    
    public CombinedWindPanel(SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor, ErrorReporter errorReporter, StringMessages stringMessages, Timer theTimer) {
        this.setSize("32px", "52px");
        this.sailingService = sailingService;
        this.asyncActionsExecutor = asyncActionsExecutor;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        this.timer = theTimer;
        timer.addTimeListener(this);

        imageResources = new RaceMapResources();
        
        transformer = imageResources.getCombinedWindIconTransformer();

        windSourceTypeNames = new ArrayList<String>();
        windSourceTypeNames.add(WindSourceType.COMBINED.name());
        
        windSymbolImage = new Image();
        add(windSymbolImage);
        
        textLabel = new Label("");
        textLabel.setSize("32px", "12px");
        textLabel.getElement().getStyle().setFontSize(12, Unit.PX);
        textLabel.setWordWrap(false);
        textLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        add(textLabel);
    }

    @Override
    public void timeChanged(Date date) {
        if (date != null) {
            if (selectedRaces != null && !selectedRaces.isEmpty()) {
                RaceIdentifier race = selectedRaces.get(selectedRaces.size() - 1);
                if (race != null) {
                    // draw the wind into the map, get the combined wind
                    GetWindInfoAction getWindInfoAction = new GetWindInfoAction(sailingService, race, date, 1000L, 1, windSourceTypeNames);
                    getWindInfoAction.setCallback(new AsyncCallback<WindInfoForRaceDTO>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                if(timer.getPlayMode() != PlayModes.Live)
                                    errorReporter.reportError("Error obtaining wind: " + caught.getMessage());
                            }

                            @Override
                            public void onSuccess(WindInfoForRaceDTO windInfoForRaceDTO) {
                                updateWind(windInfoForRaceDTO);
                            }
                        });
                    
                    asyncActionsExecutor.execute(getWindInfoAction);
                }
            }
        }
    }

    @Override
    public void onRaceSelectionChange(List<RaceIdentifier> selectedRaces) {
        this.selectedRaces = selectedRaces;
    }

    protected void updateWind(WindInfoForRaceDTO windInfo) {
        if(windInfo != null) {
            for(WindSource windSource: windInfo.windTrackInfoByWindSource.keySet()) {
                WindTrackInfoDTO windTrackInfoDTO = windInfo.windTrackInfoByWindSource.get(windSource);
                switch (windSource.getType()) {
                    case COMBINED:
                    {
                        if(windTrackInfoDTO.windFixes.size() > 0) {
                            WindDTO windDTO = windTrackInfoDTO.windFixes.get(0);
                            double speedInKnots = windDTO.dampenedTrueWindSpeedInKnots;
                            double windFromDeg = windDTO.dampenedTrueWindFromDeg;
                            NumberFormat numberFormat = NumberFormat.getFormat("0.0");
                            
                            double rotationDegOfWindSymbol = 180.0 + windFromDeg;
                            if(rotationDegOfWindSymbol >= 360.0)
                                rotationDegOfWindSymbol = rotationDegOfWindSymbol - 360; 
                            String transformedImageURL = transformer.getTransformedImageURL(rotationDegOfWindSymbol, 1.0);
                            windSymbolImage.setUrl(transformedImageURL);
                            String title = stringMessages.wind() + ": " +  Math.round(windFromDeg) + " " 
                                    + stringMessages.degreesShort() + " (" + WindSourceTypeFormatter.format(windSource, stringMessages) + ")"; 
                            windSymbolImage.setTitle(title);
                            textLabel.setText(numberFormat.format(speedInKnots) + " " + stringMessages.averageSpeedInKnotsUnit());
                            
                            if(!isVisible())
                                setVisible(true);
                        } else {
                            setVisible(false);
                        }
                    }
                    break;
                }
            }
        }
    }
}
