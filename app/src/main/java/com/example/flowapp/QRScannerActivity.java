package com.example.flowapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Size;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;

public class QRScannerActivity extends AppCompatActivity {

    private PreviewView previewView;
    private Button btnCancelScan;
    private Button btnFlipCamera;
    private Button btnGallery;
    private BarcodeScanner scanner;
    private ImageCapture imageCapture; // Declare imageCapture as a class member
    private boolean isUsingBackCamera = true;
    private boolean isFlashOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        scanner = BarcodeScanning.getClient();
        previewView = findViewById(R.id.preview_view);
        btnCancelScan = findViewById(R.id.btn_cancel_scan);
        btnFlipCamera = findViewById(R.id.btn_flip_camera);
        btnGallery = findViewById(R.id.btn_gallery);
        Button btnFlash = findViewById(R.id.btn_flash);
        btnFlash.setOnClickListener(v -> toggleFlash());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        } else {
            initializeCamera();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initializeCamera();
        } else {
            Toast.makeText(this, "Camera permission is required to scan QR codes.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void toggleFlash() {
        isFlashOn = !isFlashOn; // Toggle flash state
        if (imageCapture != null) { // Check if imageCapture is initialized
            if (isFlashOn) {
                // Enable flash
                Toast.makeText(this, "Flash On", Toast.LENGTH_SHORT).show();
                imageCapture.setFlashMode(ImageCapture.FLASH_MODE_ON);
            } else {
                // Disable flash
                Toast.makeText(this, "Flash Off", Toast.LENGTH_SHORT).show();
                imageCapture.setFlashMode(ImageCapture.FLASH_MODE_OFF);
            }
        } else {
            Toast.makeText(this, "ImageCapture is not initialized", Toast.LENGTH_SHORT).show();
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

                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new QRCodeAnalyzer());

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis, imageCapture);

                preview.setSurfaceProvider(previewView.getSurfaceProvider()); // Use PreviewView

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

    private class QRCodeAnalyzer implements ImageAnalysis.Analyzer {
        @OptIn(markerClass = ExperimentalGetImage.class)
        @Override
        public void analyze(@NonNull ImageProxy imageProxy) {
            try {
                // Convert ImageProxy to InputImage
                InputImage inputImage = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());

                // Process the image with the BarcodeScanner
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
                        })
                        .addOnCompleteListener(task -> {
                            imageProxy.close(); // Close the image proxy
                        });
            } catch (Exception e) {
                // Handle any exceptions
                imageProxy.close(); // Ensure the image proxy is closed in case of error
            }
        }
    }

}