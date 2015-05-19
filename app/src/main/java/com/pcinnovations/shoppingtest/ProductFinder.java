package com.pcinnovations.shoppingtest;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;


public class ProductFinder extends ActionBarActivity {
    public static final int REQUEST_FIND_BY_CODE = 0xc0de;
    public static final int REQUEST_FIND_BY_NAME = 0x1234;
    public static String scannedCode;
    public static String listId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_finder);

        listId = getIntent().getStringExtra("listId");

        getSharedPreferences(getString(R.string.shared_prefs_key), Context.MODE_PRIVATE).edit().putString("sc_listId", listId).commit();

        switch(getIntent().getIntExtra("requestCode", 0)) {
            case 0:
                setResult(RESULT_CANCELED);
                finishActivity(getIntent().getIntExtra("requestCode", 0));
                break;
            case 1:     // pobieranie po kodzie kreskowym
                IntentIntegrator scanIntegrator = new IntentIntegrator(this);
                scanIntegrator.initiateScan();
                break;
            case 2:     // pobieranie po nazwie
                Intent startIntent = new Intent(ProductFinder.this, GetProductsByName.class);
                startIntent.putExtra("requestCode", REQUEST_FIND_BY_NAME);
                startActivity(startIntent);
                finish();
                break;

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d("SC_ProductFinder", "Wywołano onActivityResult, kod zapytania " + String.valueOf(requestCode) + ", wynik zapytania " + String.valueOf(resultCode));
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            scannedCode = scanResult.getContents();
            new NetworkTask().execute((Uri.parse("http://93.180.174.49:50080/companion/GetProductFromCache.php?ean=" + Uri.encode(scannedCode))));
        } else {
            finish();
        }
    }

    public void endActivity(String ean, String name) {
        Intent startIntent = new Intent(ProductFinder.this, AddItemToList.class);
        getSharedPreferences(getString(R.string.shared_prefs_key), Context.MODE_PRIVATE).edit().putString("sc_returnedEan", ean).putString("sc_returnedName", name).commit();
        ProductFinder.this.startActivity(startIntent);
        ProductFinder.this.finish();
    }

    public class NetworkTask extends AsyncTask<Uri,Void,String> {

        final ProgressDialog dialog = ProgressDialog.show(ProductFinder.this, "Proszę czekać", "Pobieranie informacji o produkcie", false);

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
                //Toast.makeText(ProductFinder.this, "Nie można połączyć z siecią!", Toast.LENGTH_SHORT).show();
                return "Nie można połączyć z siecią!";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                dialog.dismiss();
                if(result.equalsIgnoreCase("NOT_FOUND")) {
                    new NetworkTask().execute((Uri.parse("http://93.180.174.49:50080/companion/GetProductByEAN.php?code=" + Uri.encode(scannedCode))));
                } else if(result.equals("Unknown")) {
                    Toast.makeText(ProductFinder.this, "Nie znaleziono produktu o podanym kodzie kreskowym!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    endActivity(scannedCode, result);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
