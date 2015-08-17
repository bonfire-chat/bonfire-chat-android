package de.tudarmstadt.informatik.bp.bonfirechat.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.tudarmstadt.informatik.bp.bonfirechat.R;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.DateHelper;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.TracerouteHopSegment;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.TracerouteSegment;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Contact;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Message;

/**
 * Created by johannes on 17.08.15.
 */
public class TracerouteSegmentAdapter extends ArrayAdapter<TracerouteSegment> {

    public TracerouteSegmentAdapter(Context context, List<TracerouteSegment> objects) {
        super(context, R.layout.traceroute_rowlayout_hop, objects);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position) instanceof TracerouteHopSegment ? 1 : 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (getItem(position) instanceof Contact) {
            convertView = inflater.inflate(R.layout.contacts_layout, parent, false);
            Contact contact = (Contact) getItem(position);

            ((TextView) convertView.findViewById(R.id.name)).setText(contact.getNickname());
            ((ImageView) convertView.findViewById(R.id.icon)).setImageResource(R.mipmap.ic_launcher);
        }
        else {
            convertView = inflater.inflate(R.layout.traceroute_rowlayout_hop, parent, false);
            TracerouteHopSegment hop = (TracerouteHopSegment) getItem(position);

            ((TextView) convertView.findViewById(R.id.protocol)).setText("über Bluetooth, " + hop.getTimeDelta());
        }

        return convertView;
    }
}
