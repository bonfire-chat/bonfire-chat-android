package de.tudarmstadt.informatik.bp.bonfirechat.ui;

import android.content.Context;
import android.graphics.Color;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.tudarmstadt.informatik.bp.bonfirechat.helper.DateHelper;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Message;
import de.tudarmstadt.informatik.bp.bonfirechat.R;

/**
 * Created by mw on 05.05.2015.
 */
public class MessagesAdapter extends ArrayAdapter<Message> {

    class ViewHolder {
        ImageView contactPhoto, encryptedIcon, protocolIcon, ackIcon;
        TextView messageBody, dateTime;
    }

    public MessagesAdapter(Context context, List<Message> objects) {
        super(context, R.layout.message_rowlayout_received, objects);
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
        ViewHolder v;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = new ViewHolder();
            switch (getItem(position).direction()) {
                case Received:
                    convertView = inflater.inflate(R.layout.message_rowlayout_received, parent, false);
                    break;
                case Sent:
                    convertView = inflater.inflate(R.layout.message_rowlayout_sent, parent, false);
                    v.ackIcon = (ImageView) convertView.findViewById(R.id.message_ack);
                    break;
            }

            v.messageBody = (TextView) convertView.findViewById(R.id.message_body);
            v.dateTime = (TextView) convertView.findViewById(R.id.message_time);
            v.contactPhoto = (ImageView) convertView.findViewById(R.id.message_photo);
            v.encryptedIcon = (ImageView) convertView.findViewById(R.id.message_encrypted);
            v.protocolIcon = (ImageView) convertView.findViewById(R.id.message_proto);
            convertView.setTag(v);
        } else {
            v = (ViewHolder)convertView.getTag();
        }
        Message msg = getItem(position);
        v.messageBody.setText(msg.body);
        if (msg.error != null) {
            v.dateTime.setText(msg.error);
            v.dateTime.setTextColor(Color.RED);
        } else {
            v.dateTime.setText(DateHelper.formatTime(msg.sentTime));
            v.dateTime.setTextColor(Color.GRAY);
        }
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
        v.protocolIcon.setVisibility(View.VISIBLE);
        if (msg.hasFlag(Message.FLAG_PROTO_BT)) v.protocolIcon.setImageResource(R.drawable.ic_bluetooth_black_24dp);
        else if (msg.hasFlag(Message.FLAG_PROTO_WIFI)) v.protocolIcon.setImageResource(R.drawable.ic_network_wifi_black_24dp);
        else if (msg.hasFlag(Message.FLAG_PROTO_CLOUD)) v.protocolIcon.setImageResource(R.drawable.ic_cloud_black_24dp);
        else v.protocolIcon.setVisibility(View.GONE);
        //lastMessage.setText(objects.get(position).getLastMessage());
        //icon.setImageResource(R.mipmap.ic_launcher);

        return convertView;
    }



}
