package com.example.androidshaper.retrofitimageupload;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99 ;
    private static final int SELECT_REQUEST_CODE = 0;
    private static final int CAPTURE_REQUEST_CODE = 1;
    Button buttonCamera,buttonGallery;
    ImageView imageView;
    private ProgressDialog progressDialog;
    OurRetrofitClient ourRetrofitClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonCamera=findViewById(R.id.buttonCamera);
        buttonGallery=findViewById(R.id.buttonGallery);
        imageView=findViewById(R.id.imageView);
        buttonGallery.setOnClickListener(this);
        buttonCamera.setOnClickListener(this);
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Image Upload....");
        Retrofit retrofit= new Retrofit.Builder().baseUrl("http://192.168.*.***/AndroidVolleyImageUpload/")
                .addConverterFactory(GsonConverterFactory.create()).build();
        ourRetrofitClient=retrofit.create(OurRetrofitClient.class);
    }

    public boolean CheckPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.CAMERA) || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Permission")
                        .setMessage("Please accept the permissions")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        MY_PERMISSIONS_REQUEST_LOCATION);


                                startActivity(new Intent(MainActivity
                                        .this, MainActivity.class));
                                MainActivity.this.overridePendingTransition(0, 0);
                            }
                        })
                        .create()
                        .show();


            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }

            return false;
        } else {

            return true;

        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId()==R.id.buttonCamera)
        {
            if (CheckPermission())
            {
                CameraImageSelected();
            }


        }
        else if(view.getId()==R.id.buttonGallery)
        {
            if(CheckPermission())
            {
                GalleryImageSelected();
            }

        }
    }

    private void GalleryImageSelected() {
        Intent intentSelect= new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intentSelect,SELECT_REQUEST_CODE);

    }

    private void CameraImageSelected() {
        Intent intentCapture=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intentCapture,CAPTURE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==SELECT_REQUEST_CODE)
        {
            if (resultCode==RESULT_OK)
            {

                try {
                    Uri uriImage=data.getData();
                    Bitmap bitmapCapture=MediaStore.Images.Media.getBitmap(this.getContentResolver(),uriImage);
                    imageView.setImageBitmap(bitmapCapture);
                    progressDialog.show();
                    uploadImage(bitmapCapture);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }
        else if(requestCode==CAPTURE_REQUEST_CODE)
        {
            if (resultCode==RESULT_OK)
            {
                Bitmap bitmapSelect= (Bitmap) data.getExtras().get("data");
                imageView.setImageBitmap(bitmapSelect);
                progressDialog.show();
                uploadImage(bitmapSelect);

            }
        }
    }

    private void uploadImage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
        String image = Base64.encodeToString(byteArrayOutputStream.toByteArray(),Base64.DEFAULT);
        String name = String.valueOf(Calendar.getInstance().getTimeInMillis());
        Call<ObjectClass> call=ourRetrofitClient.getResponse(name,image);
        call.enqueue(new Callback<ObjectClass>() {
            @Override
            public void onResponse(Call<ObjectClass> call, Response<ObjectClass> response) {
                progressDialog.dismiss();
                if (response.isSuccessful())
                {

                    Toast.makeText(getApplicationContext(),response.body().getResponse(),Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Response Error",Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<ObjectClass> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(),"Sever Error"+t.getMessage(),Toast.LENGTH_SHORT).show();

            }
        });


    }
}