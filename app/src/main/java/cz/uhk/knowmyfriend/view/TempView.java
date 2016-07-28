package cz.uhk.knowmyfriend.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.gms.vision.face.Face;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AlesB on 25.07.16.
 */
public class TempView extends View {

    List<Face> faces = new ArrayList<>();
    Bitmap bitmap = null;

    public TempView(Context context) {
        super(context);
    }

    public TempView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TempView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TempView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();
        paint.setColor(0xFF00FF00);
        paint.setTextSize(25);
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);

        /*
        int y = 30;
        for (Face face : faces) {
            canvas.drawText(face.getId() + " [" + face.getPosition().x + ";" + face.getPosition().y + "]", 0, y, paint);
            y+= 30;
        }*/

        if (bitmap != null) {
            canvas.drawBitmap(bitmap, 300, 0, paint);
        }
    }

    public void register(Face item) {
        faces.add(item);
        invalidate();
    }

    public void update(Face item) {
        //faces.set(item.getId(), item);
        invalidate();
    }

    public void update(Face item, Bitmap bitmap) {
        this.bitmap = bitmap;
        invalidate();
    }
}
