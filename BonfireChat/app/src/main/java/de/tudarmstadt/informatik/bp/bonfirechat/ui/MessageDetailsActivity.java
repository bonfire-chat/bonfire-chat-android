package de.tudarmstadt.informatik.bp.bonfirechat.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.tudarmstadt.informatik.bp.bonfirechat.R;
import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireAPI;
import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireData;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.DateHelper;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.TracerouteHopSegment;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.TracerouteSegment;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Contact;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Message;
import de.tudarmstadt.informatik.bp.bonfirechat.network.ConnectionManager;

/**
 * Created by johannes on 16.08.15.
 */
public class MessageDetailsActivity extends Activity {

    private static final String TAG = "MessageDetailsActivity";
    private final BonfireData db = BonfireData.getInstance(this);
    private Message message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_message_details);
        UUID msgUuid = (UUID) getIntent().getSerializableExtra(ConnectionManager.EXTENDED_DATA_MESSAGE_UUID);
        message = db.getMessageByUUID(msgUuid);
        if (message == null) {
            Log.e(TAG, "Error, message with id " + msgUuid + " not found");
        }
        getActionBar().setTitle(R.string.message_details);

        // show basic, locally available data
        ViewStub messageStub = (ViewStub) findViewById(R.id.message_stub);
        inflateMessageView(messageStub);
        inflateContactsView();
        inflateMessageDatails();

        // asynchronously load traceroute data and then populate list
        (new AsyncTask<Void, Void, List<TracerouteSegment>>() {
            @Override
            protected List<TracerouteSegment> doInBackground(Void... voids) {
                return BonfireAPI.getTraceroute(message.uuid);
            }

            @Override
            protected void onPostExecute(List<TracerouteSegment> traceroute) {
                inflateTraceroute(traceroute);
            }
        }).execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // makes sure parameters like conversation id are present in parent activity
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class ViewHolder {
        ImageView encryptedIcon, protocolIcon, ackIcon;
        TextView messageBody, dateTime;
    }

    private void inflateMessageView(ViewStub stub) {
        ViewHolder v = new ViewHolder();

        switch (message.direction()) {
            case Received:
                stub.setLayoutResource(R.layout.message_rowlayout_received);
                break;
            case Sent:
            default:
                stub.setLayoutResource(R.layout.message_rowlayout_sent);
                v.ackIcon = (ImageView) findViewById(R.id.message_ack);
                break;
        }

        View view = stub.inflate();

        //view.findViewById(R.id.message_photo).setVisibility(View.GONE);
        v.messageBody = (TextView) view.findViewById(R.id.message_body);
        v.dateTime = (TextView) view.findViewById(R.id.message_time);
        v.encryptedIcon = (ImageView) view.findViewById(R.id.message_encrypted);
        v.protocolIcon = (ImageView) view.findViewById(R.id.message_proto);
        view.setTag(v);

        v.messageBody.setText(message.body);
        if (message.error != null) {
            v.dateTime.setText(message.error);
            v.dateTime.setTextColor(Color.RED);
        } else {
            v.dateTime.setText(DateHelper.formatTime(message.sentTime));
            v.dateTime.setTextColor(Color.GRAY);
        }
        if (message.hasFlag(Message.FLAG_ENCRYPTED)) {
            v.encryptedIcon.setImageResource(R.drawable.ic_lock_black_24dp);
            v.encryptedIcon.setColorFilter(Color.GRAY);
        } else {
            v.encryptedIcon.setImageResource(R.drawable.ic_lock_open_black_24dp);
            v.encryptedIcon.setColorFilter(Color.RED);
        }
        if (message.hasFlag(Message.FLAG_FAILED)) {
            v.encryptedIcon.setImageResource(R.drawable.ic_warning_black_24dp);
            v.encryptedIcon.setColorFilter(Color.MAGENTA);
        }
        if (v.ackIcon != null) v.ackIcon.setVisibility(message.hasFlag(Message.FLAG_ACKNOWLEDGED) ? View.VISIBLE : View.GONE);
        v.protocolIcon.setVisibility(View.VISIBLE);
        if (message.hasFlag(Message.FLAG_PROTO_BT)) v.protocolIcon.setImageResource(R.drawable.ic_bluetooth_black_24dp);
        else if (message.hasFlag(Message.FLAG_PROTO_WIFI)) v.protocolIcon.setImageResource(R.drawable.ic_network_wifi_black_24dp);
        else if (message.hasFlag(Message.FLAG_PROTO_CLOUD)) v.protocolIcon.setImageResource(R.drawable.ic_cloud_black_24dp);
        else v.protocolIcon.setVisibility(View.GONE);
    }

    private void inflateContactsView() {
        List<Contact> contacts;

        switch (message.direction()) {
            case Received:
                ((TextView) findViewById(R.id.label_senders)).setText("Von:");
                contacts = new ArrayList<>();
                contacts.add(new Contact(message.sender.getNickname(), "", "", message.sender.getPhoneNumber(), message.sender.getPublicKey(), null, "", "", 0));
                break;
            case Sent:
            default:
                ((TextView) findViewById(R.id.label_senders)).setText("An:");
                contacts = message.recipients;
                break;
        }

        ListView contactsList = (ListView) findViewById(R.id.contactsList);
        contactsList.setAdapter(new ContactsAdapter(this, contacts));
    }

    private void inflateMessageDatails() {
        ((TextView) findViewById(R.id.datetime)).setText(DateHelper.formatDateTime(message.sentTime));

        if (message.hasFlag(Message.FLAG_PROTO_BT)) {
            ((TextView) findViewById(R.id.label_message_proto)).setText(getString(R.string.protocol_bluetooth));
            ((ImageView) findViewById(R.id.message_big_proto)).setImageResource(R.drawable.ic_bluetooth_black_24dp);
            findViewById(R.id.message_big_proto).setVisibility(View.VISIBLE);
        }
        else if (message.hasFlag(Message.FLAG_PROTO_WIFI)) {
            ((TextView) findViewById(R.id.label_message_proto)).setText(getString(R.string.protocol_wifi_direct));
            ((ImageView) findViewById(R.id.message_big_proto)).setImageResource(R.drawable.ic_network_wifi_black_24dp);
            findViewById(R.id.message_big_proto).setVisibility(View.VISIBLE);
        }
        else if (message.hasFlag(Message.FLAG_PROTO_CLOUD)) {
            ((TextView) findViewById(R.id.label_message_proto)).setText(getString(R.string.protocol_cloud));
            ((ImageView) findViewById(R.id.message_big_proto)).setImageResource(R.drawable.ic_cloud_black_24dp);
            findViewById(R.id.message_big_proto).setVisibility(View.VISIBLE);
        }
        else {
            findViewById(R.id.label_message_proto).setVisibility(View.GONE);
            findViewById(R.id.message_big_proto).setVisibility(View.GONE);
        }
    }

    private void inflateTraceroute(List<TracerouteSegment> traceroute) {
        LinearLayout tracerouteList = (LinearLayout) findViewById(R.id.tracedHopsList);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for (TracerouteSegment segment: traceroute) {
            Log.d(TAG, "adding " + segment);
            View view;
            if (segment instanceof Contact) {
                view = inflater.inflate(R.layout.contacts_layout, null);
                Contact contact = (Contact) segment;

                ((TextView) view.findViewById(R.id.name)).setText(contact.getNickname());
                ((ImageView) view.findViewById(R.id.icon)).setImageResource(R.mipmap.ic_launcher);
            }
            else {
                view = inflater.inflate(R.layout.traceroute_rowlayout_hop, null);
                TracerouteHopSegment hop = (TracerouteHopSegment) segment;

                if (hop.getProtocol() == TracerouteHopSegment.ProtocolType.BLUETOOTH) {
                    ((ImageView) view.findViewById(R.id.protocol_icon)).setImageResource(R.drawable.ic_bluetooth_black_24dp);
                    ((TextView) view.findViewById(R.id.protocol)).setText(getString(R.string.protocol_bluetooth));
                }
                else if (hop.getProtocol() == TracerouteHopSegment.ProtocolType.WIFI) {
                    ((ImageView) view.findViewById(R.id.protocol_icon)).setImageResource(R.drawable.ic_network_wifi_black_24dp);
                    ((TextView) view.findViewById(R.id.protocol)).setText(getString(R.string.protocol_wifi_direct));
                }
                else if (hop.getProtocol() == TracerouteHopSegment.ProtocolType.GCM) {
                    ((ImageView) view.findViewById(R.id.protocol_icon)).setImageResource(R.drawable.ic_cloud_black_24dp);
                    ((TextView) view.findViewById(R.id.protocol)).setText(getString(R.string.protocol_cloud));
                }
                else {
                    findViewById(R.id.protocol_icon).setVisibility(View.GONE);
                    findViewById(R.id.protocol).setVisibility(View.GONE);
                }

                ((TextView) view.findViewById(R.id.time)).setText(hop.getTimeDelta());
            }
            tracerouteList.addView(view);
        }

        // replace progress bar with actual list
        findViewById(R.id.tracerouteLoading).setVisibility(View.GONE);
        findViewById(R.id.tracedHopsList).setVisibility(View.VISIBLE);
    }
}
