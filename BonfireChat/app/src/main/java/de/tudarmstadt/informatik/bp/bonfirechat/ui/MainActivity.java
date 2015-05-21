package de.tudarmstadt.informatik.bp.bonfirechat.ui;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;

import java.util.ArrayList;

import de.tudarmstadt.informatik.bp.bonfirechat.R;
import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireData;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Identity;
import de.tudarmstadt.informatik.bp.bonfirechat.network.ClientServerProtocol;
import de.tudarmstadt.informatik.bp.bonfirechat.network.ConnectionManager;


public class MainActivity extends ActionBarActivity
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
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        initializeNetwork();
    }

    private void initializeNetwork() {
        BonfireData db = BonfireData.getInstance(this);
        if (db.getDefaultIdentity() == null) {
            Identity id = Identity.generate();
            db.createIdentity(id);
        }

        Intent intent = new Intent(this, ConnectionManager.class);
        intent.setAction(ConnectionManager.GO_ONLINE_ACTION);
        this.startService(intent);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragments.get(position))
                .commit();
    }

    public void onSectionAttached(String id) {
        switch (id) {
            case "conversations":
                mTitle = getString(R.string.title_conversations);
                break;
            case "contacts":
                mTitle = getString(R.string.title_contacts);
                break;
            case "settings":
                mTitle = getString(R.string.title_settings);
                break;
        }
    }

    public void onPageSelected(int position) {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }
}
