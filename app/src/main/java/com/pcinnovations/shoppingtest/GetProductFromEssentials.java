package com.pcinnovations.shoppingtest;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;

import android.util.Log;

import android.view.View;

import android.widget.AdapterView;

import android.widget.EditText;

import android.widget.ImageButton;
import android.widget.ListView;

import com.pcinnovations.shoppingtest.common.ApiData;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class GetProductFromEssentials extends ActionBarActivity {
    private ItemEssentialArrayAdapter adapter;
    protected ListView listView;
    protected EditText txtFilter;
    protected ImageButton btnClearFilter;

    protected ArrayList<ItemEssential> essentialList = new ArrayList<ItemEssential>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_product_from_essentials);
        listView = (ListView)findViewById(R.id.listEssentials);
        listView.setTextFilterEnabled(true);
        ImageButton btnClearFilter = (ImageButton) findViewById(R.id.btnEssentialFilterClear);

        txtFilter = (EditText) findViewById(R.id.txtEssentialFilter);
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
                Log.d("SC/GetFromEssent", "Zmiana tekstu filtra - " + s);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() > 0) {
                    adapter.getFilter().filter(s.toString());
                } else {
                    refreshList();
                }
            }
        });
        new NetworkTask().execute(Uri.parse(ApiData.API_ADDRESS + ApiData.SEPARATOR + ApiData.API_SUBFOLDER + ApiData.SEPARATOR + "GetEssentialList.php"));
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
                    essentialList.add(new ItemEssential(array.getJSONObject(i).getString("nazwa")));
                }
                refreshList();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void refreshList() {
        adapter = new ItemEssentialArrayAdapter(GetProductFromEssentials.this, R.layout.layout_essential_item, essentialList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getSharedPreferences(getString(R.string.shared_prefs_key), Context.MODE_PRIVATE).edit()
                        .putBoolean("sc_isFromEssentials", true)
                        .putString("sc_returnedEan", String.valueOf(position))
                        .putString("sc_returnedName", essentialList.get(position).getItemName())
                        .commit();
                startActivity(new Intent(GetProductFromEssentials.this, AddItemToList.class));
                finish();
            }
        });
    }
}
