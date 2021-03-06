package de.tudarmstadt.informatik.bp.bonfirechat.helper.test;

import org.junit.Before;
import org.junit.Test;;
import static org.junit.Assert.*;

import de.tudarmstadt.informatik.bp.bonfirechat.helper.StringHelper;

/**
 * Created by jonas on 17.09.15.
 */
public class StringHelperTest {

    private static String pattern;

    /**
     * initializes patters for testing
     */
    @Before
    public void initTests(){
        pattern = "BonfireChat ist cool.*";
    }

    /**
     * tests correct matching of multiple regular expressions
     * <br><br>
     * One non-matching string should result in returning false
     */
    @Test
    public void testRegexMatch(){
        assertTrue(StringHelper.regexMatch(pattern, "BonfireChat ist cool", "BonfireChat ist cool!!!!!"));
        assertFalse(StringHelper.regexMatch(pattern, "BonfireChat ist cool", "BonfireChat ist nicht cool!!!!!"));
    }
}
