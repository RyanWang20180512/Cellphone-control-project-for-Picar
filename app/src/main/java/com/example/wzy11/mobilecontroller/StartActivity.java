package com.example.wzy11.mobilecontroller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class StartActivity extends AppCompatActivity {
    private String host="192.168.137.1";
    private int port=50;

    private TextView textViewIP;
    private TextView textViewPort;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        //Get the last IP and port value, and show them
        getInfoFromSharedPreference();
        textViewIP=(TextView)findViewById(R.id.carIP);
        textViewPort=(TextView)findViewById(R.id.carPort);
        textViewIP.setText(host);
        textViewPort.setText(String.valueOf(port));
        Button buttonEnter=(Button)findViewById(R.id.enter);
        buttonEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                host=textViewIP.getText().toString();
                port=Integer.parseInt(textViewPort.getText().toString());
                Intent startMainActivityIntent=new Intent(StartActivity.this,MainActivity.class);
                startMainActivityIntent.putExtra("tcpHost",host);
                startMainActivityIntent.putExtra("tcpPort",port);
                startActivity(startMainActivityIntent);
                //Save the ip and port values
                setInfoFromSharedPreference();
                finish();
            }
        });
    }

    private void getInfoFromSharedPreference(){
        SharedPreferences preferences=getSharedPreferences("connectPara",MODE_PRIVATE);
        host=preferences.getString("IP","192.168.1.1");
        port=preferences.getInt("Port",50);

    }

    private void setInfoFromSharedPreference(){
        SharedPreferences.Editor editor=getSharedPreferences("connectPara",MODE_PRIVATE).edit();
        editor.putString("IP",host);
        editor.putInt("Port",port);
        editor.apply();
    }
}
