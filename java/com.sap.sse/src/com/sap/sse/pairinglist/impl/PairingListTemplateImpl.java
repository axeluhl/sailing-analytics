package com.sap.sse.pairinglist.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.sap.sse.pairinglist.CompetitionFormat;
import com.sap.sse.pairinglist.PairingFrameProvider;
import com.sap.sse.pairinglist.PairingList;
import com.sap.sse.pairinglist.PairingListTemplate;
import com.sap.sse.util.ThreadPoolUtil;

public class PairingListTemplateImpl implements PairingListTemplate {

    private final Random random = new Random();
    private final int[][] pairingListTemplate;
    private final double standardDev;
    private final ExecutorService executorService = ThreadPoolUtil.INSTANCE
            .getDefaultBackgroundTaskThreadPoolExecutor();

    private final int maxConstantFlights;
    private final int prefixSeedCount;
    private final int suffixSeedCount;
    private final int MAX_TASKS = 1024;
    private final int iterations;

    public PairingListTemplateImpl(PairingFrameProvider pairingFrameProvider) {
        // setting iterations to default of 100.000
        this(pairingFrameProvider, 100000);
    }

    public PairingListTemplateImpl(PairingFrameProvider pairingFrameProvider, int iterations) {
        this.iterations = iterations;
        
        if (0.9 * pairingFrameProvider.getFlightsCount() > 10) {
            this.maxConstantFlights = 10;
            this.prefixSeedCount = 2;
        } else if (pairingFrameProvider.getFlightsCount() > 4) {
            this.maxConstantFlights = (int) ((pairingFrameProvider.getFlightsCount()) * 0.5);
            this.prefixSeedCount = (int) (Math.pow(this.MAX_TASKS, (1.0 / maxConstantFlights)));
        } else {
            maxConstantFlights = 0;
            prefixSeedCount = 0;
        }
        
        this.suffixSeedCount =  (int) Math.pow((iterations / this.MAX_TASKS),
                1.0 / (pairingFrameProvider.getFlightsCount() - this.maxConstantFlights));
        
        if (this.checkValues(pairingFrameProvider.getFlightsCount(), pairingFrameProvider.getGroupsCount(),
                pairingFrameProvider.getCompetitorsCount())) {
            
            if (maxConstantFlights > 0) {
                this.pairingListTemplate = this.createPairingListTemplate(pairingFrameProvider.getFlightsCount(),
                        pairingFrameProvider.getGroupsCount(), pairingFrameProvider.getCompetitorsCount());
            } else {
                pairingListTemplate = this.createPairingListTemplateWithoutPrefix(
                        pairingFrameProvider.getFlightsCount(), pairingFrameProvider.getGroupsCount(),
                        pairingFrameProvider.getCompetitorsCount());
            }
            this.standardDev = this.calcStandardDev(incrementAssociations(this.pairingListTemplate,
                    new int[pairingFrameProvider.getCompetitorsCount()][pairingFrameProvider.getCompetitorsCount()]));
        } else {
            throw new IllegalArgumentException("Wrong arguments for creating a pairing list template: count of flights "
                    + "has to be greater than 0; count of groups has to be greater than 1; count of competitors has to "
                    + "be greater than 1; count of competitors has to be greater than count of groups; count of "
                    + "competitors has to be divisible by count of groups");
        }
    }

    @Override
    public double getQuality() {
        return this.standardDev;
    }

    @Override
    public <Flight, Group, Competitor> PairingList<Flight, Group, Competitor> createPairingList(
            CompetitionFormat<Flight, Group, Competitor> competitionFormat) {
        return new PairingListImpl<>(this, competitionFormat);
    }

    @Override
    public int[][] getPairingListTemplate() {
        return pairingListTemplate;
    }

    /**
     * Creates a pairing list template.
     * 
     * <p>The generation of pairing list templates follow two general steps:</p>
     * <ol>
     *          <li>
     *                  The <b>general algorithm</b> of creating a pairing list works as follows: <br \>
     *                  The decision, who to put into pairing list template, is taken by looking at our <b>associations</b> 
     *                  (The association matrix describes how often the competitors encounter each other). The algorithm 
     *                  of creating a single group follows the following steps:
     *                  <ol>
     *                          <li>First of all, we generate a random seed, which affects the following generation. The 
     *                              seed describes a competitor number. By setting the first competitor in the first group 
     *                              of a flight, we can now go on and select the rest of competitor to put in the pairing 
     *                              list. </li>
     *                          <li>When filling up a single group of a flight, we iterate over all competitors and check 
     *                              whether the competitor is already set in this group. Elsewise, we go on finding the 
     *                              smallest sum of encounters with the remaining competitors in association matrix and 
     *                              the smallest maximum of encounters. By checking these condition, in the end we will 
     *                              get a well distributed pairing list template. </li>
     *                  </ol>
     *                  This is how the algorithm will be applied to the groups: In the first group the random seed will
     *                  be set to the first assignment. The other assignment are generated the way describes above. The 
     *                  remaining groups unless the last one will be filled up the way the algorithm follows. In the last
     *                  group, we just place the remaining competitors that are not listed in the other groups.
     *          </li>
     *          <li>
     *                  Unfortunately, there is no systematic way of generating a well distributed pairing list. Since
     *                  we use a random generated seed, this algorithm is not deterministic and it works like the trial and 
     *                  error principle: We commit a count of iterations to the algorithm to create as much pairing list 
     *                  templates as committed and return the best (see JavaDoc of getQuality()).<br \>
     *                  Since we want to improve the performance of this algorithm, we started to work with <b>concurrency</b>.
     *                  We first create a <b>prefix</b>. The length of the prefix depends on the count of flights. The random 
     *                  seeds of this prefix are just of a small set of all competitors (the number of seeds depends on 
     *                  the iterations and the count of flights). So we have <code>Math.pow(seeds, prefixLength)</code>
     *                  possible combinations. For each combination, we create a task that will be put in a 
     *                  <code>ExecuterService</code> handling all tasks. Each task creates the <b>suffix</b> by using
     *                  random seeds from all competitors now again. Each task has now <code>iterations/tasksCount</code>
     *                  iterations to generate.<br />
     *          </li>
     * </ol>
     * </p>
     * 
     * @param flightCount
     *            count of flights
     * @param groupCount
     *            count of groups per flight
     * @param competitors
     *            count of total competitors
     */
    protected int[][] createPairingListTemplate(int flightCount, int groupCount, int competitors) {
        int[] seeds = this.generateSeeds(flightCount, competitors);
        int[][] bestPLT = new int[flightCount * groupCount][competitors / groupCount];
        double bestDev = Double.POSITIVE_INFINITY;

        ArrayList<Future<int[][]>> futures = this.createPrefix(flightCount, groupCount, competitors,
                new int[competitors][competitors], new int[flightCount * groupCount][competitors / groupCount], seeds,
                new ArrayList<>(), 0);

        for (Future<int[][]> f : futures) {
            try {
                int[][] currentPLT = f.get();
                double currentStandardDev = calcStandardDev(
                        incrementAssociations(currentPLT, new int[competitors][competitors]));

                if (currentStandardDev < bestDev) {
                    bestPLT = currentPLT;
                    bestDev = currentStandardDev;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        bestPLT = this.improveAssignment(bestPLT, flightCount, groupCount, competitors);
        bestPLT = this.improveAssignmentChanges(bestPLT, flightCount, competitors);

        futures.clear();

        return bestPLT;
    }
    
    private boolean checkValues(int flights, int groups, int competitors) {
        if ((flights > 0) && (groups > 1) && (competitors > 1) && (competitors >= groups) && (competitors % groups == 0)) {
            return true;
        }
        return false;
    }

    /**
     * Creates seeds for constant flights.
     * 
     * No duplicates allowed.
     * 
     * @param flights
     *            count of flights
     * @param competitors
     *            count of competitors
     * @return int array of random competitors
     */
    private int[] generateSeeds(int flights, int competitors) {
        return this.generateSeeds(flights, competitors, this.prefixSeedCount);
    }
    
    private int[] generateSeeds(int flights, int competitors, int count) {
        int[] seeds=new int[count];
        Arrays.fill(seeds, 0);
        for(int x=0;x<seeds.length;x++){
            int random=this.getRandomIntegerBetween(0, competitors - 1);
            while(this.contains(seeds, random)) {
                random=this.getRandomIntegerBetween(0, competitors - 1);
            }
            seeds[x] = random;
        }
        return seeds;
    }

    class SuffixCreationTask implements Callable<int[][]> {
        int flights, groups, competitors, seedLength,tasks;
        int[][] plt, associations;
        /**
         * This task is created every time a constant is generated. It generates a specific number of PairingListTemplates,which is based on the constant flights 
         * which are given to the task and returns its best. The number of generated PairingListTamplates depends on the number of constant flights and the number of seeds.
         * @param flights count of flights
         * @param groups count of groups
         * @param competitors count of competitors
         * @param constantPLT constant generated flights on which the tasks is based on  
         * @param associations associations created from constantPLT
         */
        SuffixCreationTask(int flights, int groups, int competitors, int[][] constantPLT, int[][] associations, int seedLength) {
            this.flights = flights;
            this.groups = groups;
            this.competitors = competitors;
            this.seedLength = seedLength;
            this.plt = new int[flights * groups][competitors / groups];
            this.associations = new int[competitors][competitors];
            this.tasks=(int)Math.pow(seedLength, maxConstantFlights);
            // we use here System.arraycopy, because else, we would work on the reference of constantPLT. Besides we 
            // want to avoid changing the reference. 
            for (int i = 0; i < constantPLT.length; i++) {
                System.arraycopy(constantPLT[i], 0, this.plt[i], 0, competitors / groups);
            }
            for (int i = 0; i < associations.length; i++) {
                System.arraycopy(associations[i], 0, this.associations[i], 0, competitors);
            }
        }
        
        SuffixCreationTask(int flights, int groups, int competitors, int[][] constantPLT, int[][] associations, int seedLength,int tasks) {
            this.flights = flights;
            this.groups = groups;
            this.competitors = competitors;
            this.seedLength = seedLength;
            this.plt = new int[flights * groups][competitors / groups];
            this.associations = new int[competitors][competitors];
            this.tasks=(tasks);
            // we use here System.arraycopy, because else, we would work on the reference of constantPLT. Besides we 
            // want to avoid changing the reference. 
            for (int i = 0; i < constantPLT.length; i++) {
                System.arraycopy(constantPLT[i], 0, this.plt[i], 0, competitors / groups);
            }
            for (int i = 0; i < associations.length; i++) {
                System.arraycopy(associations[i], 0, this.associations[i], 0, competitors);
            }
        }

        @Override
        public int[][] call() {
            return createSuffix(flights, groups, competitors, plt, associations, 
                    generateSeeds(flights, competitors, suffixSeedCount), maxConstantFlights);
        }
    } 
    
    private int[][] createPairingListTemplateWithoutPrefix(int flightsCount, int groupsCount,int competitorsCount){
        double bestDev=Double.POSITIVE_INFINITY;
        int[][] bestPLT=new int[flightsCount*groupsCount][competitorsCount/groupsCount];
        ArrayList<Future<int[][]>> futures=new ArrayList<>();
        for(int i=0;i<1024;i++){
        futures.add(executorService.submit(new SuffixCreationTask(flightsCount, groupsCount, competitorsCount,
                new int[flightsCount * groupsCount][competitorsCount / groupsCount],
                new int[competitorsCount][competitorsCount],flightsCount,1024)));
        }
        for (Future<int[][]> f : futures) {
            try {
                int[][] currentPLT = f.get();
                double currentStandardDev = calcStandardDev(
                        incrementAssociations(currentPLT, new int[competitorsCount][competitorsCount]));

                if (currentStandardDev < bestDev) {
                    bestPLT = currentPLT;
                    bestDev = currentStandardDev;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return bestPLT;
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
     * @return <code>ArrayList</code> of <code>Futures</code> in which the result of a single task is saved
     */
    private ArrayList<Future<int[][]>> createPrefix(int flights, int groups, int competitors, int[][] associations,
            int[][]currentPLT, int[] seeds, ArrayList<Future<int[][]>> futures, int level) {         

        if (level < this.maxConstantFlights) {
                // calculate Flights for current recurrence level
                for (int seedIndex = 0; seedIndex < seeds.length; seedIndex++) {
                    int[][] temp = this.createFlight(groups, competitors, associations, seeds[seedIndex]);
                    for (int m = 0; m < groups; m++) {
                        System.arraycopy(temp[m], 0, currentPLT[level * groups + m], 0, competitors / groups);
                    }
                    level++;
                    associations = this.incrementAssociations(temp, associations);
                    this.createPrefix(flights, groups, competitors, associations, currentPLT, this.generateSeeds(flights, competitors),
                            futures, level);
                    level--;
                    associations = this.decrementAssociations(temp, associations);
                }
                
                // reset last recurrence step
                for (int i = level * groups; i < level * groups + groups; i++) {
                    Arrays.fill(currentPLT[i], 0);
                }
            
        } else {
            // Task start
            Future<int[][]> future = executorService.submit((new SuffixCreationTask(flights, groups, competitors, currentPLT, associations, seeds.length)));
            futures.add(future);
            
            // reset last recurrence step
            for (int i = maxConstantFlights * groups - 1; i >= maxConstantFlights * groups - groups; i--) {
                Arrays.fill(currentPLT[i], 0);
            }
        }
        return futures;
    }
    
    /**
     * Generates a single flight, that depends on a specific seed. 
     * 
     * @param flightCount count of flights
     * @param groupCount count of groups in flight
     * @param competitorCount count of competitors
     * @param currentAssociations current matrix that describes how often a competitor competed
     *                             against another competitor. 
     * @param seed generation bases on this competitor number.
     * 
     * @return generated flight as <code>int[groups][competitors / groups]</code> array
     */
    protected int[][] createFlight(int groupCount, int competitorCount, int[][] currentAssociations, int seed) {
        int[][] flightColumn = new int[groupCount][competitorCount / groupCount];
        
        // filling the array with -1, because our competitor number starts at 0. So if we later ask, whether the 
        // competitor number 0 is already in our actual flight, it would say, that it is so. By filling up the array
        // with -2, we can avoid this.
        for (int[] group : flightColumn) {
            Arrays.fill(group, -1);
        }
        int[][][] associationRow = new int[groupCount][(competitorCount / groupCount) - 1][competitorCount];
        int[] associationHigh = new int[groupCount - 1];
        flightColumn[0][0] = seed;
        for (int assignmentIndex = 1; assignmentIndex < (competitorCount / groupCount); assignmentIndex++) {
            int associationSum = Integer.MAX_VALUE;
            associationHigh[0] = Integer.MAX_VALUE;
            associationRow = copyInto3rdDimension(competitorCount, currentAssociations, associationRow, flightColumn,
                    assignmentIndex, 0);

            for (int competitorIndex = 0; competitorIndex < competitorCount; competitorIndex++) {
                if ((sumOf3rdDimension(associationRow, 0, competitorIndex) <= associationSum)
                        && !contains(flightColumn, competitorIndex)
                        && findMaxValue(associationRow, 0, competitorIndex) <= associationHigh[0]) {
                    flightColumn[0][assignmentIndex] = competitorIndex;
                    associationSum = sumOf3rdDimension(associationRow, 0, competitorIndex);
                    associationHigh[0] = findMaxValue(associationRow, 0, competitorIndex);
                }
            }
        }
        for (int groupIndex = 1; groupIndex < groupCount - 1; groupIndex++) {
            for (int aux = 0; aux < competitorCount; aux++) {
                if (!contains(flightColumn, aux)) {
                    flightColumn[groupIndex][0] = aux;
                    break;
                }
            }

            for (int assignmentIndex = 1; assignmentIndex < (competitorCount / groupCount); assignmentIndex++) {
                int associationSum = Integer.MAX_VALUE;
                associationHigh[groupIndex] = Integer.MAX_VALUE;
                associationRow = copyInto3rdDimension(competitorCount, currentAssociations, associationRow, flightColumn,
                        assignmentIndex, groupIndex);

                for (int competitorIndex = 0; competitorIndex < competitorCount; competitorIndex++) {
                    if ((sumOf3rdDimension(associationRow, groupIndex, competitorIndex) <= associationSum)
                            && !contains(flightColumn, competitorIndex) 
                            && findMaxValue(associationRow, groupIndex, competitorIndex) <= associationHigh[groupIndex]) {
                        flightColumn[groupIndex][assignmentIndex] = competitorIndex;
                        associationSum = sumOf3rdDimension(associationRow, groupIndex, competitorIndex);
                        associationHigh[groupIndex] = findMaxValue(associationRow, groupIndex, competitorIndex);
                    }
                }
            }
        }
        // last Flight
        for (int assignmentIndex = 0; assignmentIndex < (competitorCount / groupCount); assignmentIndex++) {
            for (int competitorIndex = 0; competitorIndex < competitorCount; competitorIndex++) {
                if (!contains(flightColumn, competitorIndex)) {
                    flightColumn[groupCount - 1][assignmentIndex] = competitorIndex;
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
     * @param flightCount count of flights
     * @param groupCount count of groups in flight
     * @param competitorCount count of competitors
     * @param iterationCount repetitions of generating pairing lists
     * @param currentPLT constant generated flights
     * @param associations current matrix that describes how often a competitor competed
     *                      against another competitor. 
     * @return best complete pairing list out of given iterations
     */
    protected int[][] createSuffix(int flightCount, int groupCount, int competitorCount, 
            int[][] currentPLT, int[][] associations, int[] seeds, int level) {
        
        if (level < flightCount) {
            double bestDev = Double.POSITIVE_INFINITY;
            
            for (int seed : seeds) {
                int[][] flight = this.createFlight(groupCount, competitorCount, associations, seed);
                for (int m = 0; m < groupCount; m++) {
                    System.arraycopy(flight[m], 0, currentPLT[level * groupCount + m], 0, competitorCount / groupCount);
                }
                
                level++;
                associations = this.incrementAssociations(flight, associations);
                int[] nextSeeds;
                if (Math.pow((this.iterations / MAX_TASKS), 1.0 - (flightCount - level)) > 2&& suffixSeedCount<2) {
                    nextSeeds = generateSeeds(flightCount, competitorCount, 2);
                } else {
                    nextSeeds = this.generateSeeds(flightCount, competitorCount, this.suffixSeedCount);
                }
                int[][] suffix = this.createSuffix(flightCount, groupCount, competitorCount, currentPLT, associations, 
                        nextSeeds, level);
                double currentDev = this.calcStandardDev(this.incrementAssociations(suffix, 
                                        new int[competitorCount][competitorCount]));
                
                if (currentDev < bestDev) {
                    currentPLT = suffix;
                    bestDev = currentDev;
                }
                
                level--;
                associations = this.decrementAssociations(flight, associations);
            }
        }
        
        return currentPLT;
    }

    /**
     * Returns a matrix that describes how often the competitors competed on a boat. The matrix has the following
     * dimension: <code>assignmentAssociations[competitors][boats]</code>.
     * 
     * @param pairingList
     *            current pairing list from which the associations will be created
     * @param associations
     *            int array on which the assignment will be written
     * @return matrix that represents the assignment associations
     */
    protected int[][] getAssignmentAssociations(int[][] pairingList, int[][] associations) {
        for (int[] group : pairingList) {
            for (int i = 0; i < pairingList[0].length; i++) {
                associations[group[i]][i] += 1;
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
    protected int[][] improveAssignment(int[][] pairinglist, int flights, int groups, int competitors) {
        int[][] assignments = this.getAssignmentAssociations(pairinglist, new int[competitors][competitors / groups]);
        double neededAssigments = flights / (competitors / groups);
        double bestDev = Double.POSITIVE_INFINITY;
        int[][] bestPLT = new int[flights * groups][competitors / groups];
        for (int iteration = 0; iteration < 10; iteration++) {
            for (int zGroup = 0; zGroup < pairinglist.length; zGroup++) {
                int[][] groupAssignments = new int[competitors / groups][competitors / groups];
                for (int zPlace = 0; zPlace < (competitors / groups); zPlace++) {
                    System.arraycopy(assignments[pairinglist[zGroup][zPlace]], 0, groupAssignments[zPlace], 0,
                            (competitors / groups));
                }
                for (int zPlace = 0; zPlace < competitors * 50; zPlace++) {
                    int[] position = this.findWorstValuePosition(groupAssignments, (int) neededAssigments);
                    if (groupAssignments[position[0]][position[1]] > neededAssigments - 1
                            && groupAssignments[position[0]][position[1]] < neededAssigments + 1) {
                        break;
                    } else if (groupAssignments[position[0]][position[1]] < neededAssigments) {
                        int temp = 0;
                        temp = pairinglist[zGroup][position[1]];
                        pairinglist[zGroup][position[1]] = pairinglist[zGroup][position[0]];
                        pairinglist[zGroup][position[0]] = temp;
                        assignments = this.getAssignmentAssociations(pairinglist,
                                new int[competitors][competitors / groups]);
                        for (int x = 0; x < (competitors / groups); x++) {
                            System.arraycopy(assignments[pairinglist[zGroup][x]], 0, groupAssignments[x], 0,
                                    (competitors / groups));
                        }
                    } else {
                        if (position[0] == position[1]) {
                            int temp = 0;
                            temp = pairinglist[zGroup][this.findMinValuePosition(groupAssignments[position[0]])];
                            pairinglist[zGroup][this.findMinValuePosition(
                                    groupAssignments[position[0]])] = pairinglist[zGroup][position[0]];
                            pairinglist[zGroup][position[0]] = temp;
                            assignments = this.getAssignmentAssociations(pairinglist,
                                    new int[competitors][competitors / groups]);
                            for (int x = 0; x < (competitors / groups); x++) {
                                System.arraycopy(assignments[pairinglist[zGroup][x]], 0, groupAssignments[x], 0,
                                        (competitors / groups));
                            }
                        } else {
                            groupAssignments[position[0]][position[1]] = -1;
                        }
                    }
                }
                if (this.calcStandardDev(pairinglist) < bestDev) {
                    bestDev = this.calcStandardDev(pairinglist);
                    bestPLT = pairinglist;
                }
            }
        }
        return bestPLT;
    }
    private int[][] improveAssignmentChanges(int[][] pairingList, int flights, int competitors) {
        int boatChanges[] = new int[flights-1];

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
                int[] temp = new int[pairingList[0].length];
                System.arraycopy(pairingList[i * pairingList.length / flights], 0, temp, 0, temp.length);

                System.arraycopy(pairingList[i * pairingList.length / flights+ bestMatchesIndex], 0,
                        pairingList[i * pairingList.length / flights ], 0, groupNext.length);
                System.arraycopy(temp, 0, pairingList[i * pairingList.length / flights + bestMatchesIndex], 0, temp.length);
            }

            boatChanges[i - 1] = groupNext.length - bestMatch;
        }

//        System.out.println(Arrays.toString(boatChanges));

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
    
    /**
     * Returns an index that has the greatest difference to neededAssignments
     * 
     * @param groupAssignments array that contains the values
     * @param neededAssigments reference value
     * @return int array with 2 indices that represent row and column of the worst value
     */
    private int[] findWorstValuePosition(int[][] groupAssignments, int neededAssigments) {
        int[] worstValuePos = new int[2];
        int worstValue = 0;
        for (int i = 0; i < groupAssignments.length; i++) {
            for (int z = 0; z < groupAssignments[0].length; z++) {
                if (groupAssignments[i][z] >= 0) {
                    if (Math.abs(groupAssignments[i][z] - neededAssigments) > worstValue) {
                        worstValuePos[0] = i;
                        worstValuePos[1] = z;
                        worstValue = Math.abs(groupAssignments[i][z] - neededAssigments);
                    }
                }
            }
        }
        return worstValuePos;
    }

    public int[][][] copyInto3rdDimension(int competitors, int[][] currentAssociations, int[][][] associationRow,
            int[][] flightColumn, int zGroups, int fleet) {
        System.arraycopy(currentAssociations[flightColumn[fleet][zGroups - 1]], 0, associationRow[fleet][zGroups - 1], 
                0, competitors);
        return associationRow;
    }

    private boolean contains(int[][] arr, int value) {
        for (int i = 0; i < arr.length; i++) {
            for (int z = 0; z < arr[0].length; z++) {
                if (arr[i][z] == value)
                    return true;
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

    private int findMinValuePosition(int[] arr) {
        int temp = Integer.MAX_VALUE;
        int position = -1;
        for (int z = 0; z < arr.length; z++) {
            if (arr[z] < temp) {
                position = z;
                temp = arr[z];
            }
        }
        return position;
    }

    private int findMaxValue(int[][][] associationRow, int i, int comp) {
        int temp = 0;
        for (int z = 0; z < associationRow[0].length; z++) {
            if (associationRow[i][z][comp] > temp) {
                temp = associationRow[i][z][comp];
            }
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
    private int getRandomIntegerBetween(int min, int max) {
        return random.nextInt(max +1);
    }

    /**
     * Creates a matrix that describes how often the team competes against each other. Its dimensions are
     * <code>associations[competitors][competitors]</code>, e.g.:
     * 
     * 1 2 3 4 1 \ 2 3 1 2 2 \ 1 3 3 3 1 \ 2 4 1 3 2 \
     * 
     * In this example, team 2 competed 3 times against team 4.
     * 
     * @param pairingList
     *            current pairing list template
     * @param associations
     *            int array in which the association will be written
     * @return int arryay of associations
     */

    public int[][] incrementAssociations(int[][] pairingList, int[][] associations) {
        for (int[] group : pairingList) {
            for (int i = 0; i < pairingList[0].length; i++) {
                for (int j = 0; j < pairingList[0].length; j++) {
                    if (group[i] == group[j]) {
                        associations[group[i]][group[j]] = -1;
                    } else {
                        associations[group[i]][group[j]] += 1;
                    }
                }
            }
        }

        return associations;
    }
    
    public int[][] decrementAssociations(int[][] pairingList, int[][] associations) {
        for (int[] group: pairingList) {
            for (int i = 0; i < group.length; i++) {
                for (int j = 0; j < group.length; j++) {
                    if (group[i] == group[j]) {
                        associations[group[i]][group[j]] = -1;
                    } else {
                        associations[group[i]][group[j]] -= 1;
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