package com.pcinnovations.shoppingtest;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Random;

public class AddProduct extends ActionBarActivity implements OnClickListener {

    private Button btnInitiateScan;
    private Button btnFindByName;
    private Button btnAddCustom;
    //private TextView lastScanned;
    private TextView barcodeApp;
    public String lastCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);
        getSupportActionBar().setTitle(R.string.menu_add_product);
        btnInitiateScan = (Button) findViewById(R.id.btnAddProdScanCode);
        btnFindByName = (Button)findViewById(R.id.btnAddProdEnterName);
        btnAddCustom = (Button) findViewById(R.id.btnAddCustomproduct);
        //lastScanned = (TextView) findViewById(R.id.textLastScanned);
        barcodeApp = (TextView) findViewById(R.id.txtBarcodeApp);
        if(savedInstanceState != null) {
            lastCode = savedInstanceState.getString("LAST_CODE");
        }
        // if(lastCode != null) lastScanned.setText(getString(R.string.label_last_product) + " " + lastCode);
        btnInitiateScan.setOnClickListener(this);
        barcodeApp.setOnClickListener(this);
        btnFindByName.setOnClickListener(this);
        btnAddCustom.setOnClickListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(lastCode != null) outState.putString("LAST_CODE", lastCode);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public void onClick(View view) {

        switch(view.getId()) {
            case R.id.btnAddProdScanCode:
                IntentIntegrator scanIntegrator = new IntentIntegrator(this);
                scanIntegrator.initiateScan();
                break;
            case R.id.txtBarcodeApp:
                try {
                    Intent playStoreIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.zxing.client.android"));
                    AddProduct.this.startActivity(playStoreIntent);
                } catch(ActivityNotFoundException e) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(AddProduct.this);
                    alertDialog.setTitle(R.string.dialog_title_error)
                            .setMessage(R.string.dialog_market_not_found)
                            .setPositiveButton(R.string.dialog_ack, null)
                            .setIcon(R.drawable.ic_error_notif)
                            .show();
                }
                break;
            case R.id.btnAddProdEnterName:
                Intent newWindow = new Intent(AddProduct.this, GetProductsByName.class);
                startActivity(newWindow);
                break;
            case R.id.btnAddCustomproduct:
                final LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                final View promptView = inflater.inflate(R.layout.layout_add_custom, null);
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(AddProduct.this);
                alertDialog.setTitle("Własny produkt")
                        .setView(promptView)
                        .setPositiveButton("Dodaj", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int randomEan = AddProduct.getRandom(10000000, 20000000);
                                EditText nameField = (EditText) promptView.findViewById(R.id.txtCustomName);
                                String sendableName = Uri.encode(nameField.getText().toString());

                                new NetworkTask().execute(Uri.parse("http://93.180.174.49:50080/companion/AddToProductCache.php?ean=" + Uri.encode(String.valueOf(randomEan)) + "&name=" + sendableName));
                            }
                        })
                        .setNegativeButton("Anuluj",null)
                        .show();
                break;

        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode,resultCode,intent);
        if(scanResult != null) {
            String scannedCode = scanResult.getContents();
            // Toast.makeText(AddProduct.this, "ZNALEZIONO KOD: " + scannedCode, Toast.LENGTH_LONG).show();
            lastCode = scannedCode;
            //lastScanned.setText(getString(R.string.label_last_product) + " " + scannedCode);
            Intent scanResultActivity = new Intent(AddProduct.this, FindProductByCodeActivity.class)
                    .putExtra("SCANNED_CODE", scannedCode);
            startActivity(scanResultActivity);

        } else {
            Toast.makeText(AddProduct.this, "Nie znaleziono kodu!", Toast.LENGTH_SHORT).show();
        }
    }

    public static int getRandom(int min, int max){
        Random random = new Random();
        return(min + random.nextInt(max - min));
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
                //Toast.makeText(ProductFinder.this, "Nie można połączyć z siecią!", Toast.LENGTH_SHORT).show();
                return "Nie można połączyć z siecią!";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
               if(result.equalsIgnoreCase("BANANA")) {
                   Toast.makeText(AddProduct.this, "Produkt dodany!", Toast.LENGTH_SHORT).show();
               } else {
                   Toast.makeText(AddProduct.this, "Nie udało się dodać produktu", Toast.LENGTH_SHORT).show();
               }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
