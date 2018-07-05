package com.example.wzy11.mobilecontroller;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;

/**
 * This service is used as a tcp client to communicate with pi, and we can use this service to send control commands to pi
 */
public class TcpCommandService extends Service {
    private String host="192.168.137.1";
    private int port=50;
    private Socket commandSocket=null;
    public TcpCommandService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        host=intent.getStringExtra("tcpHost");
        port=intent.getIntExtra("tcpPort",50);
        initCommandSocket();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CloseSocket();
    }

    private void CloseSocket(){
        if(commandSocket!=null){
            try{
                commandSocket.close();
                commandSocket=null;
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * Init the tcp socket, and send related messages to main activity
     */
    protected void initCommandSocket(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    commandSocket=new Socket();
                    sendMessageToActivity(MainActivity.TOAST_TEXT,"try to connect tcp server");
                    commandSocket.connect(new InetSocketAddress(host,port),6000);
                    sendMessageToActivity(MainActivity.TOAST_TEXT,"connect successfully");
                }catch (IOException e){
                    sendMessageToActivity(MainActivity.TOAST_TEXT,"connect tcp server failed");
                    commandSocket=null;
                    e.printStackTrace();
                }
            }
        }).start();

    }

    /**
     * Notify the activity with specific message
     * @param "message.What"
     * @param "message.obj"
     */
    protected void sendMessageToActivity(int id,String content){
        Message message=new Message();
        message.what=id;
        message.obj=content;
        MainActivity.updateUIHandler.sendMessage(message);
    }

    private SendCommandBinder mBinder=new SendCommandBinder();
    /**
     * The main activity can use this binder to send command data
     */
    class SendCommandBinder extends Binder{

        void sendCommand(String command){
            if(commandSocket!=null){
                new Thread(new sendDataThread(command)).start();
            }
        }

        //use this method to send close or shutdown command to the car, and then notify the mainactivity to exit
        void closeOrShutdown(String command){
            new Thread(new sendDataThread(command)).start();
            sendMessageToActivity(MainActivity.CLOSE_OR_SHUTDOWN,"");
        }
    }

    /**
     * Due to synchronous socket is forbidden in main thread, so create a child thread to do socket send operation
     */
    class sendDataThread extends Thread{
        private String command;
        public sendDataThread(String command){
            this.command=command;
        }

        @Override
        public void run() {
            try{
                if(commandSocket!=null){
                    OutputStream outputStream=commandSocket.getOutputStream();
                    byte[] sendData=command.getBytes(Charset.forName("UTF-8"));
                    outputStream.write(sendData,0,sendData.length);
                    outputStream.flush();
                }
            }catch (IOException e){
                e.printStackTrace();
                CloseSocket();
                sendMessageToActivity(MainActivity.TOAST_TEXT,"connect closed");
            }
        }
    }

}
