package com.example.openbci_workingmemory.components;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;

import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;

/**
 * Writes EEG data (either raw/filtered EEG or computed FFT) into a csv. Presents a toast when
 * recording is started and starts sharing intent for sending data to email when recording is
 * completed
 */

public class EEGFileWriter {

    // ---------------------------------------------------------------------------
    // Variables

    private Context context;
    StringBuilder builder;
    int fileNum = 1;
    public FileWriter fileWriter;
    private static boolean isRecording;
    int numberOfLines = 0;

    // ---------------------------------------------------------------------------
    // Constructor

    public EEGFileWriter(Context context, String title) {
        this.context = context;
        isRecording = false;
    }

    // ---------------------------------------------------------------------------
    // Internal methods

    public void initFile() {
        builder = new StringBuilder();
        isRecording = true;
    }

    public void addDataToFile(double[] data) {
        numberOfLines = numberOfLines + 1;
        // Append timestamp
        /*Long tsLong = System.currentTimeMillis();
        builder.append(tsLong.toString() + ",");*/
        for (int j = 0; j < data.length; j++) {
            builder.append("" + (data[j]));
            if (j < data.length - 1) {
                builder.append(",");
            }
        }
        builder.append("\n");
    }

    public void addLineToFile(String line) {
        builder.append(line);
        builder.append("\n");
    }

    public int numberOfLines() {
        return numberOfLines;
    }

    public void writeFile(String title) {
        try {
            final File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

            final File file = new File(dir, title + fileNum + ".json");
            fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(builder.toString());
            bufferedWriter.close();
            sendData(file);
            fileNum++;
            isRecording = false;
        } catch (IOException e) {
        }
    }


    public void writeClassOneFile() {
        try {
            final File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

            final File file = new File(dir, "ClassOneDB.json");
            fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(builder.toString());
            bufferedWriter.close();
            sendData(file);
            fileNum++;
            isRecording = false;
        } catch (IOException e) {
        }
    }

    public void writeClassTwoFile() {
        try {
            final File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

            final File file = new File(dir, "ClassTwoDB.json");
            fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(builder.toString());
            bufferedWriter.close();
            sendData(file);
            fileNum++;
            isRecording = false;
        } catch (IOException e) {
        }
    }

    public void writeClassThreeFile() {
        try {
            final File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

            final File file = new File(dir, "ClassThreeDB.json");
            fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(builder.toString());
            bufferedWriter.close();
            sendData(file);
            fileNum++;
            isRecording = false;
        } catch (IOException e) {
        }
    }


    public void writeConfigurationsFile() {
        try {
            final File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

            final File file = new File(dir, "Configurations" + fileNum + ".json");
            fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(builder.toString());
            bufferedWriter.close();
            sendData(file);
            fileNum++;
            isRecording = false;
        } catch (IOException e) {
        }
    }


    public void writeFileDataSet() {
        try {
            final File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

            final File file = new File(dir, "Dataset" + fileNum + ".json");
            fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(builder.toString());
            bufferedWriter.close();
            sendData(file);
            fileNum++;
            isRecording = false;
        } catch (IOException e) {
        }
    }


    public void writeClassOneFileDataSet() {
        try {
            final File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

            final File file = new File(dir, "ClassOneDataSet.json");
            fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(builder.toString());
            bufferedWriter.close();
            sendData(file);
            fileNum++;
            isRecording = false;
        } catch (IOException e) {
        }
    }

    public void writeClassTwoFileDataSet() {
        try {
            final File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

            final File file = new File(dir, "ClassTwoDataSet.json");
            fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(builder.toString());
            bufferedWriter.close();
            sendData(file);
            fileNum++;
            isRecording = false;
        } catch (IOException e) {
        }
    }


    public void sendData(File dataCSV) {

        //FileProvider fileProvider = new FileProvider();
        //Intent sendIntent = new Intent();
        //sendIntent.setAction(Intent.ACTION_SEND);
        //sendIntent.setType("application/csv");
        //sendIntent.putExtra(Intent.EXTRA_STREAM, fileProvider.getUriForFile(this.context, "com.eeg_project.fileprovider", dataCSV));
        //context.startActivity(Intent.createChooser(sendIntent, "Export data to..."));
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void writeToFile(String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("config.csv", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public void addLineToFile(String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("config.csv", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public void readFile(String title) {
        try {

            final File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "filename");

            FileReader fileReader = new FileReader(file);

            BufferedReader readeasr = new BufferedReader(fileReader);

        } catch (IOException e) {
        }
    }

    public void cleanFile() {
        int builderLeng = builder.length();
        builder.delete(0,builderLeng);
    }
}
