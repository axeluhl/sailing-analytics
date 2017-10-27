package com.sap.sse.pairinglist.impl;

import com.sap.sse.pairinglist.CompetitionFormat;
import com.sap.sse.pairinglist.PairingFrameProvider;
import com.sap.sse.pairinglist.PairingList;
import com.sap.sse.pairinglist.PairingListTemplate;

public class PairingListTemplateImpl implements PairingListTemplate{
    
    private int[][] pairingListTemplate;
    private double standardDev;

    public PairingListTemplateImpl() {

    }

    public PairingListTemplateImpl(PairingFrameProvider pairingFrameProvider) {
        pairingListTemplate = new int[pairingFrameProvider.getGroupsCount()][pairingFrameProvider.getCompetitorsCount()/pairingFrameProvider.getGroupsCount()];
        this.create(pairingFrameProvider.getFlightsCount(), pairingFrameProvider.getGroupsCount(), pairingFrameProvider.getCompetitorsCount());
    }
    
    public PairingListTemplateImpl(PairingFrameProvider pairingFrameProvider, int iterations) {
        pairingListTemplate = new int[pairingFrameProvider.getGroupsCount()][pairingFrameProvider.getCompetitorsCount()/pairingFrameProvider.getGroupsCount()];
        this.create(pairingFrameProvider.getFlightsCount(), pairingFrameProvider.getGroupsCount(), pairingFrameProvider.getCompetitorsCount(), iterations);
    }


    @Override
    public double getQuality() {
        return standardDev;
    }

    @Override
    public <Flight, Group, Competitor> PairingList<Flight, Group, Competitor> createPairingList(
            CompetitionFormat<Flight, Group, Competitor> competitionFormat) {
        return null;
    }

    @Override
    public int[][] getPairingListTemplate(){
        return pairingListTemplate;
    }

    protected int[][] create(int flights, int groups, int competitors) {
        return this.create(flights, groups, competitors, 1000000);
    }

    protected int[][] create(int flights, int groups, int competitors, int iterationCount) {
        int[][] bestPLT = new int[groups][competitors / groups];

        double bestDev = Double.POSITIVE_INFINITY;

        for (int iteration = 0; iteration < iterationCount; iteration++) {
            int[][] currentAssociations = new int[competitors][competitors];
            int[][] currentPLT = new int[groups * flights][competitors / groups];
            int[][][] associationRow = new int[groups][(competitors / groups) - 1][competitors];

            for (int zFlight = 0; zFlight < flights; zFlight++) {

                int[][] flightColumn = new int[groups][competitors / groups];
                associationRow= setZero(associationRow);
                int[] associationHigh = new int[competitors / groups - 1];
                flightColumn[0][0] = randomBW(1, competitors);
                for (int zGroups = 1; zGroups <= (competitors / groups) - 1; zGroups++) {
                    int associationSum = Integer.MAX_VALUE;
                    associationHigh[0] = flights + 1;
                    associationRow=copyInto3rdDimension(competitors, currentAssociations, associationRow, flightColumn, zGroups,0);

                    for (int comp = 1; comp <= competitors; comp++) {
                        if ((sumOf3rdDimension(associationRow, 0, comp - 1) <= associationSum) &&
                                !contains(flightColumn, comp) &&
                                findMaxValue(associationRow, 0, comp - 1) <= associationHigh[0]) {
                            flightColumn[0][zGroups] = comp;
                            associationSum = sumOf3rdDimension(associationRow, 0, comp - 1);
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
                        associationRow=copyInto3rdDimension(competitors, currentAssociations, associationRow, flightColumn, zGroups,fleets);

                        for (int comp = 1; comp <= competitors; comp++) {
                            if ((sumOf3rdDimension(associationRow, fleets, comp - 1) <= associationSum) &&
                                    !contains(flightColumn, comp) &&
                                    findMaxValue(associationRow, fleets, comp - 1) <= associationHigh[fleets]) {
                                flightColumn[fleets][zGroups] = comp;
                                associationSum = sumOf3rdDimension(associationRow, fleets, comp - 1);
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

        for(int[] group : bestPLT) {
            shuffle(group);
        }
        
        System.out.println(this.calcStandardDev(this.getAssignmentAssociations(bestPLT, new int[competitors][competitors / groups])));
        
        this.standardDev = bestDev;
        this.pairingListTemplate=bestPLT;
        return bestPLT;
    }
    
    protected int[][] improveAssignments(int[][] pairingList, int groups, int competitors, int flights) {
        return null;
    }
    
    private int[][] getAssignmentAssociations(int[][] pairingList, int[][] associations) {
        for (int[] group : pairingList) {
            for (int i = 0; i < pairingList[0].length; i++) {
                associations[group[i] - 1][i] += 1;
            }
        }
        
        return associations;
    }


    public int[][][] copyInto3rdDimension(int competitors, int[][] currentAssociations, int[][][] associationRow,
            int[][] flightColumn, int zGroups,int fleet) {
        System.arraycopy(currentAssociations[flightColumn[fleet][zGroups - 1] -1], 0, associationRow[fleet][zGroups -1], 0, competitors);
        return associationRow;
    }

    private boolean contains(int[][] flightColumn, int comp) {
        for (int i = 0; i < flightColumn.length; i++) {
            for (int z = 0; z < flightColumn[0].length; z++) {
                if (flightColumn[i][z] == comp) return true;
            }
        }
        return false;
    }
    private int[][][] setZero(int[][][] temp){
        for(int x=0;x<temp.length;x++){
            for(int y=0;y<temp[0].length;y++){
                for(int z=0;z<temp[0][0].length;z++) temp[x][y][z]=0;
            }
        }
        return temp;
    }

    private int sumOf3rdDimension(int[][][] associationRow, int i, int comp) {
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

    private int[] shuffle(int[] src) {
        int[] result = src;
        for(int i = 0; i<(result.length*2); i++) {
            int o = randomBW(0, result.length-1);
            int n = randomBW(0, randomBW(0, src.length-1));

            int temp = result[o];
            result[o] = result[n];
            result[n] = temp;
        }
        return result;
    }

    private int[] getColumnIntArray(int[][] src, int row) {
        int[] result = new int[src[0].length];
        for (int i = 0; i < src[0].length; i++) {
            result[i] = src[row][i];
        }
        return result;
    }


    public int[][] getAssociationsFromPairingList(int[][] pairingList, int[][] associations) {
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

    protected double calcStandardDev(int[][] associations) {

        double standardDev = 0;
        
        int k = associations[0][0],     // first value of association array
            n = 0,                      // count of elements in association array
            exp = 0,                    //
            exp2 = 0;                   //

        for (int i = 0; i < associations.length; i++) {
            for (int j = 0; j < associations[0].length; j++) {
                if (associations[i][j] < 0) {
                    continue;
                }

                n += 1;
                exp += associations[i][j] - k;
                exp2 += Math.pow(associations[i][j] - k, 2);
            }
        }

        // expression in Math.sqrt() is equal to variance / n
        standardDev = Math.sqrt((exp2 - (Math.pow(exp, 2)) / n) / n);
        
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


