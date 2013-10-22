package com.sap.sailing.domain.test.markpassing;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.sap.sailing.domain.common.TimePoint;

public class CandidateChooser implements AbstractCandidateChooser {

    @Override
    public ArrayList<TimePoint> getMarkPasses(ArrayList<Candidate> candidates, Candidate start, Candidate end) {

        // Create Edges
        ArrayList<Edge> edges = new ArrayList<>();
        for (Candidate c1 : candidates) {
            for (Candidate c2 : candidates) {
                if (c1.getID() < c2.getID() && c2.getTimePoint().after(c1.getTimePoint())) {
                    edges.add(new Edge(c1, c2));
                }
            }
        }

        // Find cheapest Edges
        LinkedHashMap<Candidate, Candidate> candidateWithParent = new LinkedHashMap<>();
        Edge startingEdge = new Edge(start, start);
        
        while (!candidateWithParent.keySet().contains(end)) {
            Edge newCheapestEdge = startingEdge;
            
            for (Edge e : edges) {
                    if (candidateWithParent.keySet().contains(e.getStart()) && e.getCost() < newCheapestEdge.getCost()||newCheapestEdge==startingEdge) {
                        newCheapestEdge = e;
                }
            }
            if(!candidateWithParent.keySet().contains(newCheapestEdge.getEnd())){
            candidateWithParent.put(newCheapestEdge.getEnd(), newCheapestEdge.getStart());    
            }
            edges.remove(newCheapestEdge);
        }
        // Find shortest Path to Start
        ArrayList<TimePoint> markPasses = new ArrayList<>();
        Candidate current = end;
        while (!(current == start)) {
            current = candidateWithParent.get(current);
            markPasses.add(current.getTimePoint());
        }
        markPasses.remove(start.getTimePoint());
        return markPasses;
    }
}
