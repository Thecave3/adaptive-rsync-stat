package com.aenduo.health.sdkexample;

import com.aenduo.health.sdkexample.errors.CorruptFileFormatException;

import java.io.IOException;
import java.util.ArrayList;

public class ChunkSizeCalculator {

    private static final String TAG_UTIL = ChunkSizeCalculator.class.getSimpleName();

    /**
     * Algorithm 1: Procedure for adapting the chunk size
     *
     * @param dt current chunk size
     * @param pt token index vector
     * @param mu parametro adattamento (o <= mu <= 1)
     * @return new adaptive chunk size
     */
    public static int adaptiveChunkSizeCalculator(int dt, long[] pt, double mu) {
        double wSum = 0; // chunk estimate sum
        int w = 0; // step counter
        int nac = 0; // Number of consecutive, unmodified chunks
        int M = pt.length;
        double dt1w;
        long fii;

        for (int m = 2, i, nud; m < M; m++) {
            i = m - 1;
            fii = pt[m] - pt[i]; // Token difference
            if (fii == dt) {
                nac++;
            }
            else {
                if (nac > 0) {
                    w++;
                    dt1w = dt + mu * nac;
                    wSum += dt1w;
                    nac = 0;
                }
                nud = (int) Math.ceil(((double) fii / (double) dt) - 1); // No updates detected
                w++;
                dt1w = dt - mu * nud;// partial chunk estimate
                wSum += dt1w;
            }
        }
        if (nac > 0 && w == 0) {
            //help
        }
        else if (nac == 0 && w == 0)
            return dt;
        return (int) wSum / w; // dt1 = final chunk estimate
    }


    public static long[] extractTokenIndexVector(String deltaFilePath, int deltaChunkSize) throws IOException, CorruptFileFormatException {
        ArrayList<Long> tokenIndexVector;

        DeltaReader reader = new DeltaReader(deltaFilePath, deltaChunkSize);
        tokenIndexVector = reader.read();

        long[] retArray = new long[tokenIndexVector.size()];
        int count = 0;
        for (Long i : tokenIndexVector) {
            retArray[count] = i;
            count++;
        }

        return retArray;
    }
}
