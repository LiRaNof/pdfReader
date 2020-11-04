package com.example.myapplication;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
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
    //　アクティビティが生成されたときに呼ばれるコールバック関数
    protected void onCreate(Bundle savedInstanceState) {

        //　継承元である AppCompatActivity　の生成時コールバックの挙動を継承　
        super.onCreate(savedInstanceState);
        //　ビューの配置
        setContentView(R.layout.activity_main);


/*        if(!Environment.isExternalStorageManager()){
            Intent intent = new Intent();
            intent.setAction(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION );
            startActivity(intent);
        }

 */

        //　READ_EXTERNAL_STORAGE　が許可されているかどうかをチェック
        int permissionCheck = ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE);

        //　READ_EXTERNAL_STORAGEが許可されていた場合
        if (permissionCheck == PERMISSION_GRANTED) {

            Log.d("debug", "permission is granted");

            // READ_EXTERNAL_STORAGEが不許可で、許可を求める理由を表示をしてもよい場合。
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE)) {

            new AlertDialog.Builder(this)
                    .setTitle("パーミッションの追加説明")
                    .setMessage("このアプリを利用するにはパーミッションが必要です")
                    //　はいボタンの動作（許可申請）
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
            // READ_EXTERNAL_STORAGEが不許可で、許可を求める理由を表示をしてはいけない場合。
        } else {

            ActivityCompat.requestPermissions(this, new String[]{
                    READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION);
        }


        //　IDからimageviewを取得
        final ImageView image1 = findViewById(R.id.imageView);
        //クリックされたときのイベントハンドラ
        image1.setOnClickListener(new View.OnClickListener() {
            //クリックされたときに呼ばれるコールバック
            @RequiresApi(api = Build.VERSION_CODES.M)
            public void onClick(View view) {


                // **** デバッグ用表示　****
                Log.d("debug", "button1, Perform action on click");
                int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, READ_EXTERNAL_STORAGE);
                if (permissionCheck == PERMISSION_GRANTED) {
                    Log.d("debug", "permission is granted");
                } else {
                    Log.d("debug", "permission is not granted");
                }
                // **** デバッグ用表示　****


                // **ファイル読み込み部分**

                // 外部ディレクトリへのFileの取得　(API 29では使用不可？？)
                File sdcard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                // デバッグ用表示
                Log.d("debug", sdcard.getAbsolutePath());



                ParcelFileDescriptor fd = null;
                PdfRenderer renderer = null;
                PdfRenderer.Page page = null;
                try {

                    //　デバッグ用表示
                    //sdcard =MainActivity.this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                    Log.d("debug", sdcard.getAbsolutePath());


                    // *** SDカード直下からtest.pdfを読み込み、1ページ目を取得 ***

                    //PDFのアドレスをFileとして取得
                    File file = new File(sdcard, "test.pdf");

                    //ファイルプロバイダにURI を問い合わせ
                    Uri uri = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".provider", file);

                    //ContentResolver を生成
                    ContentResolver cr = getContentResolver();

                    //ContentResolverから FileDescriptorを取得
                    fd = cr.openFileDescriptor(uri, "r");

                    //fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);

                    // pdrRendererにFileDescriptor を接続
                    renderer = new PdfRenderer(fd);

                    // pdrRendererでpageを描画
                    page = renderer.openPage(0);

                    ImageView image = (ImageView) view;
                    int viewWidth = image.getWidth();
                    int viewHeight = image.getHeight();
                    float pdfWidth = page.getWidth();
                    float pdfHeight = page.getHeight();

                    //　デバッグ用表示
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