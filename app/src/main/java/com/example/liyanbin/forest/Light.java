package com.example.liyanbin.forest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

/**
 * Created by lenovo on 2017/10/4.
 */

public class Light extends Activity implements View.OnClickListener{
    private Button btn_red, btn_yellow,btn_white,btn_sky,btn_blue;
    private LinearLayout ll_sun,ll_moon,ll_main;
    private ImageView iv_min;
    private ImageView iv_add;
    private ImageView iv_gif;
    private SeekBar sb_strong;
    private Button btn_bluetooth;
    private TextView tv_top;
    private Vibrator vibrator;
    private String my_msg="";


    private static final String TAG = "Light";
    private static boolean D = true;
    private static final String info = "junge";
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;


    private String mConnectedDeviceName = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothChatService mChatService = null;

    private int sum =1;

    String mmsg = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_light);

        btn_red=(Button)findViewById(R.id.light_btn_red);
        btn_red.setOnClickListener(this);
        btn_yellow=(Button)findViewById(R.id.light_btn_yellow);
        btn_yellow.setOnClickListener(this);
        btn_white=(Button)findViewById(R.id.light_btn_white);
        btn_white.setOnClickListener(this);
        btn_sky=(Button)findViewById(R.id.light_btn_sky);
        btn_sky.setOnClickListener(this);
        btn_blue=(Button)findViewById(R.id.light_btn_blue);
        btn_blue.setOnClickListener(this);

        iv_add=(ImageView)findViewById(R.id.light_iv_add);
        iv_min=(ImageView)findViewById(R.id.light_iv_min);
        sb_strong=(SeekBar)findViewById(R.id.seekbar_strong);

        btn_bluetooth=(Button)findViewById(R.id.btn_main_bluetooth);
        tv_top=(TextView)findViewById(R.id.tv_title);


        ll_moon=(LinearLayout)findViewById(R.id.ll_moon);
        ll_sun=(LinearLayout)findViewById(R.id.ll_sun);
        ll_main=(LinearLayout)findViewById(R.id.ll_main);
        ll_sun.setOnClickListener(this);
        ll_moon.setOnClickListener(this);

        initDisable();



        sb_strong.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Toast.makeText(Light.this, " Illumination intensity"+String.valueOf(seekBar.getProgress()), Toast.LENGTH_SHORT).show();
            }
        });


        D = false;
        if (D)
            Log.e(TAG, "+++ ON CREATE +++");

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(getWindow().getAttributes().softInputMode== WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "No device", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        mChatService = new BluetoothChatService(this, mHandler);
    }

    private void initDisable(){
        btn_blue.setEnabled(false);
        btn_sky.setEnabled(false);
        btn_red.setEnabled(false);
        btn_white.setEnabled(false);
        btn_yellow.setEnabled(false);
        btn_blue.setBackgroundResource(R.drawable.image_blue_gray);
        btn_sky.setBackgroundResource(R.drawable.image_sky_gray);
        btn_red.setBackgroundResource(R.drawable.image_red_gray);
        btn_white.setBackgroundResource(R.drawable.iamge_white_gray);
        btn_yellow.setBackgroundResource(R.drawable.image_yellow_gray);
        iv_min.setImageResource(R.drawable.light_min_close);
        iv_add.setImageResource(R.drawable.light_add_close);
        sb_strong.setEnabled(false);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            /*
            * r��red
            * y��yellow
            * w��white
            * b��blue
            * p��pink
             */
            case R.id.ll_moon:{
                //tgbtn_switch.setBackgroundResource(R.drawable.switch_open);
                ll_main.setBackgroundResource(R.drawable.day);
                ll_moon.setVisibility(View.INVISIBLE);
                ll_sun.setVisibility(View.VISIBLE);
                my_msg="6";
                btn_blue.setBackgroundResource(R.drawable.image_blue);
                btn_sky.setBackgroundResource(R.drawable.image_sky);
                btn_red.setBackgroundResource(R.drawable.image_red);
                btn_white.setBackgroundResource(R.drawable.iamge_white);
                btn_yellow.setBackgroundResource(R.drawable.image_yellow);
                btn_blue.setEnabled(true);
                btn_sky.setEnabled(true);
                btn_red.setEnabled(true);
                btn_white.setEnabled(true);
                btn_yellow.setEnabled(true);
                iv_min.setImageResource(R.drawable.light_min_open);
                iv_add.setImageResource(R.drawable.light_add_open);
                sb_strong.setEnabled(true);
                break;
            }
            case R.id.ll_sun:{
                //tgbtn_switch.setBackgroundResource(R.drawable.switch_close);
                ll_main.setBackgroundResource(R.drawable.night);
                ll_moon.setVisibility(View.VISIBLE);
                ll_sun.setVisibility(View.INVISIBLE);
                initDisable();
                my_msg="5";
                break;
            }
            case R.id.btn_main_bluetooth:{
                Intent serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                break;
            }
            case R.id.light_btn_red:{
                shock();
                my_msg="0";
                break;
            }
            case R.id.light_btn_yellow:{
                shock();
                my_msg="1";
                break;
            }
            case R.id.light_btn_white:{
                shock();
                my_msg="2";
                break;
            }
            case R.id.light_btn_sky:{
                shock();
                my_msg="3";
                break;
            }
            case R.id.light_btn_blue:{
                shock();
                my_msg="4";
                break;
            }
        }
        try {
            my_msg.getBytes("ISO_8859_1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        sendMessage(my_msg);

    }

    public void shock(){
        //vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        //vibrator.vibrate(150);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (D)
            Log.e(TAG, "++ ON START ++");

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            if (mChatService == null){
                //setupChat();
            }
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if (D)
            Log.e(TAG, "+ ON RESUME +");
        if (mChatService != null) {
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                mChatService.start();
            }
        }
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if (D)
            Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (D)
            Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // �����������վ
        if (mChatService != null)
            mChatService.stop();
        if (D)
            Log.e(TAG, "--- ON DESTROY ---");
    }

    /**
     * ����һ����Ϣ
     *
     * @param message
     *            һ���ı��ַ�������.
     */
    private void sendMessage(String message) {
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, "No Device", Toast.LENGTH_SHORT).show();
            return;
        }

        if (message.length() > 0) {
            byte[] send = message.getBytes();
            mChatService.write(send);
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if (D)
                        Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            tv_top.setText("Connected");
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            tv_top.setText("Connecting...");
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            tv_top.setText("Not connect");
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    String writeMessage = new String(writeBuf);
                    sum=1;
                    mmsg += writeMessage;
                    break;
                case MESSAGE_DEVICE_NAME:
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (D)
            Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    mChatService.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                } else {
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "No BlueTooth, quit", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }


}
