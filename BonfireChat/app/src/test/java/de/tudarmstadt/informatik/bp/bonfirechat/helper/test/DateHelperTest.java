package de.tudarmstadt.informatik.bp.bonfirechat.helper.test;

import org.junit.Test;
import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.Date;

import de.tudarmstadt.informatik.bp.bonfirechat.helper.DateHelper;

/**
 * Created by mw on 12.07.15.
 */
public class DateHelperTest {

    /**
     * tests if formatTime and formatDateTime format times correctly
     * <br><br>
     * formatTime should format in the form hh:mm:ss
     * formatDateTime should format in the form yyyy-MM-dd hh:mm:ss
     */
    @Test
    public void testFormat() {
        Date date1 = new Date(114,11,24,23,42,01);
        assertEquals(DateHelper.formatTime(date1), "23:42:01");
        assertEquals(DateHelper.formatDateTime(date1), "2014-12-24 23:42:01");
    }

    /**
     * tests if parseDateTime parses Dates correctly
     * <br><br>
     * Dates in the form yyyy-MM-dd hh:mm:ss should be parsed correctly
     * Invalid Dates should be recognized and not be parsed
     */
    @Test
    public void testParse() {
        try {
            assertTrue(DateHelper.parseDateTime("2014-12-24 23:42:01").equals(
                    new Date(114, 11, 24, 23, 42, 01)));
        } catch(ParseException ex) {
            fail("Threw parseexception");
        }
        try {
            System.out.println(DateHelper.parseDateTime("24.12.2014 00:00:00"));
            fail("Accepted invalid date!");
        } catch(ParseException ex) {
        }
    }

}
