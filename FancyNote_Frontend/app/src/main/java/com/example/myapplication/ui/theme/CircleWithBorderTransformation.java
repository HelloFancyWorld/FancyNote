package com.example.myapplication.ui.theme;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Shader;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.util.Util;
import java.security.MessageDigest;

public class CircleWithBorderTransformation extends BitmapTransformation {

    private final Paint borderPaint;
    private final float borderWidth;

    public CircleWithBorderTransformation(int borderColor, float borderWidth) {
        borderPaint = new Paint();
        borderPaint.setColor(borderColor);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setAntiAlias(true);
        this.borderWidth = borderWidth;
        borderPaint.setStrokeWidth(borderWidth);
    }

    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        return circleCrop(pool, toTransform);
    }

    private Bitmap circleCrop(BitmapPool pool, Bitmap source) {
        if (source == null) return null;

        int size = Math.min(source.getWidth(), source.getHeight());
        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;

        Bitmap squared = Bitmap.createBitmap(source, x, y, size, size);

        Bitmap result = pool.get(size, size, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        canvas.drawColor(0, PorterDuff.Mode.CLEAR); // 设置透明背景

        BitmapShader shader = new BitmapShader(squared, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        paint.setShader(shader);

        float r = size / 2f;
        canvas.drawCircle(r, r, r, paint);

        if (borderWidth > 0) {
            float borderRadius = r - borderWidth / 2;
            canvas.drawCircle(r, r, borderRadius, borderPaint);
        }

        return result;
    }


    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {
        messageDigest.update((getClass().getName() + borderWidth).getBytes(CHARSET));
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof CircleWithBorderTransformation) {
            CircleWithBorderTransformation other = (CircleWithBorderTransformation) o;
            return borderWidth == other.borderWidth;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Util.hashCode(getClass().getName().hashCode(), Util.hashCode(borderWidth));
    }
}