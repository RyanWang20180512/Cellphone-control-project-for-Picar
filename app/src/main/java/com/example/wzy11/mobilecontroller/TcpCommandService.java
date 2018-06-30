package com.example.wzy11.mobilecontroller;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;


public class TcpCommandService extends Service {
    private String host="192.168.5.176";
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
        throw new UnsupportedOperationException("Not yet implemented");
    }

    protected void initCommandSocket(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    commandSocket=new Socket();
                    Toast.makeText(TcpCommandService.this,"try to connect...",Toast.LENGTH_SHORT).show();
                    commandSocket.connect(new InetSocketAddress(host,port),6000);
                    Toast.makeText(TcpCommandService.this,"connect successfully",Toast.LENGTH_SHORT).show();
                }catch (IOException e){
                    Toast.makeText(TcpCommandService.this,"connect failed",Toast.LENGTH_SHORT).show();
                    commandSocket=null;
                    e.printStackTrace();
                }
            }
        }).start();

    }
}
