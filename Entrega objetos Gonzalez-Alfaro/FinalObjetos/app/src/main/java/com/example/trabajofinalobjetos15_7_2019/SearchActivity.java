package com.example.trabajofinalobjetos15_7_2019;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.trabajofinalobjetos15_7_2019.DTOs.ImageDTO;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class SearchActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final int GALLERY_REQUEST_CODE = 16;
    private static final int CAMERA_REQUEST_CODE = 17;

    private Api_Interface api;
    private Spinner spinner;
    private ImageView imageView;
    private ImageView camButton;
    private int colorSelection;
    private TextView tagTextView;
    private String currentPhotoPath;
    private int imageId;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        imageView = findViewById(R.id.imageView);
        camButton = findViewById(R.id.imageViewCam);
        tagTextView = findViewById(R.id.tagTextView);
        Button saveSearchButton = findViewById(R.id.SaveButton);

        boolean isNew = getIntent().getBooleanExtra("isNew", true);
        colorSelection = (getIntent().getIntExtra("color", 0));
        imageId = getIntent().getIntExtra("imageId", 0);
        token = getIntent().getStringExtra("token");
        tagTextView.setText(getIntent().getStringExtra("tag"));
        currentPhotoPath = "";

        if (savedInstanceState != null) {
            imageId = savedInstanceState.getInt("imageId");
            token = savedInstanceState.getString("token");
            currentPhotoPath = savedInstanceState.getString("currentPhotoPath");
        }

        if ((imageId != 0) && currentPhotoPath.isEmpty()) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(getResources().getString(R.string.base_Url))
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            api = retrofit.create(Api_Interface.class);
            Call<ImageDTO> call = api.getImage(imageId, token);
            call.enqueue(new Callback<ImageDTO>() {
                @Override
                public void onResponse(Call<ImageDTO> call, Response<ImageDTO> response) {
                    if (!response.isSuccessful()) {
                        Toast toast = Toast.makeText(getApplicationContext(), "No existing Image", Toast.LENGTH_SHORT);
                        toast.show();
                        return;
                    }
                    ImageDTO image = response.body();
                    byte[] decodedString = Base64.decode(image.getPicture(), Base64.DEFAULT);
                    Bitmap imageBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length); //aca
                    imageView.setImageBitmap(imageBitmap);
                }

                @Override
                public void onFailure(Call<ImageDTO> call, Throwable t) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Service failure", Toast.LENGTH_SHORT);
                    toast.show();
                }
            });
        }
        if (!currentPhotoPath.isEmpty())
            imageView.setImageBitmap(BitmapFactory.decodeFile(currentPhotoPath));

        camButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isStoragePermissionGranted()) {
                    final CharSequence[] items = {"Take photo", "Select from gallery", "Cancel"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
                    builder.setTitle("Add Photo!");
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int item) {
                            switch (item) {
                                case 0: {
                                    dispatchTakePictureIntent();
                                    break;
                                }
                                case 1: {
                                    Intent intent = new Intent(Intent.ACTION_PICK);
                                    intent.setType("image/*");
                                    String[] mimeTypes = {"image/jpeg", "image/png"};
                                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                                    startActivityForResult(intent, GALLERY_REQUEST_CODE);
                                    break;
                                }
                                case 2: {
                                    dialog.dismiss();
                                    break;
                                }
                            }
                        }
                    });
                    builder.show();
                }
            }
        });

        spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.planets_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        setSpinnerSelection();

        saveSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!currentPhotoPath.isEmpty()) {
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(getResources().getString(R.string.base_Url))
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    api = retrofit.create(Api_Interface.class);
                    ImageDTO imageToSend = new ImageDTO();
                    Bitmap imageBitmap = BitmapFactory.decodeFile(currentPhotoPath);
                    byte[] byteArray = getBytesFromBitmap(imageBitmap, 50);
                    imageToSend.setPicture(Base64.encodeToString(byteArray, Base64.DEFAULT));
                    Call<ImageDTO> call = api.addImage(imageToSend, token);
                    call.enqueue(new Callback<ImageDTO>() {
                        @Override
                        public void onResponse(Call<ImageDTO> call, Response<ImageDTO> response) {
                            if (!response.isSuccessful()) {
                                Toast toast = Toast.makeText(getApplicationContext(), "Unable to save Image", Toast.LENGTH_SHORT);
                                toast.show();
                                return;
                            }
                            Intent i = new Intent();
                            i.putExtra("tag", tagTextView.getText().toString());
                            i.putExtra("color", colorSelection);
                            i.putExtra("type", (short) getIntent().getIntExtra("type", 0));
                            i.putExtra("delete", false);
                            i.putExtra("imageId", response.body().getId());
                            SearchActivity.this.setResult(RESULT_OK, i);
                            SearchActivity.this.finish();
                        }

                        @Override
                        public void onFailure(Call<ImageDTO> call, Throwable t) {
                            Toast toast = Toast.makeText(getApplicationContext(), "Service failure", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    });
                } else {
                    Intent i = new Intent();
                    i.putExtra("tag", tagTextView.getText().toString());
                    i.putExtra("color", colorSelection);
                    i.putExtra("type", (short) getIntent().getIntExtra("type", 0));
                    i.putExtra("delete", false);
                    SearchActivity.this.setResult(RESULT_OK, i);
                    SearchActivity.this.finish();
                }
            }

        });
        if ((getIntent().getIntExtra("type", 0)) == 0)
            spinner.setVisibility(View.INVISIBLE);
    }


    public static byte[] getBytesFromBitmap(Bitmap bitmap, int quality) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("imageId", imageId);
        outState.putString("token", token);
        outState.putString("currentPhotoPath", currentPhotoPath);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        colorSelection = getColorValue(parent.getItemAtPosition(position).toString());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    public int getColorValue(String color) {
        switch (color) {
            case "Green": {
                return getResources().getColor(R.color.Map_Green);
            }
            case "Blue": {
                return getResources().getColor(R.color.Map_Blue);
            }
            case "Red": {
                return getResources().getColor(R.color.Map_Red);
            }
            case "Yellow": {
                return getResources().getColor(R.color.Map_Yellow);
            }
            case "Orange": {
                return getResources().getColor(R.color.Map_Orange);
            }
            case "Purple": {
                return getResources().getColor(R.color.Map_Purple);
            }
        }
        return getResources().getColor(R.color.Map_Green);
    }

    public String getColorName(int colorValue) {
        if (colorValue == getResources().getColor(R.color.Map_Green)) {
            return "Green";
        } else if (colorValue == getResources().getColor(R.color.Map_Blue)) {
            return "Blue";
        } else if (colorValue == getResources().getColor(R.color.Map_Red)) {
            return "Red";
        } else if (colorValue == getResources().getColor(R.color.Map_Yellow)) {
            return "Yellow";
        } else if (colorValue == getResources().getColor(R.color.Map_Orange)) {
            return "Orange";
        }
        return "Purple";
    }

    public void setSpinnerSelection() {
        String color = getColorName(colorSelection);
        switch (color) {
            case "Green": {
                spinner.setSelection(0);
                break;
            }
            case "Blue": {
                spinner.setSelection(1);
                break;
            }
            case "Red": {
                spinner.setSelection(2);
                break;
            }
            case "Yellow": {
                spinner.setSelection(3);
                break;
            }
            case "Orange": {
                spinner.setSelection(4);
                break;
            }
            case "Purple": {
                spinner.setSelection(5);
                break;
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK)
            switch (requestCode) {
                case GALLERY_REQUEST_CODE: {
                    try {
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        assert selectedImage != null;
                        Cursor cursor = this.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        currentPhotoPath = cursor.getString(columnIndex);
                        cursor.close();
                        Bitmap imageBitmap = BitmapFactory.decodeFile(currentPhotoPath);
                        imageView.setImageBitmap(imageBitmap);
                        break;
                    } catch (Exception e) {
                        Toast toast = Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
                case CAMERA_REQUEST_CODE: {
                    setPic();
                    break;
                }
            }
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                try {
                    Uri photoURI = FileProvider.getUriForFile(this,
                            "com.example.android.fileprovider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
                } catch (Exception e) {
                    Toast toast = Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        }
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap imageBitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        imageView.setImageBitmap(imageBitmap);
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("", "Permission is granted");
                return true;
            } else {

                Log.v("", "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v("", "Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast toast = Toast.makeText(getApplicationContext(), "Permission Accepted", Toast.LENGTH_SHORT);
            toast.show();
            Log.v("", "Permission: " + permissions[0] + "was " + grantResults[0]);
        }
    }

}
