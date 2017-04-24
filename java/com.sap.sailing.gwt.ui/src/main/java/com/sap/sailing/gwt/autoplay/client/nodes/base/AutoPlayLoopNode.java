package com.sap.sailing.gwt.autoplay.client.nodes.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;

public class AutoPlayLoopNode extends BaseCompositeNode {

    private List<AutoPlayNode> nodes = new ArrayList<>();
    private int loopTimePerNodeInSeconds;
    private int currentPos = -1;
    private Command onLoopEnd;
    private Timer transitionTimer = new Timer() {
        @Override
        public void run() {
            gotoNext();
        }
    };

    private void gotoNext() {
        currentPos++;
        if (currentPos > nodes.size() - 1) {
            if (onLoopEnd != null) {
                onLoopEnd.execute();
            }
            currentPos = 0;
        }
        transitionTo(nodes.get(currentPos));
    }
    
    @Override
    protected void transitionTo(AutoPlayNode nextNode) {
        super.transitionTo(nextNode);
        if (!isStopped()) {
            transitionTimer.schedule(loopTimePerNodeInSeconds * 1000);
        }
    }
    
    public AutoPlayLoopNode(int loopTimePerNodeInSeconds, AutoPlayNode... nodes) {
        this.loopTimePerNodeInSeconds = loopTimePerNodeInSeconds;
        this.nodes.addAll(Arrays.asList(nodes));
    }

    public void setOnLoopEnd(Command onLoopEnd) {
        this.onLoopEnd = onLoopEnd;
    }

    @Override
    public void onStart() {
        currentPos = -1;
        gotoNext();
    }

    @Override
    public void onStop() {
        super.onStop();
        for (AutoPlayNode autoPlayNode : nodes) {
            autoPlayNode.stop();
        }
        transitionTimer.cancel();
    }


}