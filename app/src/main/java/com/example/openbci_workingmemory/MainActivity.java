package com.example.openbci_workingmemory;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
import com.example.openbci_workingmemory.components.DynamicSeries;
import com.example.openbci_workingmemory.components.Filter;
import com.example.openbci_workingmemory.components.Filter_Noch;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public Thread socketThread = null;
    public Thread dataListener = null;

    TextView textViewIP, textViewPort, textViewStatus;
    Button btnStart;
    Button btnStop;
    Button btnTraining;
    Button btnOutTraining;


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

    LinearLayout predictionContainer;

    private int notchFrequency = 14;
    private static final int PLOT_LENGTH = 255 * 3;
    public CircularBuffer eegBuffer = new CircularBuffer(220, 8);

    private static final String PLOT_TITLE = "Raw_EEG";
    // amplitud de la señal ¨consultar¨
    int maxSignalFrequency = 250000;
    int minSignalFrequency = -250000;

    public DynamicSeries dataSeriesChannelOne;
    public XYPlot filterPlotChannelOne;
    int average_channel_1 = 0;
    TextView txtAverage_channel_1;
    private LineAndPointFormatter lineFormatterChannelOne;

    public double SCALE_FACTOR_EEG = 0.022351744455307063;
    public double SCALE_FACTOR_EEG1 = (4500000) / 24 / (2 ^ 23 - 1);

    private Spinner channelsSpinner;
    private String appState = "WAITINGEVALUATION";
    private Boolean justStarted = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setFilterType();
        setFilterTypeNoch();
        startListenerThread();
        initUI();
    }

    public void initUI() {

        dataSeriesChannelOne = new DynamicSeries(PLOT_TITLE);
        filterPlotChannelOne = new XYPlot(this, PLOT_TITLE);

        initViewChannel1(this);
        initComboBox(this);

        txtAverage_channel_1 = findViewById(R.id.average_channel_1);

        textViewIP = findViewById(R.id.ipValue);
        textViewPort = findViewById(R.id.portValue);
        textViewStatus = findViewById(R.id.stateValue);

        textViewIP.setText(SERVER_IP);
        textViewPort.setText(SERVER_PORT);
        textViewStatus.setText("Disconnected");


        predictionContainer = (LinearLayout) findViewById(R.id.prediction_container);


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

        ArrayAdapter arrayAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, elementos);
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
        dataSeriesChannelOne = new DynamicSeries(PLOT_TITLE);

        // Set X and Y domain
        filterPlotChannelOne.setRangeBoundaries(minSignalFrequency, maxSignalFrequency, BoundaryMode.FIXED);
        filterPlotChannelOne.setDomainBoundaries(0, PLOT_LENGTH, BoundaryMode.FIXED);

        // Create line formatter with set color
        lineFormatterChannelOne = new FastLineAndPointRenderer.Formatter(Color.WHITE, null, null);

        // Set line thickness
        lineFormatterChannelOne.getLinePaint().setStrokeWidth(3);

        // Add line to plot
        filterPlotChannelOne.addSeries(dataSeriesChannelOne, lineFormatterChannelOne);

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
        } else if (selectedChannel.equals("Canal 2")) {
            channelOfInterest = 1;
        } else if (selectedChannel.equals("Canal 3")) {
            channelOfInterest = 2;
        } else if (selectedChannel.equals("Canal 4")) {
            channelOfInterest = 3;
        } else if (selectedChannel.equals("Canal 5")) {
            channelOfInterest = 4;
        } else if (selectedChannel.equals("Canal 6")) {
            channelOfInterest = 5;
        } else if (selectedChannel.equals("Canal 7")) {
            channelOfInterest = 6;
        } else if (selectedChannel.equals("Canal 8")) {
            channelOfInterest = 7;
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



/*
        btnStart.setOnClickListener(new View.OnClickListener() {
                if (appState.equals("WAITINGTRAINING"))
                    changeAppState("TRAINING");
                if (appState.equals("WAITINGEVALUATION"))
                    changeAppState("EVALUATING");

        btnStop = findViewById(R.id.btn_stop);
                if (appState.equals("EVALUATING"))
                    changeAppState("WAITINGEVALUATION");
                if (appState.equals("TRAINING"))
                    changeAppState("WAITINGTRAINING");

        btnTraining = findViewById(R.id.btn_training);
                changeAppState("WAITINGTRAINING");

        btnOutTraining = findViewById(R.id.btn_outTraining);
                changeAppState("WAITINGEVALUATION");

*/

        if (justStarted && (state.equals("TRAINING") || state.equals("EVALUATING"))) {
            System.out.println("Inicia el muestreo de datos");
            Thread sendMessageThread = new Thread(new SendMessageThread("enviar"));
            sendMessageThread.start();
            justStarted = false;
        }

        appState = state;

        switch (state) {
            case "TRAINING":
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
        private int frameCounter = 0;


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

                                frameCounter++;
                                if (frameCounter % 10 == 0) {
                                    updatePlot();
                                }

                                if (frameCounter % 125 == 0) {
                                    average_channel_1 = dataSeriesChannelOne.getAverage();
                                    txtAverage_channel_1.setText("Promedio: " + (average_channel_1));
                                }
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
        if (dataSeriesChannelOne.size() >= PLOT_LENGTH) {
            dataSeriesChannelOne.remove(numEEGPoints);
        }
        dataSeriesChannelOne.addAll(eegBuffer.extractSingleChannelTransposedAsDouble(numEEGPoints, channelOfInterest));
        eegBuffer.resetPts();
        filterPlotChannelOne.redraw();
    }

    public void clearPlot() {
        int dataSeriesSize = dataSeriesChannelOne.getAll().size();
        dataSeriesChannelOne.remove(dataSeriesSize);
        filterPlotChannelOne.redraw();
    }
}