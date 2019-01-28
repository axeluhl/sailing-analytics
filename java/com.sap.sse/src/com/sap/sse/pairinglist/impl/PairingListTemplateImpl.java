package com.sap.sse.pairinglist.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.common.PairingListCreationException;
import com.sap.sse.pairinglist.CompetitionFormat;
import com.sap.sse.pairinglist.PairingFrameProvider;
import com.sap.sse.pairinglist.PairingList;
import com.sap.sse.pairinglist.PairingListTemplate;
import com.sap.sse.util.ThreadPoolUtil;

public class PairingListTemplateImpl implements PairingListTemplate {
    private static final Logger logger = Logger.getLogger(PairingListTemplateImpl.class.getName());
    
    private final Random random = new Random();
    private final int[][] pairingListTemplate;
    private final double standardDev, assignmentQuality;
    private final int flightMultiplier, boatChangeFactor, boatchanges;
    private final int dummies;
    private final ExecutorService executorService = ThreadPoolUtil.INSTANCE.getDefaultBackgroundTaskThreadPoolExecutor();
    private final int iterations;

    public PairingListTemplateImpl(PairingFrameProvider pairingFrameProvider) {
        // setting iterations to default of 100.000
        this(pairingFrameProvider, /* iterations */ 100000, /* flight multiplier */ 1, /* boat change factor */ 0);
    }

    public PairingListTemplateImpl(PairingFrameProvider pairingFrameProvider, int flightMultiplier) {
        // setting iterations to default of 100.000
        this(pairingFrameProvider, /* iterations */ 100000, flightMultiplier, /* boat change factor */ 0);
    }

    public PairingListTemplateImpl(PairingFrameProvider pairingFrameProvider, int flighMultiplier, int boatChangeFactor) {
        this(pairingFrameProvider, /* iterations */ 100000, flighMultiplier, boatChangeFactor);
    }

    public PairingListTemplateImpl(PairingFrameProvider pairingFrameProvider, int iterations, int flightMultiplier, int boatChangeFactor) {
        this.iterations = iterations;
        this.flightMultiplier = flightMultiplier;
        this.boatChangeFactor = boatChangeFactor;
        if (this.checkValues(pairingFrameProvider.getFlightsCount(), pairingFrameProvider.getGroupsCount(),
                pairingFrameProvider.getCompetitorsCount())) {
            if (pairingFrameProvider.getCompetitorsCount() % pairingFrameProvider.getGroupsCount() != 0) {
                this.dummies = pairingFrameProvider.getGroupsCount()
                        - (pairingFrameProvider.getCompetitorsCount() % pairingFrameProvider.getGroupsCount());
            } else {
                dummies = 0;
            }
            this.pairingListTemplate = createPairingListTemplate(flightMultiplier, pairingFrameProvider);
            final int boatChangesFromPairingList = this.getBoatChangesFromPairingList(this.pairingListTemplate,
                    pairingFrameProvider.getFlightsCount(), pairingFrameProvider.getGroupsCount(),
                    pairingFrameProvider.getCompetitorsCount());
            if (flightMultiplier <= 1) {
                this.boatchanges = boatChangesFromPairingList;
            } else {
                this.boatchanges = boatChangesFromPairingList * flightMultiplier
                        + flightMultiplier * (this.getMatches(this.pairingListTemplate[pairingListTemplate.length - 1],
                                this.pairingListTemplate[0]));
            }
            this.standardDev = this.calcStandardDev(
                    incrementAssociations(this.pairingListTemplate, new int[pairingFrameProvider.getCompetitorsCount()
                            + dummies][pairingFrameProvider.getCompetitorsCount() + dummies]));
            this.assignmentQuality = this.calcStandardDev(getAssignmentAssociations(this.pairingListTemplate,
                    new int[pairingFrameProvider.getCompetitorsCount()
                            + dummies][(pairingFrameProvider.getCompetitorsCount() + dummies)
                                    / pairingFrameProvider.getGroupsCount()]));
            this.resetDummies(pairingListTemplate, pairingFrameProvider.getCompetitorsCount() + dummies);
        } else {
            throw new IllegalArgumentException("Wrong arguments for creating a pairing list template: count of flights "
                    + "has to be greater than 0; count of groups has to be greater than 1; count of competitors has to "
                    + "be greater than 1; count of competitors has to be greater than count of groups");
        }
    }

    private int[][] createPairingListTemplate(int flightMultiplier, PairingFrameProvider pairingFrameProvider) {
        return this.createPairingListTemplate(pairingFrameProvider.getFlightsCount() / flightMultiplier,
                pairingFrameProvider.getGroupsCount(), pairingFrameProvider.getCompetitorsCount() + dummies);
    }

    /**
     * Used to convert a {@code PairingListeTemplateDTO} to {@link PairingListTemplate}
     */
    public PairingListTemplateImpl(int[][] template, int competitorsCount, int flightMultiplier, int boatChangeFactor) {
        this.pairingListTemplate = template;
        int groupCount = (int) (competitorsCount / this.pairingListTemplate[0].length);
        if (groupCount != (competitorsCount / this.pairingListTemplate[0].length)) {
            groupCount++;
        }
        this.dummies = groupCount - (competitorsCount % groupCount);
        int dummyIndex = 0;
        for (int[] group : this.pairingListTemplate) {
            for (int competitorNumber = 0; competitorNumber < group.length; competitorNumber++) {
                if (group[competitorNumber] < 0) {
                    group[competitorNumber] = competitorsCount + dummyIndex;
                    dummyIndex++;
                    if (dummyIndex >= dummies) {
                        dummyIndex = 0;
                    }
                }
            }
        }
        this.standardDev = this.calcStandardDev(incrementAssociations(this.pairingListTemplate,
                new int[competitorsCount + this.dummies][competitorsCount + this.dummies]));
        this.assignmentQuality = this.calcStandardDev(getAssignmentAssociations(template,
                new int[competitorsCount + this.dummies][(competitorsCount + this.dummies) / groupCount]));
        this.boatchanges = this.getBoatChangesFromPairingList(template, template.length / groupCount, groupCount, competitorsCount);
        this.resetDummies(this.pairingListTemplate, competitorsCount + this.dummies);
        this.iterations = 100000;
        this.flightMultiplier = flightMultiplier;
        this.boatChangeFactor = boatChangeFactor;
    }

    @Override
    public double getQuality() {
        return this.standardDev;
    }

    @Override
    public double getAssignmentQuality() {
        return this.assignmentQuality;
    }

    @Override
    public int getBoatChanges() {
        return this.boatchanges;
    }

    @Override
    public <Flight, Group, Competitor, CompetitorAllocation> PairingList<Flight, Group, Competitor, CompetitorAllocation> createPairingList(
            CompetitionFormat<Flight, Group, Competitor, CompetitorAllocation> competitionFormat)
            throws PairingListCreationException {
        return new PairingListImpl<>(this, competitionFormat);
    }

    @Override
    public int[][] getPairingListTemplate() {
        return pairingListTemplate;
    }

    /**
     * Creates a pairing list template.
     * 
     * <p>
     * The generation of pairing list templates follow two general steps:
     * </p>
     * <ol>
     * <li>The <b>general algorithm</b> of creating a pairing list works as follows: <br \> The decision, who to put
     * into pairing list template, is taken by looking at our <b>associations</b> (The association matrix describes how
     * often the competitors encounter each other). The algorithm of creating a single group follows the following
     * steps:
     * <ol>
     * <li>First of all, we generate a random seed, which affects the following generation. The seed describes a
     * competitor number. By setting the first competitor in the first group of a flight, we can now go on and select
     * the rest of competitor to put in the pairing list.</li>
     * <li>When filling up a single group of a flight, we iterate over all competitors and check whether the competitor
     * is already set in this group. Elsewise, we go on finding the smallest sum of encounters with the remaining
     * competitors in association matrix and the smallest maximum of encounters. By checking these condition, in the end
     * we will get a well distributed pairing list template.</li>
     * </ol>
     * This is how the algorithm will be applied to the groups: In the first group the random seed will be set to the
     * first assignment. The other assignment are generated the way describes above. The remaining groups unless the
     * last one will be filled up the way the algorithm follows. In the last group, we just place the remaining
     * competitors that are not listed in the other groups.</li>
     * <li>Unfortunately, there is no systematic way of generating a well distributed pairing list. Since we use a
     * random generated seed, this algorithm is not deterministic and it works like the trial and error principle: We
     * commit a count of iterations to the algorithm to create as much pairing list templates as committed and return
     * the best (see JavaDoc of getQuality()).<br \> Since we want to improve the performance of this algorithm, we
     * started to work with <b>concurrency</b>. First we generate the amount of needed seed combinations (in default
     * case 100.000) and sort them, so seed combinations with equal seeds for the first x flights are grouped. After
     * dividing the seeds in that groups we start a task for each group. The task generates a pairinglist for every seed
     * combination and returns the best. after that we only have to search the best of all tasks an return it.</li>
     * </ol>
     * </p>
     * 
     * @param flightCount
     *            count of flights
     * @param groupCount
     *            count of groups per flight
     * @param competitorCount
     *            count of total competitors
     */
    protected int[][] createPairingListTemplate(int flightCount, int groupCount, int competitorCount) {
        int[][] bestPLT = new int[flightCount * groupCount][competitorCount / groupCount];
        double bestDev = Double.POSITIVE_INFINITY;
        int[][] allSeeds = new int[iterations][flightCount];
        for (int i = 0; i < iterations; i++) {
            allSeeds[i] = this.generateSeeds(flightCount, competitorCount, flightCount);
        }
        allSeeds = radixSort(allSeeds, competitorCount);
        ArrayList<int[][]> parts = this.divideSeeds(allSeeds);
        ArrayList<Future<int[][]>> futures = new ArrayList<>();
        for (int[][] is : parts) {
            Future<int[][]> future = executorService
                    .submit((new CreateFlightsForListOfSeeds(flightCount, groupCount, competitorCount, is)));
            futures.add(future);
        }
        for (Future<int[][]> f : futures) {
            try {
                int[][] currentPLT = f.get();
                double currentStandardDev = calcStandardDev(
                        incrementAssociations(currentPLT, new int[competitorCount][competitorCount]));
                if (currentStandardDev < bestDev) {
                    bestPLT = currentPLT;
                    bestDev = currentStandardDev;
                }
            } catch (InterruptedException | ExecutionException e) {
                logger.log(Level.WARNING, "Caught exception waiting for the seed creation", e);
            }
        }
        this.improveCompetitorAllocations(bestPLT, flightCount, groupCount, competitorCount);
        this.improveAssignmentChanges(bestPLT, flightCount, groupCount, competitorCount);
        if (flightMultiplier > 1) {
            bestPLT = this.multiplyFlights(bestPLT, flightCount, groupCount, competitorCount);
        }
        futures.clear();
        return bestPLT;
    }

    /**
     * Divides the allSeeds array into parts with seed combinations that starts with the same seeds. If there are to
     * less flights this method cuts the off Seeds into 4000 parts to avoid to less Tasks.
     * 
     * @param equalSeeds
     *            number of equal seeds at the beginning of seed combinations which should be grouped. Attention: If
     *            this number is to big there will be very many little parts.
     * @param allSeeds
     *            array with seed combinations, which should be divided
     * @return an Arraylist, that contains parts of allSeeds.
     */
    private ArrayList<int[][]> divideSeeds(int[][] allSeeds) {
        ArrayList<int[][]> output = new ArrayList<>();
        int parts = ThreadPoolUtil.INSTANCE.getReasonableThreadPoolSize();
        int partsize = allSeeds.length / parts;
        for (int x = 0; x < allSeeds.length; x += partsize) {
            if (x + partsize < allSeeds.length) {
                output.add(Arrays.copyOfRange(allSeeds, x, x + partsize));
            } else {
                output.add(Arrays.copyOfRange(allSeeds, x, allSeeds.length));
            }
        }
        return output;
    }

    /**
     * Fast sorting algorithm to sort a huge amount of seeds in max <code>n*log(n)</code> time. Uses counting sort.
     * Steps through the flights of all seeds in reverse order, starting with the seeds' last flight and uses counting
     * sort to sort by that flight. Counting sort is stable, so the sort order of flights already sorted is preserved
     * for equal keys for the next flight. Counting sort works by storing the counts of each element in order to produce
     * an array of insert indices into the output array for each distinct seed value. The values range from
     * {@code 0..competitorCount-1}. The insert indices are obtained by aggregating across the counts. Element 0 always
     * starts at index 0; subsequent elements start at the index of the previous element plus the count of the previous
     * element. As the seeds are inserted into the output array, the insert indices keep getting incremented.
     * 
     * @param allSeeds
     *            array of seeds that should be sorted; the contents of the array may be altered by this method in an
     *            undefined way; always use the array returned by this method.
     * @param competitorCount
     *            highest value inside the complete array. Needed for counting sort.
     * @return sorted array with seed combinations; may be identical to {@code allSeeds} but with element order possibly
     *         changed, but may also be a different array
     */
    protected int[][] radixSort(int[][] allSeeds, int competitorCount) {
        int lengthOfASeed = allSeeds[0].length;
        int[][] output = new int[allSeeds.length][allSeeds[0].length];
        int[] count = new int[competitorCount];
        int[] writeIndexInOutput = new int[competitorCount];
        for (int i = lengthOfASeed - 1; i >= 0; i--) { // go backwards for "LSD" radix sort (least significant digit)
            Arrays.fill(count, 0); // quicker than re-allocating a new array each loop
            for (int[] seed : allSeeds) {
                count[seed[i]]++;
            }
            writeIndexInOutput[0] = 0;
            for (int z = 1; z < count.length; z++) {
                writeIndexInOutput[z] = writeIndexInOutput[z - 1] + count[z-1];
            }
            // now count[x-1] represents the first insert position in output for the seeds that have value x at their position i;
            // count[x]-1 represents the last insert position in output for the seeds with value x at their position i;
            // It is important now to keep the element order stable within the same "bucket" (seeds with same value at position i),
            // so the placement of seeds in output needs to match the iteration order in allSeeds
            for (int[] seed : allSeeds) {
                output[writeIndexInOutput[seed[i]]++] = seed;
            }
            int[][] tmp = allSeeds;
            allSeeds = output;
            output = tmp; // re-use array of same dimensions for next loop, avoiding expensive allocation with value filling
        }
        return allSeeds;
    }

    private boolean checkValues(int flights, int groups, int competitors) {
        return ((flights > 0) && (groups > 0) && (competitors > 1) && (competitors >= groups));
    }

    /**
     * Creates seeds for single flights. No duplicates allowed.
     * 
     * @param flights
     *            count of flights
     * @param competitors
     *            count of competitors
     * @param count
     *            number of seeds needed in the result array
     * @return int array of random competitors
     */

    private int[] generateSeeds(int flights, int competitors, int count) {
        int[] seeds = new int[count];
        Arrays.fill(seeds, 0);
        for (int x = 0; x < seeds.length; x++) {
            int random = this.getRandomIntegerBetweenZeroAndMax(competitors - 1);
            seeds[x] = random;
        }
        return seeds;
    }

    class CreateFlightsForListOfSeeds implements Callable<int[][]> {
        int flights, groups, competitors;
        int[][] seeds;

        /**
         * This task creates complete pairinglists for a given amount of seed combinations and returns its best result.
         * The aim is, to give a task seed combinations with some equals at the beginning, so some flights does not need
         * to be calculated every time. Its recommended to sort the seeds and divide it into senseful parts before
         * starting this task.
         * 
         * @param flights
         *            count of flights
         * @param groups
         *            count of groups
         * @param competitors
         *            count of competitors
         * @param seeds
         *            array with all seed combinations which should be calculated in this task
         * @param equals
         *            number of equal seeds at the beginning of all seed combinations
         */
        CreateFlightsForListOfSeeds(int flights, int groups, int competitors, int[][] seeds) {
            this.flights = flights;
            this.groups = groups;
            this.competitors = competitors;
            this.seeds = seeds;
        }

        @Override
        public int[][] call() {
            return createSinglePariringListTemplate(flights, groups, competitors, seeds);
        }
    }

    /**
     * Generates a single flight, that depends on a specific seed.
     * 
     * @param flightCount
     *            count of flights
     * @param groupCount
     *            count of groups in flight
     * @param competitorCount
     *            count of competitors
     * @param currentAssociations
     *            current matrix that describes how often a competitor competed against another competitor.
     * @param seed
     *            generation bases on this competitor number.
     * 
     * @return generated flight as <code>int[groups][competitors / groups]</code> array
     */
    protected int[][] createFlight(int groupCount, int competitorCount, int[][] currentAssociations, int seed) {
        int[][] flightColumn = new int[groupCount][competitorCount / groupCount];
        // filling the array with -1, because our competitor number starts at 0.
        // So if we later ask, whether the
        // competitor number 0 is already in our actual flight, it would say,
        // that it is so. By filling up the array
        // with -2, we can avoid this.
        for (int[] group : flightColumn) {
            Arrays.fill(group, -1);
        }
        boolean[] contains = new boolean[competitorCount];
        if (groupCount > 1) {
            int[] associationHigh = new int[groupCount - 1];
            flightColumn[0][0] = seed;
            contains[seed] = true;
            int[] sumsOf3rdDemension = new int[competitorCount];
            System.arraycopy(currentAssociations[seed], 0, sumsOf3rdDemension, 0, competitorCount);
            int[] maxValues = new int[competitorCount];
            System.arraycopy(currentAssociations[seed], 0, maxValues, 0, competitorCount);
            for (int assignmentIndex = 1; assignmentIndex < (competitorCount / groupCount); assignmentIndex++) {
                int associationSum = Integer.MAX_VALUE;
                associationHigh[0] = Integer.MAX_VALUE;
                for (int competitorIndex = 0; competitorIndex < competitorCount; competitorIndex++) {
                    if (sumsOf3rdDemension[competitorIndex] <= associationSum && !contains[competitorIndex]
                            && maxValues[competitorIndex] <= associationHigh[0]) {
                        flightColumn[0][assignmentIndex] = competitorIndex;
                        associationSum = sumsOf3rdDemension[competitorIndex];
                        associationHigh[0] = maxValues[competitorIndex];
                    }
                }
                contains[flightColumn[0][assignmentIndex]] = true;
                for (int competitor = 0; competitor < competitorCount; competitor++) {
                    if (flightColumn[0][assignmentIndex] != competitor) {
                        sumsOf3rdDemension[competitor] += currentAssociations[flightColumn[0][assignmentIndex]][competitor];
                        if (maxValues[competitor] < flightColumn[0][assignmentIndex]) {
                            maxValues[competitor] = flightColumn[0][assignmentIndex];
                        }
                    }
                }
            }
            for (int groupIndex = 1; groupIndex < groupCount - 1; groupIndex++) {
                for (int competitorIndex = 0; competitorIndex < competitorCount; competitorIndex++) {
                    if (!contains[competitorIndex]) {
                        flightColumn[groupIndex][0] = competitorIndex;
                        contains[flightColumn[groupIndex][0]] = true;
                        System.arraycopy(currentAssociations[flightColumn[groupIndex][0]], 0, sumsOf3rdDemension, 0, competitorCount);
                        System.arraycopy(currentAssociations[flightColumn[groupIndex][0]], 0, maxValues, 0, competitorCount);
                        break;
                    }
                }
                for (int assignmentIndex = 1; assignmentIndex < (competitorCount / groupCount); assignmentIndex++) {
                    int associationSum = Integer.MAX_VALUE;
                    associationHigh[groupIndex] = Integer.MAX_VALUE;
                    for (int competitorIndex = 0; competitorIndex < competitorCount; competitorIndex++) {
                        if (sumsOf3rdDemension[competitorIndex] <= associationSum && !contains[competitorIndex]
                                && maxValues[competitorIndex] <= associationHigh[groupIndex]) {
                            flightColumn[groupIndex][assignmentIndex] = competitorIndex;
                            associationSum = sumsOf3rdDemension[competitorIndex];
                            associationHigh[groupIndex] = maxValues[competitorIndex];
                        }
                    }
                    contains[flightColumn[groupIndex][assignmentIndex]] = true;
                    for (int competitor = 0; competitor < competitorCount; competitor++) {
                        if (flightColumn[groupIndex][assignmentIndex] != competitor) {
                            sumsOf3rdDemension[competitor] += currentAssociations[flightColumn[groupIndex][assignmentIndex]][competitor];
                            if (maxValues[competitor] < flightColumn[groupIndex][assignmentIndex]) {
                                maxValues[competitor] = flightColumn[groupIndex][assignmentIndex];
                            }
                        }
                    }
                }
            }
        }
        for (int assignmentIndex = 0; assignmentIndex < (competitorCount / groupCount); assignmentIndex++) {
            for (int competitorIndex = 0; competitorIndex < competitorCount; competitorIndex++) {
                if (!contains[competitorIndex]) {
                    flightColumn[groupCount - 1][assignmentIndex] = competitorIndex;
                }
            }
            contains[flightColumn[groupCount - 1][assignmentIndex]] = true;
        }
        return flightColumn;
    }

    /**
     * This method is called in every task to generate a number of different pairing lists to compare and choose the
     * best.
     * 
     * @param flightCount
     *            count of flights
     * @param groupCount
     *            count of groups in flight
     * @param competitorCount
     *            count of competitors
     * @param iterationCount
     *            repetitions of generating pairing lists
     * @param currentPLT
     *            constant generated flights
     * @param associations
     *            current matrix that describes how often a competitor competed against another competitor.
     * @return best complete pairing list out of given iterations
     */
    private int[][] createSinglePariringListTemplate(int flightCount, int groupCount, int competitorCount, int[][] seeds) {
        int[][] bestPLT = new int[flightCount * groupCount][competitorCount / groupCount];
        double bestDev = Double.POSITIVE_INFINITY;
        int[][] bestAssociations = new int[competitorCount][competitorCount];
        for (int x = 0; x < seeds[0].length; x++) {
            int[][] flightColumn = this.createFlight(groupCount, competitorCount, bestAssociations, seeds[seeds.length - 1][x]);
            for (int z = 0; z < flightColumn.length; z++) {
                System.arraycopy(flightColumn[z], 0, bestPLT[x * groupCount + z], 0, flightColumn[0].length);
            }
            this.incrementAssociations(flightColumn, bestAssociations);
        }
        bestDev = calcStandardDev(bestAssociations);
        for (int z = seeds.length - 2; z >= 0; z--) {
            int[][] currentPLT = new int[flightCount * groupCount][competitorCount / groupCount];
            int[][] currentAssociations = new int[competitorCount][competitorCount];
            for (int x = 0; x < seeds[0].length; x++) {
                int[][] flightColumn = this.createFlight(groupCount, competitorCount, currentAssociations, seeds[z][x]);
                for (int y = 0; y < flightColumn.length; y++) {
                    System.arraycopy(flightColumn[y], 0, currentPLT[x * groupCount + y], 0, flightColumn[0].length);
                }
                this.incrementAssociations(flightColumn, currentAssociations);
            }
            if (bestDev > calcStandardDev(currentAssociations)) {
                bestPLT = currentPLT;
                bestDev = calcStandardDev(currentAssociations);
                bestAssociations = currentAssociations;
            }
        }
        return bestPLT;
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
     * Switches competitors inside a group to improve competitor allocations. Method does not change the order of groups
     * or flights. The standard deviation of team associations will not be influenced by this method. After executing
     * the method the assignment should be well distributed.
     * 
     * @param pairingList
     *            current pairing list, improved in place by this method
     * @param flights
     *            count of flights
     * @param groups
     *            count of groups
     * @param competitors
     *            count of competitors
     */
    private void improveCompetitorAllocations(int[][] pairingList, int flights, int groups, int competitors) {
        int[][] assignments = this.getAssignmentAssociations(pairingList, new int[competitors][competitors / groups]);
        double averageAssignments = flights / (competitors / groups);
        for (int iteration = 0; iteration < 10; iteration++) {
            for (int zGroup = 0; zGroup < pairingList.length; zGroup++) {
                int[][] groupAssignments = new int[competitors / groups][competitors / groups];
                for (int zPlace = 0; zPlace < (competitors / groups); zPlace++) {
                    // select single group to optimize
                    System.arraycopy(assignments[pairingList[zGroup][zPlace]], 0, groupAssignments[zPlace], 0,
                            (competitors / groups));
                }
                for (int zPlace = 0; zPlace < competitors * 50; zPlace++) {
                    int[] position = findWorstValuePosition(groupAssignments, (int) averageAssignments);
                    int prevFlight = (int) (zGroup / groups) - 1;
                    int prevPosition = -1;
                    if (prevFlight >= 0) {
                        // search for previous position
                        for (int i = prevFlight * groups; i < prevFlight * groups + groups; i++) {
                            for (int j = 0; j < (competitors / groups); j++) {
                                if (pairingList[i][j] == pairingList[zGroup][position[0]]) {
                                    prevPosition = j;
                                    break;
                                }
                            }
                        }
                    }
                    if (groupAssignments[position[0]][position[1]] > averageAssignments - 1
                            && groupAssignments[position[0]][position[1]] < averageAssignments + 1) {
                        // no more optimizations possible 
                        break;
                    } else {
                        int temp = 0;
                        int bestPosition = getBestPositionToChangeTo(groupAssignments[position[0]], prevPosition);
                        temp = pairingList[zGroup][bestPosition];
                        pairingList[zGroup][bestPosition] = pairingList[zGroup][position[0]];
                        pairingList[zGroup][position[0]] = temp;
                        assignments = getAssignmentAssociations(pairingList, new int[competitors][competitors / groups]);
                        for (int x = 0; x < (competitors / groups); x++) {
                            System.arraycopy(assignments[pairingList[zGroup][x]], 0, groupAssignments[x], 0, (competitors / groups));
                        }
                    }
                }
            }
        }
    }

    /**
     * Improves the Assignment changes between two flights by switching the groups inside a flight. Competitors and
     * there assignment will not be changed.
     * 
     * @param pairingList
     *            complete pairingListTemplate which needs to be improved and which is modified in place by this method
     * @param flights
     *            number of flights which was used to generate the pairingListTemplate
     * @param competitors
     *            number of competitors which was used to generate the pairingListTemplate
     * 
     * @return the improved pairingListTemplate
     */
    private void improveAssignmentChanges(int[][] pairingList, int flights, int groups, int competitors) {
        int boatChanges[] = new int[flights - 1];
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
                System.arraycopy(pairingList[i * pairingList.length / flights + bestMatchesIndex], 0,
                        pairingList[i * pairingList.length / flights], 0, groupNext.length);
                System.arraycopy(temp, 0, pairingList[i * pairingList.length / flights + bestMatchesIndex], 0,
                        temp.length);
            }
            boatChanges[i - 1] = groupNext.length - bestMatch;
        }
    }

    private int getMatches(int[] arr1, int[] arr2) {
        int matches = 0;
        for (int i = 0; i < arr1.length; i++) {
            if (arr1[i] == arr2[i]) {
                matches++;
            }
        }
        return matches;
    }

    /**
     * Duplicates flights specified by flightMultiplier, e.g.: <br />
     * <br />
     * 
     * Flight 1, Flight2, Flight3 ... -> Flight 1, FLight 1, Flight 1..., Flight 2, Flight 2, Flight 2...
     * 
     * @param bestPLT
     *            the best {@link PairingListTemplate}
     * @param flightCount
     *            count of flights
     * @param groupCount
     *            count of groups
     * @param competitorCount
     *            count of competitors
     * @return returns the duplicated {@link PairingListTemplate}
     */
    private int[][] multiplyFlights(int[][] bestPLT, int flightCount, int groupCount, int competitorCount) {
        int[][] result = new int[((flightCount * flightMultiplier) * groupCount)][competitorCount / groupCount];
        for (int flightIndex = 0; flightIndex < flightCount; flightIndex++) {
            for (int x = 0; x < flightMultiplier; x++) {
                for (int groupIndex = 0; groupIndex < groupCount; groupIndex++) {
                    result[(flightIndex * groupCount * (flightMultiplier) + groupIndex)
                            + x * groupCount] = bestPLT[flightIndex * groupCount + groupIndex];
                }
            }
        }
        return result;
    }

    /**
     * There is an neededAssigments value created in <code> improveCompetitorAllocations</code>. This value 
     * represents the number of times a competitor has to be on one boat to get the best possible competitor 
     * allocation. In this method this value is used to find the competitor which assignment is the worst. 
     * That means the number of assignments to one special boat is far to low or far to high. This can be 
     * calculated by: absolute value of given assignment - needed assignment.
     *        
     * @param groupAssignments
     *            array that contains the values
     * @param neededAssigments
     *            reference value
     * @return int array with 2 indices that represent row and column of the worst value in the two dimensional array groupAssignments
     */
    private int[] findWorstValuePosition(int[][] groupAssignments, int neededAssigments) {
        int[] worstValuePos = new int[2];
        int worstValue = 0;
        //search the hole groupAssignments array for worst value 
        for (int i = 0; i < groupAssignments.length; i++) {
            for (int z = 0; z < groupAssignments[0].length; z++) {
                if (groupAssignments[i][z] >= 0) {
                    if (Math.abs(groupAssignments[i][z]-neededAssigments) > worstValue) {
                        worstValuePos[0] = i;
                        worstValuePos[1] = z;
                        worstValue = Math.abs(groupAssignments[i][z]-neededAssigments);
                    }
                }
            }
        }
        return worstValuePos;
    }
    
    /**
     * So far competitors have been allocated to the positions (e.g., boats) in a round such that this results in a good
     * distribution of competitor-to-position assignments, so that ideally each competitor competes in each position an
     * equal number of times.
     * <p>
     * 
     * There is, however, another criterion to watch for: in-between flights it may be useful if competitors
     * participating in the last group of the previous flight and the first group of the next flight keep their
     * position. If the positions are, e.g., boats, this would mean that such a competitor doesn't need to be shuttled
     * from one boat to another, simplifying logistics between flights.
     * <p>
     * 
     * These two criteria (spreading competitors equally across positions vs. letting competitors keep their position at
     * flight boundaries) contradict each other, obviously. Leaving a competitor in the same position across two rounds
     * reduces the spread of the competitor-to-position distribution.
     * <p>
     * 
     * The {@link #boatChangeFactor} is used to express a balance between these two criteria. It may range between
     * {@code 0..#competitors/#groups}.
     * <p>
     * 
     * This method searches for the position a competitor should be changed to, to improve the competitor allocations as
     * much as possible. The decision depends on the field {@link #boatChangeFactor}. If this value is 0, the method
     * returns the position on which a competitor is too rarely registered and should be changed to to get a better
     * competitor allocation.
     * 
     * If the value is greater than 0, the method can take the allocation in the previous flight into consideration. The
     * value represents a kind of tolerance to deviate from the best possible value to place the competitor on a
     * position, which he was on in the last group. The method starts at the best position and proofs if the competitor
     * on this position in the last group if yes return this position, if not go on with the next position. This is done
     * as often as the tolerance allows it. If no position is found the best position to change to is returned
     * regardless of the position in the last flight. This is necessary if you would like to minimize boat changes at
     * the expense of competitor allocations.
     * 
     * @param competitorAllocations
     *            array that contains the current competitor allocations for one group; index is the position (or boat)
     *            number
     * @param previousPosition
     *            position of the competitor that need to be changed in the previous group
     * @return position the competitor should be changed to
     */           
    private int getBestPositionToChangeTo(int[] competitorAllocations, int previousPosition) {
        int[] temp = new int[competitorAllocations.length];
        System.arraycopy(competitorAllocations, 0, temp, 0, competitorAllocations.length);
        Arrays.sort(temp);
        // search for best position to change to with consideration of previous position
        for (int i = 0; i < boatChangeFactor; i++) {
            for (int position = 0; position < competitorAllocations.length; position++) {
                if (temp[i] == competitorAllocations[position] && position == previousPosition) {
                    return position;
                }
            }
        }
        // search for best position to change to regardless of previous position
        for (int position = 0; position < competitorAllocations.length; position++) {
            if (temp[0] == competitorAllocations[position]) {
                return position;
            }
        }
        return -1;
    }

    /**
     * Returns a random number between min and max.
     * 
     * @param min
     *            lower bound
     * @param max
     *            upper bound
     * @return a random number
     */
    private int getRandomIntegerBetweenZeroAndMax(int max) {
        return random.nextInt(max + 1);
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

    /**
     * Opposite to <code>incremetAssociations</code>. This method reduces the matrix <code>associtaions</code> by the
     * given pairinglist/flight.
     * 
     * @param pairingList
     *            pairinglist/part of pairinglist/flight which should be removed from associations
     * @param associations
     *            current associations which should be modified inside this method
     * @return the modified associations matrix
     */
    public int[][] decrementAssociations(int[][] pairingList, int[][] associations) {
        for (int[] group : pairingList) {
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
     * Calculates the standard deviation from all numbers in association matrix. The method ignores all numbers smaller
     * 0, because senseless values are marked with -1. The return value is used to compare the quality of different
     * pairing list templates.
     * 
     * @param associations
     *            association describes a 2 dimensional array of integers, which contains the information about how
     *            often the teams play against each other
     * @return standardDev returns how much the association values deviate from each other
     */
    protected double calcStandardDev(int[][] associations) {
        double standardDev = 0;
        int k = associations[0][0], // first value of association array
                n = 0, // count of elements in association array
                exp = 0, exp2 = 0;
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

    /**
     * This Method replaces the given number of dummy competitors with -1, so they are not longer valid and could not be
     * read on client side.
     * 
     * @param bestPLT
     *            actual pairinglist, which should be modified
     * @return the modified pairinglist
     */
    private int[][] resetDummies(int[][] bestPLT, int competitorCount) {
        if (dummies > 0) {
            int[] dummyCompetitors = new int[dummies];
            for (int i = 0; i < dummies; i++) {
                dummyCompetitors[i] = (competitorCount - 1) - i;
            }
            for (int x = 0; x < bestPLT.length; x++) {
                for (int y = 0; y < bestPLT[0].length; y++) {
                    for (int i : dummyCompetitors) {
                        if (bestPLT[x][y] == i) {
                            bestPLT[x][y] = -1;
                        }
                    }
                }
            }
        }
        return bestPLT;
    }

    protected int getBoatChangesFromPairingList(int[][] pairinglist, int flightcount, int groupcount, int competitorcount) {
        int sumOfBoatchanges = 0;
        for (int groupindex = groupcount - 1; groupindex < flightcount * groupcount; groupindex += groupcount) {
            if (groupindex + 1 < pairinglist.length) {
                sumOfBoatchanges += ((competitorcount / groupcount)
                        - this.getMatches(pairinglist[groupindex], pairinglist[groupindex + 1]));
            }
        }
        return sumOfBoatchanges;
    }
}