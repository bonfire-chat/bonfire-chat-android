package de.tudarmstadt.informatik.bp.bonfirechat.data.test;

import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;
import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireAPI;

/**
 * Created by jonas on 30.07.15.
 */
public class BonfireAPITest {

    @Test
    public void testEncode(){
        byte []result = {97, 98, 99, 100, 101, 102, 103};
        assertArrayEquals(result, BonfireAPI.encode("abcdefg"));
    }
}
