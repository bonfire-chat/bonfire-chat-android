package de.tudarmstadt.informatik.bp.bonfirechat.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.app.ActionBar;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;

import java.util.ArrayList;

import de.tudarmstadt.informatik.bp.bonfirechat.R;
import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireData;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.zxing.IntentIntegrator;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.zxing.IntentResult;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Identity;
import de.tudarmstadt.informatik.bp.bonfirechat.network.ConnectionManager;
import de.tudarmstadt.informatik.bp.bonfirechat.network.gcm.GcmBroadcastReceiver;


public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    private CharSequence mTitle;

    private ArrayList<Fragment> fragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fragments = new ArrayList<>();
        fragments.add(new ConversationsFragment());
        fragments.add(new ContactsFragment());
        fragments.add(new SettingsFragment());

        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        GcmBroadcastReceiver.registerForGcm(this);
        initializeNetwork();
    }

    private void initializeNetwork() {
        BonfireData db = BonfireData.getInstance(this);
        Identity id = db.getDefaultIdentity();
        if (id == null) {
            id = Identity.generate(this);
            db.createIdentity(id);
        }
        if (id.nickname.equals("")) {
            Intent intent = new Intent(this, IdentityActivity.class);
            startActivity(intent);
        }

        Intent intent = new Intent(this, ConnectionManager.class);
        intent.setAction(ConnectionManager.GO_ONLINE_ACTION);
        this.startService(intent);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragments.get(position))
                .commit();
        onSectionAttached(position);
    }

    public void onSectionAttached(int id) {
        switch (id) {
            case 0:
                mTitle = getString(R.string.title_conversations);
                break;
            case 1:
                mTitle = getString(R.string.title_contacts);
                break;
            case 2:
                mTitle = getString(R.string.title_settings);
                break;
        }
        ActionBar actionBar = getActionBar();
        //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        //actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            Intent ii = new Intent(this, ContactDetailsActivity.class);
            ii.setAction(Intent.ACTION_VIEW);
            ii.setData(Uri.parse(result.getContents()));
            startActivity(ii);
        }
    }
}
