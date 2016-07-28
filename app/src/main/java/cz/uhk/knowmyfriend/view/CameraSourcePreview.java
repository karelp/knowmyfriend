package cz.uhk.knowmyfriend.view;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.hardware.display.VirtualDisplay;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import com.google.android.gms.common.images.Size;
import com.google.android.gms.vision.CameraSource;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class CameraSourcePreview extends ViewGroup {
    private static final String TAG = "CameraSourcePreview";

    private Context mContext;
    private SurfaceView mSurfaceView;
    private boolean mStartRequested;
    private boolean mSurfaceAvailable;
    private CameraSource mCameraSource;

    private GraphicOverlay mOverlay;

    public CameraSourcePreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mStartRequested = false;
        mSurfaceAvailable = false;

        setDrawingCacheEnabled(true);

        mSurfaceView = new SurfaceView(context);
        mSurfaceView.setDrawingCacheEnabled(true);
        mSurfaceView.getHolder().addCallback(new SurfaceCallback());
        addView(mSurfaceView);
    }


    public Bitmap getBitmapAtPosition(TempView view, int x, int y, int width, int height) {

        Bitmap a = Bitmap.createBitmap(mSurfaceView.getWidth(), mSurfaceView.getHeight(), Bitmap.Config.ARGB_8888);
        draw(new Canvas(a));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            //OutputStream out = new BufferedOutputStream(outputStream);
            a.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            byte[] bytes = outputStream.toByteArray();

        Bitmap bitmapa = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);


        Paint paint = new Paint();
        paint.setColor(0xFF00FF00);
        paint.setTextSize(25);
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(0xFF00AACC);

        buildDrawingCache();

        Bitmap old = getDrawingCache();

/*
        SurfaceHolder holder = mSurfaceView.getHolder();
        if (holder != null) {
            Canvas canvas = holder.lockCanvas();
            if (canvas != null) {
                canvas.drawBitmap(bitmap, 0, 0, paint);
                mSurfaceView.getHolder().unlockCanvasAndPost(canvas);
            }
        }
        */
        canvas.drawBitmap(old, -x, -y, paint);


        //canvas.drawBitmap(mSurfaceView.getDrawingCache(), 0, 0, paint);

        view.update(null, bitmap);

        return bitmap;
    }

    public void start(CameraSource cameraSource) throws IOException {
        if (cameraSource == null) {
            stop();
        }

        mCameraSource = cameraSource;

        if (mCameraSource != null) {
            mStartRequested = true;
            startIfReady();
        }
    }

    public void start(CameraSource cameraSource, GraphicOverlay overlay) throws IOException {
        mOverlay = overlay;
        start(cameraSource);
    }

    public void stop() {
        if (mCameraSource != null) {
            mCameraSource.stop();
        }
    }

    public void release() {
        if (mCameraSource != null) {
            mCameraSource.release();
            mCameraSource = null;
        }
    }

    private void startIfReady() throws IOException {
        if (mStartRequested && mSurfaceAvailable) {
            mCameraSource.start(mSurfaceView.getHolder());
            if (mOverlay != null) {
                Size size = mCameraSource.getPreviewSize();
                int min = Math.min(size.getWidth(), size.getHeight());
                int max = Math.max(size.getWidth(), size.getHeight());
                if (isPortraitMode()) {
                    // Swap width and height sizes when in portrait, since it will be rotated by
                    // 90 degrees
                    mOverlay.setCameraInfo(min, max, mCameraSource.getCameraFacing());
                } else {
                    mOverlay.setCameraInfo(max, min, mCameraSource.getCameraFacing());
                }
                mOverlay.clear();
            }
            mStartRequested = false;
        }
    }

    private class SurfaceCallback implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder surface) {
            mSurfaceAvailable = true;
            try {
                startIfReady();
            } catch (IOException e) {
                Log.e(TAG, "Could not start camera source.", e);
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surface) {
            mSurfaceAvailable = false;
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int width = 320;
        int height = 240;
        if (mCameraSource != null) {
            Size size = mCameraSource.getPreviewSize();
            if (size != null) {
                width = size.getWidth();
                height = size.getHeight();
            }
        }

        // Swap width and height sizes when in portrait, since it will be rotated 90 degrees
        if (isPortraitMode()) {
            int tmp = width;
            width = height;
            height = tmp;
        }

        final int layoutWidth = right - left;
        final int layoutHeight = bottom - top;

        // Computes height and width for potentially doing fit width.
        int childWidth = layoutWidth;
        int childHeight = (int)(((float) layoutWidth / (float) width) * height);

        // If height is too tall using fit width, does fit height instead.
        if (childHeight > layoutHeight) {
            childHeight = layoutHeight;
            childWidth = (int)(((float) layoutHeight / (float) height) * width);
        }

        for (int i = 0; i < getChildCount(); ++i) {
            getChildAt(i).layout(0, 0, childWidth, childHeight);
        }

        try {
            startIfReady();
        } catch (IOException e) {
            Log.e(TAG, "Could not start camera source.", e);
        }
    }

    private boolean isPortraitMode() {
        int orientation = mContext.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return false;
        }
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            return true;
        }

        Log.d(TAG, "isPortraitMode returning false by default");
        return false;
    }
}