package com.sap.sse.landscape.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sap.sse.common.Util;
import com.sap.sse.landscape.Release;
import com.sap.sse.landscape.ReleaseRepository;
import com.sap.sse.util.HttpUrlConnectionHelper;

public class ReleaseRepositoryImpl implements ReleaseRepository {
    private static final Logger logger = Logger.getLogger(ReleaseRepositoryImpl.class.getName());
    private final String repositoryBase;
    
    private final String masterReleaseNamePrefix;
    
    public ReleaseRepositoryImpl(String repositoryBase, String masterReleaseNamePrefix) {
        super();
        this.repositoryBase = repositoryBase;
        this.masterReleaseNamePrefix = masterReleaseNamePrefix;
    }

    @Override
    public String getRepositoryBase() {
        return repositoryBase;
    }

    @Override
    public String getMasterReleaseNamePrefix() {
        return masterReleaseNamePrefix;
    }
    
    private Iterable<Release> getAvailableReleases() {
        final List<Release> result = new LinkedList<>();
        try {
            final URLConnection connection = HttpUrlConnectionHelper.redirectConnection(new URL(getRepositoryBase()));
            final InputStream index = (InputStream) connection.getContent();
            int read = 0;
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while ((read=index.read()) != -1) {
                bos.write(read);
            }
            index.close();
            final String contents = bos.toString();
            final Pattern pattern = Pattern.compile("<a href=\"(([^/]*)-([0-9]*))/\">([^/]*)-([0-9]*)/</a>");
            final Matcher m = pattern.matcher(contents);
            int lastMatch = 0;
            while (m.find(lastMatch)) {
                result.add(new ReleaseImpl(m.group(1), this));
                lastMatch = m.end();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error loading releases from repository at "+getRepositoryBase()+". Continuing empty.", e);
            return Collections.emptyList();
        }
        return result;
    }
    
    @Override
    public Iterator<Release> iterator() {
        return getAvailableReleases().iterator();
    }

    @Override
    public Release getLatestRelease(String releaseNamePrefix) {
        Release result = null;
        for (final Release release : getAvailableReleases()) {
            if (release.getBaseName().equals(releaseNamePrefix) &&
                    (result == null || release.getCreationDate().after(result.getCreationDate()))) {
                result = release;
            }
        }
        return result;
    }

    @Override
    public Release getRelease(String releaseName) {
        return Util.first(Util.filter(getAvailableReleases(), r->r.getName().equals(releaseName)));
    }

}
