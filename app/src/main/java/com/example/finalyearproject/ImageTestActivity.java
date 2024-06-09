package com.example.finalyearproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ImageTestActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private final String TAG = "ImageTestActivity";

    private ImageView imageView;
    private TextView tvResult;
    private Interpreter tflite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imagetest);

        imageView = findViewById(R.id.imageView);
        tvResult = findViewById(R.id.tvResult);
        Button btnCapture = findViewById(R.id.btnCapture);
        Button btnUpload = findViewById(R.id.btnUpload);

        // Load the TensorFlow Lite model
        try {
            tflite = new Interpreter(loadModelFile());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error loading model", e);
        }

        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(ImageTestActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ImageTestActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_IMAGE_CAPTURE);
                } else {
                    dispatchTakePictureIntent();
                }
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(ImageTestActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ImageTestActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_IMAGE_PICK);
                } else {
                    dispatchUploadPictureIntent();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_IMAGE_CAPTURE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent();
        } else if (requestCode == REQUEST_IMAGE_PICK && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            dispatchUploadPictureIntent();
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Log.e(TAG, "No camera app available");
        }
    }

    private void dispatchUploadPictureIntent() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (pickPhoto.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(pickPhoto, REQUEST_IMAGE_PICK);
        } else {
            Log.e(TAG, "No photo picker app available");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    imageView.setImageBitmap(imageBitmap);
                    classifyImage(imageBitmap);
                } else {
                    Log.e(TAG, "No data found in intent");
                }
            } else if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                Uri selectedImage = data.getData();
                if (selectedImage != null) {
                    try {
                        InputStream imageStream = getContentResolver().openInputStream(selectedImage);
                        Bitmap imageBitmap = BitmapFactory.decodeStream(imageStream);
                        imageView.setImageBitmap(imageBitmap);
                        classifyImage(imageBitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Log.e(TAG, "File not found", e);
                    }
                } else {
                    Log.e(TAG, "No image selected");
                }
            }
        } else {
            Log.e(TAG, "Result not OK");
        }
    }

    private MappedByteBuffer loadModelFile() throws IOException {
        FileInputStream fileInputStream = new FileInputStream(getAssets().openFd("model.tflite").getFileDescriptor());
        FileChannel fileChannel = fileInputStream.getChannel();
        long startOffset = getAssets().openFd("model.tflite").getStartOffset();
        long declaredLength = getAssets().openFd("model.tflite").getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private void classifyImage(Bitmap bitmap) {
        // Resize bitmap to 128x128
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 128, 128, true);

        // Prepare the TensorImage
        TensorImage tensorImage = new TensorImage(org.tensorflow.lite.DataType.FLOAT32);
        tensorImage.load(resizedBitmap);

        // Get the input tensor as a float array
        float[][][][] input = new float[1][128][128][3];
        for (int y = 0; y < 128; y++) {
            for (int x = 0; x < 128; x++) {
                int pixel = resizedBitmap.getPixel(x, y);
                input[0][y][x][0] = ((pixel >> 16) & 0xFF) / 255.0f;  // Red
                input[0][y][x][1] = ((pixel >> 8) & 0xFF) / 255.0f;   // Green
                input[0][y][x][2] = (pixel & 0xFF) / 255.0f;          // Blue
            }
        }

        // Create the input TensorBuffer
        TensorBuffer inputBuffer = TensorBuffer.createFixedSize(new int[]{1, 128, 128, 3}, org.tensorflow.lite.DataType.FLOAT32);
        inputBuffer.loadArray(flatten(input));

        // Create the output TensorBuffer
        TensorBuffer outputBuffer = TensorBuffer.createFixedSize(new int[]{1, 1}, org.tensorflow.lite.DataType.FLOAT32);

        // Run inference
        tflite.run(inputBuffer.getBuffer(), outputBuffer.getBuffer());

        // Get the result
        float[] probabilities = outputBuffer.getFloatArray();
        Log.d(TAG, "Probabilities: " + probabilities[0]);
        if (probabilities[0] > 0.5) {
            tvResult.setText("Non-Autistic");
        } else {
            tvResult.setText("Autistic");
        }
    }

    private float[] flatten(float[][][][] array) {
        int size = array.length * array[0].length * array[0][0].length * array[0][0][0].length;
        float[] flatArray = new float[size];
        int index = 0;
        for (float[][][] twoDArray : array) {
            for (float[][] oneDArray : twoDArray) {
                for (float[] innerArray : oneDArray) {
                    for (float value : innerArray) {
                        flatArray[index++] = value;
                    }
                }
            }
        }
        return flatArray;
    }
}
