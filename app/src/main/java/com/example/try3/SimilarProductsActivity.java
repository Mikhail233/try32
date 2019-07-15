package com.example.try3;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class SimilarProductsActivity extends AppCompatActivity {
    ArrayList<String> code = new ArrayList<String>();
    ArrayList<String> name = new ArrayList<String>();
    ArrayList<String> price = new ArrayList<String>();
    ArrayList<String> imgurl = new ArrayList<String>();
    ArrayList<String> minsize = new ArrayList<String>();
    ArrayList<String> maxsize = new ArrayList<String>();
    ArrayList<String> quantity = new ArrayList<String>();
    String cursize = "";
    GridView mGridView;
    String db_url;
    String postStr;
    SwipeRefreshLayout swipeRefreshLayout;
    Toolbar mToolbar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_similar);
        db_url = getString(R.string.db_url);
        mGridView = (GridView) findViewById(R.id.gridView);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mToolbar.setTitle(R.string.similar_products_title);

        BackgroundTask backgroundTask = new BackgroundTask();
        backgroundTask.execute();
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        BackgroundTask backgroundTask = new BackgroundTask();
                        backgroundTask.execute();
                    }
                }
        );
        Intent intent = getIntent();
        String IntentCode = intent.getStringExtra(CameraFragment.EXTRA_CODE);
        String IntentSize = intent.getStringExtra(CameraFragment.EXTRA_CURSIZE);
        cursize = IntentSize;
        if (!IntentSize.equals(""))
            mToolbar.setTitle("Похожие товары (" + IntentSize + " размер)");
        try {
            postStr = URLEncoder.encode("code","UTF-8")+"="+URLEncoder.encode(IntentCode,"UTF-8")+"&"+
                    URLEncoder.encode("size","UTF-8")+"="+URLEncoder.encode(IntentSize,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return code.size();
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
                View view = getLayoutInflater().inflate(R.layout.gridview_item, null);

                ImageView mImageView = view.findViewById(R.id.imageView);
                TextView mTextViewName = view.findViewById(R.id.catalogName);
                TextView mTextViewPrice = view.findViewById(R.id.catalogPrice);
                TextView mTextViewQuantity = view.findViewById(R.id.catalogQuantity);

                LoadImageFromUrl(imgurl.get(position), mImageView);
            /*if(quantity.size() > position) {
                if (Integer.valueOf(quantity.get(position)) > 0) {
                    mTextViewQuantity.setText("В наличии");
                    mTextViewQuantity.setBackgroundResource(R.color.quantity_yes);
                } else {
                    mTextViewQuantity.setText("Нет в наличии");
                    mTextViewQuantity.setBackgroundResource(R.color.quantity_no);
                }
            }*/
                mTextViewName.setText(code.get(position));
                mTextViewPrice.setText(price.get(position) + " руб.");
                return view;
            }

    }

    private void LoadImageFromUrl(String url, ImageView imageView) {
        Picasso.with(this).load(url).resize(150, 150).placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .into(imageView, new com.squareup.picasso.Callback() {

                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {

                    }
                });
    }

    class BackgroundTask extends AsyncTask<String, Void, String> {
        String add_info_url;
        ArrayList<String> codeTemp = new ArrayList<String>();
        ArrayList<String> nameTemp = new ArrayList<String>();
        ArrayList<String> priceTemp = new ArrayList<String>();
        ArrayList<String> imgurlTemp = new ArrayList<String>();
        ArrayList<String> minsizeTemp = new ArrayList<String>();
        ArrayList<String> maxsizeTemp = new ArrayList<String>();
        ArrayList<String> quantityTemp = new ArrayList<String>();

        @Override
        protected void onPreExecute() {
            add_info_url = db_url + "catalog_similar_info.php";
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                URL url = new URL(add_info_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setDoOutput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                String data_string = postStr;
                bufferedWriter.write(data_string);
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
            String[] separated = result.trim().split(";");
            for (int i = 0; i < separated.length; i++) {
                String[] subseparated = separated[i].split(":");
                codeTemp.add(subseparated[0]);
                nameTemp.add(subseparated[1]);
                priceTemp.add(subseparated[2]);
                minsizeTemp.add(subseparated[3]);
                maxsizeTemp.add(subseparated[4]);
                if(subseparated.length > 5)
                    quantityTemp.add(subseparated[5]);
                imgurlTemp.add(db_url + "img/products/" + codeTemp.get(i) + "/main.jpg");
            }
            code = codeTemp;
            name = nameTemp;
            price = priceTemp;
            minsize = minsizeTemp;
            maxsize = maxsizeTemp;
            quantity = quantityTemp;
            imgurl = imgurlTemp;


            CustomAdapter customAdapter = new CustomAdapter();
            mGridView.setAdapter(customAdapter);
            mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                    Intent intent = new Intent(v.getContext(), ProductInfo.class);
                    intent.putExtra(CameraFragment.EXTRA_CODE, code.get(position));
                    intent.putExtra(CameraFragment.EXTRA_NAME, name.get(position));
                    intent.putExtra(CameraFragment.EXTRA_PRICE, price.get(position));
                    intent.putExtra(CameraFragment.EXTRA_IMGURL, imgurl.get(position));
                    intent.putExtra(CameraFragment.EXTRA_MINSIZE, minsize.get(position));
                    intent.putExtra(CameraFragment.EXTRA_MAXSIZE, maxsize.get(position));
                    if (cursize.length() > 1)
                        intent.putExtra(CameraFragment.EXTRA_CURSIZE, cursize);
                    startActivity(intent);
                }
            });

            swipeRefreshLayout.setRefreshing(false);
        }


    }
}
