package com.sap.sailing.simulator.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.impl.MeterDistance;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.simulator.Boundary;
import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.PolarDiagram;
import com.sap.sailing.simulator.SailingSimulator;
import com.sap.sailing.simulator.SimulationParameters;
import com.sap.sailing.simulator.TimedPosition;
import com.sap.sailing.simulator.TimedPositionWithSpeed;
import com.sap.sailing.simulator.WindField;

public class SailingSimulatorImpl implements SailingSimulator {

	SimulationParameters simulationParameters;
	
	public SailingSimulatorImpl(SimulationParameters params) {
		simulationParameters = params;
	}
	
	@Override
	public void setSimulationParameters(SimulationParameters params) {
		simulationParameters = params;
	}

	@Override
	public SimulationParameters getSimulationParameters() {
		return simulationParameters;
	}

	@Override
	public Path getOptimumPath() {
		
		//calls either createDummy or createHeuristic()
		
		return createHeuristic();
	}
	
	private Path createDummy() {
		Boundary boundary = simulationParameters.getBoundaries();
		WindField wf = simulationParameters.getWindField();
		PolarDiagram pd = simulationParameters.getBoatPolarDiagram();
		Position start = simulationParameters.getCourse().get(0);
		Position end = simulationParameters.getCourse().get(1);
		TimePoint startTime = new MillisecondsTimePoint(0);
		List<TimedPositionWithSpeed> lst = new ArrayList<TimedPositionWithSpeed>();
		
		pd.setWind(wf.getWind(new TimedPositionWithSpeedSimple(start)));
		Bearing direct = start.getBearingGreatCircle(end);
		TimedPositionWithSpeed p1 = new TimedPositionWithSpeedImpl(startTime, start, pd.getSpeedAtBearing(direct));
		TimedPositionWithSpeed p2 = new TimedPositionWithSpeedImpl(new MillisecondsTimePoint(3600000), end, null);
		lst.add(p1);
		lst.add(p2);
		
		return new PathImpl(lst);
	}
	
	private Path createHeuristic() {
		Boundary boundary = simulationParameters.getBoundaries();
		WindField wf = simulationParameters.getWindField();
		PolarDiagram pd = simulationParameters.getBoatPolarDiagram();
		Position start = simulationParameters.getCourse().get(0);
		Position end = simulationParameters.getCourse().get(1);
		TimePoint startTime = new MillisecondsTimePoint(0);
		List<TimedPositionWithSpeed> lst = new ArrayList<TimedPositionWithSpeed>();
	
		Position currentPosition = start;
		TimePoint currentTime = startTime;
		
		//while there is more than 5% of the total distance to the finish 
		while ( currentPosition.getDistance(end).compareTo(start.getDistance(end).scale(0.05)) > 0) {
			
			TimePoint nextTime = new MillisecondsTimePoint(currentTime.asMillis() + 30000);
			
			pd.setWind(wf.getWind(new TimedPositionWithSpeedImpl(nextTime, currentPosition, null)));
			
			Bearing lft = pd.optimalDirectionsDownwind()[0];
			Bearing rght = pd.optimalDirectionsUpwind()[1];
			Bearing direct = currentPosition.getBearingGreatCircle(end);
			
			SpeedWithBearing sdirect = pd.getSpeedAtBearing(direct);
			SpeedWithBearing slft= pd.getSpeedAtBearing(lft);
			SpeedWithBearing srght = pd.getSpeedAtBearing(rght);
			
			Position pdirect = sdirect.travelTo(currentPosition, currentTime, nextTime);
			Position plft = slft.travelTo(currentPosition, currentTime, nextTime);
			Position prght = srght.travelTo(currentPosition, currentTime, nextTime);
			
			Distance ddirect = pdirect.getDistance(end);
			Distance dlft = plft.getDistance(end);
			Distance drght = prght.getDistance(end);
			
			if(ddirect.compareTo(dlft)<=0 && ddirect.compareTo(drght)<=0) {
				lst.add(new TimedPositionWithSpeedImpl(nextTime, pdirect, sdirect));
				currentPosition = pdirect;
			}
				
			if(dlft.compareTo(ddirect)<=0 && dlft.compareTo(drght)<=0) {
				lst.add(new TimedPositionWithSpeedImpl(nextTime, plft, slft));
				currentPosition = plft;
			}
				
			if(drght.compareTo(dlft)<=0 && drght.compareTo(ddirect)<=0) {
				lst.add(new TimedPositionWithSpeedImpl(nextTime, prght, srght));
				currentPosition = prght;
			}
			
			currentTime = nextTime;
			
		}
		
		return new PathImpl(lst);
	}
	
	private Path createForwardDynamic() {
		Boundary boundary = simulationParameters.getBoundaries();
		WindField wf = simulationParameters.getWindField();
		PolarDiagram pd = simulationParameters.getBoatPolarDiagram();
		Position start = simulationParameters.getCourse().get(0);
		Position end = simulationParameters.getCourse().get(1);
		TimePoint startTime = new MillisecondsTimePoint(0);
		List<TimedPositionWithSpeed> lst = new ArrayList<TimedPositionWithSpeed>();
		
		Position currentPosition = start;
		TimePoint currentTime = startTime;
		
		int gridv = 10; // number of vertical grid steps
		int gridh = 30; // number of horizontal grid steps
		Position[][] sailGrid = boundary.extractGrid(gridh, gridv);
		
		
		
		
		/*
		stretch.x <- 0.5 ## stretch-factor for horizontal grid
		
		grid.x <- (-grid.h:grid.h)/grid.h*stretch.x
		grid.y <- (0:grid.v)/grid.v
		
		sail.grid <- expand.grid(grid.x,grid.y)
		*/
		
		
		/*for(idxv=1; idxv<=hridv; idxv++) {
		
		  paths.new <- vector("list",2*grid.h+1)
		  duras.new <- vector("list",2*grid.h+1)
		  alldur <-  NULL
		
		  if (idx.v==1) {
		    range1 <- 0
		  } else {
		    range1 <- grid.h
		  }
		
		  if (idx.v==grid.v) {
		    range2 <- 0
		  } else {
		    range2 <- grid.h
		  }
		  
		  for(idx.h2 in -range2:range2) {
		
		    alldur <- NULL
		    
		    for(idx.h1 in -range1:range1) {
		
		      duration.h1h2 <- duration(idx.h1,idx.h2,idx.v)
		      
		      if (duration.h1h2 == Inf) {
		        next
		      }
		      
		      if (idx.v==1) {
		        dur <- data.frame(duration=signif(duration.h1h2,digits=sig.digits),hidx=idx.h1+range1+1,pidx=1)
		        if (is.null(alldur)) {
		          alldur <- dur
		        } else {
		          alldur <- rbind(alldur,dur)
		        }
		        
		      } else {
		
		        for(pidx in 1:length(paths[[idx.h1+range1+1]])) {
		
		          curdur = signif(duration.h1h2 + duras[[idx.h1+range1+1]][pidx],digits=sig.digits)
		          
		          dur <- data.frame(duration=curdur,hidx=idx.h1+range1+1,pidx=pidx)
		          if (is.null(alldur)) {
		            alldur <- dur
		          } else {
		            alldur <- rbind(alldur,dur)
		          }
		          
		        }
		        
		      }
		      
		    }
		    
		    if (idx.v>1) {
		
		      if (idx.v != grid.v) {
		
		        idx.min <- alldur[which(alldur[,"duration"] == min(alldur[,"duration"])),]
		
		        jdx <- 0
		        for (pidx in 1:nrow(idx.min)) {
		          jdx <- jdx+1
		          paths.new[[idx.h2+grid.h+1]][[jdx]] <- c(paths[[idx.min[pidx,"hidx"]]][[idx.min[pidx,"pidx"]]],idx.h2)
		          duras.new[[idx.h2+grid.h+1]][[jdx]] <- idx.min[pidx,"duration"]
		        }
		
		      } else {
		
		        for(ddx in 1:nrow(alldur)) {
		
		          tcks <- tacks(c(paths[[alldur[ddx,"hidx"]]][[alldur[ddx,"pidx"]]],0))
		          alldur[ddx,"duration"] <- tcks*tack.cost + alldur[ddx,"duration"]
		          
		        }
		
		        dur.idx <- order(alldur[,"duration"])
		        srtdur <- alldur[dur.idx,]
		        
		        jdx <- 1
		        kdx <- 1
		        p.grps <- length(unique(alldur[,"duration"]))
		        paths.new <- vector("list",p.grps)
		        paths.new[[kdx]][[jdx]] <- c(paths[[srtdur[1,"hidx"]]][[srtdur[1,"pidx"]]],0)
		        duras.new <- vector("list",p.grps)
		        duras.new[[kdx]] <- srtdur[1,"duration"]
		
		        for (pidx in 2:nrow(alldur)) {
		          
		          if (srtdur[pidx,"duration"]==srtdur[pidx-1,"duration"]) {
		            jdx <- jdx + 1
		          } else {
		            jdx <- 1
		            kdx <- kdx + 1
		            duras.new[[kdx]] <- srtdur[pidx,"duration"]
		          }
		          paths.new[[kdx]][[jdx]] <- c(paths[[srtdur[pidx,"hidx"]]][[srtdur[pidx,"pidx"]]],0)
		
		        }
		
		      }
		      
		    } else {
		      for (idx in -grid.h:grid.h) {
		        paths.new[[idx+grid.h+1]][[1]] <- c(0,idx)
		        dur.eval <- duration(0,idx,1)
		        duras.new[[idx+grid.h+1]][[1]] <- dur.eval[1]
		      }   
		    }
		    
		  }
		
		  paths <- paths.new
		  duras <- duras.new
		
		}
		 
		 */
		
		return new PathImpl(lst);
	}
	
}
