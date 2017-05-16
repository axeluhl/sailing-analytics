

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

/**
 * A class whose {@link #main(String[])} method can be used to convert a
 * .properties file written in the conventions as expected by
 * {@link ResourceBundleStringMessageImpl} to a plain text file. Duplicated
 * single quotes will be un-escaped into one single quote; text quoted by single
 * quotes is output as is, without the enclosing single quotes. Unquoted
 * placeholders are output as specified in {@link #createPlaceholder(int)} (the
 * current implementation only outputs the placeholder in .properties file
 * syntax, leaving it unchanged and indistinguishable from a quoted
 * placeholder).
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class PropertiesUnquoterTest {
	public static void main(String[] args) throws Exception {
		PropertiesUnquoterTest unquoter = new PropertiesUnquoterTest();
		BufferedReader br = new BufferedReader(new FileReader(args[0]));
		String line;
		while ((line=br.readLine()) != null) {
			System.out.println(unquoter.unquote(line));
		}
		br.close();
	}
	
	@Test
	public void testMessageUnquoting() {
		assertEquals("'Humba'", unquote("''Humba''"));
		assertEquals("{0}", unquote("'{0}'"));
		assertEquals("{0}", unquote("{0}"));
		assertEquals("Humba", unquote("Hum'ba'"));
	}
	
    private String unquote(String message) {
        final StringBuilder result = new StringBuilder();
        boolean withinQuotedArea = false;
        for (int i=0; i<message.length(); i++) {
        	if (isSingleQuote(message, i) || (withinQuotedArea && message.charAt(i) == '\'')) {
        		withinQuotedArea = !withinQuotedArea;
        	} else if (isDoubleQuote(message, i)) {
        		result.append('\''); // an escaped single quote
        		i++; // skip the second one
        	} else {
        		if (withinQuotedArea) {
        			result.append(message.charAt(i));
        		} else {
        			final int paramNumber = isParameterPlaceholder(message, i);
        			if (paramNumber != -1) {
        				result.append(createPlaceholder(paramNumber));
        				i += (""+paramNumber).length()+1; // skip the number plus one curly brace
        			} else {
        				result.append(message.charAt(i));
        			}
        		}
        	}
        }
        return result.toString();
    }

    private String createPlaceholder(int paramNumber) {
    	return "{"+paramNumber+"}";
    }
    
    private boolean isDoubleQuote(String message, int i) {
		return i < message.length()-1 && message.charAt(i) == '\'' && message.charAt(i+1) == '\'';
	}

	private static final Pattern placeholderMatcher = Pattern.compile("\\{([0-9]+)\\}.*$");
    /**
     * @return -1 if there is no placeholder starting at character {@code i} in {@code message},
     * or the number of the parameter represented by the placeholder, such as {@code 4} for the placeholder
     * 			<pre>{4}</pre>.
     */
	private int isParameterPlaceholder(String message, int i) {
		final Matcher matcher = placeholderMatcher.matcher(message.substring(i));
		final int result;
		if (matcher.matches()) {
			result = Integer.valueOf(matcher.group(1));
		} else {
			result = -1;
		}
		return result;
	}

	private boolean isSingleQuote(String message, int i) {
		return i<message.length() && message.charAt(i) == '\'' && (i==message.length()-1 || message.charAt(i+1) != '\'');
	}
}
