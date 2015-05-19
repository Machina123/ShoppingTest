package com.pcinnovations.shoppingtest;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;


public class FindProductByCodeActivity extends ActionBarActivity {

    public String scannedCode;
    public Bundle extras;
    public TextView resultView;
    private int requestCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        requestCode = extras.getInt("request", 0);

        setContentView(R.layout.activity_find_product_by_code);

        getSupportActionBar().setTitle(R.string.title_scan_results);

        resultView = (TextView)findViewById(R.id.txtResult);

        if (savedInstanceState != null) {
            extras = savedInstanceState.getBundle("EXTRAS");
        } else {
           extras = getIntent().getExtras();
        }


        if(extras.getString("SCANNED_CODE") != null) {
            scannedCode = extras.getString("SCANNED_CODE");

            resultView.setText("Proszę czekać...");

            new NetworkTask().execute(Uri.parse("http://93.180.174.49:50080/companion/GetProductByEAN.php?code=" + scannedCode));
        } else {
            finish();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle("EXTRAS", extras);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    public class NetworkTask extends AsyncTask<Uri,Void,String> {

        final ProgressDialog dialog = ProgressDialog.show(FindProductByCodeActivity.this, "Proszę czekać", "Pobieranie informacji o produkcie", false);

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
                //Toast.makeText(FindProductByCodeActivity.this, "Nie można połączyć z siecią!", Toast.LENGTH_SHORT).show();
                return "Nie można połączyć z siecią!";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                dialog.dismiss();
                if(result.equalsIgnoreCase("Unknown")) {
                    final LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                    final View promptView = inflater.inflate(R.layout.layout_add_custom, null);
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(FindProductByCodeActivity.this);
                    alertDialog.setTitle("Własny produkt")
                            .setView(promptView)
                            .setPositiveButton("Dodaj", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    EditText nameField = (EditText) promptView.findViewById(R.id.txtCustomName);
                                    String sendableName = Uri.encode(nameField.getText().toString());

                                    new NetworkTask().execute(Uri.parse("http://93.180.174.49:50080/companion/AddToProductCache.php?ean=" + Uri.encode(String.valueOf(scannedCode)) + "&name=" + sendableName));
                                }
                            })
                            .setNegativeButton("Anuluj",null)
                            .show();
                } else if(result.equalsIgnoreCase("banana")) {
                    Toast.makeText(FindProductByCodeActivity.this, "Produkt został zapisany!", Toast.LENGTH_SHORT).show();
                    FindProductByCodeActivity.this.finish();
                } else {
                    resultView.setText("Znaleziono produkt: " + result);
                    new NetworkTask().execute(Uri.parse("http://93.180.174.49:50080/companion/AddToProductCache.php?ean=" + Uri.encode(scannedCode) + "&name=" + Uri.encode(result)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
