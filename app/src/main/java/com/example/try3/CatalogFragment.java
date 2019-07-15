package com.example.try3;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.squareup.picasso.Picasso;

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

import static android.app.Activity.RESULT_OK;
import static com.example.try3.MainActivity.CHOOSE_SHOP;

public class CatalogFragment extends Fragment {

    ArrayList<String> code = new ArrayList<String>();
    ArrayList<String> name = new ArrayList<String>();
    ArrayList<String> price = new ArrayList<String>();
    ArrayList<String> imgurl = new ArrayList<String>();
    ArrayList<String> minsize = new ArrayList<String>();
    ArrayList<String> maxsize = new ArrayList<String>();
    GridView mGridView;
    View v;
    String db_url, search = "";
    Button filter_btn;
    SwipeRefreshLayout swipeRefreshLayout;
    boolean internetConnection;
    SearchView searchView;
    Handler handler = new Handler();
    boolean filterShopBool = false;
    String filterShopIDString, filterShopNameString;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        internetConnection = MainActivity.hasConnection(getContext());
        if (internetConnection == true) {
            v = inflater.inflate(R.layout.fragment_catalog, container, false);
            db_url = getString(R.string.db_url);
            mGridView = (GridView) v.findViewById(R.id.gridView);
            searchView = (SearchView) v.findViewById(R.id.searchview);
            swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swiperefresh);
            //filter_btn = (Button) v.findViewById(R.id.filter_btn);

            BackgroundTask backgroundTask = new BackgroundTask();
            backgroundTask.execute();

            /*filter_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(),FilterShop.class);
                    startActivityForResult(intent, CHOOSE_SHOP);
                }
            });*/

            swipeRefreshLayout.setOnRefreshListener(
                    new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            BackgroundTask backgroundTask = new BackgroundTask();
                            backgroundTask.execute();
                        }
                    }
            );
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    callSearch(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(final String newText) {
                    final String QueryString = newText;
                    handler.removeCallbacksAndMessages(null);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            callSearch(QueryString);                        }
                    }, 1000);
                    return true;
                }

                public void callSearch(String query) {
                    search = query;
                    BackgroundTask backgroundTask = new BackgroundTask();
                    backgroundTask.execute();
                }

            });

            return v;
        }
        else{
            v = inflater.inflate(R.layout.fragment_nointernet, container, false);
            return v;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(searchView != null)
            searchView.clearFocus();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CHOOSE_SHOP) {
            if (resultCode == RESULT_OK) {
                filterShopIDString = data.getStringExtra(FilterShop.SHOP_ID);
                filterShopNameString = data.getStringExtra(FilterShop.SHOP_NAME);
                if (!filterShopIDString.isEmpty()) {
                    filterShopBool = true;
                }
                BackgroundTask backgroundTask = new BackgroundTask();
                backgroundTask.execute();
            }
        }
    }

    public static CatalogFragment newInstance() {
        CatalogFragment f = new CatalogFragment();
        return f;
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
            View view = getLayoutInflater().inflate(R.layout.gridview_item,null);

            ImageView mImageView = view.findViewById(R.id.imageView);
            TextView mTextViewName = view.findViewById(R.id.catalogName);
            TextView mTextViewPrice = view.findViewById(R.id.catalogPrice);

            LoadImageFromUrl(imgurl.get(position), mImageView);
            mTextViewName.setText(code.get(position));
            mTextViewPrice.setText(price.get(position) + " руб.");
            return view;
        }

    }

    private
    void LoadImageFromUrl(String url, ImageView imageView){
        Picasso.with(v.getContext()).load(url.trim()).resize(150,150).placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .into(imageView, new com.squareup.picasso.Callback(){

                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {

                    }
                });
    }

    class BackgroundTask extends AsyncTask<String,Void,String>
    {
        String add_info_url;
        ArrayList<String> codeTemp = new ArrayList<String>();
        ArrayList<String> nameTemp = new ArrayList<String>();
        ArrayList<String> priceTemp = new ArrayList<String>();
        ArrayList<String> imgurlTemp = new ArrayList<String>();
        ArrayList<String> minsizeTemp = new ArrayList<String>();
        ArrayList<String> maxsizeTemp = new ArrayList<String>();
        @Override
        protected void onPreExecute() {
            add_info_url = db_url + "catalog_info.php";
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                URL url = new URL(add_info_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setDoOutput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));
                String data_string = URLEncoder.encode("search","UTF-8")+"="+URLEncoder.encode(search,"UTF-8");
                if (filterShopBool){
                    data_string += "&" + URLEncoder.encode("filtershop","UTF-8")+"="+URLEncoder.encode(filterShopIDString,"UTF-8");
                }
                bufferedWriter.write(data_string);
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
                String[] separated = result.trim().split(";");
                for (int i = 0; i < separated.length; i++) {
                    String[] subseparated = separated[i].split(":");
                    codeTemp.add(subseparated[0]);
                    nameTemp.add(subseparated[1]);
                    priceTemp.add(subseparated[2]);
                    minsizeTemp.add(subseparated[3]);
                    maxsizeTemp.add(subseparated[4]);
                    imgurlTemp.add(db_url + "img/products/" + codeTemp.get(i) + "/main.jpg");
                }
            }
            code = codeTemp;
            name = nameTemp;
            price = priceTemp;
            minsize = minsizeTemp;
            maxsize = maxsizeTemp;
            imgurl = imgurlTemp;
            CustomAdapter customAdapter = new CustomAdapter();
            mGridView.setAdapter(customAdapter);
            mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> adapter, View v, int position, long id)
                {
                    Intent intent = new Intent(v.getContext(),ProductInfo.class);
                    intent.putExtra(CameraFragment.EXTRA_CODE,code.get(position));
                    intent.putExtra(CameraFragment.EXTRA_NAME,name.get(position));
                    intent.putExtra(CameraFragment.EXTRA_PRICE,price.get(position));
                    intent.putExtra(CameraFragment.EXTRA_IMGURL,imgurl.get(position));
                    intent.putExtra(CameraFragment.EXTRA_MINSIZE,minsize.get(position));
                    intent.putExtra(CameraFragment.EXTRA_MAXSIZE,maxsize.get(position));
                    startActivity(intent);
                }
            });
            swipeRefreshLayout.setRefreshing(false);
        }

    }





}
