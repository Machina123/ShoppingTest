package com.pcinnovations.shoppingtest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;

public class ProductListAdapter extends ArrayAdapter<Product> implements Filterable {

    private ArrayList<Product> objects;
    private ArrayList<Product> objectsCopy;
    private Context thisContext;

    public ProductListAdapter(Context context, int resource, ArrayList<Product> objects) {
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
            FilterResults results = new FilterResults();
            ArrayList<Product> tempList = new ArrayList<Product>();
            if(constraint == null || constraint.length() == 0) {
                tempList = objectsCopy;
            } else {
                int length = objects.size();
                for(int i = 0; i < length; i++){
                    Product p = objects.get(i);

                    if(p.getName().toLowerCase().contains(constraint.toString().toLowerCase())) {
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
                objects = (ArrayList<Product>) results.values;
                notifyDataSetChanged();
            } else {
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
            v = vi.inflate(R.layout.product_item, null);
        }

        Product o = null;

        try {
           o = objects.get(position);
        } catch(IndexOutOfBoundsException e) {
            e.printStackTrace();
        }

        if (o != null) {
            TextView textName = (TextView) v.findViewById(R.id.txtName);
            TextView textEan = (TextView) v.findViewById(R.id.txtEan);
            if (textName != null) {
                textName.setText(o.getName());
            }
            if(textEan != null){
                textEan.setText("Kod kreskowy: "+ o.getEan());
            }

        }
        return v;
    }
}

