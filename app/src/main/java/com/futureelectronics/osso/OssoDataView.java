package com.futureelectronics.osso;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

/**
 * TODO: document your custom view class.
 */
public class OssoDataView extends FrameLayout {
    private TextView mTxtTitle;
    private TextView mTxtLabel1, mTxtLabel2;
    private TextView mTxtValue1, mTxtValue2;
    private ImageView mDataImage;

    private boolean value2Visible = false;

    public OssoDataView(Context context) {
        super(context);
        init(null, 0);
    }

    public OssoDataView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public OssoDataView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.OssoDataView, defStyle, 0);

        String dataTitle, dataLabel1, dataLabel2=null, dataValue1, dataValue2=null;
        Drawable imgDrawable = null;
        int value1Color = Color.TRANSPARENT, value2Color = Color.TRANSPARENT;
        boolean hasValue1Color = false, hasValue2Color = false;

        dataTitle = a.getString(R.styleable.OssoDataView_dataTitle);

        dataLabel1 = a.getString(R.styleable.OssoDataView_dataLabel1);
        dataValue1 = a.getString(R.styleable.OssoDataView_dataValue1);

        if (a.hasValue(R.styleable.OssoDataView_dataLabel2)) {
            dataLabel2 = a.getString(R.styleable.OssoDataView_dataLabel2);
        }
        if (a.hasValue(R.styleable.OssoDataView_dataValue2)) {
            dataValue2 = a.getString(R.styleable.OssoDataView_dataValue2);
        }

        if (a.hasValue(R.styleable.OssoDataView_dataImage)) {
            imgDrawable = a.getDrawable(R.styleable.OssoDataView_dataImage);
        }

        if (a.hasValue(R.styleable.OssoDataView_value1Color)) {
            hasValue1Color = true;
            value1Color = a.getColor(R.styleable.OssoDataView_value1Color, value1Color);
        }

        if (a.hasValue(R.styleable.OssoDataView_value2Color)) {
            hasValue2Color = true;
            value2Color = a.getColor(R.styleable.OssoDataView_value2Color, value2Color);
        }

        a.recycle();

        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_osso_data, this, true);

        mTxtTitle = findViewById(R.id.data_title);
        mTxtLabel1 = findViewById(R.id.data_label1);
        mTxtLabel2 = findViewById(R.id.data_label2);
        mTxtValue1 = findViewById(R.id.data_value1);
        mTxtValue2 = findViewById(R.id.data_value2);
        mDataImage = findViewById(R.id.data_image);

        mTxtTitle.setText(dataTitle);
        mTxtLabel1.setText(dataLabel1);
        mTxtValue1.setText(dataValue1);

        if(dataLabel2 != null){
            mTxtLabel2.setVisibility(VISIBLE);
            mTxtLabel2.setText(dataLabel2);
        }

        if(dataValue2 != null){
            mTxtValue2.setVisibility(VISIBLE);
            value2Visible = true;
            mTxtValue2.setText(dataValue2);
        }

        if(imgDrawable != null){
            mDataImage.setImageDrawable(imgDrawable);
        }
        else{
            mDataImage.setVisibility(INVISIBLE);
        }

        if(hasValue1Color){
            setValue1Color(value1Color);
        }

        if(hasValue2Color){
            setValue1Color(value2Color);
        }
    }

    public void setDataLabel1(String dataLabel1){
        mTxtLabel1.setText(dataLabel1);
    }

    public void setDataValue1(String dataValue1){
        mTxtValue1.setText(dataValue1);
    }

//    public void setDataValue1(int resDataValue1){
//        mTxtValue1.setText(resDataValue1);
//    }

    /**
     * Sets the view's example color attribute value. In the example view, this color
     * is the font color.
     *
     * @param value1Color The background color for value 1.
     */
    public void setValue1Color(int value1Color) {
        mTxtValue1.setBackgroundColor(value1Color);
    }

    public void setDataLabel2(String dataLabel2){
        mTxtLabel2.setText(dataLabel2);
    }

    public void setDataValue2(String dataValue2){
        mTxtValue2.setText(dataValue2);
        if(!value2Visible){
            mTxtValue2.setVisibility(VISIBLE);
            value2Visible = true;
        }
    }

    /**
     * Sets the view's example color attribute value. In the example view, this color
     * is the font color.
     *
     * @param value2Color The background color for value 1.
     */
    public void setValue2Color(int value2Color) {
        mTxtValue2.setBackgroundColor(value2Color);
    }
}
