package com.pcinnovations.shoppingtest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ProductListAdapter extends ArrayAdapter<Product> {

    private ArrayList<Product> objects;
    private Context thisContext;

    public ProductListAdapter(Context context, int resource, ArrayList<Product> objects) {
        super(context, resource, objects);
        this.objects = objects;
        this.thisContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi = (LayoutInflater)thisContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.product_item, null);
        }

        Product o = objects.get(position);

        if (o != null) {
            TextView textName = (TextView) v.findViewById(R.id.txtName);
            TextView textEan = (TextView) v.findViewById(R.id.txtEan);
            if (textName != null) {
                textName.setText(o.getName());                            }
            if(textEan != null){
                textEan.setText("Kod kreskowy: "+ o.getEan());
            }

        }
        return v;
    }
}

