package de.tudarmstadt.informatik.bp.bonfirechat.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import de.tudarmstadt.informatik.bp.bonfirechat.R;
import de.tudarmstadt.informatik.bp.bonfirechat.helper.ContactImageHelper;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Contact;

/**
 * Created by johannes on 05.05.15.
 */
public class ContactsAdapter extends ArrayAdapter<Contact> {
    private final Context context;

    private final List<Contact> objects;
    boolean[] itemSelected;

    public ContactsAdapter(Context context, List<Contact> objects) {
        super(context, R.layout.contacts_layout, objects);
        this.context = context;
        this.objects = objects;
        itemSelected = new boolean[this.objects.size()];
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        if (itemSelected.length != this.objects.size()) {
            itemSelected = new boolean[this.objects.size()];
        }
    }

    static class ViewHolder {
        TextView name;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.contacts_layout, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.name = (TextView) rowView.findViewById(R.id.name);
            rowView.setTag(holder);
        }

        Contact entry = objects.get(position);
        if (entry != null) {
            ViewHolder holder = (ViewHolder) rowView.getTag();
            holder.name.setText(entry.getNickname());
            ContactImageHelper.displayCompoundContactImage(context, objects.get(position), holder.name);
        }

        Log.d("ContactsAdapter", "getview position=" + position + "   selected=" + itemSelected[position]);
        rowView.setSelected(itemSelected[position]);
        rowView.setBackgroundColor(itemSelected[position] ? Color.parseColor("#ffbbff") : Color.TRANSPARENT);

        return rowView;
    }




}
