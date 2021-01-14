package com.sap.sse.landscape;

import com.jcraft.jsch.ChannelSftp;

/**
 * Fields acceptable as user data, either for launching an AWS instance with these user data which then get appended to
 * an {@code env.sh} file by the {@code sailing} startup / init script, or for appending to an {@code env.sh} file
 * programmatically, e.g., through a {@link ChannelSftp}.
 * <p>
 * 
 * The enum literals correspond in spelling and capitalization precisely what the {@code start} script that launches a
 * server process expects to find as environment variables. Therefore, the {@link Enum#name()} method can be used to
 * produce those variable names.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface ProcessConfigurationVariable {
    String name();
}
