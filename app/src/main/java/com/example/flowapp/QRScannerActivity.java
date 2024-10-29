package com.example.flowapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraControl;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;
import java.util.concurrent.ExecutionException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;
import com.google.common.util.concurrent.ListenableFuture;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class QRScannerActivity extends AppCompatActivity {
    private static final String TAG = "QRScannerActivity";
    private PreviewView previewView;
    private CameraManager cameraManager;
    private String cameraId;
    private Button btnCancelScan;
    private Button btnFlipCamera;
    private Button btnGallery;
    private Button btnFlash;
    private BarcodeScanner scanner;
    private ImageCapture imageCapture; // Declare imageCapture as a class member
    private boolean isUsingBackCamera = true;
    private boolean isFlashOn = false;
    private CameraControl cameraControl;

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        // Initialize CameraManager
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId = cameraManager.getCameraIdList()[0]; // Get the first camera ID (usually the back camera)
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        scanner = BarcodeScanning.getClient();
        previewView = findViewById(R.id.preview_view);
        btnCancelScan = findViewById(R.id.btn_cancel_scan);
        btnFlipCamera = findViewById(R.id.btn_flip_camera);
        btnGallery = findViewById(R.id.btn_gallery);
        btnFlash = findViewById(R.id.btn_flash);

        btnFlash.setOnClickListener(v -> toggleFlash());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        } else {
            startCamera();
        }

        btnCancelScan.setOnClickListener(v -> finish());

        btnFlipCamera.setOnClickListener(v -> {
            isUsingBackCamera = !isUsingBackCamera; // Toggle camera
            initializeCamera(); // Reinitialize the camera
        });

        btnGallery.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 100); // Open gallery
        });
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new BarcodeImageAnalyzer());

                // Bind the camera use cases
                cameraProvider.unbindAll();
                cameraControl = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
                        .getCameraControl();

                PreviewView previewView = findViewById(R.id.preview_view);
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting camera", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                Toast.makeText(this, "Camera permission is required to use the flashlight.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isFlashAvailable() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    private void toggleFlash() {
        if (isFlashAvailable()) {
            if (isFlashOn) {
                cameraControl.enableTorch(false); // Turn off the flashlight
                isFlashOn = false ;
                Toast.makeText(this, "Flashlight Off", Toast.LENGTH_SHORT).show();
            } else {
                cameraControl.enableTorch(true); // Turn on the flashlight
                isFlashOn = true;
                Toast.makeText(this, "Flashlight On", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Flashlight not available on this device.", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder()
                        .setTargetResolution(new Size(640, 480))
                        .build();

                CameraSelector cameraSelector;
                if (isUsingBackCamera) {
                    cameraSelector = new CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                            .build();
                } else {
                    cameraSelector = new CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                            .build();
                }

                // Initialize ImageCapture with default flash mode
                imageCapture = new ImageCapture.Builder()
                        .setFlashMode(isFlashOn ? ImageCapture.FLASH_MODE_ON : ImageCapture.FLASH_MODE_OFF)
                        .setTargetResolution(new Size(640, 480))
                        .build();

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(640, 480))
                        .setImageQueueDepth(1)
                        .build();

                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new BarcodeImageAnalyzer());

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis, imageCapture);

                preview.setSurfaceProvider(previewView.getSurfaceProvider()); // Use PreviewView

                // Check if flash is available
                if (!isFlashAvailable()) {
                    Toast.makeText(this, "Flash is not available on this device.", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                Toast.makeText(this, "Error initializing camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            // Process the selected image
            try {
                InputImage inputImage = InputImage.fromFilePath(this, selectedImage);
                scanner.process(inputImage)
                        .addOnSuccessListener(barcodes -> {
                            for (Barcode barcode : barcodes) {
                                String scannedData = barcode.getRawValue(); // Get the scanned QR code data
                                Intent intent = new Intent();
                                intent.putExtra("scannedData", scannedData); // Pass the scanned data back
                                setResult(RESULT_OK, intent); // Set the result
                                finish(); // Finish the QRScannerActivity
                                break; // Exit after the first successful scan
                            }
                        })
                        .addOnFailureListener(e -> {
                            // Handle failure
                        });
            } catch (Exception e) {
                // Handle any exceptions
            }
        }
    }

    private class BarcodeImageAnalyzer implements ImageAnalysis.Analyzer {
        private final BarcodeScanner scanner = BarcodeScanning.getClient();

        @Override
        public void analyze(@NonNull ImageProxy imageProxy) {
            @SuppressLint("UnsafeOptInUsageError")
            Image image = imageProxy.getImage();
            if (image != null) {
                InputImage inputImage = InputImage.fromMediaImage(image, imageProxy.getImageInfo().getRotationDegrees());
                scanner.process(inputImage)
                        .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                            @Override
                            public void onSuccess(List<Barcode> barcodes) {
                                for (Barcode barcode : barcodes) {
                                    // Process the barcode data
                                    Log.d(TAG, "Barcode detected: " + barcode.getRawValue());
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "Barcode scanning failed: " + e.getMessage());
                            }
                        })
                        .addOnCompleteListener(new OnCompleteListener<List<Barcode>>() {
                            @Override
                            public void onComplete(@NonNull Task<List<Barcode>> task) {
                                imageProxy.close();
                            }
                        });
            }
        }
    }


}