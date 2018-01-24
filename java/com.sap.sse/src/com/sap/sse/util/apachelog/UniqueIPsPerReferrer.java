package com.sap.sse.util.apachelog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * From a set of Apache log files, a set of already visited files and the pre-existing results gathered from the files
 * already visited, computes an updated version of the stats that tell which hostname received how many unique visitors
 * per month and per year. This is intended to replace Bash script that has horrible performance. We're hoping to speed
 * things up considerably by using Java for this task.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class UniqueIPsPerReferrer {
    private static final Logger logger = Logger.getLogger(UniqueIPsPerReferrer.class.getName());
    private static final String HOSTNAME_FILE_EXTENSION = ".ips";
    private static final String UNIQUE_SUFFIX = ".unique";
    private final File CACHE;
    private final File VISITED_FILES;
    private final File STATS;
    private final File OUTPUT;
    private final File MONTHS;
    private final File YEARS;
    private final File TOTALS;
    private final File EVENTTOTALS;

    public UniqueIPsPerReferrer() {
        this("/var/log/old/cache/unique-ips-per-referrer/test");
    }
    
    public UniqueIPsPerReferrer(final String cacheDir) {
        CACHE = new File(cacheDir);
        VISITED_FILES = new File(CACHE + "/visited");
        STATS = new File(CACHE + "/stats");
        OUTPUT = new File(STATS + "/results");
        MONTHS = new File(OUTPUT + "/permonth");
        YEARS = new File(OUTPUT + "/peryear");
        TOTALS = new File(OUTPUT + "/totals");
        EVENTTOTALS = new File(OUTPUT + "/eventtotals");
        ensureDirectoriesExist();
    }
    
    /**
     * The first argument is the base directory for where to place the outputs, used for {@link #CACHE}.
     * All subsequent arguments are considered to be file names of logfiles that are supposed to be
     * analyzed.
     */
    public static void main(String[] args) throws IOException, ParseException {
        final TimePoint started = MillisecondsTimePoint.now();
        final String[] logfilenames = new String[args.length-1];
        System.arraycopy(args, 1, logfilenames, 0, logfilenames.length);
        new UniqueIPsPerReferrer(args[0]).analyze(logfilenames);
        logger.info("Finished. Took "+started.until(MillisecondsTimePoint.now()));
    }
    
    public void analyze(String... filenames) throws IOException, ParseException {
        appendHitsToHostnameSpecificFiles(filenames);
        cleanUpOldResults();
        computePerMonthPerYearPerEventAndTotals();
        countUniqueEventOccurrencesPerMonthAndPerYearAndOverall();
        countTotalHits();
    }

    private void ensureDirectoriesExist() {
        CACHE.mkdirs();
        STATS.mkdirs();
        OUTPUT.mkdirs();
        MONTHS.mkdirs();
        YEARS.mkdirs();
    }
    
    private Set<String> getVisitedFileNames() throws IOException {
        final Set<String> result = new HashSet<>();
        if (VISITED_FILES.exists()) {
            final BufferedReader br = new BufferedReader(new FileReader(VISITED_FILES));
            String visitedFileName;
            while ((visitedFileName = br.readLine()) != null) {
                result.add(visitedFileName);
            }
            br.close();
        }
        return result;
    }
    
    private void appendHitsToHostnameSpecificFiles(String... filenames) throws IOException {
        final Set<String> visitedFileNames = getVisitedFileNames();
        for (final String filename : filenames) {
            logger.info("Analyzing log file "+filename);
            if (!visitedFileNames.contains(filename)) {
                appendHitsToHostnameSpecificFiles(new File(filename));
                visitedFileNames.add(filename);
                addVisitedFile(filename);
            }
        }
    }
    
    /**
     * Removes the interim files for per-month and per-year stats, as well as the interim
     * files for totals and per-event totals.
     */
    private void cleanUpOldResults() throws IOException {
        logger.info("Cleanign up old results");
        deleteDirectoryContentsRecursively(MONTHS.toPath(), "....-..");
        deleteDirectoryContentsRecursively(YEARS.toPath(), "....");
        EVENTTOTALS.delete();
        TOTALS.delete();
    }
    
    private void computePerMonthPerYearPerEventAndTotals() throws IOException, ParseException {
        final Map<String, FileWriter> monthFileWritersPerFileBaseName = new HashMap<>();
        final Map<String, FileWriter> yearFileWritersPerFileBaseName = new HashMap<>();
        final FileWriter eventTotalsWriter = new FileWriter(EVENTTOTALS);
        final FileWriter totalsWriter = new FileWriter(TOTALS);
        for (final File perHostnameFile : getAllHostnameFiles()) {
            final String hostname = perHostnameFile.getName().substring(0, perHostnameFile.getName().length()-HOSTNAME_FILE_EXTENSION.length());
            logger.info("Extracting per-month and per-year stats for hostname "+hostname);
            final BufferedReader r = new BufferedReader(new FileReader(perHostnameFile));
            try {
                String line;
                while ((line=r.readLine()) != null) {
                    final PerHostnameEntry entry = new PerHostnameEntry(line);
                    if (entry.getYear() != 0) {
                        final String monthFileBasename = ""+entry.getYear()+"_"+entry.getMonth();
                        final FileWriter monthFileWriter = getFileWriter(monthFileBasename, monthFileWritersPerFileBaseName, this::getMonthFile);
                        final String combinedLine = hostname+" "+line+"\n";
                        monthFileWriter.write(combinedLine);
                        final String yearFileBasename = ""+entry.getYear();
                        final FileWriter yearFileWriter = getFileWriter(yearFileBasename, yearFileWritersPerFileBaseName, this::getYearFile);
                        yearFileWriter.write(combinedLine);
                    }
                    eventTotalsWriter.write(hostname+" "+line+"\n");
                    totalsWriter.write(line+"\n");
                }
            } finally {
                r.close();
            }
        }
        for (final FileWriter monthFileWriter : monthFileWritersPerFileBaseName.values()) {
            monthFileWriter.close();
        }
        for (final FileWriter yearFileWriter : yearFileWritersPerFileBaseName.values()) {
            yearFileWriter.close();
        }
        eventTotalsWriter.close();
        totalsWriter.close();
    }
    
    private File getMonthFile(String monthFileBaseName) {
        return new File(MONTHS, monthFileBaseName);
    }
    
    private File getYearFile(String yearFileBaseName) {
        return new File(YEARS, yearFileBaseName);
    }

    private void countUniqueEventOccurrencesPerMonthAndPerYearAndOverall() throws IOException {
        logger.info("Counting unique visitors per host for per-month files");
        countUniqueVisitorsPerHostname(MONTHS.listFiles((dir, filename)->filename.matches("...._..")));
        logger.info("Counting unique visitors per host for per-year files");
        countUniqueVisitorsPerHostname(YEARS.listFiles((dir, filename)->filename.matches("....")));
        logger.info("Counting unique visitors per host for event totals");
        countUniqueVisitorsPerHostname(new File[] { EVENTTOTALS });
    }

    /**
     * For each file in {@code files} produces a ".unique" file next to it that counts the distinct unique visitors for
     * each hostname found in the first column of the respective file.
     * 
     * @param files
     *            files that contain lines of the form "yes2015.sapsailing.com 66.249.66.14 17/Dec/2017 Mozilla/5.0
     *            (compatible; Googlebot/2.1; +http://www.google.com/bot.html)" where the first element is the hostname
     *            and the rest is the "unique visitor" information consisting of the IP address, day and user agent
     *            string.
     */
    private void countUniqueVisitorsPerHostname(final File[] files) throws IOException {
        for (final File file : files) {
            final File uniqueFile = new File(file.getCanonicalFile()+UNIQUE_SUFFIX);
            final Set<String> uniqueLinesFromFile = readDistinctLines(file);
            final FileWriter uniqueFileWriter = new FileWriter(uniqueFile);
            final Map<String, Integer> hostnameCounts = new HashMap<>();
            for (final String uniqueLineFromMonthFile : uniqueLinesFromFile) {
                final String hostname = uniqueLineFromMonthFile.split(" ")[0];
                inc(hostnameCounts, hostname);
            }
            for (final Entry<String, Integer> e : hostnameCounts.entrySet()) {
                uniqueFileWriter.write(e.getKey()+" "+e.getValue()+"\n");
            }
            uniqueFileWriter.close();
        }
    }
    
    private void inc(Map<String, Integer> hostnameCountsForMonth, String hostname) {
        Integer count = hostnameCountsForMonth.get(hostname);
        count = count == null ? 1 : count+1;
        hostnameCountsForMonth.put(hostname, count);
    }

    private void countTotalHits() throws IOException {
        logger.info("Counting total number of unique visitors");
        final FileWriter totalsCountWriter = new FileWriter(new File(TOTALS.getCanonicalPath()+UNIQUE_SUFFIX));
        final Set<String> uniqueTotalLines = readDistinctLines(TOTALS);
        totalsCountWriter.write(uniqueTotalLines.size()+"\n");
        totalsCountWriter.close();
    }
    
    private Set<String> readDistinctLines(File fromFile) throws IOException {
        final Set<String> result = new HashSet<>();
        final BufferedReader br = new BufferedReader(new FileReader(fromFile));
        try {
            String line;
            while ((line = br.readLine()) != null) {
                result.add(line);
            }
        } finally {
            br.close();
        }
        return result;
    }

    private void deleteDirectoryContentsRecursively(final Path directory, String fileNameRegexpPattern) throws IOException {
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (fileNameRegexpPattern == null || file.getName(file.getNameCount()-1).toString().matches(fileNameRegexpPattern)) {
                    Files.delete(file);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (!dir.equals(directory)) {
                    Files.delete(dir);
                }
                return FileVisitResult.CONTINUE;
            }
         });
    }
    
    private void addVisitedFile(String filename) throws IOException {
        final FileWriter visitedFilesWriter = new FileWriter(VISITED_FILES, /* append */ true);
        visitedFilesWriter.write(filename+"\n");
        visitedFilesWriter.close();
    }

    private void appendHitsToHostnameSpecificFiles(File logfileWhichMayUseGzipCompression) throws IOException {
        final Reader reader;
        if (logfileWhichMayUseGzipCompression.getName().toLowerCase().endsWith(".gz")) {
            reader = new InputStreamReader(new GZIPInputStream(new FileInputStream(logfileWhichMayUseGzipCompression)));
        } else {
            reader = new FileReader(logfileWhichMayUseGzipCompression);
        }
        appendHitsToHostnameSpecificFiles(reader);
    }

    private void appendHitsToHostnameSpecificFiles(Reader logfileReader) throws IOException {
        final Map<String, FileWriter> fileWritersPerHostname = new HashMap<>();
        final BufferedReader br = new BufferedReader(logfileReader);
        String line;
        while ((line=br.readLine()) != null) {
            final LogEntry entry = new LogEntry(line);
            final String hostname = entry.getHostname();
            final FileWriter fileWriterForHostname = getFileWriter(hostname, fileWritersPerHostname, this::getHostnameSpecificFile);
            fileWriterForHostname.write(entry.getRequestorIpString()+" "+entry.getDateString()+" "+entry.getUserAgent()+"\n");
        }
        for (final FileWriter fw : fileWritersPerHostname.values()) {
            fw.close();
        }
    }
    
    private FileWriter getFileWriter(String key, Map<String, FileWriter> fileWriterCache, Function<String, File> fileConstructor) throws IOException {
        final FileWriter fileWriterForHostname;
        if (fileWriterCache.containsKey(key)) {
            fileWriterForHostname = fileWriterCache.get(key);
        } else {
            fileWriterForHostname = new FileWriter(fileConstructor.apply(key), /* append */ true);
            fileWriterCache.put(key, fileWriterForHostname);
        }
        return fileWriterForHostname;
    }

    private File getHostnameSpecificFile(String hostname) {
        return new File(STATS, hostname+HOSTNAME_FILE_EXTENSION);
    }
    
    private File[] getAllHostnameFiles() {
        return STATS.listFiles((dir, filename)->filename.endsWith(HOSTNAME_FILE_EXTENSION));
    }
}
