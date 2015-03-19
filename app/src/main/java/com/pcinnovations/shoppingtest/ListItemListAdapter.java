package com.pcinnovations.shoppingtest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

public class ListItemListAdapter extends ArrayAdapter<ListItem> {

    private ArrayList<ListItem> objects;
    private Context thisContext;

    public ListItemListAdapter(Context context, int resource, ArrayList<ListItem> objects) {
        super(context, resource, objects);
        this.objects = objects;
        this.thisContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi = (LayoutInflater)thisContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.layout_list_item, null);
        }

        ListItem o = objects.get(position);

        if (o != null) {
            TextView textName = (TextView) v.findViewById(R.id.txtLPItemName);
            TextView textEan = (TextView) v.findViewById(R.id.txtLPEan);
            TextView textAmount = (TextView) v.findViewById(R.id.txtLPAmount);
            TextView textBought = (TextView) v.findViewById(R.id.txtLPBought);
            if(textName != null) textName.setText(o.getName());
            if(textEan != null) textEan.setText("Kod kreskowy: "+ o.getEan());
            if(textAmount != null) textAmount.setText("Ilość: " + o.getAmount());
            if(textBought != null) textBought.setText(o.isBought() == 1 ? "Kupione: tak" : "Kupione: nie" );
        }

        return v;
    }
}

