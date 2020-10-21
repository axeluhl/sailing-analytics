package com.sap.sse.landscape.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.NamedImpl;
import com.sap.sse.landscape.Release;
import com.sap.sse.landscape.ReleaseRepository;

public class ReleaseImpl extends NamedImpl implements Release {
    private static final Logger logger = Logger.getLogger(ReleaseImpl.class.getName());
    private static final long serialVersionUID = -225240683033821028L;

    private final ReleaseRepository repository;
    
    public ReleaseImpl(String name, ReleaseRepository repository) {
        super(name);
        this.repository = repository;
    }

    @Override
    public ReleaseRepository getRepository() {
        return repository;
    }

    @Override
    public String getBaseName() {
        return getName().substring(0, getName().lastIndexOf("-"));
    }

    @Override
    public TimePoint getCreationDate() {
        final String dateSubstring = getName().substring(getName().lastIndexOf("-")+1);
        try {
            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmm");
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            return TimePoint.of(simpleDateFormat.parse(dateSubstring));
        } catch (ParseException e) {
            logger.log(Level.WARNING, "Error parsing release date "+dateSubstring+". Returning null instead.", e);
            return null;
        }
    }
}
