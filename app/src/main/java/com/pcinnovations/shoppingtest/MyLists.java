package com.pcinnovations.shoppingtest;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class MyLists extends ActionBarActivity {

    private String newListUri = "http://93.180.174.49:50080/companion/AddList.php?user=1&name=";
    private String getListsUri = "http://93.180.174.49:50080/companion/GetLists.php?user=1";
    private String renameListsUri = "http://93.180.174.49:50080/companion/RenameList.php?list=";
    private String deleteListUri = "http://93.180.174.49:50080/companion/DeleteList.php?list=";

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
                return "Nie można połączyć z siecią!";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                if(requestType == "GETLISTS") {
                    NotificationHandler handler = new NotificationHandler();
                    JSONArray lists = new JSONArray(result);
                    if(lists.length() < 1) {
                        downloadedLists.clear();
                        downloadedListsIds.clear();
                        downloadedLists.add("Brak list do wyświetlenia");
                        downloadedListsIds.add("0");
                    } else {
                        handler.sendNotification(getCurrentFocus(), MyLists.this, "Nowe listy!", "Pobrano nowe listy", android.R.drawable.ic_menu_add);
                        downloadedLists.clear();
                        downloadedListsIds.clear();
                        Log.i("JSONArray", lists.toString());
                        for (int i = 0; i < lists.length(); i++) {
                            downloadedLists.add(lists.getJSONArray(i).getString(1));
                            downloadedListsIds.add(lists.getJSONArray(i).getString(0));
                        }
                    }
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
                Toast.makeText(MyLists.this, selectedList, Toast.LENGTH_SHORT).show();
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
}
