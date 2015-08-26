package de.tudarmstadt.informatik.bp.bonfirechat.ui;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.app.ActionBar;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.PointTarget;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import java.util.ArrayList;

import de.tudarmstadt.informatik.bp.bonfirechat.R;
import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireData;
import de.tudarmstadt.informatik.bp.bonfirechat.data.ConstOptions;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.UIHelper;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.zxing.IntentIntegrator;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.zxing.IntentResult;
import de.tudarmstadt.informatik.bp.bonfirechat.location.GpsTracker;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Identity;
import de.tudarmstadt.informatik.bp.bonfirechat.network.ConnectionManager;
import de.tudarmstadt.informatik.bp.bonfirechat.network.gcm.GcmBroadcastReceiver;
import de.tudarmstadt.informatik.bp.bonfirechat.stats.CurrentStats;
import de.tudarmstadt.informatik.bp.bonfirechat.stats.StatsCollector;


public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, View.OnClickListener {

    private static final String TAG = "MainActivity";

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    private CharSequence mTitle;

    private ArrayList<Fragment> fragments;
    private int currentFragment;

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
        GpsTracker.init(this);
        initializeNetwork();
        initializeStats();

        if (UIHelper.shouldShowOobe(this)) {
            showcaseView = new ShowcaseView.Builder(this)
                    .setStyle(R.style.CustomShowcaseTheme2)
                    .setOnClickListener(this)
                    .build();
            onClick(null);
        }
    }


    // ####### First-Start Tutorial #####################################################
    private ShowcaseView showcaseView;
    private int tutorial_counter = 0;
    /**
     * Handles clicks on Close button of first-start tutorial view
     */
    @Override
    public void onClick(View v) {
        switch(tutorial_counter++) {
            case 0:
                showcaseView.setContentTitle("Willkommen!");
                showcaseView.setContentText("Hier kannst du Kontakte hinzufügen und Konversationen starten");
                showcaseView.setTarget(new PointTarget(100, 420));
                showcaseView.setButtonText("Weiter");
                break;
            case 1:
                showcaseView.setContentTitle("Deine Kontaktdaten");
                showcaseView.setContentText("Hier klicken, um deine Kontaktdaten weiterzugeben");
                showcaseView.setTarget(new ViewTarget(R.id.btnShareIdentityQR, this));
                showcaseView.setButtonText("Los geht's");
                break;
            case 2:
                showcaseView.hide();
                break;
        }
    }

    // ####### End First-Start Tutorial #####################################################


    private void initializeNetwork() {
        BonfireData db = BonfireData.getInstance(this);
        Identity id = db.getDefaultIdentity();
        if (id == null) {
            id = Identity.generate(this);
            db.createIdentity(id);
        }
        if (id.nickname.equals("")) {
            Intent intent = new Intent(this, IdentityActivity.class);
            intent.putExtra("isWelcomeScreen", "yes");
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(this, ConnectionManager.class);
            intent.setAction(ConnectionManager.GO_ONLINE_ACTION);
            this.startService(intent);
        }
    }

    private void initializeStats() {
        BonfireData db = BonfireData.getInstance(this);
        CurrentStats.setInstance(db.getLatestStatsEntry());

        Intent statsPublishIntent = new Intent(StatsCollector.PUBLISH_STATS_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, statsPublishIntent, 0);

        AlarmManager manager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        long interval = StatsCollector.PUBLISH_INTERVAL;

        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
        Log.i(TAG, "Periodic stats upload interval alarm set.");

        registerReceiver(new StatsCollector(this), new IntentFilter(StatsCollector.PUBLISH_STATS_ACTION));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragments.get(position))
                .commit();
        currentFragment = position;
        switch (position) {
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
        actionBar.setTitle(mTitle);
    }

    public void navigateFragment(int position) {
        mNavigationDrawerFragment.selectItem(position);
    }

    @Override
    public void onBackPressed() {
        if (currentFragment != 0) {
            navigateFragment(0);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * handle results from qr code scanning
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            Intent ii = new Intent(this, ContactDetailsActivity.class);
            ii.setAction(Intent.ACTION_VIEW);
            ii.setData(Uri.parse(result.getContents()));
            startActivity(ii);
        }
    }

}
