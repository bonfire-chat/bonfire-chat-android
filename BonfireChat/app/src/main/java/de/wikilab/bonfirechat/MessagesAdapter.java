package de.wikilab.bonfirechat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

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
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder v;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.message_rowlayout_received, parent, false);
            v = new ViewHolder();
            TextView name = (TextView) convertView.findViewById(R.id.name);
            TextView lastMessage = (TextView) convertView.findViewById(R.id.lastMessage);
            ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
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
