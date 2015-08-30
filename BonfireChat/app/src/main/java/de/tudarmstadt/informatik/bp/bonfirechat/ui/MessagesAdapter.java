package de.tudarmstadt.informatik.bp.bonfirechat.ui;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.util.Comparator;
import java.util.List;

import de.tudarmstadt.informatik.bp.bonfirechat.R;
import de.tudarmstadt.informatik.bp.bonfirechat.data.BonfireAPI;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.DateHelper;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Message;

/**
 * Created by mw on 05.05.2015.
 */
public class MessagesAdapter extends ArrayAdapter<Message> {

    Context context;
    boolean[] itemSelected;

    class ViewHolder {
        ImageView contactPhoto, encryptedIcon, protocolIcon, ackIcon, messageImage;
        TextView messageBody, dateTime;
        ProgressBar thumbLoading, onItsWay;
    }

    public MessagesAdapter(Context context, List<Message> objects) {
        super(context, R.layout.message_rowlayout_received, objects);
        this.context = context;
        itemSelected = new boolean[objects.size()];
    }

    @Override
    public void add(Message object) {
        super.add(object);
        this.sort(new Comparator<Message>() {
            @Override
            public int compare(Message message, Message t1) {
                return message.sentTime.compareTo(t1.sentTime);
            }
        });
        // adjust size of selected array
        itemSelected = new boolean[getCount()];
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).direction() == Message.MessageDirection.Received ? 1 : 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder v;
        if (convertView == null) {
            v = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            switch (getItem(position).direction()) {
                case Received:
                    convertView = inflater.inflate(R.layout.message_rowlayout_received, parent, false);
                    break;
                case Sent:
                    convertView = inflater.inflate(R.layout.message_rowlayout_sent, parent, false);
                    v.ackIcon = (ImageView) convertView.findViewById(R.id.message_ack);
                    v.onItsWay = (ProgressBar) convertView.findViewById(R.id.on_its_way);
                    break;
            }

            v.messageBody = (TextView) convertView.findViewById(R.id.message_body);
            v.messageImage = (ImageView) convertView.findViewById(R.id.message_image);
            v.thumbLoading = (ProgressBar) convertView.findViewById(R.id.thumb_loading);
            v.dateTime = (TextView) convertView.findViewById(R.id.message_time);
            v.contactPhoto = (ImageView) convertView.findViewById(R.id.message_photo);
            v.encryptedIcon = (ImageView) convertView.findViewById(R.id.message_encrypted);
            v.protocolIcon = (ImageView) convertView.findViewById(R.id.message_proto);
            convertView.setTag(v);
        } else {
            v = (ViewHolder) convertView.getTag();
        }
        final Message msg = getItem(position);
        v.messageBody.setText(msg.body);
        v.dateTime.setText(DateHelper.formatTime(msg.sentTime));
        v.dateTime.setTextColor(Color.GRAY);
        if (msg.hasFlag(Message.FLAG_ENCRYPTED)) {
            v.encryptedIcon.setImageResource(R.drawable.ic_lock_black_24dp);
            v.encryptedIcon.setColorFilter(Color.GRAY);
        } else {
            v.encryptedIcon.setImageResource(R.drawable.ic_lock_open_black_24dp);
            v.encryptedIcon.setColorFilter(Color.RED);
        }
        if (msg.hasFlag(Message.FLAG_FAILED)) {
            v.encryptedIcon.setImageResource(R.drawable.ic_warning_black_24dp);
            v.encryptedIcon.setColorFilter(Color.MAGENTA);
        }
        if (v.ackIcon != null) v.ackIcon.setVisibility(msg.hasFlag(Message.FLAG_ACKNOWLEDGED) ? View.VISIBLE : View.GONE);
        if (v.onItsWay != null) v.onItsWay.setVisibility((msg.hasFlag(Message.FLAG_ACKNOWLEDGED) || msg.hasFlag(Message.FLAG_FAILED)) ? View.GONE : View.VISIBLE);
        v.protocolIcon.setVisibility(View.VISIBLE);
        if (msg.hasFlag(Message.FLAG_PROTO_BT)) v.protocolIcon.setImageResource(R.drawable.ic_bluetooth_black_24dp);
        else if (msg.hasFlag(Message.FLAG_PROTO_WIFI)) v.protocolIcon.setImageResource(R.drawable.ic_network_wifi_black_24dp);
        else if (msg.hasFlag(Message.FLAG_PROTO_CLOUD)) v.protocolIcon.setImageResource(R.drawable.ic_cloud_black_24dp);
        else v.protocolIcon.setVisibility(View.GONE);

        if (msg.hasFlag(Message.FLAG_IS_FILE)) {
            // Handle message type "FILE" (image)
            v.messageBody.setVisibility(View.GONE);
            v.messageImage.setVisibility(View.VISIBLE);
            v.messageImage.setImageURI(Uri.parse("file://" + msg.body));
            v.thumbLoading.setVisibility(View.GONE);

        } else if(msg.hasFlag(Message.FLAG_IS_LOCATION)) {
            // Handle message type "LOCATION"
            v.messageBody.setVisibility(View.GONE);

            final File previewFile = msg.getImageFile();
            if (previewFile.exists()) {
                v.messageImage.setImageURI(Uri.parse("file://" + previewFile.getAbsolutePath()));
                v.messageImage.setVisibility(View.VISIBLE);
                v.thumbLoading.setVisibility(View.GONE);
            } else {
                v.messageImage.setVisibility(View.GONE);
                v.thumbLoading.setVisibility(View.VISIBLE);

                // extract coordinates from message
                String[] coords = msg.body.split(":");
                LatLng location = new LatLng(Double.parseDouble(coords[0]), Double.parseDouble(coords[1]));

                // load map preview from Google Maps static map API
                (new AsyncTask<LatLng, Void, Boolean>() {
                    @Override
                    protected Boolean doInBackground(LatLng... locations) {
                        // download map preview
                        return BonfireAPI.downloadMapPreview(locations[0], previewFile);
                    }
                    @Override
                    protected void onPostExecute(Boolean ok) {
                        if (ok) {
                            // display map preview image
                            v.messageImage.setImageURI(Uri.parse("file://" + previewFile.getAbsolutePath()));
                            v.messageImage.setVisibility(View.VISIBLE);
                        } else {
                            v.messageBody.setVisibility(View.VISIBLE);
                        }
                        v.thumbLoading.setVisibility(View.GONE);
                    }
                }).execute(location);
            }

        } else {
            // Handle message type "TEXT"
            v.messageBody.setVisibility(View.VISIBLE);
            v.thumbLoading.setVisibility(View.GONE);
            v.messageImage.setVisibility(View.GONE);
            v.messageImage.setImageDrawable(null);
        }
        convertView.setSelected(itemSelected[position]);
        convertView.setBackgroundColor(itemSelected[position] ? Color.parseColor("#ffbbff") : Color.TRANSPARENT);

        return convertView;
    }
}
