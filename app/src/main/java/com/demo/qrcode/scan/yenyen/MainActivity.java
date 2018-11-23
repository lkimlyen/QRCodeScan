package com.demo.qrcode.scan.yenyen;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.demo.qrcode.scan.yenyen.common.ActionUtils;
import com.demo.qrcode.scan.yenyen.common.QrUtils;
import com.demo.qrcode.scan.yenyen.decode.BitmapDecoder;
import com.demo.qrcode.scan.yenyen.zxing.camera.CameraManager;
import com.demo.qrcode.scan.yenyen.zxing.decoding.CaptureActivityHandler;
import com.demo.qrcode.scan.yenyen.zxing.decoding.InactivityTimer;
import com.demo.qrcode.scan.yenyen.zxing.view.ViewfinderView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.client.result.ResultParser;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    public static final int REQUEST_CODE_PICK_IMAGE = 666;
    private final String TAG = MainActivity.class.getName();

    private static final int REQUEST_PERMISSION_CAMERA = 1000;
    private static final int REQUEST_PERMISSION_PHOTO = 1001;
    private CaptureActivityHandler handler;
    @BindView(R.id.viewfinder_view)
    ViewfinderView viewfinderView;
    private boolean hasSurface;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    @BindView(R.id.img_flash_off)
    ImageView imgFlash;

    @BindView(R.id.img_list)
    ImageView imgList;

    @BindView(R.id.img_picture)
    ImageView imgPicture;


    private String photoPath = "";
    private boolean isTurnOn = false;
    InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        AdView mAdView = findViewById(R.id.banner);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("7EC1E06EE9854334195EC438256A9218").addTestDevice("1265C188D8463BE6E096069948AFD718").build();
        mAdView.loadAd(adRequest);
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.full_screen_id));
        mInterstitialAd.loadAd(new AdRequest.Builder().addTestDevice("7EC1E06EE9854334195EC438256A9218").build());
        if (mInterstitialAd != null) {
            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    // Load the next interstitial.
                    finish();
                }

            });

        }


        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
        CameraManager.init(getApplication());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA},
                        REQUEST_PERMISSION_CAMERA);
            }else {
                checkShowDialogRating();
            }
        }else {
            checkShowDialogRating();
        }


    }

    public void checkShowDialogRating() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        boolean show = sharedPref.getBoolean("DIALOG_RATING", false);

        if (!show) {
            String date = sharedPref.getString("DATE_SHOW", "");
            if (TextUtils.isEmpty(date) || !date.equals(getDateTimeCurrent())) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("DATE_SHOW", getDateTimeCurrent());
                editor.commit();
                CustomDialogRating dialog = new CustomDialogRating();
                dialog.show(getFragmentManager(), TAG);
            }
        }
    }


    private void saveCode(String result) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        String currentDateandTime = sdf.format(new Date());
        Calendar c = Calendar.getInstance();
        int Hr24 = c.get(Calendar.HOUR_OF_DAY);
        int Min = c.get(Calendar.MINUTE);
        String mHour = (Hr24 < 10) ? "0" + Hr24 : Hr24 + "";
        String mMin = (Min < 10) ? "0" + Min : Min + "";
        String dateCreate = mHour + ":" + mMin + ", " + currentDateandTime;
        QRCode item = new QRCode(RealmHelper.getMaxId(), result, dateCreate);
        RealmHelper.addItemAsync(item);
        CustomDialogResult dialogResult = new CustomDialogResult();
        dialogResult.show(getFragmentManager(), TAG);
        dialogResult.setContent(result);
        dialogResult.setListener(new CustomDialogResult.OnDismissDialogListener() {
            @Override
            public void onDismiss() {
                restartPreview();
            }
        });
    }

    @OnClick(R.id.img_picture)
    public void getPicture() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_PHOTO);
        } else {
            ActionUtils.startActivityForGallery(this, ActionUtils.PHOTO_REQUEST_GALLERY);
        }


    }


    @OnClick(R.id.layout_flash)
    public void flash() {
        if (!isTurnOn) {
            setFlashLightOpen(true);
            imgFlash.setVisibility(View.INVISIBLE);
            isTurnOn = true;
        } else {
            setFlashLightOpen(false);
            imgFlash.setVisibility(View.VISIBLE);
            isTurnOn = false;
        }
    }

    public void setFlashLightOpen(boolean open) {
        CameraManager.get().setFlashLight(open);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "xxxxxxxxxxxxxxxxxxxonResume");
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        decodeFormats = null;
        characterSet = null;


    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
        } catch (IOException ioe) {
            return;
        } catch (RuntimeException e) {
            return;
        }
        if (handler == null) {
            handler = new CaptureActivityHandler(this, decodeFormats,
                    characterSet);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "xxxxxxxxxxxxxxxxxxxonPause");
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        imgFlash.setVisibility(View.VISIBLE);

        //  flashIbtn.setImageResource(R.drawable.ic_flash_off_white_24dp);

        CameraManager.get().closeDriver();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && requestCode == REQUEST_PERMISSION_CAMERA) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("Camera permission request was denied.")
                        .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .show();
            }else {
                checkShowDialogRating();
            }
        } else if (grantResults.length > 0 && requestCode == REQUEST_PERMISSION_PHOTO) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                new AlertDialog.Builder(MainActivity.this)
                       .setMessage("Gallery permission request was denied.")
                        .setPositiveButton("Close", null)
                        .show();
            } else {
                ActionUtils.startActivityForGallery(MainActivity.this, ActionUtils.PHOTO_REQUEST_GALLERY);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK
                && data != null
                && requestCode == ActionUtils.PHOTO_REQUEST_GALLERY) {
            Uri inputUri = data.getData();
            String path = null;

            if (URLUtil.isFileUrl(inputUri.toString())) {
                // 小米手机直接返回的文件路径
                path = inputUri.getPath();
            } else {
                String[] proj = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(inputUri, proj, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
                }
            }
            if (!TextUtils.isEmpty(path)) {
                Result result = QrUtils.decodeImage(path);
                if (result != null) {
                    if (BuildConfig.DEBUG) Log.d(TAG, result.getText());
                    handleDecode(result, null);
                } else {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(MainActivity.this, "Barcode Fail", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                if (BuildConfig.DEBUG) Log.e(TAG, "image path not found");
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(MainActivity.this, "Image path not found", Toast.LENGTH_SHORT).show();
                    }
                });
            }


        }
    }

    public void handleDecode(Result result, Bitmap barcode) {
        inactivityTimer.onActivity();
        String resultString = result.getText();
        saveCode(resultString);
    }

    @OnClick(R.id.img_list)
    public void openHistory() {
        Intent intent = new Intent(this, ListScanActivity.class);
        startActivity(intent);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(surfaceHolder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        hasSurface = false;
    }


    @Override
    public void onBackPressed() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            super.onBackPressed();
        }
    }

    public static String getDateTimeCurrent() {
        Date currentTime = Calendar.getInstance().getTime();
        String sDate = "";
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        sDate = formatter.format(currentTime);
        return sDate;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }

    protected void restartPreview() {
        // 当界面跳转时 handler 可能为null
        if (handler != null) {
            Message restartMessage = Message.obtain();
            restartMessage.what = R.id.restart_preview;
            handler.handleMessage(restartMessage);
        }
    }

}
