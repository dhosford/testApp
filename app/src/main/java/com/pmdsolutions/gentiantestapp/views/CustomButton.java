package com.pmdsolutions.gentiantestapp.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.Button;

import com.pmdsolutions.gentiantestapp.R;


/**
 * Custom button for consistency
 * @author Daniel Hosford
 *
 */
public class CustomButton extends Button {

    public CustomButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTextColor(0);
        setShadowLayer(0,0,0,0);
        setTypeface(null, 0);
        String textSize = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "textSize");
        if(textSize == null){
            setTextSize(30);
        }
        else{
            textSize = textSize.substring(0, textSize.length() - 2);
            setTextSize(Float.parseFloat(textSize));
        }
    }

    @Override
    public void setBackground(Drawable background) {
        background = getResources().getDrawable(R.drawable.custom_button_selector);
        super.setBackground(background);
    }

    @Override
    public void setTextColor(int colors) {
        super.setTextColor(Color.WHITE);
    }


    @Override
    public void setTypeface(Typeface tf, int style) {
        super.setTypeface(null, Typeface.BOLD);
    }
}
