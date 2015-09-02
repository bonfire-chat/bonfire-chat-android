package de.tudarmstadt.informatik.bp.bonfirechat.ui;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.tudarmstadt.informatik.bp.bonfirechat.R;
import de.tudarmstadt.informatik.bp.bonfirechat.models.Contact;

import java.util.List;

/**
 * Created by johannes on 05.05.15.
 */
public class ContactsAdapter extends ArrayAdapter<Contact> {
    private final Context context;

    public List<Contact> getObjects() {
        return objects;
    }

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
        if (itemSelected.length != this.objects.size())
            itemSelected = new boolean[this.objects.size()];
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.contacts_layout, parent, false);
        TextView name = (TextView) rowView.findViewById(R.id.name);
        ImageView icon = (ImageView) rowView.findViewById(R.id.icon);

        name.setText(objects.get(position).getNickname());
        if(objects.get(position).getImage().equals(""))
            icon.setImageResource(R.mipmap.ic_launcher);
        else
            icon.setImageURI(Uri.parse(objects.get(position).getImage()));
        Log.d("ContactsAdapter", "getview position=" + position+"   selected="+itemSelected[position] );
        rowView.setSelected(itemSelected[position]);
        rowView.setBackgroundColor(itemSelected[position] ? Color.parseColor("#ffbbff") : Color.TRANSPARENT);

        return rowView;
    }




}
