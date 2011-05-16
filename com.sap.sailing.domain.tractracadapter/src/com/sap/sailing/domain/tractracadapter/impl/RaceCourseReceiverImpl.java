package com.sap.sailing.domain.tractracadapter.impl;

import java.util.ArrayList;
import java.util.List;

import com.maptrack.client.io.TypeController;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.impl.RaceDefinitionImpl;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.RaceCourseReceiver;
import com.tractrac.clientmodule.Race;
import com.tractrac.clientmodule.RaceCompetitor;
import com.tractrac.clientmodule.Route;
import com.tractrac.clientmodule.data.ICallbackData;
import com.tractrac.clientmodule.data.RouteData;

public class RaceCourseReceiverImpl implements RaceCourseReceiver {
    private final Event event;
    private final com.tractrac.clientmodule.Event tractracEvent;
    
    public RaceCourseReceiverImpl(Event event, com.tractrac.clientmodule.Event tractracEvent) {
        super();
        this.event = event;
        this.tractracEvent = tractracEvent;
    }

    @Override
    public Iterable<TypeController> getRouteListeners() {
        List<TypeController> result = new ArrayList<TypeController>();
        for (final Race race : tractracEvent.getRaceList()) {
            final List<Competitor> competitors = new ArrayList<Competitor>();
            for (RaceCompetitor rc : race.getRaceCompetitorList()) {
                competitors.add(DomainFactory.INSTANCE.getCompetitor(rc.getCompetitor()));
            }
            TypeController routeListener = RouteData.subscribe(race,
                    new ICallbackData<Route, RouteData>() {
                        @Override
                        public void gotData(Route route, RouteData record) {
                            Course course = DomainFactory.INSTANCE.createCourse(route.getName(), record.getPoints());
                            RaceDefinition raceDefinition = new RaceDefinitionImpl(race.getName(),
                                    course, /* TODO how to know the BoatClass? */ null, competitors);
                            event.addRace(raceDefinition);
                        }
                    });
            result.add(routeListener);
        }
        return result;
    }

}
