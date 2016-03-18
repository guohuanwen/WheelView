package com.bcgtgjyb.wheelview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bigwen on 2016/3/17.
 */
public class WheelView extends View {

    private String TAG = WheelView.class.getName();
    private Context mContext;
    private Paint paint;
    private List<String> text = new ArrayList<String>();
    private WheelItem wheelItem1;
    private WheelItem wheelItem2;
    private WheelItem wheelItem3;
    private WheelItem wheelItem4;
    private WheelItem wheelItem5;
    private boolean init = false;
    private float width;
    private float height;
    //上下两行的间距
    private int DISTANCE = 24;
    //基准
    private int position = 0;
    //字体大小
    private int TEXTSIZE = 24;
    private int startCount;
    private int endCount;
    private List<WheelItem> wheelItems = new ArrayList<>();

    public WheelView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public WheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setTextSize(dip2px(TEXTSIZE));
        for (int i = 0; i < 10; i++) {
            text.add("guo = " + i);
        }
        startCount = 0;
        endCount = 4;

        wheelItem1 = new WheelItem(0);
        wheelItem2 = new WheelItem(1);
        wheelItem3 = new WheelItem(2);
        wheelItem4 = new WheelItem(3);
        wheelItem5 = new WheelItem(4);

        wheelItems.add(wheelItem1);
        wheelItems.add(wheelItem2);
        wheelItems.add(wheelItem3);
        wheelItems.add(wheelItem4);
        wheelItems.add(wheelItem5);

        for (int i = 0; i < wheelItems.size(); i++) {
            wheelItems.get(i).positon = i;
        }
    }

    private float top = 0;
    private float bottom = 0;
    private float baseline = 0;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!init) {
            init = true;
            width = getWidth();
            height = getHeight();

            // text居中绘制，注意baseline的计算才能达到居中，y值是text中心坐标
            float x = (float) (width / 2.0);
            float y = (float) (height / 2.0);
            Paint.FontMetricsInt fmi = paint.getFontMetricsInt();
            baseline = (float) (y - (fmi.bottom / 2.0 + fmi.top / 2.0));

            wheelItem3.y = baseline;
            wheelItem2.y = wheelItem3.y - dip2px(DISTANCE);
            wheelItem1.y = wheelItem2.y - dip2px(DISTANCE);
            wheelItem4.y = wheelItem3.y + dip2px(DISTANCE);
            wheelItem5.y = wheelItem4.y + dip2px(DISTANCE);

            top = getY();
            bottom = top + getHeight();
        }
//        canvas.clipRect(0, height / 2 - dip2px(34), width, height / 2 + dip2px(38));

        //滑动偏移
        for (WheelItem wheelItem : wheelItems) {
            wheelItem.y = wheelItem.y + moveY;
        }
        //
        float move = dip2px(DISTANCE);
        //超出后偏移
        float add = dip2px(DISTANCE * 2.5f);
        float start = height / 2 - add;
        float end = height / 2 + add;
        //  text  位置 偏移
        for (int i = 0; i < wheelItems.size(); i++) {
            WheelItem wheelItem = wheelItems.get(i);
            if (wheelItem.y < start) {
                WheelItem wMax = max(wheelItems);
                wheelItem.y = wMax.y + move;

                int pM = wMax.positon + 1;
                if (pM >= text.size()) {
                    wheelItem.positon = 0;
                } else {
                    wheelItem.positon = pM;
                }
            }
            if (wheelItem.y > end) {
                WheelItem mMin = min(wheelItems);
                wheelItem.y = mMin.y - move;

                int pM = mMin.positon - 1;
                if (pM < 0) {
                    wheelItem.positon = text.size() - 1;
                } else {
                    wheelItem.positon = pM;
                }
            }
            paint.setTextSize(fTextSize(wheelItem.y));
            canvas.drawText(text.get(wheelItem.positon), getWidth() / 2, wheelItem.y, paint);
        }
    }

    private float downY = 0;
    private float moveY = 0;
    private float lastY = 0;
    private float upY = 0;
    private int nowPosition = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downY = y;
                lastY = y;
                return true;
            case MotionEvent.ACTION_MOVE:
                if (y < top || y > bottom) {
                    return false;
                }
                moveY = y - lastY;
                lastY = y;
                //限制速度，否则绘制速度更不上
                if (moveY > dip2px(10)) {
                    moveY = dip2px(10);
                }
                if (moveY < -dip2px(10)) {
                    moveY = -dip2px(10);
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                upY = y;
                WheelItem me = medium(wheelItems);
                moveY = baseline - me.y;
                nowPosition = me.positon;
                invalidate();
                Toast.makeText(mContext,"now = "+text.get(me.positon),Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onTouchEvent(event);
    }

    private int fTextSize(float rag){
        float small = dip2px(12);
        float big = dip2px(24);
        float start = getTop();
        float end = getBottom();

        float k1 = (big - small)/(baseline - start);
        float b1 = small - k1*start;

        float k2 = (big - small)/(baseline - end);
        float b2 = small - k2*end;

        if (rag >= baseline){
            return (int)(k2*rag + b2);
        }else {
            return (int)(k1*rag + b1);
        }
    }

    public float dip2px(float dpValue) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return dpValue * scale + 0.5f;
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public float px2dip(float pxValue) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return pxValue / scale + 0.5f;
    }

    private WheelItem min(List<WheelItem> list) {
        float[] param = new float[5];
        for (int i = 0; i < list.size(); i++) {
            param[i] = list.get(i).y;
        }
        float[] clear = minToMax(param);
        for (WheelItem wheelItem : wheelItems) {
            if (wheelItem.y == clear[0]) {
                return wheelItem;
            }
        }
        return wheelItems.get(0);
    }

    private WheelItem max(List<WheelItem> list) {
        float[] param = new float[5];
        for (int i = 0; i < list.size(); i++) {
            param[i] = list.get(i).y;
        }
        float[] clear = minToMax(param);
        for (WheelItem wheelItem : wheelItems) {
            if (wheelItem.y == clear[4]) {
                return wheelItem;
            }
        }
        return wheelItems.get(0);
    }

    private WheelItem medium(List<WheelItem> list) {
        float[] param = new float[5];
        for (int i = 0; i < list.size(); i++) {
            param[i] = list.get(i).y;
        }
        float[] clear = minToMax(param);
        for (WheelItem wheelItem : wheelItems) {
            if (wheelItem.y == clear[4]) {
                return wheelItem;
            }
        }
        return wheelItems.get(0);
    }

    private float[] minToMax(float[] param) {
        if (param.length < 2) {
            return param;
        }
        for (int k = param.length - 1; k >= 0; k--) {
            for (int p = 0; p < k; p++) {
                if (param[p] > param[p + 1]) {
                    float i = param[p + 1];
                    param[p + 1] = param[p];
                    param[p] = i;
                }
            }
        }
        return param;
    }

    class WheelItem {
        public WheelItem(float y) {
            this.y = y;
        }

        public int positon;
        public float y;
    }


}
