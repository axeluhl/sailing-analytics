package com.sap.sse.util;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sap.sse.common.Util;

public class JvmUtils {
    /**
     * Expects {@code memoryVariable} to be in the format {@code [0-9]+[mMgG]?}. If no {@code [mMgG]} unit
     * is specified, the size is taken as bytes and converted into the next larger number of megabytes.
     * A specification of "m" or "M" is simply stripped and the number used unchanged; a "g" or "G"
     * specification is used to multiply the number by {@code 1024} to get the number of megabytes.<p>
     * 
     * If the pattern is not matched, an empty optional is returned, otherwise the memory size in megabytes.
     */
    public static Optional<Integer> getMegabytesFromJvmSize(String memoryVariable) {
        final Matcher m = Pattern.compile("([0-9][0-9]*)([mMgG]?)").matcher(memoryVariable);
        final Integer result;
        if (m.matches()) {
            if (m.group(2).equalsIgnoreCase("m")) {
                result = Integer.valueOf(m.group(1));
            } else if (m.group(2).equalsIgnoreCase("g")) {
                result = Integer.valueOf(m.group(1))*1024;
            } else if (!Util.hasLength(m.group(2))) {
                final int mod = Integer.valueOf(m.group(1)) % (1024*1024);
                result = Integer.valueOf(m.group(1)) / 1024 / 1024 + (mod == 0 ? 0 : 1);
            } else {
                result = null;
            }
        } else {
            result = null;
        }
        return Optional.ofNullable(result);
    }
}
