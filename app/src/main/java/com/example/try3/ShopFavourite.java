package com.example.try3;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

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
import java.net.URLEncoder;
import java.util.ArrayList;

import static com.example.try3.MainActivity.APP_PREFERENCES;
import static com.example.try3.MainActivity.APP_PREFERENCES_ID_FAVOURITE;

public class ShopFavourite extends AppCompatActivity {
    public final static String FAVOURITE_SHOP_ID = "com.example.try.FAVOURITE_SHOP_ID";
    private SharedPreferences mSettings;
    int id_favourite_selected = 0;

    ListView listShops;
    ArrayList<String> shopsIDArray = new ArrayList<String>();
    ArrayList<String> shopsArray = new ArrayList<String>();

    String favouriteShopSelected;
    TextView favouriteShopSelectedView, NewShopFavourteItem;
    Toolbar mToolbar;

    String db_url;
    Button okBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite_shop);
        db_url = getString(R.string.db_url);
        listShops = (ListView) findViewById(R.id.favourite_shop_list);
        okBtn = (Button) findViewById(R.id.favourite_shop_chose);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if (mSettings.contains(APP_PREFERENCES_ID_FAVOURITE)) {
            id_favourite_selected = mSettings.getInt(APP_PREFERENCES_ID_FAVOURITE, 0);
        }
        BackgroundTask backgroundTask = new BackgroundTask();
        backgroundTask.execute();
    }

    class ShopsAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return shopsArray.size();
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

            View view = getLayoutInflater().inflate(R.layout.listview_product_shops_item,null);
            TextView textViewMetro = view.findViewById(R.id.metro);
            textViewMetro.setText(shopsArray.get(position));
            if(shopsIDArray.get(position).equals(String.valueOf(id_favourite_selected))){
                textViewMetro.setBackgroundColor(getResources().getColor(R.color.favourite_shop_item_selected));
                favouriteShopSelectedView = textViewMetro;
            }
            return view;
        }
    }

    class BackgroundTask extends AsyncTask<String,Void,String>
    {
        String add_info_url;
        @Override
        protected void onPreExecute() {
            add_info_url = db_url + "shops_info.php";
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                URL url = new URL(add_info_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setDoOutput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();

                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
                String response = "";
                String line = "";
                while((line = bufferedReader.readLine())!=null)
                {
                    response += line;
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                return response;

            }
            catch (MalformedURLException e)
            {
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
                String[] separated = result.trim().split("&");
                for (int i = 0; i < separated.length; i++) {
                    String[] subseparated = separated[i].trim().split("/");
                    shopsIDArray.add(subseparated[0]);
                    Log.d("Metro", separated[i]);
                    if(subseparated.length >= 3)
                        shopsArray.add(subseparated[1] + " (Ⓜ " + subseparated[2] + ")");
                    else
                        shopsArray.add(subseparated[1]);
                }
            }
            ShopsAdapter shopsAdapter = new ShopsAdapter();
            listShops.setAdapter(shopsAdapter);
            if (id_favourite_selected != 0){
                okBtn.setEnabled(true);
                okBtn.setTextColor(getResources().getColor(R.color.favourite_button_text));
                okBtn.setBackgroundColor(getResources().getColor(R.color.favourite_button_bg));
                favouriteShopSelected = String.valueOf(id_favourite_selected);
            }
            else{
                okBtn.setEnabled(false);
                okBtn.setTextColor(getResources().getColor(R.color.favourite_button_text_nenable));
                okBtn.setBackgroundColor(getResources().getColor(R.color.favourite_button_bg_nenable));
            }
            listShops.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> adapter, View v, int position, long id)
                {
                    NewShopFavourteItem = (TextView) v.findViewById(R.id.metro);
                    int colorFrom = getResources().getColor(R.color.product_shops_item);
                    int colorTo = getResources().getColor(R.color.favourite_shop_item_selected);
                    BackgroundColorUpdate(NewShopFavourteItem, colorTo, colorFrom);
                    if(favouriteShopSelectedView != null && favouriteShopSelectedView != NewShopFavourteItem) {
                        BackgroundColorUpdate(favouriteShopSelectedView, colorFrom, colorTo);
                    }
                    favouriteShopSelectedView = NewShopFavourteItem;
                    favouriteShopSelected = shopsIDArray.get(position);
                    if(!okBtn.isEnabled()) {
                        BackgroundColorUpdate(okBtn, getResources().getColor(R.color.favourite_button_bg), getResources().getColor(R.color.favourite_button_bg_nenable));
                        TextColorUpdate(okBtn, getResources().getColor(R.color.favourite_button_text), getResources().getColor(R.color.favourite_button_text_nenable));
                        okBtn.setEnabled(true);
                    }
                }
            });

        }
    }

    public void BackgroundColorUpdate(final View v, int colorFrom, int colorTo)
    {
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorTo, colorFrom);
        colorAnimation.setDuration(250); // milliseconds
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                v.setBackgroundColor((int) animator.getAnimatedValue());
            }
        });
        colorAnimation.start();
    }
    public void TextColorUpdate(final TextView v, int colorFrom, int colorTo)
    {
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorTo, colorFrom);
        colorAnimation.setDuration(250); // milliseconds
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                v.setTextColor((int) animator.getAnimatedValue());
            }
        });
        colorAnimation.start();
    }

    public void FavouriteOk(View view)
    {
        Intent answerIntent = new Intent();
        answerIntent.putExtra(FAVOURITE_SHOP_ID, favouriteShopSelected);
        setResult(RESULT_OK, answerIntent);
        finish();
    }
    public void FavouriteCancel(View view)
    {
        Intent answerIntent = new Intent();
        setResult(RESULT_CANCELED, answerIntent);
        finish();
    }
}
