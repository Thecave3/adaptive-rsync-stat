package com.aenduo.health.sdkexample;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static com.aenduo.health.sdkexample.ChunkSizeCalculator.adaptiveChunkSizeCalculator;
import static com.aenduo.health.sdkexample.ChunkSizeCalculator.extractTokenIndexVector;

public class Main {

    private final static String STANDARD_CHUNK_SIZE_FILE_PATH = "standard.txt";
    private final static String OPTIMIZE_CHUNK_SIZE_FILE_PATH = "optimized.txt";
    private final static int ITERATIONS = 100;
    private final static String OCTODIFF_PATH = "C:\\";
    private final static String SIGNATURE_PARAM = "signature";
    private final static String DELTA_PARAM = "delta";


    public static void main(String[] args) {

        int chunkSize = 2048;
        String newFilePath = "file_0";
        String signatureFilePath;
        String deltaFilePath;

        try {
            for (int i = 1; i < ITERATIONS; i++) {
                signatureFilePath = createSignatureFromFile(newFilePath, chunkSize);
                deltaFilePath = createDeltaFromFile(signatureFilePath, newFilePath);
                saveStandardData(i, new File(deltaFilePath).length());
                newFilePath = newFilePath.substring(0, newFilePath.length() - 1).concat("" + i);
            }

            newFilePath = "file_0";

            for (int i = 1; i < ITERATIONS; i++) {
                signatureFilePath = createSignatureFromFile(newFilePath, chunkSize);
                deltaFilePath = createDeltaFromFile(signatureFilePath, newFilePath);

                chunkSize = adaptiveChunkSizeCalculator(chunkSize, extractTokenIndexVector(deltaFilePath, chunkSize), 0.5);

                signatureFilePath = createSignatureFromFile(newFilePath, chunkSize);
                deltaFilePath = createDeltaFromFile(signatureFilePath, newFilePath);

                saveOptimizeData(i, new File(deltaFilePath).length());
                newFilePath = newFilePath.substring(0, newFilePath.length() - 1).concat("" + i);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String createSignatureFromFile(String newFilePath, int chunkSize) throws IOException {
        // octodiff signature MyApp.nupkg MyApp.nupkg.octosigs
        launchOctodiff(SIGNATURE_PARAM, newFilePath, "--chunk-size=" + chunkSize);
        return newFilePath + ".octosig";
    }


    private static String createDeltaFromFile(String signatureFilePath, String newFilePath) throws IOException {
        // octodiff delta MyApp.1.0.nupkg.octosig MyApp.1.1.nupkg MyApp.1.0_to_1.1.octodelta
        launchOctodiff(DELTA_PARAM, signatureFilePath, newFilePath);
        return newFilePath + ".octodelta";
    }

    private static void launchOctodiff(String command, String... param) throws IOException {
        Process process;
        process = new ProcessBuilder(OCTODIFF_PATH, command, param[0], param[1]).start();


        while (process.isAlive())
            System.out.println("Running");

        System.out.println("Exit value: " + process.exitValue());
    }

    private static void saveOptimizeData(int iteration, long sizeDelta) throws IOException {
        FileWriter fr = new FileWriter(new File(OPTIMIZE_CHUNK_SIZE_FILE_PATH));
        fr.append(String.valueOf(sizeDelta)).append(",");
        fr.close();
    }

    private static void saveStandardData(int iteration, long sizeDelta) throws IOException {
        FileWriter fr = new FileWriter(new File(STANDARD_CHUNK_SIZE_FILE_PATH));
        fr.append(String.valueOf(sizeDelta)).append(",");
        fr.close();
    }
}
