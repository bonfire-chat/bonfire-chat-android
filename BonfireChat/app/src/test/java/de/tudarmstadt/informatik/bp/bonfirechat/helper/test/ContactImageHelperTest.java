package de.tudarmstadt.informatik.bp.bonfirechat.helper.test;

import android.widget.ImageView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import de.tudarmstadt.informatik.bp.bonfirechat.R;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.ContactImageHelper;

import static org.mockito.Mockito.verify;

/**
 * Created by mw on 21.09.15.
 */
@RunWith(MockitoJUnitRunner.class)
public class ContactImageHelperTest {

    /**
     * Tests that the generic Bonfire icon is displayed as contact image for a contact which
     * has no contact image path set.
     * <br><br>
     * This uses a mocked version of ImageView.
     * @throws Exception
     */
    @Test
    public void testDisplayContactImage() throws Exception {
        ImageView iviewMock = Mockito.mock(ImageView.class);
        ContactImageHelper.displayContactImage("", iviewMock);
        verify(iviewMock).setImageResource(R.mipmap.ic_launcher);


    }
}
