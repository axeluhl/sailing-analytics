package com.sap.sse.pairinglist.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

import com.sap.sse.pairinglist.PairingFrameProvider;
import com.sap.sse.pairinglist.PairingList;
import com.sap.sse.pairinglist.PairingListTemplate;

/**
 * @author D070307
 *
 * @param <Flight>
 * @param <Group>
 * @param <Competitor>
 */
public class PairingListTemplateImpl<Flight,Group,Competitor> implements PairingListTemplate<Flight,Group,Competitor> {
    
    private int[][] pairingListTemplate;
    private double standardDev;
    
    public PairingListTemplateImpl(PairingFrameProvider<Flight,Group,Competitor> pPFP) {
        pairingListTemplate= new int[pPFP.getGroupsCount()][pPFP.getCompetitorCount()/pPFP.getGroupsCount()];
        this.create(pPFP.getFlightsCount(), pPFP.getGroupsCount(), pPFP.getCompetitorCount() );
    }


    @Override
    public double getQualitiy() {
        
        return 0;
    }

    @Override
    public PairingList<Flight, Group, Competitor> createPairingList(
            PairingFrameProvider<Flight, Group, Competitor> pPFP) {
 
        return null;
    }
    
    private void create(int flights, int groups, int competitors){
        int[][] bestTeamAssociations = new int[competitors][competitors];
        int[][] bestPLT = new int[groups][competitors / groups];
        
        double bestDev = Double.POSITIVE_INFINITY;
        
        for(int iteration=0;iteration<100;iteration++){
            
            int[][] currentAssociations = new int[competitors][competitors];
            int[][] currentPLT = new int[groups][competitors / groups];
            
            for(int zFlight=0;zFlight<flights;zFlight++){
                
                int [][] flightColumn=new int[groups][competitors/groups];
                int [][][] associationRow = new int[groups-1][(competitors/groups)-1][competitors];
                int [] associationHigh= new int[groups-1];
                flightColumn[0][0]=this.randomBW(1,competitors);
                    for(int zGroups=1;zGroups<(competitors/groups)-1;zGroups++){
                        int associationSum = Integer.MAX_VALUE;
                        associationHigh[0]=flights+1;
                        System.arraycopy(currentAssociations[flightColumn[0][0]], 0, associationRow[1][zGroups], 0, competitors);
                            for(int comp=0;comp<competitors;comp++){
                                if((this.sum(associationRow,0,comp)<=associationSum) && 
                                      !this.contains(flightColumn,comp) &&
                                           this.findMaxValue(associationRow, 0, comp)<=associationHigh[0] ){
                                    flightColumn[0][zGroups]=comp;
                                    associationSum=this.sum(associationRow,0,comp);
                                    associationHigh[0]=this.findMaxValue(associationRow, 0, comp);
                                }
                            }   
                    }
                   for(int fleets=1;fleets<(competitors/groups)-1;fleets++){
                     for(int aux=0;aux<competitors;aux++){
                         if(! this.contains(flightColumn, aux)){
                             flightColumn[fleets][0]=aux;
                             break;
                         }
                     }
                     for(int zGroups=1;zGroups<(competitors/groups)-1;zGroups++){
                         int associationSum = Integer.MAX_VALUE;
                         associationHigh[0]=flights+1;
                         System.arraycopy(currentAssociations[flightColumn[0][0]], 0, associationRow[1][zGroups], 0, competitors);
                             for(int comp=0;comp<competitors;comp++){
                                 if((this.sum(associationRow,fleets,comp)<=associationSum) && 
                                       !this.contains(flightColumn,comp) &&
                                            this.findMaxValue(associationRow, fleets, comp)<=associationHigh[0] ){
                                     flightColumn[0][zGroups]=comp;
                                     associationSum=this.sum(associationRow,fleets,comp);
                                     associationHigh[0]=this.findMaxValue(associationRow, fleets, comp);
                                 }
                             }   
                     }
                   }
                   for(int j=0;j<(competitors/groups);j++){
                       for(int z=0;z<competitors;z++){
                           if(!this.contains(flightColumn, z)){
                               flightColumn[groups][j]=z;
                               break;
                           }
                       }
                   }
                   currentAssociations=this.match(currentAssociations,flightColumn);
                   for(int m=0;m<groups;m++){
                      System.arraycopy(flightColumn[m],0 , currentPLT[(zFlight*groups)+m], 0, competitors/groups);
                   }
                   
            }
            for(int j=0;j<currentAssociations.length;j++){
                currentAssociations[j][j]=-1;
            }
            if(this.calcStandardDev(currentAssociations)<bestDev){
                bestPLT=currentPLT;
                bestTeamAssociations=currentAssociations;
                bestDev=this.calcStandardDev(currentAssociations);
            }
        }
        
    }
    



    private boolean contains(int[][] flightColumn, int comp) {
        for(int i=0;i<flightColumn[0].length;i++){
            for(int z=0;z<flightColumn.length;z++){
                if(flightColumn[z][i]==comp) return true;
            }
        }
        return false;
    }


    private int sum(int[][][] associationRow, int i, int comp) {
        int[] temp=new int[associationRow[0].length];
           for(int z=0;z<associationRow[0].length;z++){
               temp[z]=associationRow[i][z][comp];
           }
           return Arrays.stream(temp).sum();
    }
    private int findMaxValue(int[][][] associationRow, int i, int comp) {
        int temp=0;
           for(int z=0;z<associationRow[0].length;z++){
               if(associationRow[i][z][comp]>temp) temp=associationRow[i][z][comp];
              }
           return temp;
    }

    private int randomBW(int min,int max){
        return min+(int)(Math.random()*((max-min)+1));
    }
    
    
    private int[][] getAssociationsFromFlight(int[][] pairingList, int competitors) {
        int[][] associations = new int[competitors][competitors];
        
        for (int group = 0; group < pairingList.length; group++) {
            for (int i = 0; i < pairingList[0].length; i++) {
                for (int j = 0; j < pairingList[0].length; j++) {
                    if (pairingList[group][i] == pairingList[group][j]) {
                        associations[pairingList[group][i] - 1][pairingList[group][j] - 1] = -1;
                    } else {
                        associations[pairingList[group][i] - 1][pairingList[group][j] - 1] =
                                associations[pairingList[group][i] - 1][pairingList[group][j] - 1] + 1;
                    }

                }
            }
        }
        
        return associations;
    }
    
    /**
     * @param associations: association describes a 2 dimensional array of integers, which contains the information 
     *                      about how often the teams play against each other
     * @return standardDev: returns how much the association values deviate from each other
     */
    
    private double calcStandardDev(int[][] associations) {

        double standardDev = 0;
        double expectedValue = 0;
        double valueCount = associations.length * associations[0].length;

        /*
         * hist shows how often a specific value of one association occurs.
         * A value specifies how often one team plays against another.
         */
        int[] hist = new int[(int) (this.getMaxValueOfArray(associations) + 1)];

        // filling hist
        for (int[] key : associations) {
            for (int value : key) {
                if (value >= 0) {
                    hist[value] = hist[value] + 1;
                }
            }
        }

        // calculating the expected value of hist
        for (int i = 0; i < hist.length; i++) {
            expectedValue += i * (hist[i] / valueCount);
        }

        // calculating standard deviation by all values and expectedValue
        for (int[] key : associations) {
            for (int value : key) {
                if (value >= 0) {
                    standardDev += Math.pow(value - expectedValue, 2);
                }
            }
        }

        if (standardDev > 0) {
            standardDev = Math.sqrt(standardDev / valueCount);
        }
        
        return standardDev;
    }
    
    private double getMaxValueOfArray(double[] arr) {
        double maxValue = 0;
        
        for (double val: arr) {
            if (val > maxValue) {
                maxValue = val;
            }
        }
        
        return maxValue;
    }
    
    private double getMaxValueOfArray(int[] arr) {
        double maxValue = 0;
        
        for (int val: arr) {
            if (val > maxValue) {
                maxValue = val;
            }
        }
        
        return maxValue;
    }
    
    private double getMaxValueOfArray(double[][] arr) {
        double maxValue = 0;
        
        for (double[] val: arr) {
            if (this.getMaxValueOfArray(val) > maxValue) {
                maxValue = this.getMaxValueOfArray(val);
            }
        }
        
        return maxValue;
    }
    
    private double getMaxValueOfArray(int[][] arr) {
        double maxValue = 0;
        
        for (int[] val: arr) {
            if (this.getMaxValueOfArray(val) > maxValue) {
                maxValue = this.getMaxValueOfArray(val);
            }
        }
        
        return maxValue;
    }
}

