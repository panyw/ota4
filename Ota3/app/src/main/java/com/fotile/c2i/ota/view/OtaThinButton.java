package com.fotile.c2i.ota.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

import com.fotile.c2i.ota.util.OtaTool;

/**
 * 文件名称：ThinBoldTextView
 * 创建时间：2017-09-07 13:52
 * 文件作者：shihuijuan
 * 功能描述：文鼎细黑体TextView
 */

public class OtaThinButton extends Button {

    public OtaThinButton(Context context) {
        super(context);
        setTypeface(context);
    }

    public OtaThinButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeface(context);
    }

    public OtaThinButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setTypeface(context);
    }


    /**
     * 设置文鼎细黑体
     *
     * @param context
     */
    private void setTypeface(Context context) {
        Typeface face = OtaTool.getTypeface();
        if (null != face) {
            setTypeface(face);
        }
    }
}
