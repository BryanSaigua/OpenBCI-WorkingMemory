package com.example.openbci_workingmemory;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainActivity extends AppCompatActivity {

    Thread Thread1 = null;
    TextView  AP;
    EditText IP,Port;
    TextView Status;
    TextView IdData;
    Button Connect;
    String SERVER_IP = "192.168.43.134";
    int SERVER_PORT = 5000;
    public PrintWriter output;
    public DataInputStream input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IP = findViewById(R.id.IP);
        Port = findViewById(R.id.Port);
        Status = findViewById(R.id.Status);
        AP = findViewById(R.id.AP);
        IdData = findViewById(R.id.IdData);



        //tvMessages = findViewById(R.id.tvMessages);
        //etMessage = findViewById(R.id.etMessage);
        //btnSend = findViewById(R.id.btnSend);

        AP.setText("WiFi: RaspAP");
        Status.setText("Not connected");
        IP.setText(SERVER_IP);
        Port.setText(String.valueOf(SERVER_PORT));


        Button btnConnect1 = findViewById(R.id.backBtn);
        btnConnect1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), Instruction.class);
                startActivity(intent);
            }
        });

        Button btnConnect = findViewById(R.id.Connect);
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SERVER_IP = IP.getText().toString().trim();
                SERVER_PORT = Integer.parseInt(Port.getText().toString().trim());
                Thread1 = new Thread(new Thread1());
                Thread1.start();

                Intent intent = new Intent(v.getContext(), ConfigUser.class);
                startActivity(intent);
            }
        });
    }

//        btnSend.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String message = etMessage.getText().toString().trim();
//                if (!message.isEmpty()) {
//                    new Thread(new Thread3(message)).start();
//                }
//            }
//        });
//    }

    class Thread1 implements Runnable {
        public void run() {
            Socket socket;

//            AP.setText("WiFi: RaspAP");
//            Status.setText("Not connected");
//            IP.setText("IP: " + SERVER_IP);
//            Port.setText("Port: " + String.valueOf(SERVER_PORT));

            try {
                socket = new Socket(SERVER_IP, SERVER_PORT);
                output = new PrintWriter(socket.getOutputStream());
                input = new DataInputStream(socket.getInputStream());

//                socket.shutdownOutput();
                output.write("enviar");
                output.flush();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Status.setText("Connected\n");
                    }
                });
                new Thread(new Thread2()).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class Thread2 implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    final String message = input.readUTF();
                    if (message != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                IdData.append("server: " + message + "\n");
                            }
                        });
                    } else {
                        Thread1 = new Thread(new Thread1());
                        Thread1.start();
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
//    class Thread3 implements Runnable {
//        private String message;
//        Thread3(String message) {
//            this.message = message;
//        }
//        @Override
//        public void run() {
//            output.write(message);
//            output.flush();
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    tvMessages.append("client: " + message + "\n");
//                    etMessage.setText("");
//                }
//            });
//        }
//
//    }
}