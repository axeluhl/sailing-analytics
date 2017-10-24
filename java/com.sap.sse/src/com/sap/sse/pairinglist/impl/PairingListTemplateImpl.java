package com.sap.sse.pairinglist.impl;

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
        
        return standardDev;
    }

    @Override
    public PairingList<Flight, Group, Competitor> createPairingList(
            PairingFrameProvider<Flight, Group, Competitor> pPFP) {
        
        return null;
    }
    
    public int[][] getPairingListTemplate(){
        return pairingListTemplate;
    }

    private void create(int flights, int groups, int competitors){
        int[][] bestPLT = new int[groups][competitors / groups];

        double bestDev = Double.POSITIVE_INFINITY;

        for (int iteration = 0; iteration < 100000; iteration++) {
            System.out.println("Iteration: " + iteration);
            System.out.println(bestDev);
            int[][] currentAssociations = new int[competitors][competitors];
            int[][] currentPLT = new int[groups * flights][competitors / groups];

            for (int zFlight = 0; zFlight < flights; zFlight++) {

                int[][] flightColumn = new int[groups][competitors / groups];
                int[][][] associationRow = new int[groups][(competitors / groups) - 1][competitors];
                int[] associationHigh = new int[competitors / groups - 1];
                flightColumn[0][0] = randomBW(1, competitors);
                for (int zGroups = 1; zGroups <= (competitors / groups) - 1; zGroups++) {
                    int associationSum = Integer.MAX_VALUE;
                    associationHigh[0] = flights + 1;
                    System.arraycopy(currentAssociations[flightColumn[0][zGroups - 1] - 1], 0, associationRow[0][zGroups - 1], 0, competitors);

                    for (int comp = 1; comp <= competitors; comp++) {
                        if ((sum(associationRow, 0, comp - 1) <= associationSum) &&
                                !contains(flightColumn, comp) &&
                                findMaxValue(associationRow, 0, comp - 1) <= associationHigh[0]) {
                            flightColumn[0][zGroups] = comp;
                            associationSum = sum(associationRow, 0, comp - 1);
                            associationHigh[0] = findMaxValue(associationRow, 0, comp - 1);
                        }
                    }
                }
                
                for (int fleets = 1; fleets < groups - 1; fleets++) {
                    for (int aux = 0; aux < competitors; aux++) {
                        if (!contains(flightColumn, aux)) {
                            flightColumn[fleets][0] = aux;
                            break;
                        }
                    }
                    
                    for (int zGroups = 1; zGroups < (competitors / groups); zGroups++) {
                        int associationSum = Integer.MAX_VALUE;
                        associationHigh[fleets] = flights + 1;
                        System.arraycopy(currentAssociations[flightColumn[fleets][zGroups - 1] - 1], 0, associationRow[fleets][zGroups - 1], 0, competitors);

                        for (int comp = 1; comp <= competitors; comp++) {
                            if ((sum(associationRow, fleets, comp - 1) <= associationSum) &&
                                    !contains(flightColumn, comp) &&
                                    findMaxValue(associationRow, fleets, comp - 1) <= associationHigh[fleets]) {
                                flightColumn[fleets][zGroups] = comp;
                                associationSum = sum(associationRow, fleets, comp - 1);
                                associationHigh[fleets] = findMaxValue(associationRow, fleets, comp - 1);

                            }
                        }
                    }
                }
                
                for (int j = 0; j < (competitors / groups); j++) {
                    for (int z = 1; z <= competitors; z++) {
                        if (!contains(flightColumn, z)) {
                            flightColumn[groups - 1][j] = z;
                            break;
                        }
                    }
                }

                currentAssociations = this.getAssociationsFromPairingList(flightColumn, currentAssociations);
                for (int m = 0; m < groups; m++) {
                    System.arraycopy(getColumnIntArray(flightColumn, m), 0, currentPLT[(zFlight * groups) + m], 0, competitors / groups);
                }

            }
            for (int j = 0; j < currentAssociations.length; j++) {
                currentAssociations[j][j] = -1;
            }
            if (this.calcStandardDev(currentAssociations) < bestDev) {
                bestPLT = currentPLT;
                bestDev = this.calcStandardDev(currentAssociations);
            }
        }
        
        this.standardDev = bestDev;
        
        this.pairingListTemplate = bestPLT;
    }
    
    private boolean contains(int[][] flightColumn, int comp) {
        for (int i = 0; i < flightColumn.length; i++) {
            for (int z = 0; z < flightColumn[0].length; z++) {
                if (flightColumn[i][z] == comp) return true;
            }
        }
        return false;
    }

    private int sum(int[][][] associationRow, int i, int comp) {
        int sum = 0;
        for (int z = 0; z < associationRow[0].length; z++) {
            if (associationRow[i][z][comp] > -1) {
                sum += associationRow[i][z][comp];
            }
        }
        return sum;
    }
    
    private int findMaxValue(int[][][] associationRow, int i, int comp) {
        int temp = 0;
        for (int z = 0; z < associationRow[0].length; z++) {
            if (associationRow[i][z][comp] > temp) temp = associationRow[i][z][comp];
        }
        return temp;
    }

    private int randomBW(int min,int max){
        return min + (int) (Math.random() * ((max - min) + 1));
    }
    
    private int[] getColumnIntArray(int[][] src, int row) {
        int[] result = new int[src[0].length];
        for (int i = 0; i < src[0].length; i++) {
            result[i] = src[row][i];
        }
        return result;
    }
    
    
    private int[][] getAssociationsFromPairingList(int[][] pairingList, int[][] associations) {
        for (int[] group : pairingList) {
            for (int i = 0; i < pairingList[0].length; i++) {
                for (int j = 0; j < pairingList[0].length; j++) {
                    if (group[i] == group[j]) {
                        associations[group[i] - 1][group[j] - 1] = -1;
                    } else {
                        associations[group[i] - 1][group[j] - 1] =
                                associations[group[i] - 1][group[j] - 1] + 1;
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
        int[] hist = new int[(int) (getMaxValueOfArray(associations) + 1)];

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
    
    private double getMaxValueOfArray(int[] arr) {
        double maxValue = 0;
        
        for (int val: arr) {
            if (val > maxValue) {
                maxValue = val;
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


