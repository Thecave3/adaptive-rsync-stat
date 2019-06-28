package com.aenduo.health.sdkexample;

import com.aenduo.health.sdkexample.errors.CorruptFileFormatException;

import java.io.*;
import java.util.Scanner;

import static com.aenduo.health.sdkexample.ChunkSizeCalculator.adaptiveChunkSizeCalculator;
import static com.aenduo.health.sdkexample.ChunkSizeCalculator.extractTokenIndexVector;

public class Main {

    private final static String STANDARD_DELTA_SIZE_FILE_PATH = "standard_delta.txt";
    private final static String OPTIMIZE_DELTA_SIZE_FILE_PATH = "optimized_delta.txt";
    private final static String STANDARD_CHUNK_SIZE_FILE_PATH = "standard_chnk.txt";
    private final static String OPTIMIZE_CHUNK_SIZE_FILE_PATH = "optimized_chnk.txt";

    private final static String OPTIMIZE_EFF_FILE_PATH = "optimized_eff.txt";
    private final static String STANDARD_EFF_FILE_PATH = "standard_eff.txt";

    private final static String EFF_FILE_PATH = "efficiency.txt";
    private final static int ITERATIONS = 24;
    private final static String OCTODIFF_PATH = "octodiff";
    private final static String SIGNATURE_PARAM = "signature";
    private final static String DELTA_PARAM = "delta";
    private final static String PATCH_PARAM = "patch";
    private static FileWriter fwOpt, fwStd, fwChnkOpt, fwChnkStd, fwEff;

    public static void main(String[] args) {

        int chunkSize = 2048;
        String newFilePath = ".\\files\\Gruppo2\\file_ (";
        String signatureFilePath;
        String deltaFilePath;
        double dataToSave;
        try {
            /*
            for (int i = 1; i < ITERATIONS; i++) {

                String tempFilePath;
                if(i == 1) {
                    tempFilePath =  ".\\files\\Gruppo2\\file_ (1).edf";
                }
                else {
                    tempFilePath = newFilePath + i  + ").rdf";
                }
                deltaFilePath = newFilePath + (i+1) + ").edf";
                //deltaFilePath = createDeltaFromFile(signatureFilePath, tempFilePath, i);
                //dataToSave = (double) new File(deltaFilePath).length() / (double) new File(tempFilePath).length();
                String newFile = patchFile(tempFilePath,deltaFilePath,i);
                System.out.println(newFile);
                //if (i % (ITERATIONS / 10) == 0)
                //    System.out.println("STD: " + (i / (ITERATIONS / 100)) + "%");
            }*/

            fwStd = new FileWriter(new File(STANDARD_DELTA_SIZE_FILE_PATH));
            fwChnkStd = new FileWriter(new File(STANDARD_CHUNK_SIZE_FILE_PATH));

            for (int i = 1; i < ITERATIONS; i++) {
                String tempFilePath = newFilePath + i + ").rdf";
                signatureFilePath = createSignatureFromFile(tempFilePath, chunkSize, i);
                tempFilePath = newFilePath + (i + 1) + ").rdf";
                deltaFilePath = createDeltaFromFile(signatureFilePath, tempFilePath, i);
                //dataToSave = (double) new File(deltaFilePath).length() / (double) new File(tempFilePath).length();
                dataToSave = (double) new File(deltaFilePath).length();
                saveStandardData(dataToSave);
                saveStandardChunkData(chunkSize);

                //if (i % (ITERATIONS / 10) == 0)
                //    System.out.println("STD: " + (i / (ITERATIONS / 100)) + "%");
            }

            fwChnkStd.close();
            fwStd.close();

            fwOpt = new FileWriter(new File(OPTIMIZE_DELTA_SIZE_FILE_PATH));
            fwChnkOpt = new FileWriter(new File(OPTIMIZE_CHUNK_SIZE_FILE_PATH));
            newFilePath = ".\\files\\Gruppo2\\file_ (";

            int lastChunkSize;
            for (int i = 1; i < ITERATIONS; i++) {
                String tempFilePath = newFilePath + i + ").rdf";
                signatureFilePath = createSignatureFromFile(tempFilePath, chunkSize, i);
                tempFilePath = newFilePath + (i + 1) + ").rdf";
                deltaFilePath = createDeltaFromFile(signatureFilePath, tempFilePath, i);

                lastChunkSize = adaptiveChunkSizeCalculator(chunkSize, extractTokenIndexVector(deltaFilePath, chunkSize), 0.5);
                //System.out.println(chunkSize);
                chunkSize = lastChunkSize;

                tempFilePath = newFilePath + i + ").rdf";
                signatureFilePath = createSignatureFromFile(tempFilePath, chunkSize, i);
                tempFilePath = newFilePath + (i + 1) + ").rdf";
                deltaFilePath = createDeltaFromFile(signatureFilePath, tempFilePath, i);

                //dataToSave = (double) new File(deltaFilePath).length() / (double) new File(tempFilePath).length();
                dataToSave = (double) new File(deltaFilePath).length();
                saveOptimizeData(dataToSave);
                saveOptimizeChunkData(chunkSize);

                //if (i % (ITERATIONS / 10) == 0)
                //    System.out.println("OPT: " + (i / (ITERATIONS / 100)) + "%");
            }

            fwChnkOpt.close();
            fwOpt.close();

            System.out.println("Generating efficiency file");
            fwEff = new FileWriter(new File(EFF_FILE_PATH));
            Scanner frStdDelta = new Scanner(new File(STANDARD_DELTA_SIZE_FILE_PATH));
            Scanner frOptDelta = new Scanner(new File(OPTIMIZE_DELTA_SIZE_FILE_PATH));

            frStdDelta.useDelimiter(" ");
            frOptDelta.useDelimiter(" ");

            double standard, optimized;
            for (int i = 0; i < ITERATIONS && frOptDelta.hasNext() && frStdDelta.hasNext(); i++) {
                standard = Double.valueOf(frStdDelta.next());
                optimized = Double.valueOf(frOptDelta.next());
                fwEff.append(String.valueOf(optimized / standard)).append(" ");
            }

            fwEff.close();
        } catch (IOException | CorruptFileFormatException e) {
            e.printStackTrace();
        }
    }

    private static String createSignatureFromFile(String newFilePath, int chunkSize, int i) throws IOException {
        // octodiff signature MyApp.nupkg MyApp.nupkg.octosig
        launchOctodiff(SIGNATURE_PARAM, i, newFilePath, "--chunk-size=" + chunkSize);
        return newFilePath + ".octosig";
    }


    private static String createDeltaFromFile(String signatureFilePath, String newFilePath, int i) throws IOException {
        // octodiff delta MyApp.1.0.nupkg.octosig MyApp.1.1.nupkg MyApp.1.0_to_1.1.octodelta
        launchOctodiff(DELTA_PARAM, i, signatureFilePath, newFilePath);
        return newFilePath + ".octodelta";
    }

    private static String patchFile(String oldFilePath, String deltaFilePath, int i) throws IOException {
        // octodiff delta MyApp.1.0.nupkg.octosig MyApp.1.1.nupkg MyApp.1.0_to_1.1.octodelta
        String newFilePath = deltaFilePath.substring(0,deltaFilePath.length() - 3) + "rdf";
        launchOctodiff(PATCH_PARAM, i, oldFilePath, deltaFilePath, newFilePath);
        return newFilePath;
    }

    private static void launchOctodiff(String command, int i, String... param) throws IOException {
        try {
            Process process;
            if(param.length == 3) process = new ProcessBuilder(OCTODIFF_PATH, command, param[0], param[1],param[2]).start();
            else process = new ProcessBuilder(OCTODIFF_PATH, command, param[0], param[1]).start();
            process.waitFor();
            if (process.exitValue() != 0) {
                System.out.println("Exit value: " + process.exitValue() + ", iterazione " + i);
                System.out.println(OCTODIFF_PATH + " " + command + " " + param[0] + " " + param[1]);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void saveOptimizeChunkData(int chunkSize) throws IOException {
        fwChnkOpt.append(String.valueOf(chunkSize)).append(" ");
    }

    private static void saveOptimizeData(double sizeDelta) throws IOException {
        fwOpt.append(String.valueOf(sizeDelta)).append(" ");
    }


    private static void saveStandardChunkData(int chunkSize) throws IOException {
        fwChnkStd.append(String.valueOf(chunkSize)).append(" ");
    }

    private static void saveStandardData(double sizeDelta) throws IOException {
        fwStd.append(String.valueOf(sizeDelta)).append(" ");
    }

    private static String convertToKB(double sizeDelta) {
        return String.valueOf(sizeDelta / (double) 1000);
    }
}
