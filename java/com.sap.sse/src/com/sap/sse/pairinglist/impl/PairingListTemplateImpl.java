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
    
    private int maxConstantFlights;
    private final int ITERATIONS= 100000;
    
    public PairingListTemplateImpl() {
         
    }

    public PairingListTemplateImpl(PairingFrameProvider pairingFrameProvider) {
        pairingListTemplate = new int[pairingFrameProvider.getGroupsCount()][pairingFrameProvider.getCompetitorsCount()/pairingFrameProvider.getGroupsCount()];
        this.createPairingListTemplate(pairingFrameProvider.getFlightsCount(), pairingFrameProvider.getGroupsCount(), pairingFrameProvider.getCompetitorsCount());
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
    
    
    /**
     * Creates a pairing list template.
     * 
     * <p>
     * The generation of pairing list templates follow two general steps:
     * </p>
     * <p>
     *     1. Because of the huge amount of generated pairing lists, we first try to create
     *        some constant flights which the other flights are based on. The constant flights
     *        are generated in a recursive way, which can be imagined as a tree structure.
     *        The nodes are represented by a competitor number, that is used as seed to generate
     *        a flight (generation of seeds: generateSeeds()). Each level in our recursive method 
     *        equates to a new constant flight. By reaching a leave of the tree, we start a task 
     *        that contains step 2. At the end of this algorithm there are 
     *        <code>Math.pow(seeds, maxConstantFlights)</code> tasks.</p><p>
     *     2. Every single task fills the pairing list by generating the rest of the flights. From 
     *        now on, every seed is a randomized competitor. The tasks perform <code>iterations/tasks</code>
     *        iterations. Every task returns its best result in a <code>Future<int[][]></code>. In the end
     *        we compare all futures and return the best result, after improving the boat assignments. </p>
     *        
     * </p>
     * 
     * @param flights count of flights
     * @param groups count of groups per flight
     * @param competitors count of total competitors
     */
    
    protected void createPairingListTemplate(int flights, int groups, int competitors){
        maxConstantFlights = (int)(flights*0.9);
        int[] seeds = this.generateSeeds(flights, competitors);
        int[][] bestPLT = new int[flights*groups][competitors/groups];
        double bestDev = Double.POSITIVE_INFINITY;
        this.createConstantFlights(flights, groups, competitors, new int[competitors][competitors],
                new int[flights*groups][competitors/groups], seeds);
        
        for(int i = 0; i < futures.size(); i++){
            try{
                int[][] currentPLT = futures.get(i).get();
                double currentStandardDev = calcStandardDev(getAssociationsFromPairingList(currentPLT,new int[competitors][competitors]));
                
                if(currentStandardDev < bestDev){
                    bestPLT=currentPLT;
                    bestDev=currentStandardDev;
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        bestPLT=this.improveAssignment(bestPLT, flights, groups, competitors);
        this.standardDev = bestDev;
        this.pairingListTemplate=bestPLT;
        //executorService.shutdown();
        futures.clear();
    }
    
    /**
     * Creates seeds for constant flights.
     * 
     * No duplicates allowed.
     * 
     * @param flights count of flights
     * @param competitors count of competitors
     * @return int array of random competitors
     */
    
    private int[] generateSeeds(int flights, int competitors) {
        int[] seeds=new int[(int)(Math.log(ITERATIONS)/Math.log(flights)*0.5)];
        for(int x=0;x<seeds.length;x++){
            int random=this.randomBW(1, competitors);
            while(this.contains(seeds, random)) {
                random=this.randomBW(1, competitors);
            }
            seeds[x]=random;
        }
        return seeds;
    }
    
    /**
     * Creates constant flights. 
     * 
     * Recursive method, called in createPairingListTemplate(). Method is described in its javadoc.
     * 
     * @param flights count of flights
     * @param groups count of groups
     * @param competitors count of competitors
     * @param associations
     * @param currentPLT
     * @param seeds
     */
    
    private void createConstantFlights(int flights, int groups, int competitors, int[][] associations,
            int[][]currentPLT, int[] seeds) {
        int fleet=Integer.MAX_VALUE;
        for(int z=0;z<currentPLT.length;z++){
            if(z<this.maxConstantFlights*groups){
                if(currentPLT[z][0]==0){
                    fleet=z;
                    break;
                }
            }else{
                //Task start
                
                class Task implements Callable<int[][]> {
                    int flights,groups,competitors;
                    int[][] plt,associations;
                    
                    /**
                     * This task is created every time a constant is generated. It generates a specific number of PairingListTemplates,which is based on the constant flights 
                     * which are given to the task and returns its best. The number of generated PairingListTamplates depends on the number of constant flights and the number of seeds.
                     * @param flights count of flights
                     * @param groups count of groups
                     * @param competitors count of competitors
                     * @param constantPLT constant generated flights on which the tasks is based on  
                     * @param associations associations created from constantPLT
                     */
                    
                    Task(int flights, int groups, int competitors, int[][] constantPLT, int[][] associations){ 
                        this.flights=flights;
                        this.groups=groups;
                        this.competitors=competitors;
                        plt=new int[flights*groups][competitors/groups];
                        this.associations=new int[competitors][competitors];
                        for(int i=0;i<constantPLT.length;i++) System.arraycopy(constantPLT[i], 0, plt[i], 0, competitors/groups);
                        for(int i=0;i<associations.length;i++) System.arraycopy(associations[i], 0, this.associations[i], 0, competitors);
                    }
                    
                    @Override
                    public int[][] call() {
                       
                        return create(flights, groups, competitors, (int)(ITERATIONS/Math.pow(seeds.length, maxConstantFlights)), 
                                plt,associations );          
                    }
                }
                
                Future<int[][]> future= executorService.submit((new Task(flights,groups,competitors,currentPLT,associations)));
                futures.add(future);
                //reset last recurrence step
                for(int i=maxConstantFlights*groups-1;i>=maxConstantFlights*groups-groups;i--){
                 Arrays.fill(currentPLT[i], 0);
                }
                associations=getAssociationsFromPairingList(currentPLT, associations);
                return;
            }
        }
        //calculate Flights for current recurrence level
        for(int x=0;x<seeds.length;x++){
            int[][] temp=this.createFlight(flights, groups, competitors, associations, seeds[x]);
            for (int m = 0; m < groups; m++) { 
                System.arraycopy(temp[m], 0, currentPLT[(fleet) + m], 0, competitors / groups);
            }
            
            associations=this.getAssociationsFromPairingList(currentPLT, new int[competitors][competitors]);
            this.createConstantFlights(flights, groups, competitors, associations, currentPLT,seeds);
        }
      //reset last recurrence step
        for(int i=fleet;i<fleet+groups;i++){
            Arrays.fill(currentPLT[i], 0);
        }
        associations=getAssociationsFromPairingList(currentPLT, associations);
    }
    
    /**
     * Generates a single flight, that depends on a specific seed. 
     * 
     * @param flights count of flights
     * @param groups count of groups in flight
     * @param competitors count of competitors
     * @param currentAssociations current matrix that describes how often a competitor competed
     *                             against another competitor. 
     * @param seed generation bases on this competitor number.
     * 
     * @return generated flight as <code>int[groups][competitors / groups]</code> array
     */
    
    protected int[][] createFlight(int flights, int groups, int competitors, int[][] currentAssociations,int seed){
        int[][] flightColumn = new int[groups][competitors / groups];
        int[][][] associationRow=new int[groups][(competitors / groups) - 1][competitors];
        int[] associationHigh = new int[groups - 1];
        flightColumn[0][0] = seed;
        for (int assignmentIndex = 1; assignmentIndex <= (competitors / groups) - 1; assignmentIndex++) {
            int associationSum = Integer.MAX_VALUE;
            associationHigh[0] = flights + 1;
            associationRow=copyInto3rdDimension(competitors, currentAssociations, associationRow, flightColumn, assignmentIndex,0);

            for (int competitorIndex = 1; competitorIndex <= competitors; competitorIndex++) {
                if ((sumOf3rdDimension(associationRow, 0, competitorIndex - 1) <= associationSum) &&
                        !contains(flightColumn, competitorIndex) &&
                        findMaxValue(associationRow, 0, competitorIndex - 1) <= associationHigh[0]) {
                    flightColumn[0][assignmentIndex] = competitorIndex;
                    associationSum = sumOf3rdDimension(associationRow, 0, competitorIndex - 1);
                    associationHigh[0] = findMaxValue(associationRow, 0, competitorIndex - 1);
                }
            }
        }
        for (int groupIndex = 1; groupIndex < groups - 1; groupIndex++) {
            for (int aux = 0; aux < competitors; aux++) {
                if (!contains(flightColumn, aux)) {
                    flightColumn[groupIndex][0] = aux;
                    break;
                }
            }
            
            for (int assignmentIndex = 1; assignmentIndex < (competitors / groups); assignmentIndex++) {
                int associationSum = Integer.MAX_VALUE;
                associationHigh[groupIndex] = flights + 1;
                associationRow=copyInto3rdDimension(competitors, currentAssociations, associationRow, flightColumn, assignmentIndex,groupIndex);

                for (int competitorIndex = 1; competitorIndex <= competitors; competitorIndex++) {
                    if ((sumOf3rdDimension(associationRow, groupIndex, competitorIndex - 1) <= associationSum) &&
                            !contains(flightColumn, competitorIndex) &&
                            findMaxValue(associationRow, groupIndex, competitorIndex - 1) <= associationHigh[groupIndex]) {
                        flightColumn[groupIndex][assignmentIndex] = competitorIndex;
                        associationSum = sumOf3rdDimension(associationRow, groupIndex, competitorIndex - 1);
                        associationHigh[groupIndex] = findMaxValue(associationRow, groupIndex, competitorIndex - 1);

                    }
                }
            }
        }
        //last Flight
        for (int assignmentIndex = 0; assignmentIndex < (competitors / groups); assignmentIndex++) {
            for (int z = 1; z <= competitors; z++) {
                if (!contains(flightColumn, z)) {
                    flightColumn[groups - 1][assignmentIndex] = z;
                    break;
                }
            }
        }
        return flightColumn;
    }
    
    /**
     * This method is called in every task to generate a number of different pairing lists 
     * to compare and choose the best. 
     * 
     * @param flights count of flights
     * @param groups count of groups in flight
     * @param competitors count of competitors
     * @param iterationCount repetitions of generating pairing lists
     * @param constantPLT constant generated flights
     * @param associations current matrix that describes how often a competitor competed
     *                      against another competitor. 
     * @return best complete pairing list out of given iterations
     */
   
    protected int[][] create(int flights, int groups, int competitors, int iterationCount,
            int[][] constantPLT, int[][] associations) {

        int[][] bestPLT = new int[groups*flights][competitors / groups];
        for (int m = 0; m < maxConstantFlights*groups; m++) {
            System.arraycopy(constantPLT[m], 0, bestPLT[m], 0, constantPLT[0].length);
        }

        int[][] currentAssociations = new int[competitors][competitors];
        double bestDev = Double.POSITIVE_INFINITY;

        for (int iteration = 0; iteration < iterationCount; iteration++) {
            for (int m = 0; m < associations.length; m++) {
                System.arraycopy(associations[m], 0, currentAssociations[m], 0, associations[0].length);
            }
            int[][] currentPLT = new int[groups * flights][competitors / groups];

            for (int flightIndex = maxConstantFlights; flightIndex < flights; flightIndex++) {
                
                int[][] flightColumn = this.createFlight(flights, groups, competitors, currentAssociations, this.randomBW(1, competitors));
                currentAssociations = this.getAssociationsFromPairingList(flightColumn, currentAssociations);
                for (int m = 0; m < groups; m++) {
                    System.arraycopy(flightColumn[m], 0, currentPLT[(flightIndex * groups) + m], 0, competitors / groups);
                }

            }
            for (int j = 0; j < currentAssociations.length; j++) {
                currentAssociations[j][j] = -1;
            }
            if (this.calcStandardDev(currentAssociations) < bestDev) {
                for(int z=maxConstantFlights*groups;z<flights*groups;z++)  bestPLT[z] = currentPLT[z];
                bestDev = this.calcStandardDev(currentAssociations);
            }
        }
        return bestPLT;
    }
    
    /**
     * Returns a matrix that describes how often the competitors competed on a boat.
     * The matrix has the following dimension: <code>assignmentAssociations[competitors][boats]</code>.
     * 
     * @param pairingList current pairing list from which the associations will be created
     * @param associations int array on which the assignment will be written
     * @return matrix that represents the assignment associations
     */

    protected int[][] getAssignmentAssociations(int[][] pairingList, int[][] associations) {
        for (int[] group : pairingList) {
            for (int i = 0; i < pairingList[0].length; i++) {
                associations[group[i] - 1][i] += 1;
            }
        }
        return associations;
    }

    /**
     * Switches competitors inside a group to improve assignments.
     * Method does not change the order of groups or flights. The standard deviation
     * of team associations will not be influenced by this method. After executing the 
     * method the assignment should be well distributed. 
     * 
     * @param pairinglist current pairing list
     * @param flights count of flights
     * @param groups count of groups
     * @param competitors count of competitors
     * @return improved pairing list template
     */
    
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
                    int[] position=this.findWorstValuePosition(groupAssignments,(int)neededAssigments);
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
//    private int[][] improveAssignmentChanges(int[][] pairingList, int flights, int competitors) {
//        int boatChanges[] = new int[competitors - 1];
//
//        for (int i = 1; i < flights; i++) {
//            int[] groupPrev = pairingList[i * pairingList.length / flights - 1];
//
//            int[] groupNext = new int[pairingList[0].length];
//            int bestMatchesIndex = -1;
//            int bestMatch = 0;
//            for (int j = 0; j < pairingList.length / flights; j++) {
//                System.arraycopy(pairingList[i * pairingList.length / flights + j], 0, groupNext, 0, groupNext.length);
//                int currentMatch = this.getMatches(groupPrev, groupNext);
//                if (currentMatch > bestMatch) {
//                    bestMatch = currentMatch;
//                    bestMatchesIndex = j;
//                }
//            }
//
//            if (bestMatchesIndex > 0) {
//                int[] temp = pairingList[i * pairingList.length / flights];
//                //System.arraycopy(pairingList[i * pairingList.length / flights], 0, temp, 0, temp.length);
//
//                System.arraycopy(pairingList[i * pairingList.length / flights], 0,
//                        pairingList[i * pairingList.length / flights + bestMatchesIndex], 0, groupNext.length);
//                System.arraycopy(temp, 0, pairingList[i * pairingList.length / flights + bestMatchesIndex], 0, temp.length);
//            }
//
//            boatChanges[i - 1] = groupNext.length - bestMatch;
//        }
//
//        //System.out.println(Arrays.toString(boatChanges));
//
//        return pairingList;
//    }
//
//    private int getMatches(int[] arr1, int[] arr2) {
//        int matches = 0;
//
//        for (int value: arr1) {
//            if (this.contains(arr2, value)) {
//                matches++;
//            }
//        }
//
//        return matches;
//    }
    
    /**
     * Returns an index that has the greatest difference to neededAssignments
     * 
     * @param groupAssignments array that contains the values
     * @param neededAssigments reference value
     * @return int array with 2 indices that represent row and column of the worst value
     */

    private int[] findWorstValuePosition(int[][] groupAssignments, int neededAssigments) {
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
    

    private boolean contains(int[][] arr, int value) {
        for (int i = 0; i < arr.length; i++) {
            for (int z = 0; z < arr[0].length; z++) {
                if (arr[i][z] == value) return true;
            }
        }
        return false;
    }
    
    private boolean contains(int[] arr, int value) {
        for (int i = 0; i < arr.length; i++) {
                if (arr[i] == value) return true;
        }
        return false;
    }

    /**
     * Calculates the sum of a specific row in the 3rd dimension
     * 
     * @param arr 3 dimensional array
     * @param depth 3rd dimension of array
     * @return sum
     */
    
    private int sumOf3rdDimension(int[][][] arr, int row, int depth) {
        int sum = 0;
        for (int z = 0; z < arr[0].length; z++) {
            if (arr[row][z][depth] > -1) {
                sum += arr[row][z][depth];
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
    
    /**
     * Returns a random number between min and max
     * 
     * @param min
     * @param max
     * @return a random number
     */

    private int randomBW(int min,int max){
        return min + (int) (Math.random() * ((max - min) + 1));
    }

    /**
     * Creates a matrix that describes how often the team competes against each other.
     * Its dimensions are <code>associations[competitors][competitors]</code>, e.g.:
     * 
     *   1 2 3 4
     * 1 \ 2 3 1
     * 2 2 \ 1 3
     * 3 3 1 \ 2
     * 4 1 3 2 \
     * 
     * In this example, team 2 competed 3 times against team 4.
     * 
     * @param pairingList current pairing list template
     * @param associations int array in which the association will be written
     * @return int arryay of associations
     */
    
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
     * Calculates the standard deviation from all numbers in association matrix.
     * The method ignores all numbers smaller 0, because senseless values are marked 
     * with -1.
     * The return value is used to compare the quality of different pairing list templates.
     * 
     * @param associations association describes a 2 dimensional array of integers, which contains the information 
     *                      about how often the teams play against each other
     * @return standardDev returns how much the association values deviate from each other
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
}