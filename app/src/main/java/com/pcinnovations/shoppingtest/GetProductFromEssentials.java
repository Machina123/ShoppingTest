package com.pcinnovations.shoppingtest;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class GetProductFromEssentials extends ActionBarActivity {

    protected ListView listView;

    protected ArrayList<String> essentialList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_product_from_essentials);
        listView = (ListView)findViewById(R.id.listEssentials);
        new NetworkTask().execute(Uri.parse("http://93.180.174.49:50080/companion/GetEssentialList.php"));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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
                JSONArray array = new JSONArray(result);
                essentialList.clear();
                for(int i = 0; i < array.length(); i++) {
                    essentialList.add(array.getJSONObject(i).getString("nazwa"));
                }
                refreshList();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void refreshList() {
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(GetProductFromEssentials.this, android.R.layout.simple_list_item_1, essentialList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getSharedPreferences(getString(R.string.shared_prefs_key), Context.MODE_PRIVATE).edit()
                        .putBoolean("sc_isFromEssentials", true)
                        .putString("sc_returnedEan", String.valueOf(position))
                        .putString("sc_returnedName", essentialList.get(position))
                        .commit();
                startActivity(new Intent(GetProductFromEssentials.this, AddItemToList.class));
                finish();
            }
        });
    }
}
