package de.tudarmstadt.informatik.bp.bonfirechat.data.test;

import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;
import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireAPI;

/**
 * Created by jonas on 30.07.15.
 */
public class BonfireAPITest {

    /**
     * tests valid encoding of a String
     * <br><br>
     * String will be encoded and the resulting byte array matches the pre-computed
     * correct byte array.
     */
    @Test
    public void testEncode(){
        byte []result = {97, 98, 99, 100, 101, 102, 103};
        assertArrayEquals(result, BonfireAPI.encode("abcdefg"));
    }
}
