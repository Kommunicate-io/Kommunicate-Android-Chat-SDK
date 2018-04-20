package com.applozic.mobicomkit.uiwidgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.util.AttributeSet;
import android.view.View;

public class DashedLineView extends View {
    private float density;
    private Paint paint;
    private Path path;
    private PathEffect effects;
    private String dashColor = "#BEBBBB";
    private float dashWidth = DimensionsUtils.convertDpToPixel(4);
    private float dashSpacing = DimensionsUtils.convertDpToPixel(2);

    public DashedLineView(Context context) {
        super(context);
        init(context);
    }

    public DashedLineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DashedLineView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        density = context.getResources().getDisplayMetrics().density;
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(density * 4);
        //set your own color
        paint.setColor(Color.parseColor(dashColor));
        path = new Path();
        //array is ON and OFF distances in px (4px line then 2px space)
        effects = new DashPathEffect(new float[]{dashWidth, dashSpacing, dashWidth, dashSpacing}, 0);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        paint.setPathEffect(effects);
        int measuredHeight = getMeasuredHeight();
        int measuredWidth = getMeasuredWidth();
        if (measuredHeight <= measuredWidth) {
            // horizontal
            path.moveTo(0, 0);
            path.lineTo(measuredWidth, 0);
            canvas.drawPath(path, paint);
        } else {
            // vertical
            path.moveTo(0, 0);
            path.lineTo(0, measuredHeight);
            canvas.drawPath(path, paint);
        }

    }
}