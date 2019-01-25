package com.gbmaniac.smartdashboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import static com.gbmaniac.smartdashboard.Utilities.convertDpToPixel;

public class GesitsProgressBarCircular extends ProgressBar {
    private final static int STROKE_WIDTH = 18;
    private final static int GLOW_WIDTH = 30;
    private int strokeWidth, glowWidth;
    private int color = 0xffda1b23, background = 0xff101010, stop = 0xda1b23;
    float stops[] = {0.77f,0.78f,1f};
    private Paint progressPaint, backgroundPaint, glowPaint;
    private Paint onTextPaint, offTextPaint;
    private RectF progressRectF, glowRectF;
    private Rect textBounds;
    private boolean day_mode = false;
    private int centerX, centerY, radius;

    public GesitsProgressBarCircular(Context context) {
        super(context);
        init();
    }

    public GesitsProgressBarCircular(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GesitsProgressBarCircular(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init(){
        strokeWidth = convertDpToPixel(STROKE_WIDTH);
        glowWidth = convertDpToPixel(GLOW_WIDTH);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(strokeWidth);
        progressPaint.setColor(color);

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(strokeWidth);
        if(day_mode)
            backgroundPaint.setColor(0xffededed);
        else
            backgroundPaint.setColor(background);

        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.FILL);

        Typeface typeface = ResourcesCompat.getFont(getContext(),R.font.rajdhani_medium);
        textBounds = new Rect();
        offTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        if(!day_mode)
            offTextPaint.setColor(0xffededed);
        else
            offTextPaint.setColor(background);
        offTextPaint.setTextSize(convertDpToPixel(22));
        offTextPaint.setTypeface(typeface);

        onTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        onTextPaint.setColor(getContext().getColor(R.color.colorAccent));
        onTextPaint.setTextSize(convertDpToPixel(22));
        onTextPaint.setTypeface(typeface);
        progressRectF = null;
        glowRectF = null;
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        if(progressRectF == null || glowRectF == null){
            centerX = getMeasuredWidth()/2;
            centerY = getMeasuredHeight()/2;
            radius = Math.min(centerX,centerY);
            if(glowRectF == null){
                glowRectF =  new RectF(0,0,getMeasuredWidth(),getMeasuredHeight());
            }
            if(progressRectF == null){
                int top = convertDpToPixel(44);
                int bottom = getMeasuredHeight() - top;
                int left = convertDpToPixel(44);
                int right = getMeasuredWidth() - left;
                progressRectF = new RectF(left,top,right,bottom);
            }
            //float stops[] = new float[3];
            //stops[0] = (float)(getMeasuredWidth()-convertDpToPixel(53))/getMeasuredWidth();
            //stops[1] = stops[0]+0.01f;
            //stops[2] = 1f;
            glowPaint.setShader(new RadialGradient(centerX,centerY,radius, new int[]{stop,color&0xccffffff,stop},stops, Shader.TileMode.CLAMP));
        }

        //Menghitung jumlah batang pada progress bar
        int steps = (getProgress()*54)/getMax();
        for(int i= 0; i < 54; i++){
            canvas.drawArc(progressRectF,135.5f+5f*i,4f,false,i<steps?progressPaint:backgroundPaint);
        }
        canvas.drawArc(glowRectF,135,5f*steps,true,glowPaint);
        //drawTextValue(canvas,20,2,centerX,centerY, radius - glowWidth/3);

    }

    /**
     * Fungsi untuk menggambar angka di sekitar lingkaran
     * @param canvas yang digunakan sebagai pemuat gambar
     * @param max angka maksimal yang akan ditampilkan
     * @param skip jumlah angka yang dilewati +1
     * @param cx koordinat-x pusat lingkaran
     * @param cy koordinat-y pusat lingkaran
     * @param radius jari-jari lingkaran yang akan digambarkan angka
     */
    private void drawTextValue(Canvas canvas, int max, int skip, float cx, float cy, float radius){
        float text_cx, text_cy;
        float degreeIncrement = (float) 270 /max;
        for(int i = 0; i <= max; i++){
            if(i%skip == 0) {
                float degree = 135f + i * degreeIncrement;
                text_cx = (float) (cx + Math.cos(Math.toRadians(degree))* radius);
                text_cy = (float) (cy + Math.sin(Math.toRadians(degree))* radius);
                drawTextCentred(canvas, isDegreeAchieved(degree)?onTextPaint:offTextPaint,
                        String.valueOf(i), text_cx, text_cy);
            }
        }
    }

    /**
     * Fungsi untuk menghitung apakah progress mencapai suatu derajat
     * @param degree yang akan dibandingkan
     * @return boolean apakah progress mencapai derajat tersebut
     */
    private boolean isDegreeAchieved(float degree){
        float achieved = 135 + 270*getProgress()/getMax();
        return degree < achieved;
    }

    /**
     * Fungsi untuk menggambar angka di tengah text boundary
     * @param canvas yang digunakan sebagai pemuat gambar
     * @param paint warna teks
     * @param text yang akan ditulis
     * @param cx koordinat-x pusat lingkaran
     * @param cy koordinat-y pusat lingkaran
     */
    private void drawTextCentred(Canvas canvas, Paint paint, String text, float cx, float cy){
        paint.getTextBounds(text, 0, text.length(), textBounds);
        canvas.drawText(text, cx - textBounds.exactCenterX(), cy - textBounds.exactCenterY(), paint);
    }

    public void toggleNightMode(boolean day_mode){
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(strokeWidth);
        this.day_mode = day_mode;
        init();
        super.invalidate();
    }
}

