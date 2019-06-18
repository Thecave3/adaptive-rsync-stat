package com.aenduo.health.sdkexample;

import com.aenduo.health.sdkexample.errors.CorruptFileFormatException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static com.aenduo.health.sdkexample.ChunkSizeCalculator.adaptiveChunkSizeCalculator;
import static com.aenduo.health.sdkexample.ChunkSizeCalculator.extractTokenIndexVector;

public class Main {

    private final static String STANDARD_CHUNK_SIZE_FILE_PATH = "standard.txt";
    private final static String OPTIMIZE_CHUNK_SIZE_FILE_PATH = "optimized_new.txt";
    private final static int ITERATIONS = 100;
    private final static String OCTODIFF_PATH = "octodiff";
    private final static String SIGNATURE_PARAM = "signature";
    private final static String DELTA_PARAM = "delta";
    private static FileWriter frOpt, frStd;

    public static void main(String[] args) {

        int chunkSize = 2048;
        String newFilePath = ".\\files\\file_";
        String signatureFilePath;
        String deltaFilePath;
        double dataToSave;
        try {
            /*
            frStd = new FileWriter(new File(STANDARD_CHUNK_SIZE_FILE_PATH));

            for (int i = 0; i < ITERATIONS - 1; i++) {
                String tempFilePath = newFilePath + i;
                signatureFilePath = createSignatureFromFile(tempFilePath, chunkSize, i);
                tempFilePath = newFilePath + (i + 1);
                deltaFilePath = createDeltaFromFile(signatureFilePath, tempFilePath, i);

                dataToSave = (double) new File(deltaFilePath).length() / (double) new File(tempFilePath).length();
                saveStandardData(i, dataToSave);
                if (i % 10 == 0)
                    System.out.println("STD: " + (i) + "%");
            }

            frStd.close();
            */
            frOpt = new FileWriter(new File(OPTIMIZE_CHUNK_SIZE_FILE_PATH));
            newFilePath = ".\\files\\file_";

            int lastChunkSize;
            for (int i = 0; i < ITERATIONS - 1; i++) {
                String tempFilePath = newFilePath + i;
                signatureFilePath = createSignatureFromFile(tempFilePath, chunkSize, i);
                tempFilePath = newFilePath + (i + 1);
                deltaFilePath = createDeltaFromFile(signatureFilePath, tempFilePath, i);

                tempFilePath = newFilePath + i;

                lastChunkSize = adaptiveChunkSizeCalculator(chunkSize, extractTokenIndexVector(deltaFilePath, chunkSize), 0.5);
                System.out.println(chunkSize);

                signatureFilePath = createSignatureFromFile(tempFilePath, chunkSize, i);
                tempFilePath = newFilePath + (i + 1);
                deltaFilePath = createDeltaFromFile(signatureFilePath, tempFilePath, i);

                //dataToSave = (double) new File(deltaFilePath).length() / (double) new File(tempFilePath).length();
                dataToSave = lastChunkSize;
                saveOptimizeData(i, dataToSave);

                if (i % 10 == 0)
                    System.out.println("OPT: " + i + "%");
            }
            frOpt.close();
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

    private static void launchOctodiff(String command, int i, String... param) throws IOException {
        try {
            Process process;
            process = new ProcessBuilder(OCTODIFF_PATH, command, param[0], param[1]).start();
            process.waitFor();
            if (process.exitValue() != 0) {
                System.out.println("Exit value: " + process.exitValue() + ", iterazione " + i);
                System.out.println(OCTODIFF_PATH + " " + command + " " + param[0] + " " + param[1]);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void saveOptimizeData(int iteration, double sizeDelta) throws IOException {
        frOpt.append(String.valueOf(sizeDelta)).append(" ");
    }

    private static void saveStandardData(int iteration, double sizeDelta) throws IOException {
        frStd.append(String.valueOf(sizeDelta)).append(" ");
    }

    private static String convertToKB(double sizeDelta) {
        return String.valueOf(sizeDelta / (double) 1000);
    }
}
