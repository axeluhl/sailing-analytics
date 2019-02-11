package com.sap.sse.util.apachelog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
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
import java.util.zip.GZIPOutputStream;

import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * From a set of Apache log files, a set of already visited files and the pre-existing results gathered from the files
 * already visited, computes an updated version of the stats that tell which hostname received how many unique visitors
 * per month and per year. This is intended to replace Bash script that has horrible performance. We're hoping to speed
 * things up considerably by using Java for this task.
 * <p>
 * 
 * To use from a command line, e.g., on a server, export the {@code com.sap.sse} project from Eclipse into a single JAR
 * using a launch configuration for this class's {@link #main(String[])} method and bundle all dependencies into that
 * JAR. Move the JAR file to your server environment and launch using
 * {@code java -jar com.sap.sse.jar <dir-name> logfile1 logfile2 ...}.
 * 
 * @see #main(String[])
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class UniqueIPsPerReferrer {
    private static final String GZIP_EXTENSION = ".gz";
    private static final Logger logger = Logger.getLogger(UniqueIPsPerReferrer.class.getName());
    private static final String HOSTNAME_FILE_EXTENSION = ".ips";
    private static final String UNIQUE_SUFFIX = ".unique";
    private static final String MONTH_AND_YEAR_TOTALS_SUFFIX = "_totals";
    private static final char YEAR_MONTH_SEPARATOR = '_';
    private final File CACHE;
    private final File VISITED_FILES;
    private final File STATS;
    private final File OUTPUT;
    private final File MONTHS;
    private final File YEARS;
    private final File TOTALS;
    private final File EVENTTOTALS;
    private final File TOTALS_UNIQUE;

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
        TOTALS = new File(OUTPUT + "/totals.gz");
        EVENTTOTALS = new File(OUTPUT + "/eventtotals.gz");
        TOTALS_UNIQUE = new File(OUTPUT + "/totals"+UNIQUE_SUFFIX);
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
            if (!containsIgnoringDifferenceInCompression(visitedFileNames, filename)) {
                logger.info("Analyzing log file "+filename);
                appendHitsToHostnameSpecificFiles(new File(filename));
                visitedFileNames.add(filename);
                addVisitedFile(filename);
            } else {
                logger.info("Not analyzing log file "+filename+" as it has already been analyzed before");
            }
        }
    }

    /**
     * Returns {@code true} if and only if {@code visitedFileNames} contains a filename that equals
     * {@code filename}, ignoring any ".gz" ending on either side.
     */
    private boolean containsIgnoringDifferenceInCompression(final Set<String> visitedFileNames, final String filename) {
        return filename.toLowerCase().endsWith(GZIP_EXTENSION) && (visitedFileNames.contains(filename) || visitedFileNames.contains(filename.substring(0, filename.length()-GZIP_EXTENSION.length())))
                || !filename.toLowerCase().endsWith(GZIP_EXTENSION) && (visitedFileNames.contains(filename) || visitedFileNames.contains(filename+GZIP_EXTENSION));
    }
    
    /**
     * Removes the interim files for per-month and per-year stats, as well as the interim
     * files for totals and per-event totals.
     */
    private void cleanUpOldResults() throws IOException {
        logger.info("Cleaning up old results");
        deleteDirectoryContentsRecursively(MONTHS.toPath(), "...."+YEAR_MONTH_SEPARATOR+"..\\.gz");
        deleteDirectoryContentsRecursively(YEARS.toPath(), "....\\.gz");
        EVENTTOTALS.delete();
        TOTALS.delete();
    }
    
    private void computePerMonthPerYearPerEventAndTotals() throws IOException, ParseException {
        final Map<String, Writer> monthFileWritersPerFileBaseName = new HashMap<>();
        final Map<String, Writer> yearFileWritersPerFileBaseName = new HashMap<>();
        final Map<String, Writer> monthTotalsFileWritersPerFileBaseName = new HashMap<>();
        final Map<String, Writer> yearTotalsFileWritersPerFileBaseName = new HashMap<>();
        final Writer eventTotalsWriter = new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(EVENTTOTALS)));
        final Writer totalsWriter = new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(TOTALS)));
        for (final File perHostnameFile : getAllHostnameFiles()) {
            final String hostname = perHostnameFile.getName().substring(0, perHostnameFile.getName().length()-HOSTNAME_FILE_EXTENSION.length());
            logger.info("Extracting per-month and per-year stats for hostname "+hostname);
            final BufferedReader r = new BufferedReader(new FileReader(perHostnameFile));
            try {
                String line;
                while ((line=r.readLine()) != null) {
                    final PerHostnameEntry entry = new PerHostnameEntry(line);
                    final String combinedLine = hostname+" "+line+"\n";
                    if (entry.getYear() != 0) {
                        final String monthFileBasename = String.format("%04d"+YEAR_MONTH_SEPARATOR+"%02d", entry.getYear(), entry.getZeroBasedMonth()+1);
                        final Writer monthFileWriter = getFileWriter(monthFileBasename, monthFileWritersPerFileBaseName, this::getMonthFileCompressed, /* gzipCompressed */ true, /* append */ false);
                        monthFileWriter.write(combinedLine);
                        final String monthTotalsFileBasename = monthFileBasename+MONTH_AND_YEAR_TOTALS_SUFFIX;
                        final Writer monthTotalsFileWriter = getFileWriter(monthTotalsFileBasename, monthTotalsFileWritersPerFileBaseName, this::getMonthFileCompressed, /* gzipCompressed */ true, /* append */ false);
                        monthTotalsFileWriter.write(line+"\n");
                        final String yearFileBasename = String.format("%04d", entry.getYear());
                        final Writer yearFileWriter = getFileWriter(yearFileBasename, yearFileWritersPerFileBaseName, this::getYearFileCompressed, /* gzipCompressed */ true, /* append */ false);
                        yearFileWriter.write(combinedLine);
                        final String yearTotalsFileBasename = yearFileBasename+MONTH_AND_YEAR_TOTALS_SUFFIX;
                        final Writer yearTotalsFileWriter = getFileWriter(yearTotalsFileBasename, yearTotalsFileWritersPerFileBaseName, this::getYearFileCompressed, /* gzipCompressed */ true, /* append */ false);
                        yearTotalsFileWriter.write(line+"\n");
                    }
                    eventTotalsWriter.write(combinedLine);
                    totalsWriter.write(line+"\n");
                }
            } finally {
                r.close();
            }
        }
        for (final Writer monthFileWriter : monthFileWritersPerFileBaseName.values()) {
            monthFileWriter.close();
        }
        for (final Writer yearFileWriter : yearFileWritersPerFileBaseName.values()) {
            yearFileWriter.close();
        }
        for (final Writer monthTotalsFileWriter : monthTotalsFileWritersPerFileBaseName.values()) {
            monthTotalsFileWriter.close();
        }
        for (final Writer yearTotalsFileWriter : yearTotalsFileWritersPerFileBaseName.values()) {
            yearTotalsFileWriter.close();
        }
        eventTotalsWriter.close();
        totalsWriter.close();
    }
    
    private File getMonthFileCompressed(String monthFileBaseName) {
        return new File(MONTHS, monthFileBaseName+GZIP_EXTENSION);
    }
    
    private File getYearFileCompressed(String yearFileBaseName) {
        return new File(YEARS, yearFileBaseName+GZIP_EXTENSION);
    }

    private void countUniqueEventOccurrencesPerMonthAndPerYearAndOverall() throws IOException {
        logger.info("Counting unique visitors per host for per-month files");
        countUniqueVisitorsPerHostname(MONTHS.listFiles((dir, filename)->filename.matches("...._..\\.gz")));
        logger.info("Counting unique visitors per host for per-year files");
        countUniqueVisitorsPerHostname(YEARS.listFiles((dir, filename)->filename.matches("....\\.gz")));
        logger.info("Counting unique visitors per host for event totals");
        countUniqueVisitorsPerHostname(new File[] { EVENTTOTALS });
    }

    /**
     * For each file in {@code files} produces a ".unique" file next to it that counts the distinct unique visitors for
     * each hostname found in the first column of the respective file.
     * 
     * @param gzippedFiles
     *            files that contain lines of the form "yes2015.sapsailing.com 66.249.66.14 17/Dec/2017 Mozilla/5.0
     *            (compatible; Googlebot/2.1; +http://www.google.com/bot.html)" where the first element is the hostname
     *            and the rest is the "unique visitor" information consisting of the IP address, day and user agent
     *            string.
     */
    private void countUniqueVisitorsPerHostname(final File[] gzippedFiles) throws IOException {
        for (final File gzippedFile : gzippedFiles) {
            final String canonicalPathOfGzippedFile = gzippedFile.getCanonicalPath();
            final String canonicalPathWithoutGzipExtension = canonicalPathOfGzippedFile.substring(0, canonicalPathOfGzippedFile.length()-GZIP_EXTENSION.length());
            final File uniqueFile = new File(canonicalPathWithoutGzipExtension+UNIQUE_SUFFIX);
            final Set<String> uniqueLinesFromFile = readDistinctLinesFromGzippedFile(gzippedFile);
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
        logger.info("Counting total numbers of unique visitors for all, months, and years");
        countUniqueTotals(TOTALS, TOTALS_UNIQUE);
        for (final File monthGzippedTotals : MONTHS.listFiles((dir, filename)->filename.endsWith(MONTH_AND_YEAR_TOTALS_SUFFIX+GZIP_EXTENSION))) {
            countUniqueTotals(monthGzippedTotals, new File(monthGzippedTotals.getParentFile(),
                    monthGzippedTotals.getName().substring(0, monthGzippedTotals.getName().length()-GZIP_EXTENSION.length())+UNIQUE_SUFFIX));
        }
        for (final File yearGzippedTotals : YEARS.listFiles((dir, filename)->filename.endsWith(MONTH_AND_YEAR_TOTALS_SUFFIX+GZIP_EXTENSION))) {
            countUniqueTotals(yearGzippedTotals, new File(yearGzippedTotals.getParentFile(),
                    yearGzippedTotals.getName().substring(0, yearGzippedTotals.getName().length()-GZIP_EXTENSION.length())+UNIQUE_SUFFIX));
        }
    }

    private void countUniqueTotals(File gzippedEntriesWithoutHostname, File uniqueCount) throws IOException {
        final FileWriter totalsCountWriter = new FileWriter(uniqueCount);
        final Set<String> uniqueTotalLines = readDistinctLinesFromGzippedFile(gzippedEntriesWithoutHostname);
        totalsCountWriter.write(uniqueTotalLines.size()+"\n");
        totalsCountWriter.close();
    }
    
    private Set<String> readDistinctLinesFromGzippedFile(File gzippedFile) throws IOException {
        final Set<String> result = new HashSet<>();
        final BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(gzippedFile))));
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
        if (logfileWhichMayUseGzipCompression.getName().toLowerCase().endsWith(GZIP_EXTENSION)) {
            reader = new InputStreamReader(new GZIPInputStream(new FileInputStream(logfileWhichMayUseGzipCompression)));
        } else {
            reader = new FileReader(logfileWhichMayUseGzipCompression);
        }
        appendHitsToHostnameSpecificFiles(reader);
    }

    private void appendHitsToHostnameSpecificFiles(Reader logfileReader) throws IOException {
        final Map<String, Writer> fileWritersPerHostname = new HashMap<>();
        final BufferedReader br = new BufferedReader(logfileReader);
        String line;
        while ((line=br.readLine()) != null) {
            final LogEntry entry = new LogEntry(line);
            final String hostname = entry.getHostname();
            final Writer fileWriterForHostname = getFileWriter(hostname, fileWritersPerHostname, this::getHostnameSpecificFile, /* gzipCompressed */ false, /* append */ true);
            fileWriterForHostname.write(entry.getRequestorIpString()+" "+entry.getDateString()+" "+entry.getUserAgent()+"\n");
        }
        for (final Writer fw : fileWritersPerHostname.values()) {
            fw.close();
        }
    }
    
    /**
     * @param append if the file is not found in {@code fileWriterCache}, this parameter controls whether the file is then opened for appending or not;
     * note that appending cannot be used in conjunction with GZIP compression.
     */
    private Writer getFileWriter(String key, Map<String, Writer> fileWriterCache, Function<String, File> fileConstructor, boolean gzipCompressed, boolean append) throws IOException {
        assert !gzipCompressed || !append;
        final Writer fileWriterForHostname;
        if (fileWriterCache.containsKey(key)) {
            fileWriterForHostname = fileWriterCache.get(key);
        } else {
            final File file = fileConstructor.apply(key);
            if (gzipCompressed) {
                fileWriterForHostname = new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(file)));
            } else {
                fileWriterForHostname = new FileWriter(file, append);
            }
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
