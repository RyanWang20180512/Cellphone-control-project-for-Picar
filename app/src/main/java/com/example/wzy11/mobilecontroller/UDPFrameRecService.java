package com.example.wzy11.mobilecontroller;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

public class UDPFrameRecService extends Service {

    private boolean stopReceive=false;
    private int port=5051;
    public UDPFrameRecService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startReceiveThread();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mBinder;
    }

    /**
     * Start the frame receive thread
     */
    private void startReceiveThread(){
        new Thread(){
            @Override
            public void run() {
                super.run();
                DatagramSocket socket=null;
                try{
                    socket=new DatagramSocket(port);
                    socket.setSoTimeout(1);
                    byte[] data=new byte[65536];
                    DatagramPacket packet=new DatagramPacket(data,data.length);
                    while(!stopReceive){
                        try{
                            socket.receive(packet);
                        }catch (SocketTimeoutException timeoute){
                            continue;
                        }catch (IOException ioe){
                            throw ioe;
                        }
                        //sendMessageToActivity(MainActivity.TOAST_TEXT,"Receive a frame!");
                        //String address=packet.getAddress().getHostAddress();
                        //String hostport=String.valueOf(packet.getPort());
                        //String byteLength=String.valueOf(packet.getLength());
                        Bitmap bitmap= BitmapFactory.decodeByteArray(data,0,packet.getLength());
                        //int bitmapWidth=bitmap.getWidth();
                        //int bitmapHeight=bitmap.getHeight();

                        //String testStr="address: "+address+"\nport: "+hostport+"\nbyte length: "+byteLength+"\nbitmap info:\n -width "
                         //       +bitmapWidth+"\n -height "+bitmapHeight;
                        //sendMessageToActivity(MainActivity.UPDATE_IMAGEVIEW,testStr);
                        sendFrameToActivity(bitmap);
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }finally {
                    if(socket!=null){
                        socket.close();
                        socket=null;
                    }
                }
            }
        }.start();
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

    protected void sendFrameToActivity(Bitmap bitmap){
        Message message=new Message();
        message.what=MainActivity.UPDATE_IMAGEVIEW;
        message.obj=bitmap;
        MainActivity.updateUIHandler.sendMessage(message);
    }

    private NoticeBinder mBinder=new NoticeBinder();
    class NoticeBinder extends Binder {
        /**
         * Notice the Frame receive thread to stop
         */
        void stopFrameRecThread(){
            stopReceive=true;
        }

    }
}
