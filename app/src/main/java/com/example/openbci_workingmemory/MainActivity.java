package com.example.openbci_workingmemory;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.androidplot.ui.HorizontalPositioning;
import com.androidplot.ui.Size;
import com.androidplot.ui.SizeMetric;
import com.androidplot.ui.SizeMode;
import com.androidplot.ui.VerticalPositioning;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.FastLineAndPointRenderer;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.XYPlot;
import com.example.openbci_workingmemory.components.CircularBuffer;
import com.example.openbci_workingmemory.components.ConfigurationsFileManager;
import com.example.openbci_workingmemory.components.DataBaseFileWriter;
import com.example.openbci_workingmemory.components.DynamicSeries;
import com.example.openbci_workingmemory.components.EEGFileReader;
import com.example.openbci_workingmemory.components.EEGFileWriter;
import com.example.openbci_workingmemory.components.Filter;
import com.example.openbci_workingmemory.components.Filter_Noch;
import com.example.openbci_workingmemory.components.Knn;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    public Thread socketThread = null;
    public Thread dataListener = null;
    public int counter = 150;

    TextView textViewIP, textViewPort, textViewStatus, txtAverage_channel_1, txtTimer_value, canal, txtPrediction;
    Button btnStart, btnStop, btnTraining, btnOutTraining;

    String SERVER_IP = "192.168.0.148";
    String SERVER_PORT = "5000";

    public PrintWriter output;
    public DataInputStream input;

    public int samplingRate = 250;
    public Filter activeFilter;
    public Filter_Noch activeFilterNoch;
    public double[][] filtState;
    public double[][] filtStateNoch;

    public int channelOfInterest = 0;
    public static int detectionSensibility = 65;
    public static int probabilitySensibility = 65;
    public static int kNearestNeighbors = 15;
    public static int maxSignalFrequency = 250000;
    public static int minSignalFrequency = -250000;

    LinearLayout predictionContainer;

    private int notchFrequency = 14;
    private static final int PLOT_LENGTH = 1760;
    public CircularBuffer eegBuffer = new CircularBuffer(1760, 8);

    private static final String PLOT_TITLE = "Raw_EEG";

    public DynamicSeries dataSeriesGraph;
    public DynamicSeries dataSeriesChannelOne;
    public DynamicSeries dataSeriesChannelTwo;
    public DynamicSeries dataSeriesChannelThree;
    public DynamicSeries dataSeriesChannelFour;
    public DynamicSeries dataSeriesChannelFive;
    public DynamicSeries dataSeriesChannelSix;
    public DynamicSeries dataSeriesChannelSeven;
    public DynamicSeries dataSeriesChannelEigth;


    public XYPlot filterPlotChannelOne;
    int average_channel_1 = 0;


    private LineAndPointFormatter lineFormatterChannelOne;

    public double SCALE_FACTOR_EEG = 0.022351744455307063;

    private Spinner channelsSpinner;
    private String appState = "WAITINGEVALUATION";
    private Boolean justStarted = true;

    private CountDownTimer evaluationTimer;

    EEGFileWriter eegFile = new EEGFileWriter(this, "Captura de datos");
    private int frameCounter = 0;

    private String[] extractedArrayString = new String[4500000];

    MediaPlayer ejecucion_motoraMediaPlayer, imagen_motoraMediaPlayer, beepMediaPlayer, beep_finalMediaPlayer, sustraccionMediaPlayer;

    double[][] originalSignalClassOne;
    double[][] originalSignalClassTwo;
    double[][] originalSignalClassThree;

    private int current_umbral = 75;

    private Knn knnChannelOne;
    private Knn knnChannelTwo;
    private Knn knnChannelThree;
    private Knn knnChannelFour;
    private Knn knnChannelFive;
    private Knn knnChannelSix;
    private Knn knnChannelSeven;
    private Knn knnChannelEight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startDataBase();
        readDataBase();

        setFilterType();
        setFilterTypeNoch();
        startListenerThread();
        System.out.println("-------------" + originalSignalClassOne.length);
        System.out.println("-------------" + originalSignalClassOne[0].length);
        System.out.println("-------------" + originalSignalClassOne[1].length);

        knnChannelOne = new Knn(originalSignalClassOne[0], originalSignalClassTwo[0], originalSignalClassThree[0], current_umbral);
        knnChannelTwo = new Knn(originalSignalClassOne[1], originalSignalClassTwo[1], originalSignalClassThree[1], current_umbral);
        knnChannelThree = new Knn(originalSignalClassOne[2], originalSignalClassTwo[2], originalSignalClassThree[2], current_umbral);
        knnChannelFour = new Knn(originalSignalClassOne[3], originalSignalClassTwo[3], originalSignalClassThree[3], current_umbral);
        knnChannelFive = new Knn(originalSignalClassOne[4], originalSignalClassTwo[4], originalSignalClassThree[4], current_umbral);
        knnChannelSix = new Knn(originalSignalClassOne[5], originalSignalClassTwo[5], originalSignalClassThree[5], current_umbral);
        knnChannelSeven = new Knn(originalSignalClassOne[6], originalSignalClassTwo[6], originalSignalClassThree[6], current_umbral);
        knnChannelEight = new Knn(originalSignalClassOne[7], originalSignalClassTwo[7], originalSignalClassThree[7], current_umbral);

        initUI();

        eegFile.initFile();
    }

    public void initUI() {

        dataSeriesGraph = new DynamicSeries(PLOT_TITLE);
        dataSeriesChannelOne = new DynamicSeries(PLOT_TITLE);
        dataSeriesChannelTwo = new DynamicSeries(PLOT_TITLE);
        dataSeriesChannelThree = new DynamicSeries(PLOT_TITLE);
        dataSeriesChannelFour = new DynamicSeries(PLOT_TITLE);
        dataSeriesChannelFive = new DynamicSeries(PLOT_TITLE);
        dataSeriesChannelSix = new DynamicSeries(PLOT_TITLE);
        dataSeriesChannelSeven = new DynamicSeries(PLOT_TITLE);
        dataSeriesChannelEigth = new DynamicSeries(PLOT_TITLE);

        filterPlotChannelOne = new XYPlot(this, PLOT_TITLE);

        initViewChannel1(this);
        initComboBox(this);

        txtAverage_channel_1 = findViewById(R.id.average_channel_1);

        textViewIP = findViewById(R.id.ipValue);
        textViewPort = findViewById(R.id.portValue);
        textViewStatus = findViewById(R.id.stateValue);
        txtTimer_value = findViewById(R.id.timer_value);
        txtPrediction = findViewById(R.id.prediction_value);
        canal = findViewById(R.id.textView3);
        textViewIP.setText(SERVER_IP);
        textViewPort.setText(SERVER_PORT);
        textViewStatus.setText("Disconnected");


        predictionContainer = (LinearLayout) findViewById(R.id.prediction_container);

        ejecucion_motoraMediaPlayer = MediaPlayer.create(this, R.raw.ejecucion_motora);
        imagen_motoraMediaPlayer = MediaPlayer.create(this, R.raw.imagen_motora);
        beepMediaPlayer = MediaPlayer.create(this, R.raw.beep);
        beep_finalMediaPlayer = MediaPlayer.create(this, R.raw.beep_final);
        sustraccionMediaPlayer = MediaPlayer.create(this, R.raw.sustraccion);

        Button btnStart1 = findViewById(R.id.backBtn);
        btnStart1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), Instruction.class);
                startActivity(intent);
            }
        });

        btnStart = findViewById(R.id.btn_start);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (appState.equals("WAITINGTRAINING"))
                    changeAppState("TRAINING");
                else if (appState.equals("WAITINGEVALUATION"))
                    changeAppState("EVALUATING");
            }
        });

        btnStop = findViewById(R.id.btn_stop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restartTimer();
                if (appState.equals("EVALUATING"))
                    changeAppState("WAITINGEVALUATION");
                if (appState.equals("TRAINING"))
                    changeAppState("WAITINGTRAINING");
            }
        });

        btnTraining = findViewById(R.id.btn_training);
        btnTraining.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeAppState("WAITINGTRAINING");
            }
        });

        btnOutTraining = findViewById(R.id.btn_outTraining);
        btnOutTraining.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeAppState("WAITINGEVALUATION");
            }
        });

        changeAppState("WAITINGEVALUATION");


    }

    public void startListenerThread() {
        socketThread = new Thread(new SocketThread());
        socketThread.start();
    }

    public void initComboBox(Context context) {

        channelsSpinner = findViewById(R.id.channel_sppiner);
        ArrayList<String> elementos = new ArrayList<>();
        elementos.add("Canal 1");
        elementos.add("Canal 2");
        elementos.add("Canal 3");
        elementos.add("Canal 4");
        elementos.add("Canal 5");
        elementos.add("Canal 6");
        elementos.add("Canal 7");
        elementos.add("Canal 8");


        ArrayAdapter arrayAdapter = ArrayAdapter.createFromResource(this, R.array.channels, R.layout.spinner_item);
        arrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        channelsSpinner.setAdapter(arrayAdapter);
        channelsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int i, long l) {
                String elemento = (String) channelsSpinner.getAdapter().getItem(i);
                setChanelOfInterest(elemento);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void initViewChannel1(Context context) {

        FrameLayout frameLayout = findViewById(R.id.frame_layout_xyplot_channel_1);

        filterPlotChannelOne = new XYPlot(context, PLOT_TITLE);

        // Create dataSeries that will be drawn on plot (Y will be obtained from dataSource, x will be implicitly generated):
        dataSeriesGraph = new DynamicSeries(PLOT_TITLE);

        // Set X and Y domain
        filterPlotChannelOne.setRangeBoundaries(minSignalFrequency, maxSignalFrequency, BoundaryMode.FIXED);
        filterPlotChannelOne.setDomainBoundaries(0, PLOT_LENGTH, BoundaryMode.FIXED);

        // Create line formatter with set color
        lineFormatterChannelOne = new FastLineAndPointRenderer.Formatter(Color.WHITE, null, null);

        // Set line thickness
        lineFormatterChannelOne.getLinePaint().setStrokeWidth(3);

        // Add line to plot
        filterPlotChannelOne.addSeries(dataSeriesGraph, lineFormatterChannelOne);

        // Format plot layout
        //Remove margins, padding and border
        filterPlotChannelOne.setPlotMargins(0, 0, 0, 0);
        filterPlotChannelOne.setPlotPadding(0, 0, 0, 0);
        filterPlotChannelOne.getBorderPaint().setColor(Color.WHITE);

        // Set plot background color
        filterPlotChannelOne.getGraph().getBackgroundPaint().setColor(Color.rgb(133, 146, 158)
        );
        //rgb(133, 146, 158)

        // Remove gridlines
        filterPlotChannelOne.getGraph().getGridBackgroundPaint().setColor(Color.TRANSPARENT);
        filterPlotChannelOne.getGraph().getDomainGridLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelOne.getGraph().getDomainOriginLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelOne.getGraph().getRangeGridLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelOne.getGraph().getRangeOriginLinePaint().setColor(Color.TRANSPARENT);

        //filterPlot.getGraph().setLineLabelEdges(XYGraphWidget.Edge.LEFT, XYGraphWidget.Edge.BOTTOM);

        // Remove axis labels and values
        // Domain = X; Range = Y
        filterPlotChannelOne.setDomainLabel(null);
        filterPlotChannelOne.setRangeLabel(null);
        filterPlotChannelOne.getGraph().getRangeGridLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelOne.getGraph().getRangeOriginLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelOne.getGraph().getDomainGridLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelOne.getGraph().getDomainOriginLinePaint().setColor(Color.TRANSPARENT);

        // Remove extraneous elements
        filterPlotChannelOne.getLayoutManager().remove(filterPlotChannelOne.getLegend());

        // Set size of plot
        SizeMetric height = new SizeMetric(1, SizeMode.FILL);
        SizeMetric width = new SizeMetric(1, SizeMode.FILL);
        filterPlotChannelOne.getGraph().setSize(new Size(height, width));

        // Set position of plot (should be tweaked in order to center chart position)
        filterPlotChannelOne.getGraph().position(0, HorizontalPositioning.ABSOLUTE_FROM_LEFT.ABSOLUTE_FROM_LEFT,
                0, VerticalPositioning.ABSOLUTE_FROM_TOP);

        // Add plot to FilterGraph
        frameLayout.addView(filterPlotChannelOne);
    }

    public void setChanelOfInterest(String selectedChannel) {
        clearPlot();
        if (selectedChannel.equals("Canal 1")) {
            channelOfInterest = 0;
            canal.setText("Canal: 1");
        } else if (selectedChannel.equals("Canal 2")) {
            channelOfInterest = 1;
            canal.setText("Canal: 2");
        } else if (selectedChannel.equals("Canal 3")) {
            channelOfInterest = 2;
            canal.setText("Canal: 3");
        } else if (selectedChannel.equals("Canal 4")) {
            channelOfInterest = 3;
            canal.setText("Canal: 4");
        } else if (selectedChannel.equals("Canal 5")) {
            channelOfInterest = 4;
            canal.setText("Canal: 5");
        } else if (selectedChannel.equals("Canal 6")) {
            channelOfInterest = 5;
            canal.setText("Canal: 6");
        } else if (selectedChannel.equals("Canal 7")) {
            channelOfInterest = 6;
            canal.setText("Canal: 7");
        } else if (selectedChannel.equals("Canal 8")) {
            channelOfInterest = 7;
            canal.setText("Canal: 8");
        }
    }

    public void setFilterTypeNoch() {
        activeFilterNoch = new Filter_Noch(samplingRate, "bandstop", 5, 1, 6);
        filtStateNoch = new double[8][activeFilterNoch.getNB()];
    }

    public void setFilterType() {
        activeFilter = new Filter(samplingRate, "bandstop", 5, 1, 6);
        filtState = new double[8][activeFilter.getNB()];
    }


    public void changeAppState(String state) {


        if (justStarted && (state.equals("TRAINING") || state.equals("EVALUATING"))) {
            System.out.println("Inicia el muestreo de datos");
            Thread sendMessageThread = new Thread(new SendMessageThread("enviar"));
            sendMessageThread.start();
            justStarted = false;
        }

        appState = state;

        switch (state) {
            case "TRAINING":
                startTimer();
                clearPlot();
                btnTraining.setVisibility(LinearLayout.GONE);
                btnTraining.setEnabled(false);
                btnTraining.setBackground(getResources().getDrawable(R.drawable.disable_button));

                btnOutTraining.setVisibility(LinearLayout.VISIBLE);
                btnOutTraining.setEnabled(false);
                btnOutTraining.setBackground(getResources().getDrawable(R.drawable.disable_button));

                btnStart.setVisibility(LinearLayout.GONE);
                btnStart.setEnabled(false);
                btnStart.setBackground(getResources().getDrawable(R.drawable.disable_button));

                btnStop.setVisibility(LinearLayout.VISIBLE);
                btnStop.setEnabled(true);
                btnStop.setBackground(getResources().getDrawable(R.drawable.stilebutton));

                textViewStatus.setText("Entrenando");
                predictionContainer.setVisibility(View.GONE);

                break;
            case "EVALUATING":

                startTimer();

                clearPlot();
                btnTraining.setVisibility(LinearLayout.VISIBLE);
                btnTraining.setEnabled(false);
                btnTraining.setBackground(getResources().getDrawable(R.drawable.disable_button));

                btnOutTraining.setVisibility(LinearLayout.GONE);
                btnOutTraining.setEnabled(false);
                btnOutTraining.setBackground(getResources().getDrawable(R.drawable.disable_button));

                btnStart.setVisibility(LinearLayout.GONE);
                btnStart.setEnabled(true);
                btnStart.setBackground(getResources().getDrawable(R.drawable.disable_button));

                btnStop.setVisibility(LinearLayout.VISIBLE);
                btnStop.setEnabled(true);
                btnStop.setBackground(getResources().getDrawable(R.drawable.stilebutton));

                textViewStatus.setText("Evaluando");
                predictionContainer.setVisibility(View.VISIBLE);

                break;
            case "WAITINGEVALUATION":

                btnTraining.setVisibility(LinearLayout.VISIBLE);
                btnTraining.setEnabled(true);
                btnTraining.setBackground(getResources().getDrawable(R.drawable.stilebutton));

                btnOutTraining.setVisibility(LinearLayout.GONE);
                btnOutTraining.setEnabled(false);
                btnOutTraining.setBackground(getResources().getDrawable(R.drawable.disable_button));

                btnStart.setVisibility(LinearLayout.VISIBLE);
                btnStart.setEnabled(true);
                btnStart.setBackground(getResources().getDrawable(R.drawable.stilebutton));

                btnStop.setVisibility(LinearLayout.GONE);
                btnStop.setEnabled(false);
                btnStop.setBackground(getResources().getDrawable(R.drawable.disable_button));

                textViewStatus.setText("Esperando evaluación");
                predictionContainer.setVisibility(View.VISIBLE);


                break;

            case "WAITINGTRAINING":

                btnTraining.setVisibility(LinearLayout.GONE);
                btnTraining.setEnabled(false);
                btnTraining.setBackground(getResources().getDrawable(R.drawable.disable_button));

                btnOutTraining.setVisibility(LinearLayout.VISIBLE);
                btnOutTraining.setEnabled(true);
                btnOutTraining.setBackground(getResources().getDrawable(R.drawable.stilebutton));

                btnStart.setVisibility(LinearLayout.VISIBLE);
                btnStart.setEnabled(true);
                btnStart.setBackground(getResources().getDrawable(R.drawable.stilebutton));

                btnStop.setVisibility(LinearLayout.GONE);
                btnStop.setEnabled(false);
                btnStop.setBackground(getResources().getDrawable(R.drawable.disable_button));

                textViewStatus.setText("Esperando entrenamiento");
                predictionContainer.setVisibility(View.GONE);

                break;
        }


    }

    public void startTimer() {
        evaluationTimer = new CountDownTimer(150000, 1000) {
            int minutos;
            int segundos;
            String segundosString;
            String prediction;

            public void onTick(long millisUntilFinished) {
                minutos = counter / 60;
                segundos = counter % 60;
                if (segundos < 10)
                    segundosString = "0" + segundos;
                else
                    segundosString = "" + segundos;


                txtTimer_value.setText(minutos + ":" + segundosString);
                counter--;


                System.out.println(counter);
                if (counter == 89) {
                    extractedArrayString[frameCounter] = "---------------Inicia ejecucion motora--------------------";
                    ejecucion_motoraMediaPlayer.start();
                } else if (counter == 80) {

                    prediction = "Canal 1: " + knnChannelOne.evaluateBlink(dataSeriesChannelOne)/* + "\n"
                            + "Canal 2: " + knnChannelTwo.evaluateBlink(dataSeriesChannelTwo) + "\n"
                            + "Canal 3: " + knnChannelThree.evaluateBlink(dataSeriesChannelThree) + "\n"
                            + "Canal 4: " + knnChannelFour.evaluateBlink(dataSeriesChannelFour) + "\n"
                            + "Canal 5: " + knnChannelFive.evaluateBlink(dataSeriesChannelFive) + "\n"
                            + "Canal 6: " + knnChannelSix.evaluateBlink(dataSeriesChannelSix) + "\n"
                            + "Canal 7: " + knnChannelSeven.evaluateBlink(dataSeriesChannelSeven) + "\n"
                            + "Canal 8: " + knnChannelEight.evaluateBlink(dataSeriesChannelEigth)*/;

                    txtPrediction.setText(prediction);

                    beepMediaPlayer.start();
                } else if (counter == 78) {
                    extractedArrayString[frameCounter] = "---------------Inicia imagen motora--------------------";
                    imagen_motoraMediaPlayer.start();
                } else if (counter == 70) {

                    prediction = "Canal 1: " + knnChannelOne.evaluateBlink(dataSeriesChannelOne)/* + "\n"
                            + "Canal 2: " + knnChannelTwo.evaluateBlink(dataSeriesChannelTwo) + "\n"
                            + "Canal 3: " + knnChannelThree.evaluateBlink(dataSeriesChannelThree) + "\n"
                            + "Canal 4: " + knnChannelFour.evaluateBlink(dataSeriesChannelFour) + "\n"
                            + "Canal 5: " + knnChannelFive.evaluateBlink(dataSeriesChannelFive) + "\n"
                            + "Canal 6: " + knnChannelSix.evaluateBlink(dataSeriesChannelSix) + "\n"
                            + "Canal 7: " + knnChannelSeven.evaluateBlink(dataSeriesChannelSeven) + "\n"
                            + "Canal 8: " + knnChannelEight.evaluateBlink(dataSeriesChannelEigth)*/;


                    beepMediaPlayer.start();
                } else if (counter == 67) {
                    extractedArrayString[frameCounter] = "---------------Inicia sustracción--------------------";
                    sustraccionMediaPlayer.start();

                } else if (counter == 59) {

                    prediction = "Canal 1: " + knnChannelOne.evaluateBlink(dataSeriesChannelOne)/* + "\n"
                            + "Canal 2: " + knnChannelTwo.evaluateBlink(dataSeriesChannelTwo) + "\n"
                            + "Canal 3: " + knnChannelThree.evaluateBlink(dataSeriesChannelThree) + "\n"
                            + "Canal 4: " + knnChannelFour.evaluateBlink(dataSeriesChannelFour) + "\n"
                            + "Canal 5: " + knnChannelFive.evaluateBlink(dataSeriesChannelFive) + "\n"
                            + "Canal 6: " + knnChannelSix.evaluateBlink(dataSeriesChannelSix) + "\n"
                            + "Canal 7: " + knnChannelSeven.evaluateBlink(dataSeriesChannelSeven) + "\n"
                            + "Canal 8: " + knnChannelEight.evaluateBlink(dataSeriesChannelEigth)*/;


                    beep_finalMediaPlayer.start();
                }

            }

            public void onFinish() {
                saveRecord();
                restartTimer();
                if (appState.equals("EVALUATING"))
                    changeAppState("WAITINGEVALUATION");
                if (appState.equals("TRAINING"))
                    changeAppState("WAITINGTRAINING");
            }
        }.start();

    }

    public void restartTimer() {
        counter = 150;
        evaluationTimer.cancel();
        txtTimer_value.setText("2:30");

    }

    public void saveRecord() {

        for (int i = 0; i < frameCounter; i++) {
            if (extractedArrayString[i] != null)
                eegFile.addLineToFile(extractedArrayString[i]);
        }
        eegFile.writeFileDataSet();
    }


    void startDataBase() {

        boolean databaseReady = false;

        String dbClassOne = "ClassOneDB";
        String dbClassTwo = "ClassTwoDB";
        String dbClassThree = "ClassThreeDB";

        final File fileClassOneDB = new File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), dbClassOne + ".json");
        final File fileClassTwoDB = new File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), dbClassTwo + ".json");
        final File fileClassThreeDB = new File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), dbClassThree + ".json");

        try {
            new FileReader(fileClassOneDB);
            new java.io.FileReader(fileClassTwoDB);
            new java.io.FileReader(fileClassThreeDB);
        } catch (FileNotFoundException e) {
            databaseReady = true;
        }

        if (databaseReady) {
            EEGFileWriter shorBlinkFile = new EEGFileWriter(this, "Captura de datos");
            EEGFileWriter ClassTwoFile = new EEGFileWriter(this, "Captura de datos");
            EEGFileWriter ClassThreeFile = new EEGFileWriter(this, "Captura de datos");

            shorBlinkFile.initFile();
            ClassTwoFile.initFile();
            ClassThreeFile.initFile();

            try {

                InputStream inputStream = getResources().getAssets().open(dbClassOne + ".json");
                DataBaseFileWriter fileReader = new DataBaseFileWriter(inputStream);
                fileReader.writeClassOneDataBase(shorBlinkFile);
            } catch (IOException e) {
                Log.w("EEGGraph", "File not found error");
            }

            try {

                InputStream inputStream = getResources().getAssets().open(dbClassTwo + ".json");
                DataBaseFileWriter fileReader = new DataBaseFileWriter(inputStream);
                fileReader.writeClassTwoDataBase(ClassTwoFile);
            } catch (IOException e) {
                Log.w("EEGGraph", "File not found error");
            }

            try {

                InputStream inputStream = getResources().getAssets().open(dbClassThree + ".json");
                DataBaseFileWriter fileReader = new DataBaseFileWriter(inputStream);
                fileReader.writeClassThreeDataBase(ClassThreeFile);
            } catch (IOException e) {
                Log.w("EEGGraph", "File not found error");
            }

        }

    }


    void readDataBase() {

        String dbClassOne = "ClassOneDB";
        String dbClassTwo = "ClassTwoDB";
        String dbClassThree = "ClassThreeDB";

        try {
            final File file = new File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), dbClassOne + ".json");
            FileReader filePathReader = new java.io.FileReader(file);
            InputStream inputStream = getResources().getAssets().open(dbClassOne + ".json");
            EEGFileReader fileReader = new EEGFileReader(filePathReader);
            //EEGFileReader fileReader = new EEGFileReader(inputStream);

            double[][] readArray = new double[10][35200];

            readArray[0] = fileReader.readToVector(0);
            readArray[1] = fileReader.readToVector(1);
            readArray[2] = fileReader.readToVector(2);
            readArray[3] = fileReader.readToVector(3);
            readArray[4] = fileReader.readToVector(4);
            readArray[5] = fileReader.readToVector(5);
            readArray[6] = fileReader.readToVector(6);
            readArray[7] = fileReader.readToVector(7);

            originalSignalClassOne = readArray;
            System.out.println("Lectura del primer archivo" + originalSignalClassOne[0].length);
            System.out.println("Lectura del primer archivo" + originalSignalClassOne[1].length);
            System.out.println("Lectura del primer archivo" + originalSignalClassOne[2].length);
            System.out.println("Lectura del primer archivo" + originalSignalClassOne[3].length);

        } catch (IOException e) {
            Log.w("EEGGraph", "File not found error");
        }

        try {
            final File file = new File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), dbClassTwo + ".json");
            FileReader filePathReader = new java.io.FileReader(file);
            InputStream inputStream = getResources().getAssets().open(dbClassTwo + ".json");
            EEGFileReader fileReader = new EEGFileReader(filePathReader);
            //EEGFileReader fileReader = new EEGFileReader(inputStream);

            double[][] readArray = new double[10][35200];

            readArray[0] = fileReader.readToVector(0);
            readArray[1] = fileReader.readToVector(1);
            readArray[2] = fileReader.readToVector(2);
            readArray[3] = fileReader.readToVector(3);
            readArray[4] = fileReader.readToVector(4);
            readArray[5] = fileReader.readToVector(5);
            readArray[6] = fileReader.readToVector(6);
            readArray[7] = fileReader.readToVector(7);
            originalSignalClassTwo = readArray;


            System.out.println("Lectura del segundo archivo");
        } catch (IOException e) {
            Log.w("EEGGraph", "File not found error");
        }

        try {
            final File file = new File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), dbClassThree + ".json");
            FileReader filePathReader = new java.io.FileReader(file);
            InputStream inputStream = getResources().getAssets().open(dbClassThree + ".json");
            EEGFileReader fileReader = new EEGFileReader(filePathReader);
            //EEGFileReader fileReader = new EEGFileReader(inputStream);


            double[][] readArray = new double[10][35200];

            readArray[0] = fileReader.readToVector(0);
            readArray[1] = fileReader.readToVector(1);
            readArray[2] = fileReader.readToVector(2);
            readArray[3] = fileReader.readToVector(3);
            readArray[4] = fileReader.readToVector(4);
            readArray[5] = fileReader.readToVector(5);
            readArray[6] = fileReader.readToVector(6);
            readArray[7] = fileReader.readToVector(7);
            originalSignalClassThree = readArray;

            System.out.println("Lectura del tercer archivo");

        } catch (IOException e) {
            Log.w("EEGGraph", "File not found error");
        }
    }

    class SocketThread implements Runnable {
        public void run() {
            Socket socket;
            try {
                socket = new Socket(SERVER_IP, Integer.parseInt(SERVER_PORT));
                output = new PrintWriter(socket.getOutputStream());
                input = new DataInputStream(socket.getInputStream());
                dataListener = new Thread(new ListenerThread());
                dataListener.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class SendMessageThread implements Runnable {
        private String message;

        SendMessageThread(String message) {
            this.message = message;
        }

        @Override
        public void run() {
            output.write(message);
            output.flush();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    return;
                }
            });
            return;
        }
    }

    class ListenerThread implements Runnable {

        public double[] newData;
        public Filter bandstopFilter;


        @Override
        public void run() {
            newData = new double[8];
            while (true) {
                try {
                    final String[] message = {input.readUTF()};
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            newData = getEegChannelValues(message[0]);
                            if (appState.equals("EVALUATING") || appState.equals("TRAINING")) {
                                filtState = activeFilter.transform(newData, filtState);
                                filtStateNoch = activeFilterNoch.transform(newData, filtStateNoch);

                                double[] vector1 = activeFilter.extractFilteredSamples(filtState);
                                double[] vector2 = activeFilterNoch.extractFilteredSamples(filtStateNoch);

                                eegBuffer.update(sumarVectores(vector1, vector2));

                                if ((counter < 88 && counter > 80) || (counter < 78 && counter > 70) || (counter < 67 && counter > 59)) {

                                    extractedArrayString[frameCounter] = Arrays.toString(sumarVectores(vector1, vector2));

                                }

                                frameCounter++;
                                if (frameCounter % 10 == 0) {
                                    updatePlot();
                                }

                                if (frameCounter % 125 == 0) {
                                    average_channel_1 = dataSeriesGraph.getAverage();
                                    txtAverage_channel_1.setText("Promedio: " + (average_channel_1) + " uV/count");
                                }
                            } else {
                                frameCounter = 0;
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private double[] getEegChannelValues(String p) {
            p = p.substring(1, p.length() - 1);
            String[] values = p.split(",");
            if (values.length >= 7) {
                newData[0] = Double.parseDouble(values[0]) * SCALE_FACTOR_EEG;
                newData[1] = Double.parseDouble(values[1]) * SCALE_FACTOR_EEG;
                newData[2] = Double.parseDouble(values[2]) * SCALE_FACTOR_EEG;
                newData[3] = Double.parseDouble(values[3]) * SCALE_FACTOR_EEG;
                newData[4] = Double.parseDouble(values[4]) * SCALE_FACTOR_EEG;
                newData[5] = Double.parseDouble(values[5]) * SCALE_FACTOR_EEG;
                newData[6] = Double.parseDouble(values[6]) * SCALE_FACTOR_EEG;
                newData[7] = Double.parseDouble(values[7]) * SCALE_FACTOR_EEG;
            }
            return newData;
        }


        private double[] sumarVectores(double[] vector1, double[] vector2) {
            double[] vectorSuma = new double[vector1.length];
            for (int i = 0; i < vector1.length; i++) {
                vectorSuma[i] = vector1[i] + vector2[i];
            }
            return vectorSuma;
        }

        public void updateFilter(int notchFrequency) {
            if (bandstopFilter != null) {
                bandstopFilter.updateFilter(notchFrequency - 5, notchFrequency + 5);
            }
        }

    }

    public void updatePlot() {
        int numEEGPoints = eegBuffer.getPts();
        if (dataSeriesGraph.size() >= PLOT_LENGTH) {

            dataSeriesGraph.remove(numEEGPoints);

            dataSeriesChannelOne.remove(numEEGPoints);
            dataSeriesChannelTwo.remove(numEEGPoints);
            dataSeriesChannelThree.remove(numEEGPoints);
            dataSeriesChannelFour.remove(numEEGPoints);
            dataSeriesChannelFive.remove(numEEGPoints);
            dataSeriesChannelSix.remove(numEEGPoints);
            dataSeriesChannelSeven.remove(numEEGPoints);
            dataSeriesChannelEigth.remove(numEEGPoints);
        }
        dataSeriesGraph.addAll(eegBuffer.extractSingleChannelTransposedAsDouble(numEEGPoints, channelOfInterest));


        dataSeriesChannelOne.addAll(eegBuffer.extractSingleChannelTransposedAsDouble(numEEGPoints, 0));
        dataSeriesChannelTwo.addAll(eegBuffer.extractSingleChannelTransposedAsDouble(numEEGPoints, 1));
        dataSeriesChannelThree.addAll(eegBuffer.extractSingleChannelTransposedAsDouble(numEEGPoints, 2));
        dataSeriesChannelFour.addAll(eegBuffer.extractSingleChannelTransposedAsDouble(numEEGPoints, 3));
        dataSeriesChannelFive.addAll(eegBuffer.extractSingleChannelTransposedAsDouble(numEEGPoints, 4));
        dataSeriesChannelSix.addAll(eegBuffer.extractSingleChannelTransposedAsDouble(numEEGPoints, 5));
        dataSeriesChannelSeven.addAll(eegBuffer.extractSingleChannelTransposedAsDouble(numEEGPoints, 6));
        dataSeriesChannelEigth.addAll(eegBuffer.extractSingleChannelTransposedAsDouble(numEEGPoints, 7));
        eegBuffer.resetPts();
        filterPlotChannelOne.redraw();
    }

    public void clearPlot() {
        int dataSeriesSize = dataSeriesGraph.getAll().size();
        dataSeriesGraph.remove(dataSeriesSize);
        filterPlotChannelOne.redraw();
    }
}