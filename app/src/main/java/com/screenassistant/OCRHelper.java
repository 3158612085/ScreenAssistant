package com.screenassistant;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

public class OCRHelper {
    private Context context;
    private TextRecognizer recognizer;

    public interface OCRCallback {
        void onSuccess(String text);
        void onFailure(String error);
    }

    public OCRHelper(Context context) {
        this.context = context;
        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
    }

    public void recognizeText(Bitmap bitmap, OCRCallback callback) {
        if (bitmap == null) {
            callback.onFailure("Bitmap is null");
            return;
        }

        InputImage image = InputImage.fromBitmap(bitmap, 0);
        
        recognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    String resultText = visionText.getText();
                    callback.onSuccess(resultText);
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                });
    }

    public void close() {
        if (recognizer != null) {
            recognizer.close();
        }
    }
}
