package com.showjoy.android.toast.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.showjoy.android.toast.R;
import com.showjoy.android.toast.util.SHViewUtils;

/**
 * 自定义标签 圆角标签
 * Created by lufei on 6/1/16.
 */
public class SHTagView extends LinearLayout {

    GradientDrawable gradientDrawable;
    TextView textView;

    float radius = 30;
    float strokeWidth = 2;
    int strokeColor = Color.parseColor("#C3C3C3");
    int color = Color.WHITE;
    int textColor = Color.BLACK;
    String text = "";
    float textSize = 12;

    float paddingLet = 0;
    float paddingTop = 0;

    public SHTagView(Context context) {
        super(context);
        init(context, null);
    }

    public SHTagView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SHTagView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (null != attrs) {
            TypedArray typeArray = context.obtainStyledAttributes(attrs,
                    R.styleable.SHTagView);

            text = typeArray.getString(R.styleable.SHTagView_tag_text);
            textSize = typeArray.getDimension(R.styleable.SHTagView_tag_text_size, textSize);
            textColor = typeArray.getColor(R.styleable.SHTagView_tag_text_color, textColor);
            strokeColor = typeArray.getColor(R.styleable.SHTagView_tag_stroke_color, strokeColor);
            strokeWidth = typeArray.getDimension(R.styleable.SHTagView_tag_stroke_width, strokeWidth);
            radius = typeArray.getDimension(R.styleable.SHTagView_tag_radius, radius);
            color = typeArray.getColor(R.styleable.SHTagView_tag_color, color);
            paddingLet = typeArray.getDimension(R.styleable.SHTagView_tag_padding_h, 15);
            paddingTop = typeArray.getDimension(R.styleable.SHTagView_tag_padding_v, 5);
            typeArray.recycle();
        }


        gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(color);
        gradientDrawable.setCornerRadius(radius);
        gradientDrawable.setStroke((int)strokeWidth, strokeColor);
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        setBackgroundDrawable(gradientDrawable);

        setPadding(paddingLet, paddingTop);
        setGravity(Gravity.CENTER);

        textView = new TextView(getContext());
        textView.setTextColor(textColor);
        textView.setText(text);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        addView(textView);
    }

    public String getText() {
        return textView.getText().toString();
    }

    public SHTagView setText(CharSequence text) {
        textView.setText(text);
        return this;
    }

    public SHTagView setTextColor(int color) {
        textView.setTextColor(color);
        return this;
    }

    public SHTagView setTextSize(float size) {
        textView.setTextSize(size);
        return this;
    }

    public SHTagView setColor(int color) {
        gradientDrawable.setColor(color);
        setBackgroundDrawable(gradientDrawable);
        return this;
    }

    public SHTagView setCornerRadius(int radius) {
        gradientDrawable.setCornerRadius(radius);
        setBackgroundDrawable(gradientDrawable);
        return this;
    }

    public SHTagView setStroke(int width, int color) {
        gradientDrawable.setStroke(width, color);
        setBackgroundDrawable(gradientDrawable);
        return this;
    }

    public SHTagView setPadding(float paddingLet, float paddingTop) {
        int paddingLetTmp = SHViewUtils.dp2px(getContext(), paddingLet);
        int paddingTopTmp = SHViewUtils.dp2px(getContext(), paddingTop);
        setPadding(paddingLetTmp, paddingTopTmp, paddingLetTmp, paddingTopTmp);
        return this;
    }
}
