package de.tudarmstadt.informatik.bp.bonfirechat.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.informatik.bp.bonfirechat.helper.ContactImageHelper;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Conversation;
import de.tudarmstadt.informatik.bp.bonfirechat.R;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Message;

/**
 * Created by johannes on 05.05.15.
 */
public class ConversationsAdapter extends ArrayAdapter<Conversation> {
    private final Context context;
    private List<Conversation> objects;

    boolean[] itemSelected;

    public ConversationsAdapter(Context context, List<Conversation> objects) {
        super(context, R.layout.conversations_layout, objects);
        this.context = context;
        this.objects = new ArrayList<>(objects);
        itemSelected = new boolean[this.objects.size()];
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        if (itemSelected.length != this.getCount()) {
            itemSelected = new boolean[this.getCount()];
        }
    }

    static class ViewHolder {
        ImageView icon;
        TextView name;
        TextView lastMessage;
        TextView lastMessageDate;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder v;
        if (convertView == null) {
            v = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.conversations_layout, parent, false);
            v.icon = (ImageView) convertView.findViewById(R.id.icon);
            v.name = (TextView) convertView.findViewById(R.id.name);
            v.lastMessage = (TextView) convertView.findViewById(R.id.lastMessage);
            v.lastMessageDate = (TextView) convertView.findViewById(R.id.lastMessageDate);
            convertView.setTag(v);
        } else {
            v = (ViewHolder) convertView.getTag();
        }

        if (position < objects.size()) {
            ContactImageHelper.displayContactImage(objects.get(position).getPeer(), v.icon);
        }

        v.name.setText(getItem(position).getName());
        Message lastMessage = getItem(position).getLastMessage();
        if (lastMessage == null) {
            v.lastMessage.setText("");
        } else {
            v.lastMessage.setText(lastMessage.getDisplayBody(context));
            v.lastMessage.setTypeface(null,
                    (lastMessage.hasFlag(Message.FLAG_IS_FILE) || lastMessage.hasFlag(Message.FLAG_IS_LOCATION))
                        ? Typeface.ITALIC : Typeface.NORMAL);
        }

        v.lastMessageDate.setText(getItem(position).getLastMessageDate());
        Log.d("ConversationsAdapter", "getview position=" + position + "   selected=" + itemSelected[position]);
        convertView.setSelected(itemSelected[position]);
        convertView.setBackgroundColor(itemSelected[position] ? Color.parseColor("#ffbbff") : Color.TRANSPARENT);

        return convertView;
    }
}
