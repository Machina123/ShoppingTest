package com.pcinnovations.shoppingtest;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class GetProductsByName extends ActionBarActivity {

    public int requestCode;
    private String queryName;
    private static JSONArray products;
    private ListView listProds;
    private EditText txtSearch;
    private ImageButton btnSearch;
    private TextView txtItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_get_products_by_name);

        int extra = getIntent().getIntExtra("requestCode", 0);
        Log.d("GetProductsByName.Xtras", String.valueOf(extra));
        requestCode = extra;

        getSupportActionBar().setTitle(R.string.title_scan_results);

        listProds = (ListView) findViewById(R.id.listProds);
        txtSearch = (EditText) findViewById(R.id.txtSearch);
        btnSearch = (ImageButton) findViewById(R.id.btnSearch);
        txtItems = (TextView) findViewById(R.id.txtItems);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new NetworkTask().execute(Uri.parse("http://93.180.174.49:50080/companion/GetProductByName.php?name=" + Uri.encode(txtSearch.getText().toString().replace(" ", "%"))));
            }
        });

        if(savedInstanceState != null) {
            try {
                products = new JSONArray(savedInstanceState.getString("PRODUCTS_JSON"));
                queryName = savedInstanceState.getString("NAME");

                txtSearch.setText(queryName);
                refreshList();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(products != null) outState.putString("PRODUCTS_JSON", products.toString());
        outState.putString("NAME", txtSearch.getText().toString());
    }

    public class NetworkTask extends AsyncTask<Uri,Void,String> {

        final ProgressDialog dialog = ProgressDialog.show(GetProductsByName.this, "Proszę czekać...", "Pobieranie listy produktów...", false);

        @Override
        protected String doInBackground(Uri... uri) {
            HttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(uri[0].toString());
            HttpResponse response = null;
            try {
                response = client.execute(httpGet);
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                String responseGet;
                String resp = "";
                while((responseGet = reader.readLine() )!=null) {
                    resp += responseGet;
                }
                return resp;
            } catch (Exception e) {
                e.printStackTrace();
                //Toast.makeText(GetProductsByName.this, "Nie można połączyć z siecią!", Toast.LENGTH_SHORT).show();
                return "Nie można połączyć z siecią!";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                dialog.dismiss();
                if(result.equalsIgnoreCase("Unknown")) {
                    Toast.makeText(GetProductsByName.this, "Nie znaleziono produktu o podanym kodzie kreskowym!", Toast.LENGTH_SHORT).show();
                    GetProductsByName.this.finish();
                }
                if(result.equalsIgnoreCase("banana")) {
                    Toast.makeText(GetProductsByName.this, "Produkt został zapisany!", Toast.LENGTH_SHORT).show();
                    GetProductsByName.this.finish();
                }
                JSONArray prods = new JSONArray(result);
                GetProductsByName.products = prods;
                refreshList();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private ArrayList<Product> getProductsFromJsonArray(JSONArray array) {
        ArrayList<Product> returned = new ArrayList<Product>();
        txtItems.setText("Znaleziono " + array.length() + " produktów");
        for(int i = 0; i < array.length(); i++) {
            try {
                JSONObject o = array.getJSONObject(i);
                Product p = new Product(o.getString("kod"), o.getString("nazwa"));
                returned.add(p);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return returned;
    }

    public void refreshList() {
        final ArrayList<Product> productsArray = getProductsFromJsonArray(products);
        final ProductListAdapter adapter = new ProductListAdapter(GetProductsByName.this, R.layout.product_item, productsArray );

        listProds.setAdapter(adapter);
        listProds.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Product selected = productsArray.get(position);
                Toast.makeText(GetProductsByName.this, selected.getName(), Toast.LENGTH_SHORT).show();
                Log.d("SC_RequestCode", String.valueOf(requestCode));
                if(requestCode != 0) {
                    Intent intent = new Intent(GetProductsByName.this, AddItemToList.class);
                    getSharedPreferences(getString(R.string.shared_prefs_key), Context.MODE_PRIVATE).edit().putString("sc_returnedEan", selected.getEan()).putString("sc_returnedName", selected.getName()).commit();
                    startActivity(intent);
                    finish();
                }
                else {
                    new NetworkTask().execute(Uri.parse("http://93.180.174.49:50080/companion/AddToProductCache.php?ean=" + Uri.encode(selected.getEan()) + "&name=" + Uri.encode(selected.getName())));
                }
            }
        });
    }

}
