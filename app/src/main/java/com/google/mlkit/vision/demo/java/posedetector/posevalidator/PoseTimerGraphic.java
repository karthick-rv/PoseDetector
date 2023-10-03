package com.google.mlkit.vision.demo.java.posedetector.posevalidator;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.google.mlkit.vision.demo.GraphicOverlay;

import java.util.Locale;

public class PoseTimerGraphic extends GraphicOverlay.Graphic {

    final String count;
    private final Paint textPaint;

    private Rect rect = new Rect();


    public PoseTimerGraphic(GraphicOverlay overlay, String count) {
        super(overlay);
        this.count = count;
        textPaint = new Paint();

        textPaint.setStrokeWidth(200);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(200);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.getClipBounds(rect);
        int cHeight = rect.height();
        int cWidth = rect.width();
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.getTextBounds(count, 0, count.length(), rect);
        float x = cWidth / 2f - rect.width() / 2f - rect.left;
        float y = cHeight / 2f + rect.height() / 2f - rect.bottom;
        canvas.drawText(
                count,
                x,
                y,
                textPaint);
    }
}
