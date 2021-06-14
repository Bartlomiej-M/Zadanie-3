/*3.
  Należy stworzyć aplikację, która dla zdjęcia, zrobionego aparatem lub załadowanego z
  pamięci urządzenia, zweryfikuje jego rozmycie (blur) i wyświetli komunikat po przekroczeniu
  pewnego dowolnie ustalonego progu tego rozmycia.*/

package com.example.zadanie_3_blur;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends AppCompatActivity {
    private ImageView MainprofilImage;

    private TextView MainBlurTexView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainprofilImage = (ImageView) findViewById(R.id.MainprofilImage);// profil picture
        MainBlurTexView =  findViewById(R.id.MainBlurTexView);

        ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(/////////display image <-very important
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {

                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Toast.makeText(MainActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                            Uri imageUri = result.getData().getData();
                            MainprofilImage.setImageURI(imageUri);

                            if(MainprofilImage != null){
                                BitmapDrawable drawable = (BitmapDrawable) MainprofilImage.getDrawable();

                                Bitmap bitmap = drawable.getBitmap();

                                BitmapFactory.Options opt = new BitmapFactory.Options();
                                opt.inDither = true;
                                opt.inPreferredConfig = Bitmap.Config.ARGB_8888;

                                int l = CvType.CV_8UC1;
                                Mat matImage = new Mat();
                                Utils.bitmapToMat(bitmap, matImage);
                                Mat matImageGrey = new Mat();
                                Imgproc.cvtColor(matImage, matImageGrey, Imgproc.COLOR_BGR2GRAY);

                                Mat dst2 = new Mat();
                                Utils.bitmapToMat(bitmap, dst2);

                                Mat laplacianImage = new Mat();
                                dst2.convertTo(laplacianImage, l);
                                Imgproc.Laplacian(matImageGrey, laplacianImage, CvType.CV_8U);
                                Mat laplacianImage8bit = new Mat();
                                laplacianImage.convertTo(laplacianImage8bit, l);
                                System.gc();

                                Bitmap bmp = Bitmap.createBitmap(laplacianImage8bit.cols(),
                                        laplacianImage8bit.rows(), Bitmap.Config.ARGB_8888);

                                Utils.matToBitmap(laplacianImage8bit, bmp);

                                int[] pixels = new int[bmp.getHeight() * bmp.getWidth()];
                                bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(),
                                        bmp.getHeight());
                                if (bmp != null)
                                    if (!bmp.isRecycled()) {
                                        bmp.recycle();

                                    }
                                int maxLap = -16777216;

                                for (int i = 0; i < pixels.length; i++) {

                                    if (pixels[i] > maxLap) {
                                        maxLap = pixels[i];
                                    }
                                }
                                int soglia = -1118750;


                                if (maxLap < soglia || maxLap == soglia) {
                                    Toast.makeText(MainActivity.this, "The photo exceeds the BLUR threshold ", Toast.LENGTH_SHORT).show();
                                    MainBlurTexView.setText(maxLap + " Blur");
                                    MainBlurTexView.setTextColor(Color.RED);
                                } else {
                                    Toast.makeText(MainActivity.this, "The photo does not exceed the BLUR threshold", Toast.LENGTH_SHORT).show();
                                    MainBlurTexView.setText(maxLap + " Blur");
                                    MainBlurTexView.setTextColor(Color.GREEN);
                                }
                            }else {
                                Toast.makeText(MainActivity.this, "No photo to test", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Toast.makeText(MainActivity.this, "Image display problem", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        MainprofilImage.setClickable(true);
        MainprofilImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                someActivityResultLauncher.launch(openGalleryIntent);
            }
        });
    }
}