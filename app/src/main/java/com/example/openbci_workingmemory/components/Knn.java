package com.example.openbci_workingmemory.components;

import java.util.List;

public class Knn {

    double[] originalSignalClassOne;
    double[] originalSignalClassTwo;
    double[] originalSignalClassThree;
    int chanelOfInteres = 2;
    int sampleLength = 1760;
    double umbral = 0.7;

    private List data;
    private DTW dtw = new DTW();

    public Knn(double[] originalSignalClassOne, double[] originalSignalClassTwo, double[] originalSignalClassThree, double umbral) {

        this.originalSignalClassOne = originalSignalClassOne;
        this.originalSignalClassTwo = originalSignalClassTwo;
        this.originalSignalClassThree = originalSignalClassThree;
        this.umbral = umbral * 0.01;

    }

    public String evaluateBlink(DynamicSeries dataSeries) {

        double[][] distancesBlink = new double[150][150];
        float[] pSample = dataSeries.getPSample();

        for (int i = 0; i < 10; i++) {

            distancesBlink[1][i] = dtw.compute(pSample, getSampleRange(originalSignalClassOne, i)).getDistance();
            distancesBlink[2][i] = 0;

            distancesBlink[1][i + 10] = dtw.compute(pSample, getSampleRange(originalSignalClassTwo, i)).getDistance();
            distancesBlink[2][i + 10] = 1;

            distancesBlink[1][i + 20] = dtw.compute(pSample, getSampleRange(originalSignalClassThree, i)).getDistance();
            distancesBlink[2][i + 20] = 2;
        }

        double temp = 0;

        for (int j = 0; j < 28; j++) {
            for (int i = 0; i < 28; i++) {
                if (distancesBlink[1][i] > distancesBlink[1][i + 1]) {
                    temp = distancesBlink[1][i];
                    distancesBlink[1][i] = distancesBlink[1][i + 1];
                    distancesBlink[1][i + 1] = temp;
                    distancesBlink[1][i + 1] = temp;
                    temp = distancesBlink[2][i];
                    distancesBlink[2][i] = distancesBlink[2][i + 1];
                    distancesBlink[2][i + 1] = temp;
                    distancesBlink[2][i + 1] = temp;
                }
            }
        }

        int k = 20;

        double numberOfClassOnes = 0;
        double numberOfClassTwos = 0;
        double numberOfClassThrees = 0;

        for (int i = 0; i < 5; i++) {

            if (distancesBlink[2][i] == 0)
                numberOfClassOnes = numberOfClassOnes + 100/k;
            if (distancesBlink[2][i] == 1)
                numberOfClassTwos = numberOfClassTwos +  100/k ;
            if (distancesBlink[2][i] == 2)
                numberOfClassThrees = numberOfClassThrees + 100/k ;

        }

        System.out.println("Numero de clase 1: " + numberOfClassOnes);
        System.out.println("Numero de clase 2: " + numberOfClassTwos);
        System.out.println("Numero de clase 3: " + numberOfClassThrees);
        System.out.println("Numero umbral: " + umbral);
        System.out.println("Numero de vecinos: " + k);


        return ""+numberOfClassOnes+" - "+numberOfClassTwos+" - "+numberOfClassThrees;
    }

    public float[] getPSample(double[][] doubleList, int chanelOfInteres) {

        float[] pSample = new float[15300];
        for (int i = 0; i < 15300; i++) {
            pSample[i] = (float) doubleList[i][chanelOfInteres];
        }
        return pSample;
    }

    public float[] getSampleRange(double[] signal, int sampleNumber) {
        float[] sample = new float[sampleLength];
        int sampleStart = (sampleNumber * sampleLength) - sampleLength;
        if (sampleStart < 0)
            sampleStart = 0;
        for (int i = 0; i < sampleLength; i++) {
            if (signal != null)
                sample[i] = (float) signal[i + sampleStart];
        }
        return sample;
    }

}