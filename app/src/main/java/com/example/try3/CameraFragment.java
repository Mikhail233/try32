package com.example.try3;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.vision.barcode.Barcode;
import com.notbytes.barcode_reader.BarcodeReaderFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CameraFragment extends Fragment implements BarcodeReaderFragment.BarcodeReaderListener  {
    public static final String EXTRA_CODE = "com.example.try.EXTRA_CODE";
    public static final String EXTRA_NAME = "com.example.try.EXTRA_NAME";
    public static final String EXTRA_PRICE = "com.example.try.EXTRA_PRICE";
    public static final String EXTRA_IMGURL = "com.example.try.EXTRA_IMGURL";
    public static final String EXTRA_MINSIZE = "com.example.try.EXTRA_MINSIZE";
    public static final String EXTRA_MAXSIZE = "com.example.try.EXTRA_MAXSIZE";
    public static final String EXTRA_CURSIZE = "com.example.try.EXTRA_CURSIZE";


    String db_url;

    String curQR = "";
    String code = "";
    String name = "";
    String price = "";
    String imgurl = "";
    String minsize = "";
    String maxsize = "";
    String cursize = "";
    View v;
    View flash_toggle;
    boolean flash_toggle_bool = false;
    boolean internetConnection;

    private RequestQueue mQueue;


    private static final String TAG = CameraFragment.class.getSimpleName();

    public static Boolean isCameraChosen = true;
    public static BarcodeReaderFragment barcodeReader;

    public static CameraFragment newInstance() {
        Bundle args = new Bundle();
        CameraFragment fragment = new CameraFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_camera, container, false);

        db_url = getString(R.string.db_url);

        barcodeReader = (BarcodeReaderFragment) getChildFragmentManager().findFragmentById(R.id.barcode_fragment);
        barcodeReader.setListener(this);

        mQueue = Volley.newRequestQueue(v.getContext());

        flash_toggle = (View) v.findViewById(R.id.flash_toggle);
        flash_toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(flash_toggle_bool == true) {
                    Log.d("111", "false");
                    barcodeReader.setUseFlash(false);
                    flash_toggle_bool = false;
                }
                else {
                    Log.d("111", "true");
                    barcodeReader.setUseFlash(true);
                    flash_toggle_bool = true;
                }
            }
        });


        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
         if(flash_toggle_bool) {
            barcodeReader.setUseFlash(false);
            flash_toggle_bool = false;
        }
        barcodeReader.onPause();
        barcodeReader.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d("Pause", "ON");
        if(ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            barcodeReader.setUseFlash(false);
            flash_toggle_bool = false;
        }
        barcodeReader.onPause();
    }

    public void goProduct(){

    }


    @Override
    public void onScanned(final Barcode barcode) {
        Log.d("Scan", "YES");
        if(!curQR.equals(barcode.displayValue) && isCameraChosen == true) {
            internetConnection = MainActivity.hasConnection(getContext());
            if (internetConnection) {
                barcodeReader.onPause();
                curQR = barcode.displayValue.trim();
                String gtin = "";
                char[] info = curQR.toCharArray();
                if (info.length > 15) {
                    for (int i = 2; i <= 15; i++) {
                        gtin += info[i];
                    }
                } else {
                    gtin = curQR;
                    Toast.makeText(getActivity(), "Code: " + gtin, Toast.LENGTH_SHORT).show();
                }
                code = curQR;
                jsonParse(gtin);
                //BackgroundTask backgroundTask = new BackgroundTask();
                //backgroundTask.execute(gtin);
                //Vibrator vibrator = (Vibrator) v.getContext().getSystemService(Context.VIBRATOR_SERVICE);
                //vibrator.vibrate(1000);
            /*curQR = barcode.displayValue.trim();
            String model = "", size = "";
            String[] info = curQR.toUpperCase().split("\n");
            for (int i = 0; i < info.length; i++) {
                String[] line = info[i].split(":");
                if (line[0].trim().equals("МОДЕЛЬ")) {
                    model = line[1].trim();
                }
                if (line[0].trim().equals("РАЗМЕР")) {
                    size = line[1].trim();
                    cursize = size;
                }
            }
            code = curQR;
            BackgroundTask backgroundTask = new BackgroundTask();
            backgroundTask.execute(model);*/
            }
            else{
                barcodeReader.onPause();
                new AlertDialog.Builder(getActivity())
                        .setTitle("Отсутствует интернет соединение")
                        .setMessage(barcode.displayValue)

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton("Назад", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                barcodeReader.onResume();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                barcodeReader.onResume();
                            }
                        })
                        .show();
            }
        }
    }

    private void jsonParse(String gtin) {

        String url = db_url + "json_read_info.php";
        //Map<String, String> params = new HashMap();
        //params.put("gtin", gtin);
        //JSONObject parameters = new JSONObject(params);


        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url + "?gtin=" + gtin, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("object");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject obj = jsonArray.getJSONObject(i);

                                code = obj.getString("code");
                                name = obj.getString("name");
                                price = obj.getString("price");
                                minsize = obj.getString("minSize");
                                maxsize = obj.getString("maxSize");
                                cursize = obj.getString("size");
                                imgurl = db_url + "img/products/" + code + "/main.jpg";
                                openWeightPicker();
                                curQR = "";
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        mQueue.add(request);
    }

    @Override
    public void onScannedMultiple(List<Barcode> barcodes) {
        /*Log.e(TAG, "onScannedMultiple: " + barcodes.size());

        String codes = "";
        for (Barcode barcode : barcodes) {
            codes += barcode.displayValue + ", ";
        }

        final String finalCodes = codes;
        Toast.makeText(getActivity(), "Barcodes: " + finalCodes, Toast.LENGTH_SHORT).show();*/
    }

    @Override
    public void onBitmapScanned(SparseArray<Barcode> sparseArray) {

    }

    @Override
    public void onScanError(String errorMessage) {
        Log.e(TAG, "onScanError: " + errorMessage);
    }

    @Override
    public void onCameraPermissionDenied() {
        //Toast.makeText(getActivity(), "Camera permission denied!", Toast.LENGTH_LONG).show();
    }


    class BackgroundTask extends AsyncTask<String,Void,String>
    {
        String add_info_url;
        @Override
        protected void onPreExecute() {
            add_info_url = db_url + "read_info.php";
        }

        @Override
        protected String doInBackground(String... args) {
            String code;
            code = args[0];
            try {
                URL url = new URL(add_info_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));
                String data_string = URLEncoder.encode("gtin","UTF-8")+"="+URLEncoder.encode(code,"UTF-8");
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
            if(result != null) {
                String[] separated = result.split(":");
                if (separated.length >= 3) {
                    code = separated[0];
                    name = separated[1];
                    price = separated[2];
                    minsize = separated[3];
                    maxsize = separated[4];
                    cursize = separated[5];
                } else {
                    code = "";
                    name = "";
                    price = "";
                }
                imgurl = db_url + "img/products/" + code + "/main.jpg";

                /*FragmentManager manager = getFragmentManager();
                DialogFragment myDialogFragment = new DialogQrCode();
                Bundle bundle = new Bundle();
                bundle.putString("QRcode", curQR);
                bundle.putString("code", code);
                bundle.putString("size", cursize);
                myDialogFragment.setArguments(bundle);
                myDialogFragment.setTargetFragment(this,REQUEST_WEIGHT);
                myDialogFragment.show(manager,"dialog");*/
                openWeightPicker();
                curQR = "";
            }
        }
    }

    private static final int REQUEST_WEIGHT = 1;
    public void openWeightPicker() {
        DialogFragment fragment = new DialogQrCode();
        Bundle bundle = new Bundle();
        bundle.putString("QRcode", curQR);
        bundle.putString("code", code);
        bundle.putString("size", cursize);
        bundle.putString("imgurl", imgurl);
        bundle.putString("price", price);
        fragment.setArguments(bundle);
        fragment.setTargetFragment(this, REQUEST_WEIGHT);
        fragment.show(getFragmentManager(), fragment.getClass().getName());
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_WEIGHT) {
            if (resultCode == Activity.RESULT_OK) {
                Intent intent = new Intent(v.getContext(), ProductInfo.class);
                intent.putExtra(EXTRA_CODE, code);
                intent.putExtra(EXTRA_NAME, name);
                intent.putExtra(EXTRA_PRICE, price);
                intent.putExtra(EXTRA_IMGURL, imgurl);
                intent.putExtra(EXTRA_MINSIZE, minsize);
                intent.putExtra(EXTRA_MAXSIZE, maxsize);
                intent.putExtra(EXTRA_CURSIZE, cursize);
                startActivity(intent);
            }
        }
    }
}