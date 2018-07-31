package com.tractrac.subscription.app.tracapi;

import java.net.URI;
import java.util.UUID;

import com.tractrac.model.lib.api.data.IControlPassings;
import com.tractrac.model.lib.api.data.IMessageData;
import com.tractrac.model.lib.api.data.IPosition;
import com.tractrac.model.lib.api.data.IPositionOffset;
import com.tractrac.model.lib.api.data.IPositionSnapped;
import com.tractrac.model.lib.api.data.IStartStopData;
import com.tractrac.model.lib.api.event.DataSource;
import com.tractrac.model.lib.api.event.ICompetitor;
import com.tractrac.model.lib.api.event.IEvent;
import com.tractrac.model.lib.api.event.IRace;
import com.tractrac.model.lib.api.event.IRaceCompetitor;
import com.tractrac.model.lib.api.route.IControl;
import com.tractrac.model.lib.api.route.IControlRoute;
import com.tractrac.model.lib.api.route.IPathRoute;
import com.tractrac.model.lib.api.sensor.ISensorData;
import com.tractrac.subscription.lib.api.competitor.ICompetitorSensorDataListener;
import com.tractrac.subscription.lib.api.competitor.ICompetitorsListener;
import com.tractrac.subscription.lib.api.competitor.IPositionListener;
import com.tractrac.subscription.lib.api.competitor.IPositionOffsetListener;
import com.tractrac.subscription.lib.api.competitor.IPositionSnappedListener;
import com.tractrac.subscription.lib.api.control.IControlPassingsListener;
import com.tractrac.subscription.lib.api.control.IControlPointPositionListener;
import com.tractrac.subscription.lib.api.control.IControlRouteChangeListener;
import com.tractrac.subscription.lib.api.control.IControlsListener;
import com.tractrac.subscription.lib.api.event.IConnectionStatusListener;
import com.tractrac.subscription.lib.api.event.IEventMessageListener;
import com.tractrac.subscription.lib.api.event.ILiveDataEvent;
import com.tractrac.subscription.lib.api.event.IStoredDataEvent;
import com.tractrac.subscription.lib.api.race.IRaceMessageListener;
import com.tractrac.subscription.lib.api.race.IRaceStartStopTimesChangeListener;
import com.tractrac.subscription.lib.api.race.IRacesListener;

/**
 * @author <a href="mailto:jorge@tractrac.dk">Jorge Piera Llodr&aacute;</a>
 */
public abstract class AbstractListener  implements IEventMessageListener,
        IRaceMessageListener, IPositionListener, IPositionOffsetListener,
        IPositionSnappedListener, IConnectionStatusListener, IControlPointPositionListener,
        IControlPassingsListener, IRaceStartStopTimesChangeListener,
        IControlRouteChangeListener, ICompetitorSensorDataListener,
        IRacesListener, ICompetitorsListener, IControlsListener {

    @Override
    public void gotStoredDataEvent(IStoredDataEvent storedDataEvent) {

    }

    @Override
    public void gotLiveDataEvent(ILiveDataEvent liveDataEvent) {

    }

    @Override
    public void gotRouteChange(IControlRoute controlRoute, long timeStamp) {

    }

    @Override
    public void gotRouteChange(IPathRoute controlRoute, long timeStamp) {

    }

    @Override
    public void gotControlPassings(IRaceCompetitor raceCompetitor,
                                   IControlPassings markPassings) {

    }

    @Override
    public void gotControlPointPosition(IControl control, IPosition position, int markNumber) {

    }

    @Override
    public void gotPositionSnapped(IRaceCompetitor raceCompetitor,
                                   IPositionSnapped positionSnapped) {

    }

    @Override
    public void gotPositionOffset(IRaceCompetitor raceCompetitor,
                                  IPositionOffset position) {

    }

    @Override
    public void gotPosition(IRaceCompetitor raceCompetitor, IPosition position) {

    }

    @Override
    public void gotRaceStartStopTime(IRace race, IStartStopData startStopData) {

    }

    @Override
    public void gotTrackingStartStopTime(IRace race, IStartStopData startStopData) {

    }

    @Override
    public void gotRaceMessage(IRace race, IMessageData messageData) {

    }

    @Override
    public void stopped(Object object) {

    }

    @Override
    public void gotEventMessage(IEvent event, IMessageData messageData) {

    }

    @Override
    public void gotSensorData(IRaceCompetitor raceCompetitor, ISensorData sensorData) {

    }

    @Override
    public void updateCompetitor(ICompetitor competitor) {

    }

    @Override
    public void addCompetitor(ICompetitor competitor) {

    }

    @Override
    public void deleteCompetitor(UUID competitorId) {

    }

    @Override
    public void updateControl(IControl control) {

    }

    @Override
    public void addControl(IControl control) {

    }

    @Override
    public void deleteControl(UUID controlId) {

    }

    @Override
    public void updateRace(IRace race) {

    }

    @Override
    public void addRace(IRace race) {

    }

    @Override
    public void deleteRace(UUID raceId) {

    }

    @Override
    public void reloadRace(UUID raceId) {

    }

    @Override
    public void abandonRace(UUID raceId) {

    }

    @Override
    public void startTracking(UUID raceId) {

    }

    @Override
    public void dataSourceChanged(IRace race, DataSource oldDataSource, URI oldLiveURI, URI oldStoredURI) {

    }
}
