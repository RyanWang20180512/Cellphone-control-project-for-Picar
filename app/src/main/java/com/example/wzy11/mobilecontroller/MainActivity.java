package com.example.wzy11.mobilecontroller;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity{
    private String host;
    private int port;
    public static Context context;
    private TextView textViewTest;
    private TextView showImageViewSize;
    private ImageView cameraDisplayImageView;
    public TcpCommandService.SendCommandBinder sendCommandBinder=null; //Use this binder to notify service sending command data
    private ServiceConnection tcpConnection=new ServiceConnection() { //The connection between Main activity and TcpCommandService
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            sendCommandBinder=(TcpCommandService.SendCommandBinder)service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    public UDPFrameRecService.NoticeBinder noticeUDPBinder=null;//Use this binder to notify UDP service
    private ServiceConnection udpConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            noticeUDPBinder=(UDPFrameRecService.NoticeBinder)service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //context=getApplicationContext();
        context=this;//Get the context of this activity so that the static functions can use

        Intent startInfoIntent=getIntent();
        host=startInfoIntent.getStringExtra("tcpHost");
        port=startInfoIntent.getIntExtra("tcpPort",50);

        Intent startTcpServiceIntent=new Intent(this,TcpCommandService.class);
        startTcpServiceIntent.putExtra("tcpHost",host);
        startTcpServiceIntent.putExtra("tcpPort",port);
        //Start and bind TcpCommandService
        startService(startTcpServiceIntent);
        bindService(startTcpServiceIntent,tcpConnection,BIND_AUTO_CREATE);
        initButtons();

        //Start and bind UDPFrameRecService
        startService(new Intent(this,UDPFrameRecService.class));
        bindService(new Intent(this,UDPFrameRecService.class),udpConnection,BIND_AUTO_CREATE);

        //textViewTest=(TextView)findViewById(R.id.texttest);
        cameraDisplayImageView=(ImageView)findViewById(R.id.cameraDisplay);

        showImageViewSize=(TextView)findViewById(R.id.imageViewSize);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(tcpConnection);
        stopService(new Intent(this,TcpCommandService.class));
        unbindService(udpConnection);
        stopService(new Intent(this,UDPFrameRecService.class));
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

        Button buttonClose=(Button)findViewById(R.id.buttonClose);
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommandBinder.closeOrShutdown("Close"); //Notify the car this client will close the connection
            }
        });

        Button buttonShutdown=(Button)findViewById(R.id.buttonShutdown);
        buttonShutdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommandBinder.closeOrShutdown("Shutdown"); //Notify the car to shutdown itself
            }
        });
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
                    //Toast.makeText(MainActivity.this,buttonUpCommand,Toast.LENGTH_SHORT).show();
                    break;
                case MotionEvent.ACTION_DOWN:
                    //Notify the TcpCommandService to send command
                    sendCommandBinder.sendCommand(buttonDownCommand);
                    //Toast.makeText(MainActivity.this,buttonDownCommand,Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
            return false;
        }
    }


    public static final int TOAST_TEXT=1;
    public static final int CLOSE_OR_SHUTDOWN=2;
    public static final int UPDATE_IMAGEVIEW=3;
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
                case CLOSE_OR_SHUTDOWN: //The tcp service has sent close or shutdown command, and the main activity should exit now
                    ((MainActivity)context).finish();
                    break;
                case UPDATE_IMAGEVIEW: //The udp service has received a frame, the main activity should display it
                    //((MainActivity)context).textViewTest.setText((String)msg.obj);
                    //int width=((MainActivity)context).cameraDisplayImageView.getWidth();
                    //int height=((MainActivity)context).cameraDisplayImageView.getHeight();
                    //((MainActivity)context).showImageViewSize.setText("width: "+width+" height: "+height);
                    ((MainActivity)context).cameraDisplayImageView.setImageBitmap((Bitmap)msg.obj);
                default:
                    break;
            }
        }
    };
}
