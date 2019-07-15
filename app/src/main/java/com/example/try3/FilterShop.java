package com.example.try3;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class FilterShop extends AppCompatActivity {
    public final static String SHOP_ID = "com.example.try.SHOP_ID";
    public final static String SHOP_NAME = "com.example.try.SHOP_NAME";

    ArrayList<String> shops = new ArrayList<String>();
    ArrayList<String> shopsID = new ArrayList<String>();
    HashMap<Integer,Boolean> shopsSelectedID = new HashMap<Integer,Boolean>();

    ListView listView;
    Toolbar mToolbar;

    String db_url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_shop);
        listView = (ListView) findViewById(R.id.listview_shop);

        db_url = getString(R.string.db_url);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mToolbar.setTitle("Выберите магазин");

        BackgroundTask backgroundTask = new BackgroundTask();
        backgroundTask.execute();
    }

    class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return shops.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = getLayoutInflater().inflate(R.layout.listview_item,null);

            CheckedTextView textViewFilter = view.findViewById(R.id.textView_filter);

            textViewFilter.setText(shops.get(position));
            return view;
        }
    }

    class BackgroundTask extends AsyncTask<String,Void,String> {
        String add_info_url;

        @Override
        protected void onPreExecute() {
            add_info_url = db_url + "filter_shops_info.php";
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                URL url = new URL(add_info_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setDoOutput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();

                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                String response = "";
                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    response += line;
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                return response;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String result) {
            if (!result.isEmpty()) {
                String[] separated = result.trim().split(";");
                for (int i = 0; i < separated.length; i++) {
                    String[] subseparated = separated[i].split(":");
                    shopsID.add(subseparated[0]);
                    shopsSelectedID.put(Integer.valueOf(subseparated[0]),false);
                    shops.add(subseparated[1]);
                }
            }
            CustomAdapter customAdapter = new CustomAdapter();
            listView.setAdapter(customAdapter);
            listView.setChoiceMode(1);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> adapter, View v, int position, long id)
                {
                    CheckedTextView selectedShop = (CheckedTextView) v.findViewById(R.id.textView_filter);
                    int num = Integer.valueOf(shopsID.get(position));
                    if (selectedShop.isChecked())
                    {
                        selectedShop.setChecked(false);
                        shopsSelectedID.put(num,false);
                    }
                    else {
                        selectedShop.setChecked(true);
                        shopsSelectedID.put(num,true);
                    }
                }
            });
        }
    }

    public void filterGoBack(View view)
    {
        Intent answerIntent = new Intent();

        String checkedItemPositions = "";
        String checkedShopNames = "";

        for (int i = 0; i < shopsID.size(); i++) {
            if (shopsSelectedID.get(Integer.valueOf(shopsID.get(i)))) {
                checkedItemPositions += shopsID.get(i) + ";";
                checkedShopNames += shops.get(i) + ", ";
            }
        }
        answerIntent.putExtra(SHOP_ID, checkedItemPositions);
        answerIntent.putExtra(SHOP_NAME, checkedShopNames);

        setResult(RESULT_OK, answerIntent);
        finish();
    }

}
