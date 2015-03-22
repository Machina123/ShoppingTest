package com.pcinnovations.shoppingtest;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;

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


        if(extras.getString("SCANNED_CODE") != null)
            scannedCode = extras.getString("SCANNED_CODE");

        resultView.setText("Proszę czekać...");

        new NetworkTask().execute(Uri.parse("http://93.180.174.49:50080/companion/GetProductByEAN.php?code=" + scannedCode));

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
                    Toast.makeText(FindProductByCodeActivity.this, "Nie znaleziono produktu o podanym kodzie kreskowym!", Toast.LENGTH_SHORT).show();
                    FindProductByCodeActivity.this.finish();
                }
                if(result.equalsIgnoreCase("banana")) {
                    Toast.makeText(FindProductByCodeActivity.this, "Produkt został zapisany!", Toast.LENGTH_SHORT).show();
                    FindProductByCodeActivity.this.finish();
                }
                resultView.setText("Znaleziono produkt: " + result);
                new NetworkTask().execute(Uri.parse("http://93.180.174.49:50080/companion/AddToProductCache.php?ean=" + Uri.encode(scannedCode) + "&name=" + Uri.encode(result)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
