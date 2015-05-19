package com.pcinnovations.shoppingtest;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;

public class ItemEssentialArrayAdapter extends ArrayAdapter<ItemEssential> implements Filterable {

    private ArrayList<ItemEssential> objects;
    private ArrayList<ItemEssential> objectsCopy;
    private Context thisContext;

    public ItemEssentialArrayAdapter(Context context, int resource, ArrayList<ItemEssential> objects) {
        super(context, resource, objects);
        this.objects = objects;
        this.objectsCopy = objects;
        this.thisContext = context;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    private Filter myFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            Log.d("SC/EssentialFilter", "Filtrowanie - " + constraint);
            FilterResults results = new FilterResults();
            ArrayList<ItemEssential> tempList = new ArrayList<ItemEssential>();
            if(constraint == null || constraint.length() == 0) {
                tempList = objectsCopy;
            } else {
                int length = objects.size();
                for(int i = 0; i < length; i++){
                    ItemEssential p = objects.get(i);

                    if(p.getItemName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        tempList.add(p);
                    }
                }

                results.values = tempList;
                results.count = tempList.size();
            }

            return results;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results.count > 0) {
                objects = (ArrayList<ItemEssential>) results.values;
                notifyDataSetChanged();
            } else {
                //notifyDataSetChanged();
                notifyDataSetInvalidated();
            }
        }
    };

    @Override
    public Filter getFilter() {
        return myFilter;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater vi = (LayoutInflater)thisContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.layout_essential_item, null);
        }

        ItemEssential o = null;

        try {
            o = objects.get(position);
        } catch(IndexOutOfBoundsException e) {
            e.printStackTrace();
        }

        if (o != null) {
            TextView textName = (TextView) v.findViewById(R.id.txtEssentialName);

            if (textName != null) {
                textName.setText(o.getItemName());
            }
        }
        return v;
    }

}

