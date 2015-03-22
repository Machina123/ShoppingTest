package com.pcinnovations.shoppingtest;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class ShowList extends ActionBarActivity {
    private String listId;
    private String listName;
    protected ListView listItems;
    protected ArrayList<String> itemIds = new ArrayList<String>();

    protected String mProductId;
    private ActionMode mActionMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_list);
        listItems = (ListView) findViewById(R.id.listListItems);
        try {
            listId = getSharedPreferences(getString(R.string.shared_prefs_key), Context.MODE_PRIVATE).getString("sc_listId", "null");
            listName = getSharedPreferences(getString(R.string.shared_prefs_key), Context.MODE_PRIVATE).getString("sc_listName", "null");
        } catch(Exception e) {
            e.printStackTrace();
        }
        getSupportActionBar().setTitle(listName);
        getItems();
    }

    public void getItems() {
        new NetworkTask().execute(Uri.parse("http://93.180.174.49:50080/companion/GetItemsFromList.php?list=" + Uri.encode(listId)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        getItems();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_show_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id){
            case R.id.action_add_product:
                addProductToList();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public class NetworkTask extends AsyncTask<Uri,Void,String> {
        String operation = "";
        @Override
        protected String doInBackground(Uri... uri) {
            if(uri[0].toString().contains("GetItemsFromList")) operation = "get";
            else operation = "default";

            HttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(uri[0].toString());
            HttpResponse response;
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
                //Toast.makeText(ShowList.this, "Nie można połączyć z siecią!", Toast.LENGTH_SHORT).show();
                return "Nie można połączyć z siecią!";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            switch(operation) {
                case "get" :
                    //Toast.makeText(ShowList.this, "Aktualizowanie listy...", Toast.LENGTH_SHORT).show();
                    updateLists(result);
                    break;
                case "default":
                    new NetworkTask().execute(Uri.parse("http://93.180.174.49:50080/companion/GetItemsFromList.php?list=" + Uri.encode(listId)));
                    break;
            }

        }
    }

    public JSONArray getJsonArrayFromString(String jsonString) {
        try {
            return new JSONArray(jsonString);
        } catch(Exception e) {
            e.printStackTrace();
            return new JSONArray();
        }
    }

    public ArrayList<ListItem> getItemsFromJson(JSONArray jsonArray) {
        ArrayList<ListItem> returned = new ArrayList<ListItem>();
        itemIds.clear();
        for(int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject json = jsonArray.getJSONObject(i);
                ListItem li = new ListItem();
                itemIds.add(json.getString("id"));
                li.setEan(json.getString("ean"));
                li.setName(json.getString("nazwa"));
                li.setAmount(json.getString("ilosc"));
                li.setBought(json.getInt("kupione"));
                returned.add(li);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return returned;
    }

     private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_list_item_actions, menu);
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
                case R.id.action_changeAmount:
                    changeProductAmount(mProductId);
                    mode.finish();
                    return true;
                case R.id.action_deleteListItem:
                    deleteFromList(mProductId);
                    mode.finish();
                    return true;
            }
            return false;
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mProductId = null;
            mActionMode = null;
        }
    };

    public void changeProductAmount(String productId) {
        final String paramProdId = productId;
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final View promptView = inflater.inflate(R.layout.layout_set_product_amount, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.label_set_amount)
                .setView(promptView)
                .setIcon(R.drawable.ic_rename)
                .setPositiveButton(R.string.action_change_amount, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final EditText prodAmount = (EditText) promptView.findViewById(R.id.txtProductAmount);
                        String sendableAmount;

                        if (prodAmount.getText().toString().length() >= 1)
                            sendableAmount = Uri.encode(prodAmount.getText().toString());
                        else
                            sendableAmount = Uri.encode("1");

                        new NetworkTask().execute(Uri.parse("http://93.180.174.49:50080/companion/ChangeProductAmount.php?list=" + Uri.encode(listId)  + "&id=" + Uri.encode(paramProdId) + "&amount=" + sendableAmount ));
                    }
                })
                .setNegativeButton("Anuluj", null)
                .show();
    }

    public void addProductToList() {
        Intent intent = new Intent(ShowList.this, AddItemToList.class);
        startActivity(intent);
    }

    public void deleteFromList(final String productId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Usuwanie")
                .setMessage("Czy na pewno chcesz usunąć ten wpis?")
                .setPositiveButton("Tak", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new NetworkTask().execute(Uri.parse("http://93.180.174.49:50080/companion/DeleteItemFromList.php?list=" + Uri.encode(listId) + "&id=" + Uri.encode(productId)));
                    }
                })
                .setNegativeButton("Nie", null)
                .setCancelable(false)
                .setIcon(R.drawable.ic_question_mark)
                .show();
    }

    public void updateLists(String result) {
        final ArrayList<ListItem> productsArray = getItemsFromJson(getJsonArrayFromString(result));
        final ListItemListAdapter adapter = new ListItemListAdapter(ShowList.this, R.layout.layout_list_item, productsArray);

        listItems.setAdapter(adapter);
        listItems.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                mProductId = itemIds.get(position);
                mActionMode = startSupportActionMode(mActionModeCallback);
                return true;
            }
        });
        listItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                new NetworkTask().execute(Uri.parse("http://93.180.174.49:50080/companion/SetBought.php?list=" + Uri.encode(listId) + "&id=" + Uri.encode(itemIds.get(position)) + "&bought=" + Uri.encode(productsArray.get(position).isBought() == 0 ? "1" : "0")));
            }
        });
    }
}
