package com.example.wzy11.mobilecontroller;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity{
    final String host="192.168.137.1";
    final int port=50;
    public static Context context;
    private TcpCommandService.SendCommandBinder sendCommandBinder=null; //Use this binder to notify service sending command data
    private ServiceConnection tcpConnection=new ServiceConnection() { //The connection between Main activity and TcpCommandService
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            sendCommandBinder=(TcpCommandService.SendCommandBinder)service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context=getApplicationContext(); //Get the context of this activity so that the static functions can use
        Intent startTcpServiceIntent=new Intent(this,TcpCommandService.class);
        startTcpServiceIntent.putExtra("tcpHost",host);
        startTcpServiceIntent.putExtra("tcpPort",port);
        //Start and bind TcpCommandService
        startService(startTcpServiceIntent);
        bindService(startTcpServiceIntent,tcpConnection,BIND_AUTO_CREATE);
        initButtons();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(tcpConnection);
        stopService(new Intent(this,TcpCommandService.class));
    }


    /**
     * Init the Direction Buttons and Camera Buttons, set OnTouchListener for these buttons so 
     * command can be sent when buttons are down or up.
     */
    public void initButtons()
    {
        Button buttonDirFor=(Button)findViewById(R.id.buttonDirForward);
        buttonDirFor.setOnTouchListener(new ButtonListener("DirStop","DirForward"));
        Button buttonDirBack=(Button)findViewById(R.id.buttonDirBack);
        buttonDirBack.setOnTouchListener(new ButtonListener("DirStop","DirBack"));
        Button buttonDirLeft=(Button)findViewById(R.id.buttonDirLeft);
        buttonDirLeft.setOnTouchListener(new ButtonListener("DirStop","DirLeft"));
        Button buttonDirRight=(Button)findViewById(R.id.buttonDirRight);
        buttonDirRight.setOnTouchListener(new ButtonListener("DirStop","DirRight"));
        Button buttonCamUp=(Button)findViewById(R.id.buttonCamUp);
        buttonCamUp.setOnTouchListener(new ButtonListener("CamStop","CamUp"));
        Button buttonCamDown=(Button)findViewById(R.id.buttonCamDown);
        buttonCamDown.setOnTouchListener(new ButtonListener("CamStop","CamDown"));
        Button buttonCamLeft=(Button)findViewById(R.id.buttonCamLeft);
        buttonCamLeft.setOnTouchListener(new ButtonListener("CamStop","CamLeft"));
        Button buttonCamRight=(Button)findViewById(R.id.buttonCamRight);
        buttonCamRight.setOnTouchListener(new ButtonListener("CamStop","CamRight"));
    }

    /**
     * When a control button is down or up, send the command 
     */
    class ButtonListener implements View.OnTouchListener{
        private String buttonUpCommand; //Specify the command when button is up
        private String buttonDownCommand; //Specify the command when button is down
        public ButtonListener(String buttonUpCommand,String buttonDownCommand){
            this.buttonUpCommand=buttonUpCommand;
            this.buttonDownCommand=buttonDownCommand;
        }
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()){
                case MotionEvent.ACTION_UP:
                    //Notify the TcpCommandService to send command
                    sendCommandBinder.sendCommand(buttonUpCommand);
                    Toast.makeText(MainActivity.this,buttonUpCommand,Toast.LENGTH_SHORT).show();
                    break;
                case MotionEvent.ACTION_DOWN:
                    //Notify the TcpCommandService to send command
                    sendCommandBinder.sendCommand(buttonDownCommand);
                    Toast.makeText(MainActivity.this,buttonDownCommand,Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
            return false;
        }
    }


    public static final int TOAST_TEXT=1;
    /**
     * Use the handler to update main activity according to service's message
     */
    public static Handler updateUIHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case TOAST_TEXT: //Display the toast according to the message from service
                    Toast.makeText(context,(String)msg.obj,Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };
}
