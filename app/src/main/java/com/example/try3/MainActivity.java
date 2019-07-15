package com.example.try3;



import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;


public class MainActivity extends AppCompatActivity {
    public static final String APP_PREFERENCES = "mysettings";
    public static final String APP_PREFERENCES_ID_FAVOURITE = "id_favourite";
    private SharedPreferences mSettings;


    static final public int CHOOSE_SHOP = 0;
    static final public int CHOOSE_FAVOURITE = 0;

    TextView cameraBar, catalogBar;
    ViewPager pager;
    boolean internetConnection;
    LinearLayout mainLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},1);
        }*/

        internetConnection = hasConnection(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        int id_favourite = 0;
        if (mSettings.contains(APP_PREFERENCES_ID_FAVOURITE)) {
            id_favourite = mSettings.getInt(APP_PREFERENCES_ID_FAVOURITE, 0);
        }
        /////////////////////////////////////////////////////////////////////////////////////////
        //SharedPreferences.Editor editor = mSettings.edit();
        //editor.putInt(APP_PREFERENCES_ID_FAVOURITE, 0);
        //editor.apply();
        //id_favourite = 0;
        /////////////////////////////////////////////////////////////////////////////////////////
        if(id_favourite==0 && internetConnection) {
            Intent intent = new Intent(this, ShopFavourite.class);
            startActivityForResult(intent, CHOOSE_FAVOURITE);
        }

        cameraBar = (TextView) findViewById(R.id.camerabar);
        catalogBar = (TextView) findViewById(R.id.catalogbar);
        pager = (ViewPager) findViewById(R.id.pager);
        mainLinearLayout = (LinearLayout) findViewById(R.id.main);
        pager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
                if (i1 == 0)
                    CameraFragment.isCameraChosen = true;
                else
                    CameraFragment.isCameraChosen = false;

            }
            @Override
            public void onPageSelected(int i) {
                switch(i) {
                    case 0:
                        cameraBar.setBackgroundResource(R.drawable.main_pageselection_selected);
                        catalogBar.setBackgroundResource(R.drawable.main_pageselection);
                        mainLinearLayout.setKeepScreenOn(true);
                        break;
                    default:
                        cameraBar.setBackgroundResource(R.drawable.main_pageselection);
                        catalogBar.setBackgroundResource(R.drawable.main_pageselection_selected);
                        mainLinearLayout.setKeepScreenOn(false);
                        break;
                }
            }
            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                Log.d("FavouriTE: ", favourite);
            }
        }
    }


    public void goCameraPage(View view)
    {
        pager.setCurrentItem(0);
    }
    public void goCatalogPage(View view)
    {
        pager.setCurrentItem(1);
    }



    private class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int pos) {
            switch(pos) {

                case 0: return CameraFragment.newInstance();
                default: return CatalogFragment.newInstance();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    public static boolean hasConnection(final Context context)
    {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiInfo != null && wifiInfo.isConnected())
        {
            return true;
        }
        wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifiInfo != null && wifiInfo.isConnected())
        {
            return true;
        }
        wifiInfo = cm.getActiveNetworkInfo();
        if (wifiInfo != null && wifiInfo.isConnected())
        {
            return true;
        }
        return false;
    }

}
