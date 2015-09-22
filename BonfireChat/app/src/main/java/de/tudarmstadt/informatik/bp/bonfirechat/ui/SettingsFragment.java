package de.tudarmstadt.informatik.bp.bonfirechat.ui;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.tudarmstadt.informatik.bp.bonfirechat.R;
import de.tudarmstadt.informatik.bp.bonfirechat.data.ConstOptions;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.UIHelper;
import de.tudarmstadt.informatik.bp.bonfirechat.network.BluetoothProtocol;
import de.tudarmstadt.informatik.bp.bonfirechat.network.ConnectionManager;
import de.tudarmstadt.informatik.bp.bonfirechat.network.GcmProtocol;

/**
 * settings list
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        PackageInfo pInfo = null;
        try {
            pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            findPreference("version_number").setTitle("Version " + pInfo.versionName);
            findPreference("version_number").setSummary(pInfo.packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        findPreference("edit_account").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), IdentityActivity.class));
                return true;
            }
        });

        findPreference("goto_homepage").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://bonfire.projects.teamwiki.net/?src=app_prefs"));
                startActivity(browserIntent);
                return true;
            }
        });

        findPreference("update_dev").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        String url = "https://ci.projects.teamwiki.net/job/BonfireChat/lastSuccessfulBuild/"
                        + "artifact/BonfireChat/app/build/outputs/apk/app-debug.apk";
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription("Update wird geladen...");
        request.setTitle("BonfireChat");
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Bonfirechat-dev.apk");

        // get download service and enqueue file
        DownloadManager manager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
        return true;
      }
    });

        findPreference("debugShow").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String debug = ConstOptions.getDebugInfo();
                Log.d("DEBUG", debug);
                UIHelper.info(getActivity(), "Debug", debug);
                return true;
            }
        });

        if (!BluetoothProtocol.isSupported()) {
            CheckBoxPreference p = (CheckBoxPreference) findPreference("enable_BluetoothProtocol");
            p.setEnabled(false);
            p.setChecked(false);
        }
        if (!GcmProtocol.isSupported(getActivity())) {
            CheckBoxPreference p = (CheckBoxPreference) findPreference("enable_GcmProtocol");
            p.setEnabled(false);
            p.setChecked(false);
        }

        initProtocolCheckbox("BluetoothProtocol");
        initProtocolCheckbox("WifiProtocol");
        initProtocolCheckbox("GcmProtocol");

    }

    private void initProtocolCheckbox(String name) {
        CheckBoxPreference p = (CheckBoxPreference) findPreference("enable_" + name);
        p.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Intent intent = new Intent(getActivity(), ConnectionManager.class);
                intent.setAction(ConnectionManager.GO_ONLINE_ACTION);
                getActivity().startService(intent);
                return true;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }
}
