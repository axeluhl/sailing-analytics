package com.sap.sse.security.shared.impl;

import com.sap.sse.common.Util;
import com.sap.sse.security.shared.PermissionStringEncoder;
import com.sap.sse.security.shared.WildcardPermission;

/**
 * Leading and trailing whitespaces are encoded using the character sequence "_xx" where the "xx" is replaced by the two
 * hexadecimal digits representing the character's ASCII code. For example, "_20" represents a space character, and
 * "_09" represents a tab character. An underscore character is encoded using two underscores ("__"). The part separator
 * token is encoded using "_/", and the sub-part separator token is encoded using "_|".
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class WildcardPermissionEncoder implements PermissionStringEncoder<WildcardPermission> {
    private static final char ESCAPE_CHARACTER = '\\';
    private static final char PART_DIVIDER_ENCODED = '/';  
    private static final char SUBPART_DIVIDER_ENCODED = '|';
    
    @Override
    public String encodeAsPermissionPart(String s) {
        assert WildcardPermission.PART_DIVIDER_TOKEN.length() == 1;
        assert WildcardPermission.SUBPART_DIVIDER_TOKEN.length() == 1;
        final StringBuilder result = new StringBuilder();
        boolean inLeadingWhitespace = true;
        int firstTrailingWhitespace = s.length();
        for (int i=0; i<s.length(); i++) {
            final char c = s.charAt(i);
            if ((inLeadingWhitespace || i>=firstTrailingWhitespace) && Character.isWhitespace(c)) {
                result.append(encodeWhitespace(c));
            } else {
                if (inLeadingWhitespace) {
                    inLeadingWhitespace = false;
                    firstTrailingWhitespace = s.length()-(s.substring(i).length()-s.substring(i).trim().length());
                }
                if (c == WildcardPermission.PART_DIVIDER_TOKEN.charAt(0)) {
                    result.append(ESCAPE_CHARACTER);
                    result.append(PART_DIVIDER_ENCODED);
                } else if (c == WildcardPermission.SUBPART_DIVIDER_TOKEN.charAt(0)) {
                    result.append(ESCAPE_CHARACTER);
                    result.append(SUBPART_DIVIDER_ENCODED);
                } else if (c == ESCAPE_CHARACTER) {
                    result.append(ESCAPE_CHARACTER);
                    result.append(ESCAPE_CHARACTER);
                } else {
                    result.append(c);
                }
            }
        }
        return result.toString();
    }

    private String encodeWhitespace(char c) {
        return ESCAPE_CHARACTER+asTwoHexDigits(c);
    }

    private final static String hexDigits = "0123456789abcdef";
    private String asTwoHexDigits(char c) {
        int cAsInt = (int) c;
        return ""+hexDigits.charAt(cAsInt/16)+hexDigits.charAt(cAsInt%16);
    }

    @Override
    public String decodePermissionPart(String permissionPart) {
        final StringBuilder result = new StringBuilder();
        for (int i=0; i<permissionPart.length(); i++) {
            final char c = permissionPart.charAt(i);
            if (c == ESCAPE_CHARACTER) {
                assert permissionPart.length() > i+1;
                final char next = permissionPart.charAt(++i);
                switch (next) {
                case PART_DIVIDER_ENCODED:
                    result.append(WildcardPermission.PART_DIVIDER_TOKEN);
                    break;
                case SUBPART_DIVIDER_ENCODED:
                    result.append(WildcardPermission.SUBPART_DIVIDER_TOKEN);
                    break;
                case ESCAPE_CHARACTER:
                    result.append(ESCAPE_CHARACTER);
                    break;
                default:
                    assert permissionPart.length() > i+1;
                    // this has to be a two-digit hex encoding of a leading or trailing whitespace character:
                    result.append((char) Integer.parseInt(""+next+permissionPart.charAt(++i), /* radix */ 16));
                }
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    @Override
    public String encodeStringList(String... strings) {
        final String[] firstPassResult = new String[strings.length];
        for (int i=0; i<strings.length; i++) {
            firstPassResult[i] = encodeAsPermissionPart(strings[i]);
        }
        final String concatenatedFirstPassResult = Util.join(WildcardPermission.PART_DIVIDER_TOKEN, firstPassResult);
        return encodeAsPermissionPart(concatenatedFirstPassResult);
    }

    @Override
    public String[] decodeStringList(String stringEncodedWithEncodeStringList) {
        final String decodedPartList = decodePermissionPart(stringEncodedWithEncodeStringList);
        final String[] parts = decodedPartList.split(WildcardPermission.PART_DIVIDER_TOKEN);
        final String[] result = new String[parts.length];
        for (int i=0; i<result.length; i++) {
            result[i] = decodePermissionPart(parts[i]);
        }
        return result;
    }

}
