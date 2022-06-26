package com.example.openbci_workingmemory.components;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads EEG data from CSV files
 */

public class EEGFileReader {

    // ---------------------------------------------------------------------------
    // Variables

    FileReader inputStream;
    //InputStream inputStream;
    private Context context;
    List<double[]> readList;
    boolean fileWasRead = false;

    public EEGFileReader(FileReader inputStream) {
        //    public EEGFileReader(InputStream inputStream) {
        this.inputStream = inputStream;
        this.context = context;
    }

    public List read() throws IOException {
        List<double[]> resultList = new ArrayList();
        BufferedReader reader = new BufferedReader(inputStream);
        //BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String csvLine;
        while ((csvLine = reader.readLine()) != null) {
            String[] stringLine = csvLine.split(",");
            double[] line = new double[stringLine.length];
            for (int i = 0; i < line.length; i++) {
                line[i] = Double.parseDouble(stringLine[i]);
            }
            resultList.add(line);
        }

        return resultList;
    }

/*    public double[][] readToArray() {
        readList = read();
        int len = readList.size();

        double[][] readArray = new double[readList.size()][8];
        System.out.println("tamaño ---------"+readArray.length);
        for (int i = 0; i < len; i++) {
            readArray[i] = readList.get(i);
        }

        return readArray;
    }*/

    public double[] readToVector(int channelOfInterest) {

        try {
            if (!fileWasRead) {
                readList = read();
                fileWasRead = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        int len = readList.size();

        double[] readArray = new double[readList.size()];

        for (int i = 0; i < len; i++) {
            readArray[i] = (float) readList.get(i)[channelOfInterest];
        }
            return readArray;
        }

    /*public float[] readNoneBlink() {
        readList = read();
        int len = readList.size();

        float[] readArray = new float[readList.size()];

        for (int i = 0; i < len; i++) {
            readArray[i] = (float) 0;
        }

        return readArray;
    }*/

/*    public int[] readConfigurations() {
        readList = read();
        int len = readList.size();

        int[] readArray = new int[readList.size()];

        for (int i = 0; i < len; i++) {
            readArray[i] = (int) readList.get(i)[0];
        }

        return readArray;
    }*/
    }
