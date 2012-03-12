package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.TimeListener;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDTO;
import com.sap.sailing.gwt.ui.shared.WindTrackInfoDTO;

public class CombinedWindPanel extends SimplePanel implements TimeListener, RaceSelectionChangeListener {
    private ImageTransformer transformer;
    private RaceMapResources imageResources;
    
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;

    private final Timer timer;

    private List<String> windSourceTypeNames;
    
    private List<RaceIdentifier> selectedRaces;

    private Image windSymbolImage;
    
    public CombinedWindPanel(SailingServiceAsync sailingService, ErrorReporter errorReporter, Timer theTimer) {
        this.setSize("32px", "32px");
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.timer = theTimer;
        timer.addTimeListener(this);

        imageResources = new RaceMapResources();
        
        transformer = imageResources.getCombinedWindIconTransformer();

        windSourceTypeNames = new ArrayList<String>();
        windSourceTypeNames.add(WindSourceType.COMBINED.name());
        
        windSymbolImage = new Image();
        setWidget(windSymbolImage);
    }

    @Override
    public void timeChanged(Date date) {
        if (date != null) {
            if (selectedRaces != null && !selectedRaces.isEmpty()) {
                RaceIdentifier race = selectedRaces.get(selectedRaces.size() - 1);
                
                if (race != null) {
                    // draw the wind into the map, get the combined wind
                    sailingService.getWindInfo(race, date, 1000L, 1, 0.0, 0.0, null,
                            new AsyncCallback<WindInfoForRaceDTO>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    errorReporter.reportError("Error obtaining wind: " + caught.getMessage());
                                }

                                @Override
                                public void onSuccess(WindInfoForRaceDTO windInfoForRaceDTO) {
                                    updateWind(windInfoForRaceDTO);
                                }
                            });
                }
            }
        }
    }

    @Override
    public void onRaceSelectionChange(List<RaceIdentifier> selectedRaces) {
        this.selectedRaces = selectedRaces;
    }

    protected void updateWind(WindInfoForRaceDTO windInfo) {
        for(WindSource windSource: windInfo.windTrackInfoByWindSource.keySet()) {
            WindTrackInfoDTO windTrackInfoDTO = windInfo.windTrackInfoByWindSource.get(windSource);
            switch (windSource.getType()) {
                    case COMBINED:
                    {
                        if(windTrackInfoDTO.windFixes.size() > 0) {
                            WindDTO windDTO = windTrackInfoDTO.windFixes.get(0);
                            // double speedInKnots = windDTO.dampenedTrueWindSpeedInKnots;
                            double windFromDeg = windDTO.dampenedTrueWindFromDeg;

                            String transformedImageURL = transformer.getTransformedImageURL(windFromDeg, 1.0);
                            windSymbolImage.setUrl(transformedImageURL);
                            
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
