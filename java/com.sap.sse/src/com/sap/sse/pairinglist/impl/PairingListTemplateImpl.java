package com.sap.sse.pairinglist.impl;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.sap.sse.pairinglist.CompetitionFormat;
import com.sap.sse.pairinglist.PairingFrameProvider;
import com.sap.sse.pairinglist.PairingList;
import com.sap.sse.pairinglist.PairingListTemplate;
import com.sap.sse.util.ThreadPoolUtil;

public class PairingListTemplateImpl implements PairingListTemplate{

    private int[][] pairingListTemplate;
    private double standardDev;
    private ExecutorService executorService= ThreadPoolUtil.INSTANCE.getDefaultBackgroundTaskThreadPoolExecutor();
    ArrayList<Future<int[][]>> futures= new ArrayList<Future<int[][]>>();
    
    private final int MAX_CONSTANT_FLIGHTS = 3;

    public PairingListTemplateImpl() {
         
    }

    public PairingListTemplateImpl(PairingFrameProvider pairingFrameProvider) {
        pairingListTemplate = new int[pairingFrameProvider.getGroupsCount()][pairingFrameProvider.getCompetitorsCount()/pairingFrameProvider.getGroupsCount()];
        //this.create(pairingFrameProvider.getFlightsCount(), pairingFrameProvider.getGroupsCount(), pairingFrameProvider.getCompetitorsCount());
    }

    public PairingListTemplateImpl(PairingFrameProvider pairingFrameProvider, int iterations) {
        pairingListTemplate = new int[pairingFrameProvider.getGroupsCount()][pairingFrameProvider.getCompetitorsCount()/pairingFrameProvider.getGroupsCount()];
       // this.create(pairingFrameProvider.getFlightsCount(), pairingFrameProvider.getGroupsCount(), pairingFrameProvider.getCompetitorsCount(), iterations);

        
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
    
    protected void createPairingListTemplate(int flights,int groups,int competitors){
        int[][] currentPLT=new int[flights*groups][competitors/groups];
        int[] seeds= {1,18,2,17,3};
        this.createPairingListTemplate_(flights, groups, competitors, new int[competitors][competitors],currentPLT,seeds);
        double dev=Double.POSITIVE_INFINITY;
        for(int z=0;z<futures.size();z++){
            try{
                System.out.println(calcStandardDev(getAssociationsFromPairingList(futures.get(z).get(),new int[competitors][competitors])));
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
//        System.out.println(dev);
        executorService.shutdown();
    }
    
    
    private int[] generateSeeds(int flights, int competitors) {
        int[] seeds=new int[(int)(Math.log(1000000)/Math.log(flights))];
        for(int x=0;x<seeds.length;x++){
            int random=this.randomBW(1, competitors);
            while(this.contains(seeds, random)) random=this.randomBW(1, competitors);
            seeds[x]=random;
        }
        return seeds;
    }
//    private void createPairingListTemplateNOSEEDS(int flights,int groups,int competitors,int[][] associations,int[][]currentPLT) {
//        for(int x=0;x<10;x++){
//        int[][] currentAssociations=new int [competitors][competitors];
//        class Task implements Callable<int[][]> {
//            int flights,groups,competitors;
//            int[][] plt,associations;
//            Task(int flights,int groups,int competitors,int[][] constantplt,int[][] associations){ 
//                this.flights=flights;
//                this.groups=groups;
//                this.competitors=competitors;
//                plt=new int[flights*groups][competitors/groups];
//                this.associations=new int[competitors][competitors];
//                for(int i=0;i<constantplt.length;i++) System.arraycopy(constantplt[i], 0, plt[i], 0, competitors/groups);
//                for(int i=0;i<associations.length;i++) System.arraycopy(associations[i], 0, this.associations[i], 0, competitors);
//                }
//            @Override
//            public int[][] call() {
//                return create(flights, groups, competitors, 10000);          
//            }
//        }
//        Future<int[][]> future= executorService.submit((new Task(flights,groups,competitors,currentPLT,currentAssociations)));
//        futures.add(future);
//        }
//    }
    private void createPairingListTemplate_(int flights,int groups,int competitors,int[][] associations,int[][]currentPLT,int[] seeds){
        int[][] currentAssociations=new int [competitors][competitors];
        for(int m=0;m<competitors;m++){
            System.arraycopy(getColumnIntArray(associations, m), 0, currentAssociations[m], 0, competitors);
        }
        int fleet=Integer.MAX_VALUE;
        for(int z=0;z<currentPLT.length;z++){
            if(z<this.MAX_CONSTANT_FLIGHTS*groups){
                if(currentPLT[z][0]==0){
                    fleet=z;
                    break;
                }
            }else{
                //Task start
                
                class Task implements Callable<int[][]> {
                    int flights,groups,competitors;
                    int[][] plt,associations;
                    Task(int flights,int groups,int competitors,int[][] constantplt,int[][] associations){ 
                        this.flights=flights;
                        this.groups=groups;
                        this.competitors=competitors;
                        plt=new int[flights*groups][competitors/groups];
                        this.associations=new int[competitors][competitors];
                        for(int i=0;i<constantplt.length;i++) System.arraycopy(constantplt[i], 0, plt[i], 0, competitors/groups);
                        for(int i=0;i<associations.length;i++) System.arraycopy(associations[i], 0, this.associations[i], 0, competitors);
                        }
                    @Override
                    public int[][] call() {
                        return create(flights, groups, competitors, 4600, associations, plt );          
                    }
                }
                Future<int[][]> future= executorService.submit((new Task(flights,groups,competitors,currentPLT,currentAssociations)));
                futures.add(future);
               
                
                
                Arrays.fill(currentPLT[z], 0);
                Arrays.fill(currentPLT[z+1], 0);
                Arrays.fill(currentPLT[z+2], 0);
                return;
                }
        }
        for(int x=0;x<seeds.length;x++){
            int[][] temp=this.createFlight(flights, groups, competitors, currentAssociations, seeds[x]);
            for (int m = 0; m < groups; m++) {
                System.arraycopy(getColumnIntArray(temp, m), 0, currentPLT[(fleet) + m], 0, competitors / groups);
            }
            
            currentAssociations=this.getAssociationsFromPairingList(currentPLT, currentAssociations);
            this.createPairingListTemplate_(flights, groups, competitors, currentAssociations, currentPLT,seeds);
        }
        for(int i=fleet;i<fleet+groups;i++){
            Arrays.fill(currentPLT[i], 0);
        }
    }
    
    protected int[][] createFlight(int flights, int groups, int competitors, int[][] currentAssociations,int seed){
        int[][] flightColumn = new int[groups][competitors / groups];
        int[][][] associationRow=new int[groups][(competitors / groups) - 1][competitors];
        int[] associationHigh = new int[competitors / groups - 1];
        flightColumn[0][0] = seed;
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
        //last Flight
        for (int j = 0; j < (competitors / groups); j++) {
            for (int z = 1; z <= competitors; z++) {
                if (!contains(flightColumn, z)) {
                    flightColumn[groups - 1][j] = z;
                    break;
                }
            }
        }
        return flightColumn;
    }
    
   
    protected int[][] create(int flights, int groups, int competitors, int iterationCount,int[][] associations,int[][] constantPLT) {
        int[][] bestPLT = new int[groups*flights][competitors / groups];
        for (int m = 0; m < MAX_CONSTANT_FLIGHTS*groups; m++) {
            System.arraycopy(constantPLT[m], 0, bestPLT[m], 0, constantPLT[0].length);
        }
        int[][] currentAssociations = new int[competitors][competitors];
        for (int m = 0; m < associations.length; m++) {
            System.arraycopy(associations[m], 0, currentAssociations[m], 0, associations[0].length);
        }
        double bestDev = Double.POSITIVE_INFINITY;

        for (int iteration = 0; iteration < iterationCount; iteration++) {
            for (int m = 0; m < associations.length; m++) {
                System.arraycopy(associations[m], 0, currentAssociations[m], 0, associations[0].length);
            }
            int[][] currentPLT = new int[groups * flights][competitors / groups];
            int[][][] associationRow = new int[groups][(competitors / groups) - 1][competitors];

            for (int zFlight = MAX_CONSTANT_FLIGHTS; zFlight < flights; zFlight++) {
                //first Flight
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
                
                //second to pre-last Flight
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
                //last Flight
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
                for(int z=MAX_CONSTANT_FLIGHTS*groups;z<flights*groups;z++)  bestPLT[z] = currentPLT[z];
                bestDev = this.calcStandardDev(currentAssociations);
            }
        }

        for(int[] group : bestPLT) {
            shuffle(group);
        }

        //bestPLT=this.improveAssignment(bestPLT, flights, groups, competitors);
        //bestPLT = this.improveAssignmentChanges(bestPLT, flights, competitors);
        this.standardDev = bestDev;
        this.pairingListTemplate=bestPLT;
        return bestPLT;
    }



    protected int[][] getAssignmentAssociations(int[][] pairingList, int[][] associations) {
        for (int[] group : pairingList) {
            for (int i = 0; i < pairingList[0].length; i++) {
                associations[group[i] - 1][i] += 1;
            }
        }

        return associations;
    }

    protected int[][] improveAssignment(int[][] pairinglist,int flights, int groups, int competitors){
        int[][] assignments= this.getAssignmentAssociations(pairinglist, new int[competitors][competitors/groups]);
        double neededAssigments= flights/(competitors/groups);
        double bestDev=Double.POSITIVE_INFINITY;
        int[][] bestPLT=new int[flights*groups][competitors/groups];
        for(int iteration=0;iteration<10;iteration++){
            for(int zGroup=0;zGroup<assignments.length;zGroup++){
                int[][] groupAssignments=new int[competitors/groups][competitors/groups];
                for(int zPlace=0;zPlace<(competitors/groups);zPlace++){
                    System.arraycopy(assignments[pairinglist[zGroup][zPlace]-1], 0, groupAssignments[zPlace], 0, (competitors/groups));
                }
                for(int zPlace=0;zPlace<competitors*50;zPlace++){
                    int[] position=this.findWorstValue(groupAssignments,(int)neededAssigments);
                    if(groupAssignments[position[0]][position[1]]>neededAssigments-1&&groupAssignments[position[0]][position[1]]<neededAssigments+1){
                        break;
                    }else if(groupAssignments[position[0]][position[1]]<neededAssigments){
                        int temp=0;
                        temp=pairinglist[zGroup][position[1]];
                        pairinglist[zGroup][position[1]]=pairinglist[zGroup][position[0]];
                        pairinglist[zGroup][position[0]]=temp;
                        assignments=this.getAssignmentAssociations(pairinglist, new int[competitors][competitors/groups]);
                        for(int x=0;x<(competitors/groups);x++){
                            System.arraycopy(assignments[pairinglist[zGroup][x]-1], 0, groupAssignments[x], 0, (competitors/groups));
                        }
                    }else{
                        if(position[0]==position[1]){
                            int temp=0;
                            temp=pairinglist[zGroup][this.findMinValuePosition(groupAssignments[position[0]])];
                            pairinglist[zGroup][this.findMinValuePosition(groupAssignments[position[0]])]=pairinglist[zGroup][position[0]];
                            pairinglist[zGroup][position[0]]=temp;
                            assignments=this.getAssignmentAssociations(pairinglist, new int[competitors][competitors/groups]);
                            for(int x=0;x<(competitors/groups);x++){
                                System.arraycopy(assignments[pairinglist[zGroup][x]-1], 0, groupAssignments[x], 0, (competitors/groups));
                            }
                        }else{
                            groupAssignments[position[0]][position[1]]=-1;
                        }
                    }
                }
                if(this.calcStandardDev(pairinglist)<bestDev){
                    bestDev=this.calcStandardDev(pairinglist);
                    bestPLT=pairinglist;

                }
            }
        }
        return bestPLT;
    }
    
    private int[][] improveAssignmentChanges(int[][] pairingList, int flights, int competitors) {
        int boatChanges[] = new int[competitors - 1];
        
        for (int i = 1; i < flights; i++) {
            int[] groupPrev = pairingList[i * pairingList.length / flights - 1];
            
            int[] groupNext = new int[pairingList[0].length];
            int bestMatchesIndex = -1;
            int bestMatch = 0;
            for (int j = 0; j < pairingList.length / flights; j++) {
                System.arraycopy(pairingList[i * pairingList.length / flights + j], 0, groupNext, 0, groupNext.length);
                int currentMatch = this.getMatches(groupPrev, groupNext);
                if (currentMatch > bestMatch) {
                    bestMatch = currentMatch;
                    bestMatchesIndex = j;
                }
            }
            
            if (bestMatchesIndex > 0) {
                int[] temp = new int[groupNext.length];
                System.arraycopy(pairingList[i * pairingList.length / flights], 0, temp, 0, temp.length);
                
                System.arraycopy(pairingList[i * pairingList.length / flights], 0,
                        pairingList[i * pairingList.length / flights + bestMatchesIndex], 0, groupNext.length);
                System.arraycopy(temp, 0, pairingList[i * pairingList.length / flights + bestMatchesIndex], 0, temp.length);
            }
            
            boatChanges[i - 1] = groupNext.length - bestMatch;
        }
        
        //System.out.println(Arrays.toString(boatChanges));
        
        return pairingList;
    }
    
    private int getMatches(int[] arr1, int[] arr2) {
        int matches = 0;
        
        for (int value: arr1) {
            if (this.contains(arr2, value)) {
                matches++;
            }
        }
        
        return matches;
    }
    
    private int[] findWorstValue(int[][] groupAssignments, int neededAssigments) {
        int[] worstValuePos=new int[2];
        int worstValue=0;
        for(int i=0;i<groupAssignments.length;i++){
            for(int z=0;z<groupAssignments[0].length;z++){
                if(groupAssignments[i][z]>=0){
                    if(Math.abs(groupAssignments[i][z]-neededAssigments)>worstValue){
                        worstValuePos[0]=i;
                        worstValuePos[1]=z;
                        worstValue=Math.abs(groupAssignments[i][z]-neededAssigments);
                    }
                }
            }
        }
        return worstValuePos;
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
    private boolean contains(int[] flightColumn, int comp) {
        for (int i = 0; i < flightColumn.length; i++) {
                if (flightColumn[i] == comp) return true;
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
    private int findMinValuePosition(int[] arr){
        int temp=Integer.MAX_VALUE;
        int position=-1;
        for (int z = 0; z < arr.length; z++) {
            if (arr[z]< temp){
                position=z ;
                temp=arr[z];
            }
        }
        return position;
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
                        if(group[i]>0 && group[j]>0){
                        associations[group[i] - 1][group[j] - 1] = -1;
                        }
                    } else {
                        if(group[i]>0 && group[j]>0){
                        associations[group[i] - 1][group[j] - 1] =
                                associations[group[i] - 1][group[j] - 1] + 1;
                        }
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



