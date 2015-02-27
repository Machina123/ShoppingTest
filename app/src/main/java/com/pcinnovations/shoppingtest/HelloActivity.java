package com.pcinnovations.shoppingtest;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class HelloActivity extends ActionBarActivity {

    public Button btnMyLists;
    public Button btnAddProduct;
    public Button btnListProducts;
    public Button btnUserMgmt;
    public Button btnSettings;
    public Button btnExitApp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello);
        getSupportActionBar().setTitle(getString(R.string.project_name));
        getSupportActionBar().setLogo(R.mipmap.ic_new);
        getSupportActionBar().setIcon(R.mipmap.ic_new);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        btnMyLists = (Button)findViewById(R.id.buttonMyLists);
        btnAddProduct = (Button) findViewById(R.id.buttonAddProduct);
        btnListProducts = (Button) findViewById(R.id.buttonListProducts);
        btnUserMgmt = (Button) findViewById(R.id.buttonUserMgmt);

        addButtonListeners();
    }

    public void addButtonListeners() {
        btnMyLists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMyListClick();
            }
        });

        btnAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddProductClick();
            }
        });

        btnListProducts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onListProductsClick();
            }
        });

        btnUserMgmt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onUserMgmtClick();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_hello, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id) {
            case R.id.action_settings:
                onSettingsClick();
                return true;
            case R.id.action_exitApp:
                onExitClick();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static long back_pressed;
    @Override
    public void onBackPressed()
    {
        if (back_pressed + 1000 > System.currentTimeMillis()) super.onBackPressed();
        else Toast.makeText(this, getString(R.string.action_press_back), Toast.LENGTH_SHORT).show();

        back_pressed = System.currentTimeMillis();
    }

    public void onMyListClick() {
        startActivity(new Intent(this, MyLists.class));
    }

    public void onAddProductClick() {
        startActivity(new Intent(this, AddProduct.class));
    }

    public void onListProductsClick() {
        Toast.makeText(this,"Zapisane produkty", Toast.LENGTH_SHORT).show();
    }

    public void onUserMgmtClick() {
        Toast.makeText(this,"Zarządzanie użytkownikami", Toast.LENGTH_SHORT).show();
    }

    public void onSettingsClick() {
        Toast.makeText(this,"Ustawienia", Toast.LENGTH_SHORT).show();
    }

    public void onExitClick() {
        finish();
    }
}
