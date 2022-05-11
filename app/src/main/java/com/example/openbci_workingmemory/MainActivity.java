package com.example.openbci_workingmemory;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
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

public class MainActivity extends AppCompatActivity {

    Thread Thread1 = null;
    public Thread2 dataListener;


    TextView textViewIP, textViewPort, textViewStatus;
    Button btnConnect, Disconect;

    String SERVER_IP = "192.168.100.101";
    String SERVER_PORT = "5000";

    public PrintWriter output;
    public DataInputStream input;

    public int samplingRate = 250;
    public Filter activeFilter;
    public Filter_Noch activeFilterNoch;
    public double[][] filtState;
    public double[][] filtStateNoch;
    String message;

    private int notchFrequency = 14;
    private static final int PLOT_LENGTH = 255 * 3;
    public CircularBuffer eegBuffer = new CircularBuffer(220, 4);

    private static final String PLOT_TITLE = "Raw_EEG";
    // amplitud de la señal ¨consultar¨
    int maxSignalFrequency = 250000;
    int minSignalFrequency = -250000;

    public DynamicSeries dataSeriesChannelOne;
    public DynamicSeries dataSeriesChannelTwo;
    public DynamicSeries dataSeriesChannelTree;
    public DynamicSeries dataSeriesChannelFour;

    public XYPlot filterPlotChannelOne;
    public XYPlot filterPlotChannelTwo;
    public XYPlot filterPlotChannelTree;
    public XYPlot filterPlotChannelFour;

    int average_channel_1 = 0;
    int average_channel_2 = 0;
    int average_channel_3 = 0;
    int average_channel_4 = 0;

    TextView txtAverage_channel_1;
    TextView txtAverage_channel_2;
    TextView txtAverage_channel_3;
    TextView txtAverage_channel_4;

    private LineAndPointFormatter lineFormatterChannelOne;
    private LineAndPointFormatter lineFormatterChannelTwo;
    private LineAndPointFormatter lineFormatterChannelTre;
    private LineAndPointFormatter lineFormatterChannelFour;

    public double SCALE_FACTOR_EEG = 0.022351744455307063;
    public double SCALE_FACTOR_EEG1 = (4500000) / 24 / (2 ^ 23 - 1);


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setNotchFrequency(notchFrequency);
        setFilterType();
        setFilterTypeNoch();
        initUI();


    }


    public void initUI() {
        dataSeriesChannelOne = new DynamicSeries(PLOT_TITLE);
        dataSeriesChannelTwo = new DynamicSeries(PLOT_TITLE);
        dataSeriesChannelTree = new DynamicSeries(PLOT_TITLE);
        dataSeriesChannelFour = new DynamicSeries(PLOT_TITLE);

        filterPlotChannelOne = new XYPlot(this, PLOT_TITLE);
        filterPlotChannelTwo = new XYPlot(this, PLOT_TITLE);
        filterPlotChannelTree = new XYPlot(this, PLOT_TITLE);
        filterPlotChannelFour = new XYPlot(this, PLOT_TITLE);

        initViewChannel1(this);
        initViewChannel2(this);
        initViewChannel3(this);
        initViewChannel4(this);

        txtAverage_channel_1 = findViewById(R.id.average_channel_1);
        txtAverage_channel_2 = findViewById(R.id.average_channel_2);
        txtAverage_channel_3 = findViewById(R.id.average_channel_3);
        txtAverage_channel_4 = findViewById(R.id.average_channel_4);

        textViewIP = findViewById(R.id.ipValue);
        textViewPort = findViewById(R.id.portValue);
        textViewStatus = findViewById(R.id.stateValue);

        textViewIP.setText(SERVER_IP);
        textViewPort.setText(SERVER_PORT);
        textViewStatus.setText("Disconnected");

        Button btnConnect1 = findViewById(R.id.backBtn);
        btnConnect1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), Instruction.class);
                startActivity(intent);
            }
        });
// inicializa el socket
        Thread1 = new Thread(new Thread1());
        Thread1.start();
// entra al hilo 3 con enviar
        btnConnect = findViewById(R.id.Connect);
        btnConnect.setOnClickListener(new View.OnClickListener() {
           @Override
            public void onClick(View v) {
                /*SERVER_IP = textViewIP.getText().toString().trim();
                SERVER_PORT = Integer.parseInt(textViewPort.getText().toString().trim());*/
               message = "enviar";
               if (!message.isEmpty()) {
                   new Thread(new Thread3(message)).start();
                       textViewStatus.setText("Connected\n");
               }
           }
           });
// entra al hilo 3 con salir
        Disconect = findViewById(R.id.Disconect);
        Disconect.setOnClickListener(new View.OnClickListener() {
            @Override
           public void onClick(View view) {
                message = "salir";
                if (!message.isEmpty()) {
                    new Thread(new Thread3(message)).start();
                    textViewStatus.setText("Disconnected\n");
               } }
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

    public void initViewChannel2(Context context) {

        FrameLayout frameLayout = findViewById(R.id.frame_layout_xyplot_channel_2);

        filterPlotChannelTwo = new XYPlot(context, PLOT_TITLE);

        // Create dataSeries that will be drawn on plot (Y will be obtained from dataSource, x will be implicitly generated):
        dataSeriesChannelTwo = new DynamicSeries(PLOT_TITLE);

        // Set X and Y domain
        filterPlotChannelTwo.setRangeBoundaries(minSignalFrequency, maxSignalFrequency, BoundaryMode.FIXED);
        filterPlotChannelTwo.setDomainBoundaries(0, PLOT_LENGTH, BoundaryMode.FIXED);

        // Create line formatter with set color
        lineFormatterChannelTwo = new FastLineAndPointRenderer.Formatter(Color.WHITE, null, null);

        // Set line thickness
        lineFormatterChannelTwo.getLinePaint().setStrokeWidth(3);

        // Add line to plot
        filterPlotChannelTwo.addSeries(dataSeriesChannelTwo, lineFormatterChannelTwo);

        // Format plot layout
        //Remove margins, padding and border
        filterPlotChannelTwo.setPlotMargins(0, 0, 0, 0);
        filterPlotChannelTwo.setPlotPadding(0, 0, 0, 0);
        filterPlotChannelTwo.getBorderPaint().setColor(Color.WHITE);

        // Set plot background color
        filterPlotChannelTwo.getGraph().getBackgroundPaint().setColor(Color.rgb(175, 122, 197));

        // Remove gridlines
        filterPlotChannelTwo.getGraph().getGridBackgroundPaint().setColor(Color.TRANSPARENT);
        filterPlotChannelTwo.getGraph().getDomainGridLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelTwo.getGraph().getDomainOriginLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelTwo.getGraph().getRangeGridLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelTwo.getGraph().getRangeOriginLinePaint().setColor(Color.TRANSPARENT);

        //filterPlot.getGraph().setLineLabelEdges(XYGraphWidget.Edge.LEFT, XYGraphWidget.Edge.BOTTOM);

        // Remove axis labels and values
        // Domain = X; Range = Y
        filterPlotChannelTwo.setDomainLabel(null);
        filterPlotChannelTwo.setRangeLabel(null);
        filterPlotChannelTwo.getGraph().getRangeGridLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelTwo.getGraph().getRangeOriginLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelTwo.getGraph().getDomainGridLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelTwo.getGraph().getDomainOriginLinePaint().setColor(Color.TRANSPARENT);

        // Remove extraneous elements
        filterPlotChannelTwo.getLayoutManager().remove(filterPlotChannelTwo.getLegend());

        // Set size of plot
        SizeMetric height = new SizeMetric(1, SizeMode.FILL);
        SizeMetric width = new SizeMetric(1, SizeMode.FILL);
        filterPlotChannelTwo.getGraph().setSize(new Size(height, width));

        // Set position of plot (should be tweaked in order to center chart position)
        filterPlotChannelTwo.getGraph().position(0, HorizontalPositioning.ABSOLUTE_FROM_LEFT.ABSOLUTE_FROM_LEFT,
                0, VerticalPositioning.ABSOLUTE_FROM_TOP);

        // Add plot to FilterGraph
        frameLayout.addView(filterPlotChannelTwo);
    }

    public void initViewChannel3(Context context) {

        FrameLayout frameLayout = findViewById(R.id.frame_layout_xyplot_channel_3);

        filterPlotChannelTree = new XYPlot(context, PLOT_TITLE);

        // Create dataSeries that will be drawn on plot (Y will be obtained from dataSource, x will be implicitly generated):
        dataSeriesChannelTree = new DynamicSeries(PLOT_TITLE);

        // Set X and Y domain
        filterPlotChannelTree.setRangeBoundaries(minSignalFrequency, maxSignalFrequency, BoundaryMode.FIXED);
        filterPlotChannelTree.setDomainBoundaries(0, PLOT_LENGTH, BoundaryMode.FIXED);

        // Create line formatter with set color
        lineFormatterChannelTre = new FastLineAndPointRenderer.Formatter(Color.WHITE, null, null);

        // Set line thickness
        lineFormatterChannelTre.getLinePaint().setStrokeWidth(3);

        // Add line to plot
        filterPlotChannelTree.addSeries(dataSeriesChannelTree, lineFormatterChannelTre);

        // Format plot layout
        //Remove margins, padding and border
        filterPlotChannelTree.setPlotMargins(0, 0, 0, 0);
        filterPlotChannelTree.setPlotPadding(0, 0, 0, 0);
        filterPlotChannelTree.getBorderPaint().setColor(Color.WHITE);

        // Set plot background color
        filterPlotChannelTree.getGraph().getBackgroundPaint().setColor(Color.rgb(11, 126, 217));

        // Remove gridlines
        filterPlotChannelTree.getGraph().getGridBackgroundPaint().setColor(Color.TRANSPARENT);
        filterPlotChannelTree.getGraph().getDomainGridLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelTree.getGraph().getDomainOriginLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelTree.getGraph().getRangeGridLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelTree.getGraph().getRangeOriginLinePaint().setColor(Color.TRANSPARENT);

        //filterPlot.getGraph().setLineLabelEdges(XYGraphWidget.Edge.LEFT, XYGraphWidget.Edge.BOTTOM);

        // Remove axis labels and values
        // Domain = X; Range = Y
        filterPlotChannelTree.setDomainLabel(null);
        filterPlotChannelTree.setRangeLabel(null);
        filterPlotChannelTree.getGraph().getRangeGridLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelTree.getGraph().getRangeOriginLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelTree.getGraph().getDomainGridLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelTree.getGraph().getDomainOriginLinePaint().setColor(Color.TRANSPARENT);

        // Remove extraneous elements
        filterPlotChannelTree.getLayoutManager().remove(filterPlotChannelTree.getLegend());

        // Set size of plot
        SizeMetric height = new SizeMetric(1, SizeMode.FILL);
        SizeMetric width = new SizeMetric(1, SizeMode.FILL);
        filterPlotChannelTree.getGraph().setSize(new Size(height, width));

        // Set position of plot (should be tweaked in order to center chart position)
        filterPlotChannelTree.getGraph().position(0, HorizontalPositioning.ABSOLUTE_FROM_LEFT.ABSOLUTE_FROM_LEFT,
                0, VerticalPositioning.ABSOLUTE_FROM_TOP);

        // Add plot to FilterGraph
        frameLayout.addView(filterPlotChannelTree);
    }

    public void initViewChannel4(Context context) {

        FrameLayout frameLayout = findViewById(R.id.frame_layout_xyplot_channel_4);

        filterPlotChannelFour = new XYPlot(context, PLOT_TITLE);

        // Create dataSeries that will be drawn on plot (Y will be obtained from dataSource, x will be implicitly generated):
        dataSeriesChannelFour = new DynamicSeries(PLOT_TITLE);

        // Set X and Y domain
        filterPlotChannelFour.setRangeBoundaries(minSignalFrequency, maxSignalFrequency, BoundaryMode.FIXED);
        filterPlotChannelFour.setDomainBoundaries(0, PLOT_LENGTH, BoundaryMode.FIXED);

        // Create line formatter with set color
        lineFormatterChannelFour = new FastLineAndPointRenderer.Formatter(Color.WHITE, null, null);

        // Set line thickness
        lineFormatterChannelFour.getLinePaint().setStrokeWidth(3);

        // Add line to plot
        filterPlotChannelFour.addSeries(dataSeriesChannelFour, lineFormatterChannelFour);

        // Format plot layout
        //Remove margins, padding and border
        filterPlotChannelFour.setPlotMargins(0, 0, 0, 0);
        filterPlotChannelFour.setPlotPadding(0, 0, 0, 0);
        filterPlotChannelFour.getBorderPaint().setColor(Color.WHITE);

        // Set plot background color
        filterPlotChannelFour.getGraph().getBackgroundPaint().setColor(Color.rgb(115, 198, 182));

        // Remove gridlines
        filterPlotChannelFour.getGraph().getGridBackgroundPaint().setColor(Color.TRANSPARENT);
        filterPlotChannelFour.getGraph().getDomainGridLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelFour.getGraph().getDomainOriginLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelFour.getGraph().getRangeGridLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelFour.getGraph().getRangeOriginLinePaint().setColor(Color.TRANSPARENT);

        //filterPlot.getGraph().setLineLabelEdges(XYGraphWidget.Edge.LEFT, XYGraphWidget.Edge.BOTTOM);

        // Remove axis labels and values
        // Domain = X; Range = Y
        filterPlotChannelFour.setDomainLabel(null);
        filterPlotChannelFour.setRangeLabel(null);
        filterPlotChannelFour.getGraph().getRangeGridLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelFour.getGraph().getRangeOriginLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelFour.getGraph().getDomainGridLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelFour.getGraph().getDomainOriginLinePaint().setColor(Color.TRANSPARENT);

        // Remove extraneous elements
        filterPlotChannelFour.getLayoutManager().remove(filterPlotChannelFour.getLegend());

        // Set size of plot
        SizeMetric height = new SizeMetric(1, SizeMode.FILL);
        SizeMetric width = new SizeMetric(1, SizeMode.FILL);
        filterPlotChannelFour.getGraph().setSize(new Size(height, width));

        // Set position of plot (should be tweaked in order to center chart position)
        filterPlotChannelFour.getGraph().position(0, HorizontalPositioning.ABSOLUTE_FROM_LEFT.ABSOLUTE_FROM_LEFT,
                0, VerticalPositioning.ABSOLUTE_FROM_TOP);

        // Add plot to FilterGraph
        frameLayout.addView(filterPlotChannelFour);
    }

    public void setFilterTypeNoch() {
        activeFilterNoch = new Filter_Noch(samplingRate, "bandstop", 5, 1, 6);
        filtStateNoch = new double[4][activeFilterNoch.getNB()];
    }


    public void setFilterType() {
        activeFilter = new Filter(samplingRate, "bandstop", 5, 1, 6);
        filtState = new double[4][activeFilter.getNB()];
    }


    public void setNotchFrequency(int notchFrequency) {
        this.notchFrequency = notchFrequency;
        if (dataListener != null) {
            dataListener.updateFilter(notchFrequency);
        }
    }

    class Thread1 implements Runnable {
        public void run() {

            Socket socket;
            try {
                socket = new Socket(SERVER_IP, Integer.parseInt(SERVER_PORT));
                output = new PrintWriter(socket.getOutputStream());
                input = new DataInputStream(socket.getInputStream());
               // output.write("enviar");
               // output.flush();
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        textViewStatus.setText("Connected\n");
//
//                    }
//                });
                new Thread(new Thread2()).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class Thread2 implements Runnable {

        public double[] newData;
        public Filter bandstopFilter;
        private int frameCounter = 0;


        @Override
        public void run() {

            newData = new double[4];

            while (true) {
                try {
                    final String[] message = {input.readUTF()};
                    if (message[0] != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                System.out.println("Se inicializa el socket de llegada");
                                System.out.println("server: " + message[0] + "\n");

                                newData = getEegChannelValues(message[0]);
                                if (newData.length >= 4) {
                                    filtState = activeFilter.transform(newData, filtState);
                                    filtStateNoch = activeFilterNoch.transform(newData, filtStateNoch);

                                    // vector
                                    double[] vector1 = activeFilter.extractFilteredSamples(filtState);
                                    double[] vector2 = activeFilterNoch.extractFilteredSamples(filtStateNoch);
                                    //solo 1
                                    //activeFilter.extractFilteredSamples(filtState);
                                    eegBuffer.update(sumarVectores(vector1,vector2));

                                    frameCounter++;
                                    if (frameCounter % 10 == 0) {
                                        updatePlot();
                                    }

                                    if (frameCounter % 125 == 0) {
                                        average_channel_1 = dataSeriesChannelOne.getAverage();
                                        average_channel_2 = dataSeriesChannelTwo.getAverage();
                                        average_channel_3 = dataSeriesChannelTree.getAverage();
                                        average_channel_4 = dataSeriesChannelFour.getAverage();

                                        txtAverage_channel_1.setText("Promedio: " + (average_channel_1));
                                        txtAverage_channel_2.setText("Promedio: " + (average_channel_2));
                                        txtAverage_channel_3.setText("Promedio: " + (average_channel_3));
                                        txtAverage_channel_4.setText("Promedio: " + (average_channel_4));
                                    }
                                }
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



        //private void getEegChannelValues(double[] newData, String p) {
        private double[] getEegChannelValues(String p) {
            p = p.substring(1, p.length() - 1);
            String[] values = p.split(",");
            // Validar que llegue un array de 8 elementos, si el sv envia cualquier error que no se pueda
            // hacer slit en un array tomara el valor anterior.
            if (values.length >= 4) {
                newData[0] = Double.parseDouble(values[0]) * SCALE_FACTOR_EEG;
                newData[1] = Double.parseDouble(values[1]) * SCALE_FACTOR_EEG;
                newData[2] = Double.parseDouble(values[2]) * SCALE_FACTOR_EEG;
                newData[3] = Double.parseDouble(values[3]) * SCALE_FACTOR_EEG;
            }
            return newData;
        }


        private double[] sumarVectores(double[] vector1, double[] vector2) {
            double[] vectorSuma = new double[vector1.length];
            for (int i = 0; i < vector1.length; i++) {
                vectorSuma[i]=vector1[i]+vector2[i];
            }
            return vectorSuma;
        }


        public void updateFilter(int notchFrequency) {
            if (bandstopFilter != null) {
                bandstopFilter.updateFilter(notchFrequency - 5, notchFrequency + 5);
            }
        }

    }


    class Thread3 implements Runnable {
        private String message;
        Thread3(String message) {
            this.message = message;
        }
        @Override
        public void run() {
            output.write(message);
            output.flush();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                   // tvMessages.append("server: " + message + "\n");
                   // etMessage.setText("");
                }
            });
        }
    }

    public void updatePlot() {
        int numEEGPoints = eegBuffer.getPts();
        if (dataSeriesChannelOne.size() >= PLOT_LENGTH ||
                dataSeriesChannelTwo.size() >= PLOT_LENGTH ||
                dataSeriesChannelTree.size() >= PLOT_LENGTH ||
                dataSeriesChannelFour.size() >= PLOT_LENGTH) {
            dataSeriesChannelOne.remove(numEEGPoints);
            dataSeriesChannelTwo.remove(numEEGPoints);
            dataSeriesChannelTree.remove(numEEGPoints);
            dataSeriesChannelFour.remove(numEEGPoints);
        }

        dataSeriesChannelOne.addAll(eegBuffer.extractSingleChannelTransposedAsDouble(numEEGPoints, 0));
        dataSeriesChannelTwo.addAll(eegBuffer.extractSingleChannelTransposedAsDouble(numEEGPoints, 1));
        dataSeriesChannelTree.addAll(eegBuffer.extractSingleChannelTransposedAsDouble(numEEGPoints, 2));
        dataSeriesChannelFour.addAll(eegBuffer.extractSingleChannelTransposedAsDouble(numEEGPoints, 3));

        eegBuffer.resetPts();
        filterPlotChannelOne.redraw();
        filterPlotChannelTwo.redraw();
        filterPlotChannelTree.redraw();
        filterPlotChannelFour.redraw();

    }
}