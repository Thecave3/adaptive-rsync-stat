package com.aenduo.health.sdkexample;

import com.aenduo.health.sdkexample.errors.CorruptFileFormatException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class DeltaReader {

    private File deltaFile;
    private long chunkSize;

    public DeltaReader(String deltaFilePath, long chunkSize) {
        this.deltaFile = new File(deltaFilePath);
        this.chunkSize = chunkSize;
    }

    public ArrayList<Long> read() throws IOException, CorruptFileFormatException {
        if (!deltaFile.exists())
            throw new FileNotFoundException();
        BinaryDeltaReader reader = new BinaryDeltaReader(deltaFile, chunkSize);
        return reader.Apply();
    }
}
