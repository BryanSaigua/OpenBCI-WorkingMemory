package com.example.openbci_workingmemory;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SocketConnect extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket_connect);

        Button btnConnect = findViewById(R.id.Continue);
        btnConnect.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
                       Intent intent = new Intent(v.getContext(), Instruction.class);
            startActivity(intent);
        }
    });
}
    }