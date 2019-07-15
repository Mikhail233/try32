package com.example.try3;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.squareup.picasso.Picasso;

public class DialogQrCode extends DialogFragment {


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        CameraFragment.barcodeReader.onPause();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_fragment_qrcode, null);
        //TextView textViewQR = (TextView) view.findViewById(R.id.dialog_text);
        TextView textViewCode = (TextView) view.findViewById(R.id.dialog_code);
        ImageView imgPreview = (ImageView) view.findViewById(R.id.img_preview);
        builder.setView(view).setTitle("Найденная модель");

        String  code = "", qr = "", size = "",imgurl = "", price = "";
        Bundle args = getArguments();
        if (args != null) {
            //qr = args.getString("QRcode");
            //textViewQR.setText(qr);
            code = args.getString("code");
            size = args.getString("size");
            imgurl = args.getString("imgurl");
            price = args.getString("price");

            if(code == ""){
                imgPreview.setVisibility(View.GONE);
                textViewCode.setText("Такой модели не существует");
                builder.setNegativeButton("Назад", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        CameraFragment.barcodeReader.onResume();
                    }
                });
            }
            else{
                builder.setPositiveButton("Просмотр", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent();
                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
                    }
                });
                builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        CameraFragment.barcodeReader.onResume();
                    }
                });

                textViewCode.setText("Модель: " + code + "\nРазмер: " + size + "\nЦена:" + price + " руб.");
                Picasso.with(view.getContext()).load(imgurl.trim())
                        .into(imgPreview, new com.squareup.picasso.Callback(){
                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onError() {
                            }
                        });
            }
        }
        // Остальной код
        return builder.create();
    }
    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        CameraFragment.barcodeReader.onResume();
    }
}
