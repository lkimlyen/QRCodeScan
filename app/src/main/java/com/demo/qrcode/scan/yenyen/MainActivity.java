package com.demo.qrcode.scan.yenyen;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.demo.qrcode.scan.yenyen.decode.BitmapDecoder;
import com.google.zxing.Result;
import com.google.zxing.client.result.ResultParser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.dm7.barcodescanner.core.IViewFinder;
import me.dm7.barcodescanner.core.ViewFinderView;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    public static final int REQUEST_CODE_PICK_IMAGE = 666;
    private final String TAG = MainActivity.class.getName();
    private ZXingScannerView mScannerView;

    @BindView(R.id.img_flash_off)
    ImageView imgFlash;


    @BindView(R.id.img_list)
    ImageView imgList;

    @BindView(R.id.img_picture)
    ImageView imgPicture;

    @BindView(R.id.content_frame)
    ViewGroup contentFrame;

    private
    String photoPath = "";
    private boolean isTurnOn = false;
    private Bitmap selectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mScannerView = new ZXingScannerView(this) {
            @Override
            protected IViewFinder createViewFinderView(Context context) {
                return new CustomViewFinderView(context);
            }
        };
        mScannerView.setBorderColor(getResources().getColor(R.color.colorPrimary));
        mScannerView.setBorderLineLength(getResources().getDimensionPixelSize(R.dimen.size_50dp));
        mScannerView.setBorderStrokeWidth(getResources().getDimensionPixelSize(R.dimen.size_7dp));
        checPermissionCamera();
    }

    private void checPermissionCamera() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    3);
            return;
        }
        contentFrame.addView(mScannerView);
    }

    @Override
    public void handleResult(Result result) {
        // Do something with the result here
        Log.v(TAG, result.getText()); // Prints scan results
        Log.v(TAG, result.getBarcodeFormat().toString());// Prints the scan format (qrcode, pdf417 etc.)
        saveCode(result.getText());

        // If you would like to resume scanning, call this method below:
        // mScannerView.resumeCameraPreview(this);
    }

    private void saveCode(String result) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        String currentDateandTime = sdf.format(new Date());
        Calendar c = Calendar.getInstance();
        int Hr24 = c.get(Calendar.HOUR_OF_DAY);
        int Min = c.get(Calendar.MINUTE);
        String mHour = (Hr24 < 10) ? "0" + Hr24 : Hr24 + "";
        String mMin = (Min < 10) ? "0" + Min : Min + "";

        QRCode item = new QRCode(RealmHelper.getMaxId() + 1, result, mHour + ":" + mMin + ", " + currentDateandTime);
        RealmHelper.addItemAsync(item);
        CustomDialogResult dialogResult = new CustomDialogResult();
        dialogResult.show(getFragmentManager(), TAG);
        dialogResult.setContent(result);
        dialogResult.setListener(new CustomDialogResult.OnDismissDialogListener() {
            @Override
            public void onDismiss() {
                mScannerView.resumeCameraPreview(MainActivity.this);
            }
        });
    }

    @OnClick(R.id.img_picture)
    public void getPicture() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);
            return;
        }
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, REQUEST_CODE_PICK_IMAGE);
    }


    @OnClick(R.id.layout_flash)
    public void flash() {
        if (!isTurnOn) {
            mScannerView.setFlash(true);
            imgFlash.setVisibility(View.INVISIBLE);
            isTurnOn = true;
        } else {
            mScannerView.setFlash(false);
            imgFlash.setVisibility(View.VISIBLE);
            isTurnOn = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == 1) {
                getPicture();
            }
            if (requestCode == 3) checPermissionCamera();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_PICK_IMAGE:

                    Cursor cursor = getContentResolver().query(
                            data.getData(), null, null, null, null);
                    if (cursor.moveToFirst()) {
                        photoPath = cursor.getString(cursor
                                .getColumnIndex(MediaStore.Images.Media.DATA));
                    }
                    cursor.close();


                    Bitmap img = BitmapUtils
                            .getCompressedBitmap(photoPath);

                    BitmapDecoder decoder = new BitmapDecoder(
                            MainActivity.this);
                    Result result = decoder.getRawResult(img);

                    if (result != null) {
                        saveCode(ResultParser.parseResult(result)
                                .toString());
                    } else {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(MainActivity.this, "Barcode Fail", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

            }


        }
    }


    @OnClick(R.id.img_list)
    public void openHistory() {
        Intent intent = new Intent(this, ListScanActivity.class);
        startActivity(intent);
    }

    private static class CustomViewFinderView extends ViewFinderView {
        public static final int TRADE_MARK_TEXT_SIZE_SP = 40;
        public final Paint PAINT = new Paint();

        public CustomViewFinderView(Context context) {
            super(context);
            init();
        }

        public CustomViewFinderView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        private void init() {
            PAINT.setColor(Color.WHITE);
            PAINT.setAntiAlias(true);
            float textPixelSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                    TRADE_MARK_TEXT_SIZE_SP, getResources().getDisplayMetrics());
            PAINT.setTextSize(textPixelSize);
            setSquareViewFinder(true);
        }

        @Override
        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);

        }

        private void drawTradeMark(Canvas canvas) {
            Rect framingRect = getFramingRect();
            float tradeMarkTop;
            float tradeMarkLeft;
            if (framingRect != null) {
                tradeMarkTop = framingRect.bottom + PAINT.getTextSize() + 10;
                tradeMarkLeft = framingRect.left;
            } else {
                tradeMarkTop = 10;
                tradeMarkLeft = canvas.getHeight() - PAINT.getTextSize() - 10;
            }
        }
    }

}
