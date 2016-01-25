package com.pmdsolutions.gentiantestapp;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by PMD Solutions on 30/04/2015.
 */
public class LoadingDialog extends Dialog implements View.OnClickListener {

    public Activity c;
    public Dialog d;
    public ProgressBar load;
    public TextView percentTV, title;
    public SICActivity sAct;
    int max;
    private int percentage = 0;
    private int battOn = 1;
    public Button btnOk;
    private SICActivity SICAct;

    private Handler flashingTimer = new Handler();

    public LoadingDialog(Activity a) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
    }

    private Runnable flasher = new Runnable(){
        public void run(){
            batteryFlash();
            flashingTimer.postDelayed(flasher, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.loading_dialog);
        btnOk = (Button) findViewById(R.id.btnOkDialog);
        load = (ProgressBar) findViewById(R.id.loadingBar);
        sAct = SICActivity.getInstance();
        percentTV = (TextView) findViewById(R.id.percent);
        title = (TextView) findViewById(R.id.title);
        flashingTimer.postDelayed(flasher, 1000);
        btnOk.setOnClickListener(this);


    }
    public void setMax(long y){
        load.setMax((int) y);
        Log.wtf("Max value ", (int) y + "");
        //max =(int) y;
    }
    public void setProgress(int x){
        load.setProgress(x);

    }


    public void setPrecentage(int percentage) {
        percentTV.setText(percentage + "%");
    }

    private void batteryFlash() {
        if(battOn == 1){
            title.setText("Exporting.  ");
            battOn = 2;
        }
        else if(battOn == 2){
            title.setText("Exporting.. ");
            battOn = 3;
        }
        else if (battOn == 3){
            title.setText("Exporting...");
            battOn = 1;
        }
    }

    public void finish(String filer) {
        flashingTimer.removeCallbacks(null);
        flashingTimer.removeCallbacksAndMessages(null);
        title.setText("File Exported");
        percentTV.setText("File Saved to: \n"+ GlobalValues.FilePaths.MAINT_DIRECTORY_PATH + filer);
        btnOk.setVisibility(View.VISIBLE);

    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnOkDialog:
                Log.wtf("DIALOG", "CLICKED!!");
                SICAct.getInstance().end();
                dismiss();
                break;

            default:
                break;
        }
    }

}