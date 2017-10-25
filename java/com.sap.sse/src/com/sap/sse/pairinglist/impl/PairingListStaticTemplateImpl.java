package com.sap.sse.pairinglist.impl;

import java.util.Arrays;

public class PairingListStaticTemplateImpl {

    public static void main(String[] args) {
        create(15, 3, 18);
    }
    
    private static void create(int flights, int groups, int competitors){
        int[][] bestPLT = new int[groups][competitors / groups];

        double bestDev = Double.POSITIVE_INFINITY;

        for (int iteration = 0; iteration < 10000; iteration++) {
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

                currentAssociations = getAssociationsFromPairingList(flightColumn, currentAssociations);
                for (int m = 0; m < groups; m++) {
                    System.arraycopy(getColumnIntArray(flightColumn, m), 0, currentPLT[(zFlight * groups) + m], 0, competitors / groups);
                }

            }
            for (int j = 0; j < currentAssociations.length; j++) {
                currentAssociations[j][j] = -1;
            }
            if (calcStandardDev(currentAssociations) < bestDev) {
                bestPLT = currentPLT;
                bestDev = calcStandardDev(currentAssociations);
            }
        }

        for(int[] group : bestPLT) {
            shuffle(group);
            System.out.println(Arrays.toString(group));
        }
        System.out.println(bestDev);
    }
    
    private static boolean contains(int[][] flightColumn, int comp) {
        for (int i = 0; i < flightColumn.length; i++) {
            for (int z = 0; z < flightColumn[0].length; z++) {
                if (flightColumn[i][z] == comp) return true;
            }
        }
        return false;
    }

    private static int sum(int[][][] associationRow, int i, int comp) {
        int sum = 0;
        for (int z = 0; z < associationRow[0].length; z++) {
            if (associationRow[i][z][comp] > -1) {
                sum += associationRow[i][z][comp];
            }
        }
        return sum;
    }
    
    private static int findMaxValue(int[][][] associationRow, int i, int comp) {
        int temp = 0;
        for (int z = 0; z < associationRow[0].length; z++) {
            if (associationRow[i][z][comp] > temp) temp = associationRow[i][z][comp];
        }
        return temp;
    }

    private static int randomBW(int min,int max){
        return min + (int) (Math.random() * ((max - min) + 1));
    }
    
    private static int[] shuffle(int[] src) {
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
    
    private static int[] getColumnIntArray(int[][] src, int row) {
        int[] result = new int[src[0].length];
        for (int i = 0; i < src[0].length; i++) {
            result[i] = src[row][i];
        }
        return result;
    }
    
    
    private static int[][] getAssociationsFromPairingList(int[][] pairingList, int[][] associations) {
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
    
    private static double calcStandardDev(int[][] associations) {

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
    
    private static double getMaxValueOfArray(int[] arr) {
        double maxValue = 0;
        
        for (int val: arr) {
            if (val > maxValue) {
                maxValue = val;
            }
        }
        
        return maxValue;
    }
    
    private static double getMaxValueOfArray(int[][] arr) {
        double maxValue = 0;
        
        for (int[] val: arr) {
            if (getMaxValueOfArray(val) > maxValue) {
                maxValue = getMaxValueOfArray(val);
            }
        }
        
        return maxValue;
    }

    
}
