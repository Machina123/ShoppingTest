package com.pcinnovations.shoppingtest;

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
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MyLists extends ActionBarActivity {

    private ListView listView;
    public ArrayList<String> downloadedLists = new ArrayList<String>();
    public ArrayList<String> downloadedListsIds = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_lists);
        getSupportActionBar().setTitle(R.string.menu_my_lists);

        downloadedListsIds.add("0");
        downloadedLists.add("Nie masz żadnej listy :( \nKliknij \"Pobierz\" aby odświeżyć");
        listView = (ListView) findViewById(R.id.lvDownloadedLists);

        if(savedInstanceState != null) {
            downloadedLists.clear();
            downloadedLists = savedInstanceState.getStringArrayList("LISTS");
            downloadedListsIds.clear();
            downloadedListsIds = savedInstanceState.getStringArrayList("LISTS_IDS");
        }
        refreshLists();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("LISTS", downloadedLists);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_lists, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id){
            case R.id.action_download:
                downloadLists();
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
                return "Nie można połączyć z siecią!";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                NotificationHandler handler = new NotificationHandler();
                JSONArray lists = new JSONArray(result);
                handler.sendNotification(getCurrentFocus(), MyLists.this, "Nowe listy!", "Pobrano nowe listy", android.R.drawable.ic_menu_add);
                downloadedLists.clear();
                downloadedListsIds.clear();
                Log.i("JSONArray", lists.toString());
                for(int i = 0; i < lists.length(); i++) {
                    downloadedLists.add(lists.getJSONArray(i).getString(1));
                    downloadedListsIds.add(lists.getJSONArray(i).getString(0));
                }
                refreshLists();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void downloadLists() {
        new NetworkTask().execute(Uri.parse("http://93.180.174.49:50080/companion/GetLists.php?user=1"));
    }

    public void refreshLists() {
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(MyLists.this, android.R.layout.simple_list_item_1, downloadedLists);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedList = downloadedListsIds.get(position);
                Toast.makeText(MyLists.this, selectedList, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
