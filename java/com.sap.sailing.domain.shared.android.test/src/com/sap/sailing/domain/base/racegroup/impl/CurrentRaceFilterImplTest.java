package com.sap.sailing.domain.base.racegroup.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.sap.sailing.domain.base.SeriesBase;
import com.sap.sailing.domain.base.racegroup.CurrentRaceFilter;
import com.sap.sailing.domain.base.racegroup.IsRaceFragment;
import com.sap.sailing.domain.base.racegroup.IsFleetFragment;
import com.sap.sailing.domain.base.racegroup.RaceGroupFragment;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;

public class CurrentRaceFilterImplTest {
    
    private CurrentRaceFilter<RaceGroupFragment, SeriesFragment, RaceFragment> fixture;
    
    @Before
    public void setUp(){
        fixture = new CurrentRaceFilterImpl<>();
    }

    @Test
    public void easyRaceGroupTest() throws Exception{
        SeriesBase seriesBase = Mockito.mock(SeriesBase.class);
        List<RaceGroupFragment>  values = new ArrayList<>();
        SeriesFragment fristSerie = new SeriesFragment(seriesBase);
        values.add(fristSerie);
        RaceFragment firstRace = new RaceFragment(RaceLogRaceStatus.UNSCHEDULED);
        values.add(firstRace);
        
         List<RaceGroupFragment> result = fixture.filterCurrentRaces(values);
        Assert.assertThat(result.size(), Matchers.equalTo(2));
        Assert.assertThat(result.get(0), Matchers.equalTo(fristSerie));
        Assert.assertThat(result.get(1), Matchers.equalTo(firstRace));
    }
    
    @Test
    public void raceGroupWithTwoSeries() throws Exception{
         SeriesBase seriesBase1 = Mockito.mock(SeriesBase.class);
         SeriesBase seriesBase2 = Mockito.mock(SeriesBase.class);
         List<RaceGroupFragment>  values = new ArrayList<>();
         values.addAll(createRacesForSeries(3, seriesBase1));
         values.addAll(createRacesForSeries(3, seriesBase2));
         
         List<RaceGroupFragment> result = fixture.filterCurrentRaces(values);
         Assert.assertThat(result.size(), Matchers.equalTo(2));
         Assert.assertThat(result.get(0), Matchers.equalTo(values.get(0)));
         Assert.assertThat(result.get(1), Matchers.equalTo(values.get(1)));
         
         ((RaceFragment)values.get(5)).setCurrentStatus(RaceLogRaceStatus.PRESCHEDULED);
         
         result = fixture.filterCurrentRaces(values);
         Assert.assertThat(result.size(), Matchers.equalTo(5));
         Assert.assertThat(result.get(0), Matchers.equalTo(values.get(0)));
         Assert.assertThat(result.get(1), Matchers.equalTo(values.get(1)));
         Assert.assertThat(result.get(2), Matchers.equalTo(values.get(4)));
         Assert.assertThat(result.get(3), Matchers.equalTo(values.get(5)));
         Assert.assertThat(result.get(4), Matchers.equalTo(values.get(6)));
         
         ((RaceFragment)values.get(5)).setCurrentStatus(RaceLogRaceStatus.RUNNING);
         
         result = fixture.filterCurrentRaces(values);
         Assert.assertThat(result.size(), Matchers.equalTo(3));
         Assert.assertThat(result.get(0), Matchers.equalTo(values.get(4)));
         Assert.assertThat(result.get(1), Matchers.equalTo(values.get(5)));
         Assert.assertThat(result.get(2), Matchers.equalTo(values.get(6)));
    }
    
    
    private Collection<? extends RaceGroupFragment> createRacesForSeries(int max, SeriesBase seriesBase) {
        List<RaceGroupFragment> results = new ArrayList<>();
        results.add(new SeriesFragment(seriesBase));
        for(int i = 0; i<max; i++){
            results.add(new RaceFragment(RaceLogRaceStatus.UNSCHEDULED));
        }
        return results;
    }


    private class SeriesFragment implements IsFleetFragment{
        private SeriesBase series;
        
        public SeriesFragment(SeriesBase series) {
            this.series = series;
        }
        

        @Override
        public SeriesBase getSeries() {
            return series;
        }
        
    }
    
    
    private class RaceFragment implements IsRaceFragment{
        
        private RaceLogRaceStatus currentStatus;
        
        public RaceFragment(RaceLogRaceStatus currentStatus) {
            this.currentStatus = currentStatus;
        }
        

        @Override
        public RaceLogRaceStatus getCurrentStatus() {
            return currentStatus;
        }
        
        public void setCurrentStatus(RaceLogRaceStatus newStatus) {
            currentStatus = newStatus;
        }
        
    }
}
