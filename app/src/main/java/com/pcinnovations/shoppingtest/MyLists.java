package com.pcinnovations.shoppingtest;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pcinnovations.shoppingtest.common.ApiData;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class MyLists extends ActionBarActivity {

    private static final String API_BASE_ADDR = ApiData.API_ADDRESS + ApiData.SEPARATOR + ApiData.API_SUBFOLDER + ApiData.SEPARATOR;
    private String newListUri = API_BASE_ADDR + "AddList.php?user=1&name=";
    private String getListsUri = API_BASE_ADDR + "GetLists.php?user=1";
    private String renameListsUri = API_BASE_ADDR + "RenameList.php?list=";
    private String deleteListUri = API_BASE_ADDR + "DeleteList.php?list=";

    private static final String FILE_NAME = "lists.json";

    private String jsonString = "";

    private JSONArray jsonArray;

    private ActionMode mActionMode;
    public String editingId;

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

        jsonArray = getSavedLists();

        getListsFromJson(getSavedLists());
        refreshLists();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("LISTS", downloadedLists);
        outState.putStringArrayList("LISTS_IDS", downloadedListsIds);
        saveLists();
    }

    @Override
    protected void onDestroy() {
        saveLists();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_lists, menu);
        return true;
    }

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_lists_actions, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_renameList:
                    renameList(editingId);
                    mode.finish();
                    return true;
                case R.id.action_deleteList:
                    removeList(editingId);
                    mode.finish();
                    return true;
            }
            return false;
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            editingId = null;
            mActionMode = null;
        }
    };

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
                break;
            case R.id.action_new:
                createList();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public class NetworkTask extends AsyncTask<Uri,Void,String> {
        String requestType;

        @Override
        protected String doInBackground(Uri... uri) {
            if(uri[0].toString().contains("GetLists")) requestType = "GETLISTS";
            if(uri[0].toString().contains("AddList")) requestType = "NEWLIST";
            if(uri[0].toString().contains("RenameList")) requestType = "RENAMELIST";
            if(uri[0].toString().contains("DeleteList")) requestType = "DELETELIST";
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
                //Toast.makeText(MyLists.this, "Nie można połączyć z siecią!", Toast.LENGTH_SHORT).show();
                return "Nie można połączyć z siecią!";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                if(requestType == "GETLISTS") {
                    NotificationHandler handler = new NotificationHandler();
                    JSONArray lists = new JSONArray(result);
                    jsonArray = lists;
                    if(lists.length() < 1) {
                        downloadedLists.clear();
                        downloadedListsIds.clear();
                        downloadedLists.add("Brak list do wyświetlenia");
                        downloadedListsIds.add("0");
                    } else {
                        PendingIntent pendingIntent = PendingIntent.getActivity(MyLists.this, 0, new Intent(MyLists.this, MyLists.class), PendingIntent.FLAG_ONE_SHOT);
                        handler.sendNotification(getCurrentFocus(), MyLists.this, "Nowe listy!", "Pobrano nowe listy", android.R.drawable.ic_menu_add, pendingIntent);
                        downloadedLists.clear();
                        downloadedListsIds.clear();
                        Log.i("JSONArray", lists.toString());
                        jsonString = lists.toString();
                        for (int i = 0; i < lists.length(); i++) {
                            downloadedLists.add(lists.getJSONArray(i).getString(1));
                            downloadedListsIds.add(lists.getJSONArray(i).getString(0));
                        }
                        saveLists();
                    }
                    saveLists();
                    refreshLists();
                } else if(requestType == "NEWLIST") {
                    Toast.makeText(MyLists.this, "Lista została utworzona!", Toast.LENGTH_SHORT).show();
                    new NetworkTask().execute(Uri.parse(getListsUri));
                } else if(requestType == "RENAMELIST") {
                    Toast.makeText(MyLists.this, "Nazwa listy została zmieniona!", Toast.LENGTH_SHORT).show();
                    new NetworkTask().execute(Uri.parse(getListsUri));
                } else if(requestType == "DELETELIST") {
                    Toast.makeText(MyLists.this, "Lista została usunięta!", Toast.LENGTH_SHORT).show();
                    new NetworkTask().execute(Uri.parse(getListsUri));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void downloadLists() {
        new NetworkTask().execute(Uri.parse(getListsUri));
    }

    public void createList() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final View promptView = inflater.inflate(R.layout.layout_new_list, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.hint_new_list)
                .setView(promptView)
                .setIcon(R.drawable.ic_add)
                .setPositiveButton("Dodaj", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final EditText listName = (EditText) promptView.findViewById(R.id.txtListName);
                        String sendableListName;

                        if(listName.getText().toString().length() > 1)
                            sendableListName = Uri.encode(listName.getText().toString());
                        else
                            sendableListName = Uri.encode("Nowa lista");

                        new NetworkTask().execute(Uri.parse(newListUri + sendableListName));
                    }
                })
                .setNegativeButton("Anuluj", null)
                .show();
    }

    public void renameList(String _id) {
        final String editedId = _id;
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final View promptView = inflater.inflate(R.layout.layout_new_list, null);
        final TextView promptLabel = (TextView) promptView.findViewById(R.id.lblNewListName);
        promptLabel.setText("Podaj nową nazwę:");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.action_rename_list)
                .setView(promptView)
                .setIcon(R.drawable.ic_rename)
                .setPositiveButton("Zmień", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final EditText listName = (EditText) promptView.findViewById(R.id.txtListName);
                        String sendableListName;

                        if(listName.getText().toString().length() > 1)
                            sendableListName = Uri.encode(listName.getText().toString());
                        else
                            sendableListName = Uri.encode("Nowa lista");

                        new NetworkTask().execute(Uri.parse(renameListsUri + Uri.encode(editedId) + "&name=" + Uri.encode(sendableListName)));
                    }
                })
                .setNegativeButton("Anuluj", null)
                .show();
    }

    public void removeList(String _id) {
        final String editedId = _id;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.action_delete_list)
                .setMessage("Czy na pewno chcesz usunąć listę?")
                .setIcon(R.drawable.ic_remove)
                .setPositiveButton("Tak", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new NetworkTask().execute(Uri.parse(deleteListUri + Uri.encode(editedId)));
                    }
                })
                .setNegativeButton("Nie", null)
                .show();

    }

    public void refreshLists() {
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(MyLists.this, android.R.layout.simple_list_item_1, downloadedLists);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedList = downloadedListsIds.get(position);
                //Toast.makeText(MyLists.this, selectedList, Toast.LENGTH_SHORT).show();
                Intent startIntent = new Intent(MyLists.this, ShowList.class);
                getSharedPreferences(getString(R.string.shared_prefs_key), Context.MODE_PRIVATE).edit().putString("sc_listId", selectedList).putString("sc_listName",downloadedLists.get(position)).commit();
                startActivity(startIntent);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                editingId = downloadedListsIds.get(position);
                mActionMode = startSupportActionMode(mActionModeCallback);
                return true;
            }
        });
    }

    public JSONArray getSavedLists() {
        try {
            File dataLocation = getApplicationContext().getCacheDir();
            File savedFile = new File(dataLocation, FILE_NAME);
            if(!savedFile.exists()) return new JSONArray();
            if(savedFile.length() < 1) return new JSONArray();
            BufferedReader reader = new BufferedReader(new FileReader(savedFile));
            String savedJsonString = "";
            String line = "";
            while( (line = reader.readLine()) != null) {
                savedJsonString += line;
                Log.i("GetSavedLists", line);
            }
            reader.close();
            Log.i("GetSavedJsonString", savedJsonString);
            return new JSONArray(savedJsonString);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return new JSONArray();
        } catch (IOException e) {
            e.printStackTrace();
            return new JSONArray();
        } catch (JSONException e) {
            e.printStackTrace();
            return new JSONArray();
        } catch (NullPointerException e) {
            e.printStackTrace();
            return new JSONArray();
        }
    }

    public void saveLists() {
        try {
            File dataLocation = getApplicationContext().getCacheDir();
            File savedFile = new File(dataLocation, FILE_NAME);
            if(!savedFile.exists()) savedFile.createNewFile();
            FileWriter writer = new FileWriter(savedFile, false);
            writer.write(jsonArray.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getListsFromJson(JSONArray jsonObject) {
        Log.d("GetListsFromJson", jsonObject.toString());
        if(jsonObject.length() < 1 || jsonObject == null) {
            downloadedLists.clear();
            downloadedLists.add("Brak list do wyświetlenia");
            downloadedListsIds.clear();
            downloadedListsIds.add("0");
        } else {
            downloadedLists.clear();
            downloadedListsIds.clear();
            for(int i = 0; i < jsonObject.length(); i++) {
                try {
                    downloadedLists.add(jsonObject.getJSONArray(i).getString(1));
                    downloadedListsIds.add(jsonObject.getJSONArray(i).getString(0));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        refreshLists();
    }
}
