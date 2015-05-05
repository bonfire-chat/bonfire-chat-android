package de.wikilab.bonfirechat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by johannes on 05.05.15.
 */
public class ConversationsAdapter extends ArrayAdapter<Conversation> {
    private final Context context;
    private final ArrayList<Conversation> objects;

    public ConversationsAdapter(Context context, List<Conversation> objects) {
        super(context, R.layout.conversations_layout, objects);
        this.context = context;
        this.objects = new ArrayList<>(objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.conversations_layout, parent, false);
        TextView name = (TextView) rowView.findViewById(R.id.name);
        TextView lastMessage = (TextView) rowView.findViewById(R.id.lastMessage);
        ImageView icon = (ImageView) rowView.findViewById(R.id.icon);

        name.setText(objects.get(position).getName());
        lastMessage.setText(objects.get(position).getLastMessage());
        icon.setImageResource(R.mipmap.ic_launcher);

        return rowView;
    }
}
