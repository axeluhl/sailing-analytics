package com.sap.sse.util;

import java.util.regex.Pattern;

/**
 * Helps identifying IPv4 and IPv6 address literals, using regular expressions.
 * <p>
 * 
 * This can be helpful when trying to tell whether a host address is provided as
 * a symbolic hostname that requires DNS resolution, or as an address literal that
 * can directly be used.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class IPAddressUtil {
    private static final Pattern ipv4Pattern = Pattern.compile("[1-9][0-9]{0,2}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}");
    private static final Pattern ipv6Pattern = Pattern.compile("(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))");
    
    public static boolean isIPv4AddressLiteral(String address) {
        return ipv4Pattern.matcher(address).matches();
    }

    public static boolean isIPv6AddressLiteral(String address) {
        return ipv6Pattern.matcher(address).matches();
    }
    
    /**
     * Tells if {@code address} is an IPv4 or IPv6 address literal.
     */
    public static boolean isIPAddressLiteral(String address) {
        return isIPv4AddressLiteral(address) || isIPv6AddressLiteral(address);
    }
}
