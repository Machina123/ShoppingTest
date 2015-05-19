package com.pcinnovations.shoppingtest;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;


public class AddItemToList extends ActionBarActivity {

    private String listId;
    private String listName;

    private String returnedEan;
    private String returnedName;

    public boolean selectedFromEssentials = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        listName = getSharedPreferences(getString(R.string.shared_prefs_key), Context.MODE_PRIVATE).getString("sc_listName", "null");
        listId = getSharedPreferences(getString(R.string.shared_prefs_key), Context.MODE_PRIVATE).getString("sc_listId", "null");

        Log.d("SC_AddItemToList", "ID listy: " + listId + " | Nazwa listy: " + listName);

        returnedEan = getSharedPreferences(getString(R.string.shared_prefs_key), Context.MODE_PRIVATE).getString("sc_returnedEan", "0000000000000");
        returnedName = getSharedPreferences(getString(R.string.shared_prefs_key), Context.MODE_PRIVATE).getString("sc_returnedName", "null");
        selectedFromEssentials = getSharedPreferences(getString(R.string.shared_prefs_key), Context.MODE_PRIVATE).getBoolean("sc_isFromEssentials", false);

        if(!returnedEan.equals("0000000000000") && !returnedName.equals("null")) {
            addProduct();
        }

        setContentView(R.layout.activity_add_item_to_list);
        Button btnGetFromRecent = (Button) findViewById(R.id.btnGetFromRecent);
        Button btnGetFromDb = (Button) findViewById(R.id.btnGetFromDb);
        Button btnGetFromEss = (Button) findViewById(R.id.btnGetFromEssentials);
        getSupportActionBar().setSubtitle(listName);

        btnGetFromRecent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startIntent = new Intent(AddItemToList.this, SavedProducts.class);
                startIntent.putExtra("sc_requestCode", 1);
                startActivity(startIntent);
            }
        });

        btnGetFromEss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AddItemToList.this, GetProductFromEssentials.class));
            }
        });

        btnGetFromDb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AddItemToList.this);
                builder.setTitle("Pytanie")
                        .setMessage("W jaki sposób wyszukać dodawany produkt?")
                        .setPositiveButton("Kod kreskowy", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent startIntent = new Intent(AddItemToList.this, ProductFinder.class);
                                startIntent.putExtra("requestCode", 1);
                                startIntent.putExtra("listId", listId);
                                startActivity(startIntent);

                            }
                        })
                        .setNegativeButton("Nazwa produktu", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent startIntent = new Intent(AddItemToList.this, ProductFinder.class);
                                startIntent.putExtra("requestCode", 2);
                                startIntent.putExtra("listId", listId);
                                startActivity(startIntent);
                            }
                        })
                        .show();
            }
        });
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
                //Toast.makeText(AddItemToList.this, "Nie można połączyć z siecią!", Toast.LENGTH_SHORT).show();
                return "Nie można połączyć z siecią!";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                if(result.equals("BANANA"))
                    Toast.makeText(AddItemToList.this, "Dodano produkt do listy!", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(AddItemToList.this, "Nie udało się dodać produktu do listy", Toast.LENGTH_SHORT).show();

                getSharedPreferences(getString(R.string.shared_prefs_key), Context.MODE_PRIVATE).edit().clear().putString("sc_listId", listId).putString("sc_listName", listName).commit();
                AddItemToList.this.finish();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void addProduct() {

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final View promptView = inflater.inflate(R.layout.layout_set_product_amount, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.label_add_product_dialog)
                .setView(promptView)
                .setIcon(R.drawable.ic_rename)
                .setPositiveButton("Dodaj produkt", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final EditText prodAmount = (EditText) promptView.findViewById(R.id.txtProductAmount);
                        String sendableAmount;

                        if (prodAmount.getText().toString().length() >= 1)
                            sendableAmount = Uri.encode(prodAmount.getText().toString());
                        else
                            sendableAmount = Uri.encode("1");

                        new NetworkTask().execute(Uri.parse("http://93.180.174.49:50080/companion/AddItemToList.php?list=" + Uri.encode(listId) + "&ean=" + Uri.encode(returnedEan) + "&name=" + Uri.encode(returnedName) + "&amount=" + sendableAmount + "&save=" + (selectedFromEssentials ? "1" : "0" ) ));
                        if(selectedFromEssentials) getSharedPreferences(getString(R.string.shared_prefs_key), Context.MODE_PRIVATE).edit().remove("sc_isFromEssentials").commit();
                    }
                })
                .setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getSharedPreferences(getString(R.string.shared_prefs_key), Context.MODE_PRIVATE).edit().clear().putString("sc_listId", listId).putString("sc_listName", listName).commit();
                    }
                })
                .show();
    }
}
