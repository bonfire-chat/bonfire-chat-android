package de.tudarmstadt.informatik.bp.bonfirechat.ui;

import android.app.Activity;
import android.os.Bundle;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.PreferenceFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.tudarmstadt.informatik.bp.bonfirechat.R;

/**
 * settings list
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
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
        ((MainActivity) activity).onSectionAttached("settings");
    }
}