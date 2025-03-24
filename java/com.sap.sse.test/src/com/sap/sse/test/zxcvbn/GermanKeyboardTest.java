package com.sap.sse.test.zxcvbn;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Test;

import com.nulabinc.zxcvbn.StandardDictionaries;
import com.nulabinc.zxcvbn.StandardKeyboards;
import com.nulabinc.zxcvbn.Zxcvbn;
import com.nulabinc.zxcvbn.ZxcvbnBuilder;
import com.nulabinc.zxcvbn.matchers.Keyboard;
import com.nulabinc.zxcvbn.matchers.SlantedKeyboardLoader;

public class GermanKeyboardTest {
    private static final String QWERTZ = "^° 1! 2\" 3§ 4$ 5% 6& 7/ 8( 9) 0= ß? ´`\n"  +
                                         "    qQ wW eE rR tT zZ uU iI oO pP üÜ +*\n"  +
                                         "     aA sS dD fF gG hH jJ kK lL öÖ äÄ #'\n" +
                                         "      yY xX cC vV bB nN mM ,; .: -_" ;

    @Test
    public void testGermanKeyboard() throws IOException {
        final String DE_PASSWD1 = "klöä#";
        final String DE_PASSWD2 = "iopü+";
        final Keyboard germanKeyboard = new SlantedKeyboardLoader(DE_PASSWD1, ()->new ByteArrayInputStream(QWERTZ.getBytes())).load();
        final ZxcvbnBuilder builder = new ZxcvbnBuilder();
        builder.dictionaries(StandardDictionaries.loadAllDictionaries());
        builder.keyboards(StandardKeyboards.loadAllKeyboards());
        builder.keyboard(germanKeyboard);
        final Zxcvbn zxcvbnWithGermanKeyboard = builder.build();
        final Zxcvbn zxcvbn = new Zxcvbn();
        {
            final double defaultGuessesForQuertz = zxcvbn.measure(DE_PASSWD1).getGuesses();
            final double germanGuesseForQuertz = zxcvbnWithGermanKeyboard.measure(DE_PASSWD1).getGuesses();
            assertTrue(defaultGuessesForQuertz > germanGuesseForQuertz);
        }
        {
            final double defaultGuessesForQuertz = zxcvbn.measure(DE_PASSWD2).getGuesses();
            final double germanGuesseForQuertz = zxcvbnWithGermanKeyboard.measure(DE_PASSWD2).getGuesses();
            assertTrue(defaultGuessesForQuertz > germanGuesseForQuertz);
        }
    }
}