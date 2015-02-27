package com.pcinnovations.shoppingtest;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.w3c.dom.Text;

public class AddProduct extends ActionBarActivity implements OnClickListener {

    private Button btnInitiateScan;
    private TextView lastScanned;
    private TextView barcodeApp;
    public String lastCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);
        getSupportActionBar().setTitle(R.string.menu_add_product);
        btnInitiateScan = (Button) findViewById(R.id.btnAddProdScanCode);
        lastScanned = (TextView) findViewById(R.id.textLastScanned);
        barcodeApp = (TextView) findViewById(R.id.txtBarcodeApp);
        if(savedInstanceState != null) {
            lastCode = savedInstanceState.getString("LAST_CODE");
        }
        if(lastCode != null) lastScanned.setText(getString(R.string.label_last_product) + " " + lastCode);
        btnInitiateScan.setOnClickListener(this);
        barcodeApp.setOnClickListener(this);
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
                            .setIcon(android.R.drawable.stat_notify_error)
                            .show();
                }
                break;
        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode,resultCode,intent);
        if(scanResult != null) {
            String scannedCode = scanResult.getContents();
            // Toast.makeText(AddProduct.this, "ZNALEZIONO KOD: " + scannedCode, Toast.LENGTH_LONG).show();
            lastCode = scannedCode;
            lastScanned.setText(getString(R.string.label_last_product) + " " + scannedCode);
            Intent scanResultActivity = new Intent(AddProduct.this, FindProductByCodeActivity.class)
                    .putExtra("SCANNED_CODE", scannedCode);
            startActivity(scanResultActivity);

        } else {
            Toast.makeText(AddProduct.this, "Nie znaleziono kodu!", Toast.LENGTH_SHORT).show();
        }
    }
}
