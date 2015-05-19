package com.pcinnovations.shoppingtest;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
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

public class SavedProducts extends ActionBarActivity {

    public static JSONArray products;
    protected ListView listProds;
    protected EditText txtFilter;
    protected int requestCode;
    protected ProductListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestCode = getIntent().getIntExtra("sc_requestCode", 0);
        Log.d("SC/RequestCode", String.valueOf(requestCode));
        setContentView(R.layout.activity_saved_products);
        listProds = (ListView) findViewById(R.id.listProdCache);
        ImageButton btnClearFilter = (ImageButton) findViewById(R.id.btnCacheFilterClear);

        txtFilter = (EditText) findViewById(R.id.txtCacheFilter);
        btnClearFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtFilter.setText("");
                refreshList();
            }
        });

        txtFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() != 0) {
                    adapter.getFilter().filter(s.toString());
                } else {
                    refreshList();
                }
            }
        });
        if(requestCode != 0) {
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(false);
            getSupportActionBar().setHomeAsUpIndicator(null);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        new NetworkTask().execute(Uri.parse("http://93.180.174.49:50080/companion/GetProductCache.php"));


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(requestCode == 0) {
            getMenuInflater().inflate(R.menu.menu_saved_products, menu);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id){
            case R.id.action_clear_prod_cache:
                clearProdCache();
                break;
            case R.id.action_refresh_cache:
                new NetworkTask().execute(Uri.parse("http://93.180.174.49:50080/companion/GetProductCache.php"));
                Toast.makeText(SavedProducts.this, "Odświeżanie zawartości...",  Toast.LENGTH_SHORT).show();
                break;
        }

        return super.onOptionsItemSelected(item);

    }

    public class NetworkTask extends AsyncTask<Uri,Void,String> {

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
                JSONArray prods = new JSONArray(result);
                SavedProducts.products = prods;
                refreshList();
            } catch (Exception e) {
                e.printStackTrace();
                SavedProducts.products = new JSONArray();
                refreshList();
            }
        }
    }

    private ArrayList<Product> getProductsFromJsonArray(JSONArray array) {
        ArrayList<Product> returned = new ArrayList<Product>();
        for(int i = 0; i < array.length(); i++) {
            try {
                JSONObject o = array.getJSONObject(i);
                Product p = new Product(o.getString("ean"), o.getString("nazwa"));
                returned.add(p);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return returned;
    }

    public void refreshList() {
       refreshList(null);
    }

    public void refreshList(String filter) {
        final ArrayList<Product> productsArray = getProductsFromJsonArray(products);
        this.adapter = new ProductListAdapter(SavedProducts.this, R.layout.product_item, productsArray );
        listProds.setAdapter(adapter);
        if(requestCode != 0) {
            listProds.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Product p = productsArray.get(position);
                    getSharedPreferences(getString(R.string.shared_prefs_key), Context.MODE_PRIVATE)
                            .edit()
                            .putString("sc_returnedEan", p.getEan())
                            .putString("sc_returnedName", p.getName())
                            .commit();
                    startActivity(new Intent(SavedProducts.this, AddItemToList.class));
                    SavedProducts.this.finish();
                }
            });
        }
    }


    public void clearProdCache() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.alert_clear_cache_title)
                .setMessage(R.string.alert_clear_cache_body)
                .setIcon(R.drawable.ic_question_mark)
                .setCancelable(false)
                .setPositiveButton("Tak", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       new NetworkTask().execute(Uri.parse("http://93.180.174.49:50080/companion/ClearProductCache.php"));
                    }
                })
                .setNegativeButton("Nie", null)
                .show();
    }
}
