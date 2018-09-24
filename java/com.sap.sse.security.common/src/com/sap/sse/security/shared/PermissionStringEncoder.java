package com.sap.sse.security.shared;

/**
 * Permission strings follow a specific format. For example, a {@link WildcardPermission} will trim its parts
 * and separates parts by a separator token, and sub-parts by another sub-part separator token. In order to
 * encode an arbitrary {@link String} as as part of a permission string, encoding needs to take place, where
 * occurrences of the separator tokens as well as occurrences of leading and trailing whitespace need to be
 * escaped. This process needs to work both ways because the object ID part of a permission string needs to
 * be used as lookup key, e.g., for the access control store.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface PermissionStringEncoder<PermissionType> {
    /**
     * Encodes the string {@code s} such that it can safely be put into a part of a permission of type
     * {@code PermissionType} from where it can be retrieved without modification and when passed to
     * {@link #decodePermissionPart(String)} will produce a string that equals {@code s} again.
     */
    String encodeAsPermissionPart(String s);

    /**
     * Decodes a string that was previously encoded by {@link #encodeAsPermissionPart(String)}, returning
     * a string that equals that passed as argument to {@link #encodeAsPermissionPart(String)} earlier.
     * Put formally, {@code this.decodePermissionPart(this.encodeAsPermissionPart(s)).equals(s)}
     */
    String decodePermissionPart(String permissionPart);
    
    /**
     * Encodes a list of {@link String}s using {@link #encodeAsPermissionPart(String)} and concatenates them using
     * a separator character that is not legal in any permission string of the {@code PermissionType}. Then, the concatenated
     * result is again encoded using {@link #encodeAsPermissionPart(String)}.
     * 
     * @param strings must not be {@code null}
     * 
     * @see #decodeStrings(String)
     */
    String encodeStringList(String... strings);

    /**
     * Decodes a list of {@link String}s that were previously encoded using {@link #encodeStringList(String...)}.
     * 
     * @return a non-{@code null} array which is empty if and only if {@code stringEncodedWithEncodeStringList} is empty
     */
    String[] decodeStrings(String stringEncodedWithEncodeStringList);
}
