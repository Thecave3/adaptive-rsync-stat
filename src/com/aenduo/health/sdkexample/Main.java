package com.aenduo.health.sdkexample;

import com.aenduo.health.sdkexample.errors.CorruptFileFormatException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static com.aenduo.health.sdkexample.ChunkSizeCalculator.adaptiveChunkSizeCalculator;
import static com.aenduo.health.sdkexample.ChunkSizeCalculator.extractTokenIndexVector;

public class Main {

    private final static String STANDARD_CHUNK_SIZE_FILE_PATH = "standard.txt";
    private final static String OPTIMIZE_CHUNK_SIZE_FILE_PATH = "optimized.txt";
    private final static int ITERATIONS = 1000;
    private final static String OCTODIFF_PATH = "octodiff";
    private final static String SIGNATURE_PARAM = "signature";
    private final static String DELTA_PARAM = "delta";
    private static FileWriter frOpt, frStd;

    public static void main(String[] args) {

        int chunkSize = 2048;
        String newFilePath = "C:\\Users\\gigi_\\Desktop\\files\\test1000\\file_";
        String signatureFilePath;
        String deltaFilePath;

        try {
            /*
            frStd = new FileWriter(new File(STANDARD_CHUNK_SIZE_FILE_PATH));

            for (int i = 0; i < ITERATIONS - 1; i++) {
                String tempFilePath = newFilePath + i;
                signatureFilePath = createSignatureFromFile(tempFilePath, chunkSize, i);
                tempFilePath = newFilePath + (i + 1);
                deltaFilePath = createDeltaFromFile(signatureFilePath, tempFilePath, i);
                saveStandardData(i, new File(deltaFilePath).length());
            }

            frStd.close();
            */
            frOpt = new FileWriter(new File(OPTIMIZE_CHUNK_SIZE_FILE_PATH));
            newFilePath = "C:\\Users\\gigi_\\Desktop\\files\\test1000\\file_";

            int lastChunkSize;
            for (int i = 0; i < ITERATIONS - 1; i++) {
                String tempFilePath = newFilePath + i;
                signatureFilePath = createSignatureFromFile(tempFilePath, chunkSize, i);
                tempFilePath = newFilePath + (i + 1);
                deltaFilePath = createDeltaFromFile(signatureFilePath, tempFilePath, i);

                tempFilePath = newFilePath + i;

                lastChunkSize = adaptiveChunkSizeCalculator(chunkSize, extractTokenIndexVector(deltaFilePath, chunkSize), 0.5);
                if (lastChunkSize != 0)
                    chunkSize = lastChunkSize;

                //System.out.println(chunkSize);

                signatureFilePath = createSignatureFromFile(tempFilePath, chunkSize, i);
                tempFilePath = newFilePath + (i + 1);
                deltaFilePath = createDeltaFromFile(signatureFilePath, tempFilePath, i);

                saveOptimizeData(i, new File(deltaFilePath).length());
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
        Process process;
        process = new ProcessBuilder(OCTODIFF_PATH, command, param[0], param[1]).start();

        while (process.isAlive()) {
            continue;
        }
        if (process.exitValue() != 0) {
            System.out.println("Exit value: " + process.exitValue() + ", iterazione " + i);
            System.out.println(OCTODIFF_PATH + " " + command + " " + param[0] + " " + param[1]);

        }
    }

    private static void saveOptimizeData(int iteration, long sizeDelta) throws IOException {
        frOpt.append(String.valueOf(((double) sizeDelta) / (double) 1000)).append(" ");
    }

    private static void saveStandardData(int iteration, long sizeDelta) throws IOException {
        frStd.append(String.valueOf(((double) sizeDelta) / (double) 1000)).append(" ");
    }
}
