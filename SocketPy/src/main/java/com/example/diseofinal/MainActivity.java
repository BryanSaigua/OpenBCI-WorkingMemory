package com.example.diseofinal;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private Spinner Myspinner;
    TextView stateValue,estimulo,crono;
    Button Connect;
    FrameLayout frame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Myspinner = findViewById(R.id.Myspinner);
        stateValue = findViewById(R.id.stateValue);
        Connect = findViewById(R.id.Connect);
        estimulo = findViewById(R.id.estimulo);
        crono = findViewById(R.id.crono);
        frame = findViewById(R.id.frame_layout_xyplot_channel_1);
        Date date = new Date();
        stateValue.setText(""+ date);

        MediaPlayer mediaPlayer = MediaPlayer.create(this,R.raw.beep);

        ArrayList<String> elementos = new ArrayList<>();

        elementos.add("Canal 1");
        elementos.add("Canal 2");
        elementos.add("Canal 3");
        elementos.add("Canal 4");
        elementos.add("Canal 5");
        elementos.add("Canal 6");
        elementos.add("Canal 7");
        elementos.add("Canal 8");

        ArrayAdapter adp = new ArrayAdapter(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, elementos);
        Myspinner.setAdapter(adp);
        Myspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int i, long l) {
                String elemento = (String) Myspinner.getAdapter().getItem(i);
//                System.out.println(elemento);
//                Toast toast = Toast.makeText(MainActivity.this, "Seleccionaste", Toast.LENGTH_SHORT);
//                toast.show();
                if (elemento == "Canal 3"){
                    System.out.println("elemento");
                    frame.setBackgroundColor(Color.GREEN);
                    frame.invalidate();
                }
                else{
                    frame.getSolidColor();
                }
                //Toast.makeText(MainActivity.this, "Seleccionaste"+ elemento, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    long duration = TimeUnit.SECONDS.toMillis(60);

        CountDownTimer start = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long l) {
                String sDuration = String.format(Locale.ENGLISH, "%02d: %02d"
                        ,TimeUnit.MILLISECONDS.toMinutes(l),TimeUnit.MILLISECONDS.toSeconds(l)
                                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(l)));
                crono.setText(sDuration);
                System.out.println(l);
                if (sDuration == "" ){
                    estimulo.setText("ASD");
                }else if(l >39999 & l<49999){
                    estimulo.setText("vamosbien");
                    mediaPlayer.start();
                }
            }

            @Override
            public void onFinish() {

                //estimulo.setVisibility(View.GONE);
                estimulo.setText("Finalizo");
            }
        }.start();


        Connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                System.out.println("si llego");
                           }
        });

    }


}

