package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Rect;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ImageView image1 = findViewById(R.id.imageView);
        image1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Log.d("debug", "button1, Perform action on click");
                File sdcard = Environment.getExternalStorageDirectory();
                ParcelFileDescriptor fd = null;
                PdfRenderer renderer = null;
                PdfRenderer.Page page = null;
                try {
                    // SDカード直下からtest.pdfを読み込み、1ページ目を取得

                    fd = ParcelFileDescriptor.open(new File(sdcard, "test.pdf"), ParcelFileDescriptor.MODE_READ_ONLY);
                    renderer = new PdfRenderer(fd);
                    page = renderer.openPage(0);

                    ImageView image = (ImageView) view;
                    int viewWidth = image.getWidth();
                    int viewHeight = image.getHeight();
                    float pdfWidth = page.getWidth();
                    float pdfHeight = page.getHeight();
                    Log.i("test", "viewWidth=" + viewWidth + ", viewHeight=" + viewHeight
                            + ", pdfWidth=" + pdfWidth + ", pdfHeight=" + pdfHeight);

                    // 縦横比合うように計算
                    float wRatio = viewWidth / pdfWidth;
                    float hRatio = viewHeight / pdfHeight;
                    if (wRatio <= hRatio) {
                        viewHeight = (int) Math.ceil(pdfHeight * wRatio);
                    } else {
                        viewWidth = (int) Math.ceil(pdfWidth * hRatio);
                    }
                    Log.i("test", "drawWidth=" + viewWidth + ", drawHeight=" + viewHeight);

                    // Bitmap生成して描画
                    Bitmap bitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888);
                    page.render(bitmap, new Rect(0, 0, viewWidth, viewHeight), null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                    image.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (fd != null) {
                            fd.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (page != null) {
                        page.close();
                    }
                    if (renderer != null) {
                        renderer.close();
                    }
                }
            }
        });
    }

}