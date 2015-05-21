package de.tudarmstadt.informatik.bp.bonfirechat.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.tudarmstadt.informatik.bp.bonfirechat.models.Conversation;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Message;
import de.tudarmstadt.informatik.bp.bonfirechat.R;

/**
 * Created by mw on 05.05.2015.
 */
public class MessagesAdapter extends ArrayAdapter<Message> {

    class ViewHolder {
        ImageView contactPhoto;
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
        return getItem(position).direction == Message.MessageDirection.Received ? 1 : 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder v;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            switch (getItem(position).direction) {
                case Received:
                    convertView = inflater.inflate(R.layout.message_rowlayout_received, parent, false);
                    break;
                case Sent:
                    convertView = inflater.inflate(R.layout.message_rowlayout_sent, parent, false);
                    break;
            }

            v = new ViewHolder();
            v.messageBody = (TextView) convertView.findViewById(R.id.message_body);
            v.dateTime = (TextView) convertView.findViewById(R.id.message_time);
            v.contactPhoto = (ImageView) convertView.findViewById(R.id.message_photo);
            convertView.setTag(v);
        } else {
            v = (ViewHolder)convertView.getTag();
        }

        v.messageBody.setText(getItem(position).body);
        //lastMessage.setText(objects.get(position).getLastMessage());
        //icon.setImageResource(R.mipmap.ic_launcher);

        return convertView;
    }

}
