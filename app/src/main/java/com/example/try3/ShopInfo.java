package com.example.try3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ShopInfo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_information);
        Intent intent = getIntent();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        String shopAddr = intent.getStringExtra(ProductInfo.EXTRA_SHOP_ADDR);
        String shopMetro = intent.getStringExtra(ProductInfo.EXTRA_SHOP_METRO);
        String shopWorkTime = intent.getStringExtra(ProductInfo.EXTRA_SHOP_WORK_TIME);
        String shopContacts = intent.getStringExtra(ProductInfo.EXTRA_SHOP_CONTACTS);

        TextView addr = (TextView) findViewById(R.id.address);
        TextView workTime = (TextView) findViewById(R.id.work_time);
        TextView contacts = (TextView) findViewById(R.id.contacts);
        TextView workTimeTitle = (TextView) findViewById(R.id.work_time_title);
        TextView contactsTitle = (TextView) findViewById(R.id.contacts_title);


        if(!shopMetro.equals(""))
            addr.setText(shopAddr + " (Ⓜ " + shopMetro + ")");
        else
            addr.setText(shopAddr);

        if(!shopWorkTime.equals("")) {
            workTime.setText(shopWorkTime);
        }
        else{
            workTimeTitle.setHeight(0);
            workTime.setHeight(0);
        }
        if(!shopContacts.equals("")) {
            contacts.setText(shopContacts);
        }
        else{
            contactsTitle.setHeight(0);
            contacts.setHeight(0);
        }
    }

}

