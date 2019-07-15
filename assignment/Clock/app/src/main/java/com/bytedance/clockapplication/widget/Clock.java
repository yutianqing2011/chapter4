package com.bytedance.clockapplication.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.view.View;

import java.util.Calendar;
import java.util.Locale;

public class Clock extends View {
    @SuppressLint("HandlerLeak") Handler myhandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            invalidate();
            sendMessageDelayed(Message.obtain(this, 0x0), 1000);
        }
    };
    public void stop(){
        myhandler.removeMessages(0);
    }
    private final static String TAG = Clock.class.getSimpleName();

    private static final int FULL_ANGLE = 360;

    private static final int CUSTOM_ALPHA = 140;
    private static final int FULL_ALPHA = 255;

    private static final int DEFAULT_PRIMARY_COLOR = Color.WHITE;
    private static final int DEFAULT_SECONDARY_COLOR = Color.LTGRAY;

    private static final float DEFAULT_DEGREE_STROKE_WIDTH = 0.010f;

    public final static int AM = 0;

    private static final int RIGHT_ANGLE = 90;

    private int mWidth, mCenterX, mCenterY, mRadius;

    /**
     * properties
     */
    private int centerInnerColor;
    private int centerOuterColor;

    private int secondsNeedleColor;
    private int hoursNeedleColor;
    private int minutesNeedleColor;

    private int degreesColor;

    private int hoursValuesColor;

    private int numbersColor;

    private boolean mShowAnalog = true;

    public Clock(Context context) {
        super(context);
        init(context, null);
    }

    public Clock(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public Clock(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int size;
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int widthWithoutPadding = width - getPaddingLeft() - getPaddingRight();
        int heightWithoutPadding = height - getPaddingTop() - getPaddingBottom();

        if (widthWithoutPadding > heightWithoutPadding) {
            size = heightWithoutPadding;
        } else {
            size = widthWithoutPadding;
        }

        setMeasuredDimension(size + getPaddingLeft() + getPaddingRight(), size + getPaddingTop() + getPaddingBottom());
    }

    private void init(Context context, AttributeSet attrs) {

        this.centerInnerColor = Color.LTGRAY;
        this.centerOuterColor = DEFAULT_PRIMARY_COLOR;

        this.secondsNeedleColor = DEFAULT_SECONDARY_COLOR;
        this.hoursNeedleColor = DEFAULT_PRIMARY_COLOR;
        this.minutesNeedleColor = DEFAULT_PRIMARY_COLOR;

        this.degreesColor = DEFAULT_PRIMARY_COLOR;

        this.hoursValuesColor = DEFAULT_PRIMARY_COLOR;

        numbersColor = Color.WHITE;


        myhandler.sendMessage(Message.obtain(myhandler, 0x0));

    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        mWidth = getHeight() > getWidth() ? getWidth() : getHeight();

        int halfWidth = mWidth / 2;
        mCenterX = halfWidth;
        mCenterY = halfWidth;
        mRadius = halfWidth;

        if (mShowAnalog) {
            drawDegrees(canvas);
            drawHoursValues(canvas);
            drawNeedles(canvas);
            drawCenter(canvas);
        } else {
            drawNumbers(canvas);
        }

    }

    private void drawDegrees(Canvas canvas) {

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(mWidth * DEFAULT_DEGREE_STROKE_WIDTH);
        paint.setColor(degreesColor);

        int rPadded = mCenterX - (int) (mWidth * 0.01f);
        int rEnd = mCenterX - (int) (mWidth * 0.05f);

        for (int i = 0; i < FULL_ANGLE; i += 6 /* Step */) {

            if ((i % RIGHT_ANGLE) != 0 && (i % 15) != 0)
                paint.setAlpha(CUSTOM_ALPHA);
            else {
                paint.setAlpha(FULL_ALPHA);
            }

            int startY = (int) (mCenterX + rPadded * Math.cos(Math.toRadians(i)));
            int startX = (int) (mCenterX - rPadded * Math.sin(Math.toRadians(i)));

            int stopY = (int) (mCenterX + rEnd * Math.cos(Math.toRadians(i)));
            int stopX = (int) (mCenterX - rEnd * Math.sin(Math.toRadians(i)));

            canvas.drawLine(startX, startY, stopX, stopY, paint);

        }
    }

    /**
     * @param canvas
     */
    private void drawNumbers(Canvas canvas) {

        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(mWidth * 0.2f);
        textPaint.setColor(numbersColor);
        textPaint.setColor(numbersColor);
        textPaint.setAntiAlias(true);

        Calendar calendar = Calendar.getInstance();

        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        int amPm = calendar.get(Calendar.AM_PM);

        String time = String.format("%s:%s:%s%s",
                String.format(Locale.getDefault(), "%02d", hour),
                String.format(Locale.getDefault(), "%02d", minute),
                String.format(Locale.getDefault(), "%02d", second),
                amPm == AM ? "AM" : "PM");

        SpannableStringBuilder spannableString = new SpannableStringBuilder(time);
        spannableString.setSpan(new RelativeSizeSpan(0.3f), spannableString.toString().length() - 2, spannableString.toString().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // se superscript percent

        StaticLayout layout = new StaticLayout(spannableString, textPaint, canvas.getWidth(), Layout.Alignment.ALIGN_CENTER, 1, 1, true);
        canvas.translate(mCenterX - layout.getWidth() / 2f, mCenterY - layout.getHeight() / 2f);
        layout.draw(canvas);

    }

    /**
     * Draw Hour Text Values, such as 1 2 3 ...
     *
     * @param canvas
     */
    private void drawHoursValues(Canvas canvas) {
        // Default Color:
        // - hoursValuesColor
        int rPadded = 400;
        final String nums[]={"12","1","2","3","4","5","6","7","8","9","10","11"};
        System.out.println("角度center："+mCenterX+"  "+mCenterY);
        for(int i=0;i<12;i++) {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setTextSize(50);
            paint.setColor(degreesColor);
            paint.setStyle(Paint.Style.STROKE);

            //paint.setStyle(Paint.Style.FILL_AND_STROKE);
           // paint.setStrokeCap(Paint.Cap.ROUND);
            //paint.setStrokeWidth(mWidth * DEFAULT_DEGREE_STROKE_WIDTH);


            //int startX = (int) (mCenterX - layout.getWidth() / 2f + (int) (rPadded * Math.sin(Math.toRadians(i*30))));
            //int startY = (int) (mCenterY - layout.getHeight() / 2f - (int) (rPadded * Math.cos(Math.toRadians(i*30))));

            int startX =  (int) (rPadded * Math.sin(Math.toRadians(i*30)));
            int startY = - (int) (rPadded * Math.cos(Math.toRadians(i*30)));
            //canvas.translate((startX+mCenterX), (startY+mCenterY));
            //layout.draw(canvas);
            //System.out.println("角度："+i+"  "+startX+"  "+startY+"  "+(rPadded * Math.cos(Math.toRadians(i*30))));
            canvas.drawText(nums[i],(startX+mCenterX-15), (startY+mCenterY+15),paint);
        }

    }

    /**
     * Draw hours, minutes needles
     * Draw progress that indicates hours needle disposition.
     *
     * @param canvas
     */
    Calendar calendar;
    Paint paintsec,paintmin,painthour;
    int secEnd,mintEnd,hourEnd;
    private void drawNeedles(final Canvas canvas) {
        // Default Color:
        // - secondsNeedleColor
        // - hoursNeedleColor
        // - minutesNeedleColor
        secEnd = mCenterX - (int) (mWidth * 0.08f);
        mintEnd = mCenterX - (int) (mWidth * 0.16f);
        hourEnd = mCenterX - (int) (mWidth * 0.24);



        paintsec = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintsec.setStyle(Paint.Style.FILL_AND_STROKE);
        paintsec.setStrokeCap(Paint.Cap.ROUND);
        paintsec.setStrokeWidth(mWidth * DEFAULT_DEGREE_STROKE_WIDTH);
        paintsec.setColor(degreesColor);

        paintmin = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintmin.setStyle(Paint.Style.FILL_AND_STROKE);
        paintmin.setStrokeCap(Paint.Cap.ROUND);
        paintmin.setStrokeWidth(2*mWidth * DEFAULT_DEGREE_STROKE_WIDTH);
        paintmin.setColor(degreesColor);

        painthour = new Paint(Paint.ANTI_ALIAS_FLAG);
        painthour.setStyle(Paint.Style.FILL_AND_STROKE);
        painthour.setStrokeCap(Paint.Cap.ROUND);
        painthour.setStrokeWidth(4*mWidth * DEFAULT_DEGREE_STROKE_WIDTH);
        painthour.setColor(degreesColor);


        calendar = Calendar.getInstance();

        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        int amPm = calendar.get(Calendar.AM_PM);


        int secStopX = (int) (mCenterX + secEnd * Math.sin(Math.toRadians(second*6)));
        int secStopY = (int) (mCenterX - secEnd * Math.cos(Math.toRadians(second*6)));

        int minStopX = (int) (mCenterX + mintEnd * Math.sin(Math.toRadians(minute*6)));
        int minStopY = (int) (mCenterX - mintEnd * Math.cos(Math.toRadians(minute*6)));

        int hourStopX = (int) (mCenterX + hourEnd * Math.sin(Math.toRadians(hour*30)));
        int hourStopY = (int) (mCenterX - hourEnd * Math.cos(Math.toRadians(hour*30)));

        //System.out.println("位置："+secStopX+"  "+secStopY+" second:"+second);

        canvas.drawLine(mCenterX,mCenterY,secStopX,secStopY,paintsec);
        canvas.drawLine(mCenterX,mCenterY,minStopX,minStopY,paintmin);
        canvas.drawLine(mCenterX,mCenterY,hourStopX,hourStopY,painthour);
        //canvas.drawLine(second, second, second*20, second*20, paintsec);

    }

    /**
     * Draw Center Dot
     *
     * @param canvas
     */
    private void drawCenter(Canvas canvas) {
        // Default Color:
        // - centerInnerColor
        // - centerOuterColor
        //canvas.drawCircle();
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setAntiAlias(false);                       //设置画笔为无锯齿
        paint.setColor(Color.WHITE);                    //设置画笔颜色
        paint.setStrokeWidth((float) 3.0);              //线宽
        paint.setStyle(Paint.Style.STROKE);                   //空心效果
        canvas.drawCircle(mCenterX, mCenterY, 10, paint);           //绘制圆形
    }

    public void setShowAnalog(boolean showAnalog) {
        mShowAnalog = showAnalog;
        invalidate();
    }

    public boolean isShowAnalog() {
        return mShowAnalog;
    }

}