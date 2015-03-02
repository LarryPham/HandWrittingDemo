package com.techiedb.apps.handquill;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.*;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;

public class SignatureActivity extends Activity {
    public static final String TAG = SignatureActivity.class.getSimpleName();
    private FrameLayout mSignatureContent;
    private SignatureView mSignature;
    private Button mClearButton, mSaveButton, mCancelButton;
    private static String sTempDirectory;
    public int mCount = 1;

    public String mCurrent = null;
    private Bitmap mBitmap;
    private View mView;
    private File mContentFile;

    private String mUniqueId;
    private EditText mYourName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_editor_signature);

        sTempDirectory = Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.external_dir) + "/";
        ContextWrapper mContextWrapper = new ContextWrapper(getApplicationContext());
        File directory = mContextWrapper.getDir(getResources().getString(R.string.external_dir), Context.MODE_PRIVATE);
        prepareDirectory();
        mUniqueId = getTodayDate() + "_" + getCurrentTime() + "_" + Math.random();
        mCurrent = mUniqueId +".png";
        mContentFile = new File(directory, mCurrent);

        mSignatureContent = (FrameLayout) findViewById(R.id.signature_content);
        mSignature = new SignatureView(this, null);
        mSignature.setBackgroundColor(Color.WHITE);
        mSignatureContent.addView(mSignature, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mClearButton = (Button) findViewById(R.id.editor_clear_button);
        mSaveButton = (Button) findViewById(R.id.editor_save_button);
        mCancelButton = (Button) findViewById(R.id.editor_cancel_button);
        mView = mSignatureContent;

        mYourName = (EditText) findViewById(R.id.editext_your_name);
        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG, "Panel Cleared");
                mSignature.clear();
                mSaveButton.setEnabled(false);
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Panel Saved");
                boolean error = captureSignature();
                if (!error) {
                    mView.setDrawingCacheEnabled(true);
                    mSignature.save(mView);
                    final Bundle bundle = new Bundle();
                    bundle.putString("statue", "done");
                    final Intent intent = new Intent();
                    intent.putExtras(bundle);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG, "Panel Canceled");
                final Bundle bundle = new Bundle();
                bundle.putString("status", "cancel");
                final Intent intent = new Intent();
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        Log.w(TAG, "onDestroy");
        super.onDestroy();
    }

    private boolean captureSignature() {
        boolean error = false;
        String errorMessage = "";
        if (mYourName.getText().toString().equalsIgnoreCase("")){
            errorMessage = errorMessage + "Please enter your name\n";
            error = true;
        }
        if (error) {
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 105, 50);
            toast.show();
        }
        return error;
    }

    private String getTodayDate() {
        final Calendar calendar = Calendar.getInstance();
        int todayDate = ((calendar.get(Calendar.YEAR) * 10000) + ((calendar.get(Calendar.MONTH) + 1) * 100)
                + calendar.get(Calendar.DAY_OF_MONTH));
        Log.w("DATE: ", String.valueOf(todayDate));
        return String.valueOf(todayDate);
    }

    private String getCurrentTime() {
        final Calendar calendar = Calendar.getInstance();
        int currentTime = (calendar.get(Calendar.HOUR_OF_DAY) * 10000) + (calendar.get(Calendar.MINUTE)* 100)
                + (calendar.get(Calendar.SECOND));
        Log.w("TIME: ",String.valueOf(currentTime));
        return String.valueOf(currentTime);
    }

    private boolean prepareDirectory() {
        try {
            if (makeDirs()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(this, "Could not initialize system.Is Sdcard mounted properly?\n", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private boolean makeDirs() {
        File tempDir = new File(sTempDirectory);
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        if (tempDir.isDirectory()) {
            File[] files = tempDir.listFiles();
            for (File file : files) {
                if (!file.delete()) {
                    System.out.println("Failed to delete" + file);
                }
            }
        }
        return (tempDir.isDirectory());
    }

    public class SignatureView extends View {
        private static final float STROKE_WIDTH = 5f;
        private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;

        private Paint mPaint = new Paint();
        private Path mPath = new Path();

        private float mLastTouchX;
        private float mLastTouchY;
        private final RectF mDirtyRect = new RectF();

        public SignatureView(Context context, AttributeSet attrs) {
            super(context, attrs);
            mPaint.setAntiAlias(true);
            mPaint.setColor(Color.BLACK);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setStrokeWidth(STROKE_WIDTH);
        }

        public void save(View view) {
            Log.d("LOGTAG", String.format("width: %d, height: %d",view.getWidth(), view.getHeight()));
            if (mBitmap == null) {
                mBitmap = Bitmap.createBitmap(mSignatureContent.getWidth(), mSignatureContent.getHeight(), Bitmap.Config.RGB_565);
            }

            Canvas canvas = new Canvas(mBitmap);
            try {
                FileOutputStream mFileOutputStream = new FileOutputStream(mContentFile);
                view.draw(canvas);
                mBitmap.compress(Bitmap.CompressFormat.PNG, 90, mFileOutputStream);
                mFileOutputStream.flush();
                mFileOutputStream.close();
                String url = MediaStore.Images.Media.insertImage(getContentResolver(), mBitmap, "title", null);
                Log.v("LogTag", "url: " + url);
            } catch (Exception ex) {
                Log.v("LogTag", ex.toString());
            }
        }

        public void clear() {
            mPath.reset();
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawPath(mPath, mPaint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float eventX = event.getX();
            float eventY = event.getY();

            mSaveButton.setEnabled(true);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mPath.moveTo(eventX, eventY);
                    mLastTouchX = eventX;
                    mLastTouchY = eventY;
                    return true;
                case MotionEvent.ACTION_MOVE:
                case MotionEvent.ACTION_UP:
                    resetDirtyRect(eventX, eventY);
                    int historySize = event.getHistorySize();
                    for (int i = 0; i < historySize; i++) {
                        float historicalX = event.getHistoricalX(i);
                        float historicalY = event.getHistoricalY(i);
                        expandDirtyRect(historicalX, historicalY);
                        mPath.lineTo(historicalX, historicalY);
                    }
                    mPath.lineTo(eventX, eventY);
                    break;
                default:
                    Log.d("LOGTAG", String.format("Ignored touch event: %s", event.toString()));
                    return false;
            }
            invalidate((int) (mDirtyRect.left - HALF_STROKE_WIDTH), (int) (mDirtyRect.top - HALF_STROKE_WIDTH),
                    (int) ( mDirtyRect.right + HALF_STROKE_WIDTH), (int) (mDirtyRect.bottom + HALF_STROKE_WIDTH));
            mLastTouchX = eventX;
            mLastTouchY = eventY;
            return true;
        }

        public void expandDirtyRect(float historicalX, float historicalY) {
            if (historicalX < mDirtyRect.left) {
                mDirtyRect.left = historicalX;
            } else if (historicalX > mDirtyRect.right) {
                mDirtyRect.right = historicalX;
            }

            if (historicalY < mDirtyRect.top) {
                mDirtyRect.top = historicalY;
            } else if (historicalY > mDirtyRect.bottom) {
                mDirtyRect.bottom = historicalY;
            }
        }

        public void resetDirtyRect(float eventX, float eventY) {
            mDirtyRect.left = Math.min(mLastTouchX, eventX);
            mDirtyRect.right = Math.max(mLastTouchX, eventX);

            mDirtyRect.top = Math.min(mLastTouchY, eventY);
            mDirtyRect.bottom = Math.max(mLastTouchY, eventY);
        }
    }
}
