package com.pcinnovations.shoppingtest;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_products);
        listProds = (ListView) findViewById(R.id.listProdCache);
        new NetworkTask().execute(Uri.parse("http://93.180.174.49:50080/companion/GetProductCache.php"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_saved_products, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id){
            case R.id.action_clear_prod_cache:
                clearProdCache();
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
        final ArrayList<Product> productsArray = getProductsFromJsonArray(products);
        final ProductListAdapter adapter = new ProductListAdapter(SavedProducts.this, R.layout.product_item, productsArray );
        listProds.setAdapter(adapter);
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
