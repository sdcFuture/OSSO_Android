package com.futureelectronics.osso;

import androidx.databinding.BindingAdapter;
import androidx.databinding.BindingConversion;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by Kyle Harman on 12/5/2018.
 */
public class BindingAdapters {
    @BindingAdapter("visibleGone")
    public static void showHide(View view, boolean show) {
        view.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @BindingAdapter("clearOnFocusAndDispatch")
    public static void clearOnFocusAndDispatch(EditText view, View.OnFocusChangeListener listener) {
        view.setOnFocusChangeListener((View focusedView, boolean hasFocus) -> {
            TextView textView = (TextView)focusedView;
            if(hasFocus){
                view.setTag(R.id.previous_value, textView.getText());
                textView.setText("");
            }
            else {
                boolean usingOldTxt = false;
                if(textView.getText().length() == 0){
                    CharSequence oldTxt = (CharSequence)textView.getTag(R.id.previous_value);
                    if(oldTxt != null){
                        usingOldTxt = true;
                        textView.setText(oldTxt);
                    }
                }
                if (listener != null && !usingOldTxt) {
                    listener.onFocusChange(focusedView, hasFocus);
                }
            }
        });
    }

    @BindingAdapter("setUvIndexColor")
    public static void setUvIndexColor(OssoDataView view, int uv_index)
    {
        Context context = view.getContext();
        if(uv_index < 3){
            view.setValue1Color(context.getResources().getColor(R.color.colorUvIndexGood));
        }
        else if(uv_index < 6){
            view.setValue1Color(context.getResources().getColor(R.color.colorUvIndexOk));
        }
        else if(uv_index < 8){
            view.setValue1Color(context.getResources().getColor(R.color.colorUvIndexHigh));
        }
        else if(uv_index < 11){
            view.setValue1Color(context.getResources().getColor(R.color.colorUvIndexVeryHigh));
        }
        else{
            view.setValue1Color(context.getResources().getColor(R.color.colorUvIndexExtreme));
        }
    }
}
