package com.example.myapplication;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.pdf.PdfRenderer;
import android.os.Environment;
import android.os.ParcelFileDescriptor;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;


public class MainActivity extends AppCompatActivity {
    private int REQUEST_CODE_PERMISSION = 0x01;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int permissionCheck = ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE);
        if (permissionCheck == PERMISSION_GRANTED) {
            Log.d("debug", "permission is granted");
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle("パーミッションの追加説明")
                    .setMessage("このアプリを利用するにはパーミッションが必要です")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{READ_EXTERNAL_STORAGE},
                                    REQUEST_CODE_PERMISSION);
                        }
                    })
                    .create()
                    .show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                            READ_EXTERNAL_STORAGE
                    },
                    REQUEST_CODE_PERMISSION);

        }


        final ImageView image1 = findViewById(R.id.imageView);
        image1.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            public void onClick(View view) {
                Log.d("debug", "button1, Perform action on click");
                int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, READ_EXTERNAL_STORAGE);
                if (permissionCheck == PERMISSION_GRANTED) {
                    Log.d("debug", "permission is granted");
                }else{
                    Log.d("debug", "permission is not granted");
                }

                File sdcard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) ;
                Log.d("debug", sdcard.getAbsolutePath());
                ParcelFileDescriptor fd = null;
                PdfRenderer renderer = null;
                PdfRenderer.Page page = null;
                try {
                    //sdcard =MainActivity.this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                    Log.d("debug", sdcard.getAbsolutePath());
                    // SDカード直下からtest.pdfを読み込み、1ページ目を取得
                    File file =new File(sdcard, "test.pdf");
                    Uri uri=FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".provider", file);

                    ContentResolver cr = getContentResolver();

                    fd= cr.openFileDescriptor(uri, "r");
                   // fd = ParcelFileDescriptor.open(), ParcelFileDescriptor.MODE_READ_ONLY);
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