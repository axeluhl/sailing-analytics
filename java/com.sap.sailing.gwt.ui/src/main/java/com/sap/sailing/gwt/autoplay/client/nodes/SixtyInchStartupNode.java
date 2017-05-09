package com.sap.sailing.gwt.autoplay.client.nodes;

import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.autoplay.client.nodes.base.AutoPlayLoopNode;
import com.sap.sailing.gwt.autoplay.client.nodes.base.AutoPlayNode;
import com.sap.sailing.gwt.autoplay.client.nodes.base.BaseCompositeNode;

public class SixtyInchStartupNode extends BaseCompositeNode {
    private AutoPlayClientFactory cf;
    private AutoPlayNode whenReadyNode;

    public SixtyInchStartupNode(final AutoPlayClientFactory cf) {
        this.cf = cf;
        AutoPlayLoopNode idleLoop = new AutoPlayLoopNode(30, new IdleUpNextNode(cf));
        AutoPlayLoopNode preLifeRaceLoop = new AutoPlayLoopNode(30, new PreRaceWithRacemapNode(cf));
        AutoPlayLoopNode lifeRaceLoop = new AutoPlayLoopNode(30, new LiveRaceWithRacemapNode(cf));
        AutoPlayLoopNode afterLifeRaceLoop = new AutoPlayLoopNode(30, new RaceEndWithCompetitorsNode(cf), idleLoop);
        SixtyInchRootNode raceLoop = new SixtyInchRootNode(cf, idleLoop, preLifeRaceLoop, lifeRaceLoop,
                afterLifeRaceLoop);
        whenReadyNode = raceLoop;
    }

    @Override
    public void onStart() {
        transitionTo(whenReadyNode);
    }
}
