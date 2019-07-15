package com.example.try3;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

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

import static com.example.try3.MainActivity.APP_PREFERENCES;
import static com.example.try3.MainActivity.APP_PREFERENCES_ID_FAVOURITE;
import static com.example.try3.MainActivity.CHOOSE_FAVOURITE;

public class ProductInfo extends AppCompatActivity {
    public static final String EXTRA_SHOP_ADDR = "com.example.try.EXTRA_SHOP_ADDR";
    public static final String EXTRA_SHOP_METRO = "com.example.try.EXTRA_SHOP_METRO";
    public static final String EXTRA_SHOP_WORK_TIME = "com.example.try.EXTRA_SHOP_WORK_TIME";
    public static final String EXTRA_SHOP_CONTACTS = "com.example.try.EXTRA_SHOP_CONTACTS";

    TextView Code, Price, SizeTextView, textViewFavourite, changeFavouriteLink;
    ImageView imageView;
    GridView gridView;
    Toolbar mToolbar;
    ListView listViewShops;
    ScrollView scrollView;
    Button similarBtn;

    int minSize,maxSize;
    ArrayList<String> sizes = new ArrayList<String>();
    ArrayList<Boolean> sizesFavouriteEnabled = new ArrayList<Boolean>();
    ArrayList<Boolean> sizesEnabled = new ArrayList<Boolean>();
    ArrayList<String> shopsAddr = new ArrayList<String>();
    ArrayList<String> shopsMetro = new ArrayList<String>();
    ArrayList<String> shopsWorkTime = new ArrayList<String>();
    ArrayList<String> shopsContacts = new ArrayList<String>();
    TextView prevSizeSelected = null, textViewProductShops;
    Boolean prevSizeSelectedIsFavourite = false;
    String codetosimilar = "", sizetosimilar = "";
    String db_url;

    Boolean onSizeClicked = false;

    private SharedPreferences mSettings;
    int id_favourite = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
        db_url = getString(R.string.db_url);
        Code = (TextView) findViewById(R.id.code);
        Price = (TextView) findViewById(R.id.price);
        SizeTextView = (TextView) findViewById(R.id.size_text_view);
        imageView = (ImageView) findViewById(R.id.image);
        gridView = (GridView) findViewById(R.id.sizes);
        listViewShops = (ListView) findViewById(R.id.listview_product_shops);
        textViewProductShops = (TextView) findViewById(R.id.textView_product_shops);
        textViewFavourite = (TextView) findViewById(R.id.textView_favourite);
        changeFavouriteLink = (TextView) findViewById(R.id.change_favourite);
        scrollView = (ScrollView) findViewById(R.id.scroll_view);
        similarBtn = (Button) findViewById(R.id.similar_btn);
        ((ExpandableHeightGridView) gridView).setExpanded(true);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Intent intent = getIntent();
        String IntentCode = intent.getStringExtra(CameraFragment.EXTRA_CODE);
        String IntentPrice = intent.getStringExtra(CameraFragment.EXTRA_PRICE);
        String IntentImgurl = intent.getStringExtra(CameraFragment.EXTRA_IMGURL);
        String IntentMinSize = intent.getStringExtra(CameraFragment.EXTRA_MINSIZE);
        String IntentMaxSize = intent.getStringExtra(CameraFragment.EXTRA_MAXSIZE);
        String IntentCurSize = intent.getStringExtra(CameraFragment.EXTRA_CURSIZE);

        Code.setText("Артикул: " + IntentCode);
        Price.setText("Цена: " + IntentPrice + " руб.");
        LoadImageFromUrl(IntentImgurl,imageView);
        minSize = Integer.parseInt(IntentMinSize);
        maxSize = Integer.parseInt(IntentMaxSize);
        for (int i = minSize; i<=maxSize ; i++){
            sizes.add(String.valueOf(i));
        }

        codetosimilar = IntentCode;
        if (IntentCurSize != null) {
            sizetosimilar = IntentCurSize;
        }

        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int scrollY = scrollView.getScrollY(); // For ScrollView
                int scrollX = scrollView.getScrollX(); // For HorizontalScrollView
                View view = (View) scrollView.getChildAt(scrollView.getChildCount() - 1);
                int bottom = (view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY()));
                Log.d("Coord","Y: "+scrollY+ " coord:"+SizeTextView.getTop() + " bottom: " + bottom);
                if((SizeTextView.getTop() < scrollY) || (bottom - scrollY)<50){
                    ValueAnimator anim = ValueAnimator.ofInt(similarBtn.getMeasuredHeight(), 70);
                    anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            int val = (Integer) valueAnimator.getAnimatedValue();
                            ViewGroup.LayoutParams layoutParams = similarBtn.getLayoutParams();
                            layoutParams.height = val;
                            similarBtn.setLayoutParams(layoutParams);
                        }
                    });
                    anim.setDuration(150);
                    anim.start();
                }
                else{
                    ValueAnimator anim = ValueAnimator.ofInt(similarBtn.getMeasuredHeight(), 0);
                    anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            int val = (Integer) valueAnimator.getAnimatedValue();
                            ViewGroup.LayoutParams layoutParams = similarBtn.getLayoutParams();
                            layoutParams.height = val;
                            similarBtn.setLayoutParams(layoutParams);
                        }
                    });
                    anim.setDuration(150);
                    anim.start();
                   // ViewGroup.LayoutParams params = similarBtn.getLayoutParams();
                    //params.height = 0;
                    //similarBtn.setLayoutParams(params);
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if (mSettings.contains(APP_PREFERENCES_ID_FAVOURITE)) {
            id_favourite = mSettings.getInt(APP_PREFERENCES_ID_FAVOURITE, 0);
        }
        if(id_favourite != 0){
            changeFavouriteLink.setText(R.string.underlined_change_favourite);
        }
        else{
            changeFavouriteLink.setText(R.string.underlined_select_favourite);
        }

        BackgroundTask backgroundTask = new BackgroundTask();
        backgroundTask.execute();

        BackgroundTaskSizesEnable backgroundTaskSizesEnableTask = new BackgroundTaskSizesEnable();
        backgroundTaskSizesEnableTask.execute();
    }

    public void goToChangeFavourite(View view)
    {
        Intent intent = new Intent(this, ShopFavourite.class);
        startActivityForResult(intent, CHOOSE_FAVOURITE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_FAVOURITE) {
            if (resultCode == RESULT_OK) {
                String favourite = data.getStringExtra(ShopFavourite.FAVOURITE_SHOP_ID);
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putInt(APP_PREFERENCES_ID_FAVOURITE, Integer.parseInt(favourite));
                editor.apply();

                mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
                if (mSettings.contains(APP_PREFERENCES_ID_FAVOURITE)) {
                    id_favourite = mSettings.getInt(APP_PREFERENCES_ID_FAVOURITE, 0);
                }
                BackgroundTask backgroundTask = new BackgroundTask();
                backgroundTask.execute();
            }
        }
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = listView.getPaddingTop() + listView.getPaddingBottom();
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);

            if(listItem != null){
                // This next line is needed before you call measure or else you won't get measured height at all. The listitem needs to be drawn first to know the height.
                listItem.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
                totalHeight += listItem.getMeasuredHeight();

            }
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    class ShopsAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return shopsAddr.size();
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
            if(!shopsMetro.get(position).equals(""))
                textViewMetro.setText(shopsAddr.get(position) + " (Ⓜ " + shopsMetro.get(position) + ")");
            else
                textViewMetro.setText(shopsAddr.get(position));
            return view;
        }
    }

    class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return sizes.size();
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
            View view = getLayoutInflater().inflate(R.layout.gridview_sizes,null);
            TextView mTextView = view.findViewById(R.id.size);
            mTextView.setText(sizes.get(position));
            if(sizetosimilar == sizes.get(position)) {
                mTextView.setBackgroundResource(R.drawable.size_selector_selected);
                prevSizeSelected = mTextView;
            }
            if(sizesEnabled.get(position) == false) {
                mTextView.setBackgroundResource(R.drawable.size_selector_not_enable);
                mTextView.setEnabled(false);
                if(sizetosimilar == sizes.get(position))
                {
                    mTextView.setBackgroundResource(R.drawable.size_selector_not_enable_selected);
                    prevSizeSelected = mTextView;
                }
            }
            if(id_favourite!=0 && !sizesFavouriteEnabled.isEmpty()) {
                if (sizesFavouriteEnabled.get(position) == true) {
                    mTextView.setBackgroundResource(R.drawable.size_selector_favourite);
                    if (sizetosimilar == sizes.get(position)) {
                        mTextView.setBackgroundResource(R.drawable.size_selector_favourite_selected);
                        prevSizeSelected = mTextView;
                        prevSizeSelectedIsFavourite = true;
                    }
                }
            }
            return view;
        }

    }

    private void LoadImageFromUrl(String url, ImageView imageView){
        Picasso.with(this).load(url.trim())
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
        ArrayList<String> shopsAddrTemp = new ArrayList<String>();
        ArrayList<String> shopsMetroTemp = new ArrayList<String>();
        ArrayList<String> shopsWorkTimeTemp = new ArrayList<String>();
        ArrayList<String> shopsContactsTemp = new ArrayList<String>();
        String id_favourite_str = "";
        @Override
        protected void onPreExecute() {
            add_info_url = db_url + "product_shops_info.php";
            if(id_favourite != 0){
                id_favourite_str = String.valueOf(id_favourite);
            }
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                URL url = new URL(add_info_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setDoOutput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));
                String data_string = URLEncoder.encode("code","UTF-8")+"="+URLEncoder.encode(codetosimilar,"UTF-8");
                if (sizetosimilar != "")
                    data_string += "&" + URLEncoder.encode("size","UTF-8")+"="+URLEncoder.encode(sizetosimilar,"UTF-8");
                if (id_favourite != 0)
                    data_string += "&" + URLEncoder.encode("favourite","UTF-8")+"="+URLEncoder.encode(id_favourite_str,"UTF-8");
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
            if (id_favourite != 0){
                ViewGroup.LayoutParams params = textViewFavourite.getLayoutParams();
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                textViewFavourite.setLayoutParams(params);
                textViewFavourite.setText(id_favourite_str + " " + result);
                String[] separated = result.trim().split("#");
                if(separated.length >=2)
                    result = separated[1];
                else
                    result = "";
                final String subseparated[] = separated[0].trim().split("/");
                final int[] i = {0};
                if(subseparated[i[0]].trim().equals("do_not_have")) {
                    i[0]++;
                    if(subseparated.length >= i[0]+2) {
                        if (!subseparated[i[0] + 1].equals(""))
                            textViewFavourite.setText("Нет в наличии по адресу: " + subseparated[i[0]] + " (Ⓜ " + subseparated[i[0] + 1] + ")");
                        else
                            textViewFavourite.setText("Нет в наличии по адресу: " + subseparated[i[0]]);
                    }
                    else
                        textViewFavourite.setText("Нет в наличии по адресу: " + subseparated[i[0]]);
                }
                else{
                    if(subseparated.length >= i[0]+1) {
                        if (!subseparated[i[0] + 1].equals(""))
                            textViewFavourite.setText("Имеется в наличии по адресу: " + subseparated[i[0]] + " (Ⓜ " + subseparated[i[0] + 1] + ")");
                        else
                            textViewFavourite.setText("Имеется в наличии по адресу: " + subseparated[i[0]]);
                    }
                    else
                        textViewFavourite.setText("Имеется в наличии по адресу: " + subseparated[i[0]]);
                }
                textViewFavourite.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(ProductInfo.this,ShopInfo.class);
                        if(subseparated.length >= i[0]+1) {
                            if(!subseparated[i[0]].equals(""))
                                intent.putExtra(EXTRA_SHOP_ADDR, subseparated[i[0]]);
                            else
                                intent.putExtra(EXTRA_SHOP_ADDR, "");
                        }
                        else
                            intent.putExtra(EXTRA_SHOP_ADDR, "");
                        i[0]++;
                        if(subseparated.length >= i[0]+1) {
                            if(!subseparated[i[0]].equals(""))
                                intent.putExtra(EXTRA_SHOP_METRO, subseparated[i[0]]);
                            else
                                intent.putExtra(EXTRA_SHOP_METRO, "");
                        }
                        else
                            intent.putExtra(EXTRA_SHOP_METRO, "");
                        i[0]++;
                        if(subseparated.length >= i[0]+1) {
                            if(!subseparated[i[0]].equals(""))
                                intent.putExtra(EXTRA_SHOP_WORK_TIME, subseparated[i[0]]);
                            else
                                intent.putExtra(EXTRA_SHOP_WORK_TIME, "");
                        }
                        else
                            intent.putExtra(EXTRA_SHOP_WORK_TIME, "");
                        i[0]++;
                        if(subseparated.length >= i[0]+1) {
                            if(!subseparated[i[0]].equals(""))
                                intent.putExtra(EXTRA_SHOP_CONTACTS, subseparated[i[0]]);
                            else
                                intent.putExtra(EXTRA_SHOP_CONTACTS, "");
                        }
                        else
                            intent.putExtra(EXTRA_SHOP_CONTACTS, "");
                        startActivity(intent);
                    }
                });
            }
            else{
                ViewGroup.LayoutParams params = textViewFavourite.getLayoutParams();
                params.height = 0;
                textViewFavourite.setLayoutParams(params);
            }
            if (!result.isEmpty()) {
                String[] separated = result.trim().split("&");
                for (int i = 0; i < separated.length; i++) {
                    String[] subseparated = separated[i].trim().split("/");
                    if(subseparated.length>=4) {
                        shopsAddrTemp.add(subseparated[0]);
                        shopsMetroTemp.add(subseparated[1]);
                        shopsWorkTimeTemp.add(subseparated[2]);
                        shopsContactsTemp.add(subseparated[3]);
                    }
                    else{
                        shopsAddrTemp.add(separated[i]);
                    }
                }
            }
            shopsAddr = shopsAddrTemp;
            shopsMetro = shopsMetroTemp;
            shopsWorkTime = shopsWorkTimeTemp;
            shopsContacts = shopsContactsTemp;

            ShopsAdapter shopsAdapter = new ShopsAdapter();
            listViewShops.setAdapter(shopsAdapter);
            setListViewHeightBasedOnChildren(listViewShops);

            listViewShops.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> adapter, View v, int position, long id)
                {
                    Intent intent = new Intent(ProductInfo.this,ShopInfo.class);
                    intent.putExtra(EXTRA_SHOP_ADDR,shopsAddr.get(position));
                    intent.putExtra(EXTRA_SHOP_METRO,shopsMetro.get(position));
                    intent.putExtra(EXTRA_SHOP_WORK_TIME,shopsWorkTime.get(position));
                    intent.putExtra(EXTRA_SHOP_CONTACTS,shopsContacts.get(position));
                    startActivity(intent);
                }
            });

            if(shopsAddrTemp.size() == 0)
            {
                if (sizetosimilar != "")
                    textViewProductShops.setText("Размера " + sizetosimilar + " нет в наличии в сети магазинов");
                else
                    textViewProductShops.setText("Нет в наличии в сети магазинов");
                Log.d("Нет в наличии", String.valueOf(shopsAddrTemp.size()));
            }
            else{
                if (sizetosimilar != "")
                    textViewProductShops.setText("Размер " + sizetosimilar + " также можно приобрести в магазинах:");
                else
                    textViewProductShops.setText("Также можно приобрести в магазинах:");
                Log.d("В наличии", String.valueOf(shopsAddrTemp.size()));
            }
            if(onSizeClicked) {
                scrollView.getMeasuredHeight();
                scrollView.smoothScrollTo(0, SizeTextView.getTop());
            }
        }
    }


    class BackgroundTaskSizesEnable extends AsyncTask<String,Void,String>
    {
        String add_info_url;
        ArrayList<Boolean> sizeEnableTemp = new ArrayList<Boolean>();
        ArrayList<Boolean> sizesFavouriteEnabledTemp = new ArrayList<Boolean>();
        @Override
        protected void onPreExecute() {
            add_info_url = db_url + "sizes_enable_info.php";
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                URL url = new URL(add_info_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setDoOutput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));
                String data_string = URLEncoder.encode("code","UTF-8")+"="+URLEncoder.encode(codetosimilar,"UTF-8");
                if (id_favourite != 0)
                    data_string += "&" + URLEncoder.encode("favourite","UTF-8")+"="+URLEncoder.encode(String.valueOf(id_favourite),"UTF-8");
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
            Log.d("Server result", result);
            if (id_favourite != 0){
                String[] separated = result.trim().split("#");
                if(separated.length >=2)
                    result = separated[1];
                else
                    result = "";
                String[] subseparated = separated[0].trim().split("/");
                for (int i = 0; i < sizes.size(); i++) {
                    Boolean enable = false;
                    for (int j = 0; j<subseparated.length; j++){
                        if (subseparated[j].equals(sizes.get(i))){
                            enable = true;
                            break;
                        }
                    }
                    sizesFavouriteEnabledTemp.add(i,enable);
                }
                sizesFavouriteEnabled = sizesFavouriteEnabledTemp;
            }

            Log.d("Server result cut", result + sizesFavouriteEnabled);
            if (!result.isEmpty()) {
                String[] separated = result.trim().split("/");
                for (int i = 0; i < sizes.size(); i++) {
                    Boolean enable = false;
                    for (int j = 0; j<separated.length; j++){
                        if (separated[j].equals(sizes.get(i))){
                            enable = true;
                            break;
                        }
                    }
                    sizeEnableTemp.add(i,enable);
                }
            sizesEnabled = sizeEnableTemp;
            }

            CustomAdapter customAdapter = new CustomAdapter();
            gridView.setAdapter(customAdapter);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> adapter, View v, int position, long id)
                {
                    TextView size = (TextView) v.findViewById(R.id.size);
                    if (prevSizeSelected != null && prevSizeSelected != size){
                        if(prevSizeSelected.isEnabled()) {
                            Log.d("Prev size", "enable");
                            if(prevSizeSelectedIsFavourite == true)
                                prevSizeSelected.setBackgroundResource(R.drawable.size_selector_favourite);
                            else
                                prevSizeSelected.setBackgroundResource(R.drawable.size_selector);
                        }
                        else {
                            Log.d("Prev size", "disable");
                            prevSizeSelected.setBackgroundResource(R.drawable.size_selector_not_enable);
                        }
                    }
                    if(size.isEnabled()){
                        if(id_favourite != 0) {
                            if (sizesFavouriteEnabled.get(position) == true)
                                size.setBackgroundResource(R.drawable.size_selector_favourite_selected);
                            else
                                size.setBackgroundResource(R.drawable.size_selector_selected);
                        }
                        else
                            size.setBackgroundResource(R.drawable.size_selector_selected);
                    }
                    else
                        size.setBackgroundResource(R.drawable.size_selector_not_enable_selected);
                    prevSizeSelected = size;
                    if(id_favourite != 0) {
                        if (sizesFavouriteEnabled.get(position) == true)
                            prevSizeSelectedIsFavourite = true;
                        else
                            prevSizeSelectedIsFavourite = false;
                    }
                    else
                        prevSizeSelectedIsFavourite = false;
                    sizetosimilar = String.valueOf(size.getText());
                    onSizeClicked = true;
                    BackgroundTask backgroundTask = new BackgroundTask();
                    backgroundTask.execute();
                }
            });
        }

    }

    public void goSimilarProducts(View view)
    {
        Intent intent = new Intent(this,SimilarProductsActivity.class);
        intent.putExtra(CameraFragment.EXTRA_CODE,codetosimilar);
        intent.putExtra(CameraFragment.EXTRA_CURSIZE,sizetosimilar);
        startActivity(intent);
    }
}
