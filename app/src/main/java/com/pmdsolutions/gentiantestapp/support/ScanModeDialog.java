package com.pmdsolutions.gentiantestapp.support;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.pmdsolutions.gentiantestapp.MainActivity;
import com.pmdsolutions.gentiantestapp.R;


import java.util.ArrayList;

/**
 * Created by PMD Solutions on 8/6/2015.
 */
public class ScanModeDialog extends Dialog implements View.OnClickListener{

    public Activity c;
    public Dialog d;
    public Button yes, no;
    public MainActivity hAct;

    public ScanModeDialog(Activity a) {
        super(a);
        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.scanmode_dialog);

        attachOnClickListeners();


    }
    private void attachOnClickListeners(){
        LinearLayout rl = (LinearLayout)findViewById(R.id.scanModeLayout);
        ArrayList<View> touchables = rl.getTouchables();
        //sets each touchable item in the layout's on click to itself (see onClick())
        for(View touchable : touchables)
        {
            if(touchable instanceof Button || touchable instanceof ImageButton){
                touchable.setOnClickListener(this);
            }
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSMDCamera:
                if (c instanceof MainActivity){
                    MainActivity.getInstance().launchScanner();
                }
                break;
            case R.id.btnSMDBT:
                if (c instanceof MainActivity){
                    MainActivity.getInstance().launchBluetooth();
                }
                break;
            default:
                break;
        }
        dismiss();
    }


}
