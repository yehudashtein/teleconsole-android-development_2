package com.telebroad.teleconsole.helpers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class VisualizerViewSMS extends View {
    private static final int LINE_WIDTH = 1; // width of visualizer lines
    private static final int LINE_SCALE = 75; // scales visualizer lines
    private List<Float> amplitudes; // amplitudes for line lengths
    private int width; // width of this View
    private int height; // height of this View
    private final Paint linePaint; // specifies line drawing characteristics
    private boolean sizeChangedOnce = false;

    // constructor
    public VisualizerViewSMS(Context context, AttributeSet attrs) {
        super(context, attrs); // call superclass constructor
        linePaint = new Paint(); // create Paint for lines
        String hexColor = "#5c6bc0";
        int color = Color.parseColor(hexColor);
        linePaint.setColor(color); // set color to green
        linePaint.setStrokeWidth(LINE_WIDTH); // set stroke width
        //Log.d("clearVisualizer","clear2");
    }
    // called when the dimensions of the View change
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (sizeChangedOnce) {
            return; // Return if size has already changed once
        }
        width = w; // new width of this View
        height = h; // new height of this View
        amplitudes = new ArrayList<>(width / LINE_WIDTH);
       // Log.d("clearVisualizer","clear1");
        sizeChangedOnce = true;
    }

    // clear all amplitudes to prepare for a new visualization
    public void clear() {
        //Log.d("clearVisualizer","clear");
        if (amplitudes != null){
            amplitudes.clear();
        }
    }
//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        // Get the suggested minimum width and height
//        int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
//        int minh = getPaddingTop() + getPaddingBottom() + getSuggestedMinimumHeight();
//
//        // Compute the width and height based on the measure spec
//        int w = resolveSizeAndState(minw, widthMeasureSpec, 1);
//        int h = resolveSizeAndState(minh, heightMeasureSpec, 0);
//
//        // Set the measured dimensions for the view
//        setMeasuredDimension(w, h);
//    }


    // add the given amplitude to the amplitudes ArrayList
    public void addAmplitude(float amplitude) {
        amplitudes.add(amplitude); // add newest to the amplitudes ArrayList
        // if the power lines completely fill the VisualizerView
        if (amplitudes.size() * LINE_WIDTH >= width) {
            amplitudes.remove(0); // remove oldest power value
        }
    }
    public void removeWaves(){
        amplitudes.remove(0);
    }

    // draw the visualizer with scaled lines representing the amplitudes
    @Override
    public void onDraw(Canvas canvas) {
        //.d("clearVisualizer","clear3");
//        int middle = height / 2; // get the middle of the View
//        float curX = 0; // start curX at zero
//        // for each item in the amplitudes ArrayList
//        for (float power : amplitudes) {
//            float scaledHeight = power / LINE_SCALE; // scale the power
//            curX += LINE_WIDTH; // increase X by LINE_WIDTH
//            // draw a line representing this item in the amplitudes ArrayList
//            canvas.drawLine(curX, middle + scaledHeight / 2, curX, middle
//                    - scaledHeight / 2, linePaint);
//        }
        int middle = height / 2; // get the middle of the View
        float curX = 0; // start curX at zero
        // for each item in the amplitudes ArrayList
        for (float power : amplitudes) {
            float scaledHeight = power / LINE_SCALE; // scale the power
            curX += LINE_WIDTH; // increase X by LINE_WIDTH
            if (curX >= width) curX = 0; // reset the x-coordinate if it exceeds the width
            // draw a line representing this item in the amplitudes ArrayList
            canvas.drawLine(curX, middle + scaledHeight / 2, curX, middle
                    - scaledHeight / 2, linePaint);
        }
    }
}
