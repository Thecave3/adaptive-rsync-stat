package com.aenduo.health.sdkexample;


import com.aenduo.health.sdkexample.errors.CorruptFileFormatException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class BinaryDeltaReader {

    // We omit the conversion of the progress reporter since we do not care
    private final File file;
    private String TAG = "OUD";
    private ByteBuffer buffer;
    private byte[] expectedHash;
    private String hashAlgorithm;
    private boolean hasReadMetadata;
    private long chunkSize;
    private ArrayList<Long> tokenArray;

    public BinaryDeltaReader(File file, long chunkSize) {
        this.file = file;
        this.chunkSize = chunkSize;
        tokenArray = new ArrayList<>();
    }

    private final char[] hexArray = "0123456789ABCDEF".toCharArray();

    public String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }


    public byte[] getExpectedHash() throws CorruptFileFormatException, IOException {
        EnsureMetadata();
        return expectedHash;

    }

    public String getHashAlgorithm() throws CorruptFileFormatException, IOException {
        EnsureMetadata();
        return hashAlgorithm;
    }

    public ArrayList<Long> getTokenArray() throws CorruptFileFormatException, IOException {
        EnsureMetadata();
        return tokenArray;
    }


    private void EnsureMetadata() throws CorruptFileFormatException, IOException {
        if (hasReadMetadata)
            return;

        int size = (int) file.length();
        byte[] bytes = new byte[size];
        BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
        buf.read(bytes, 0, bytes.length);
        buf.close();


        buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        byte[] first = new byte[BinaryFormat.DeltaHeader.length];
        buffer.get(first);

        // Corruption test of the header
        if (!Arrays.equals(first, BinaryFormat.DeltaHeader))
            throw new CorruptFileFormatException("The delta file appears to be corrupt.");

        byte version = buffer.get();
        if (version != BinaryFormat.Version)
            throw new CorruptFileFormatException("The delta file uses a newer file format than this program can handle.");

        int hashNameLength = buffer.get();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < hashNameLength; i++) {
            builder.append(new String(new byte[]{buffer.get()}, StandardCharsets.US_ASCII));
        }

        hashAlgorithm = builder.toString();
        // Log.d("OUD: ","HashAlgoritm: " + hashAlgorithm);
        // Log.d("OUD: ","Posizione: " + buffer.position() + ", mancanti " + buffer.remaining());

        int hashLength = buffer.getInt();
        // Log.d("OUD: ","HashLength: " + hashLength);
        // Log.d("OUD: ","Posizione: " + buffer.position() + ", mancanti " + buffer.remaining());
        expectedHash = new byte[hashLength];
        buffer.get(expectedHash);

        // Log.d(TAG, "EnsureMetadata: " + expectedHash.length);
        // Log.d(TAG, "EnsureMetadata: " + bytesToHex(expectedHash));

        byte[] endOfMeta = new byte[BinaryFormat.EndOfMetadata.length];
        buffer.get(endOfMeta);
        // Log.d(TAG, "EnsureMetadata: " + endOfMeta.length);

        // Log.d(TAG, "EnsureMetadata: " + bytesToHex(endOfMeta));
        if (!Arrays.equals(BinaryFormat.EndOfMetadata, endOfMeta)) {
            //  Log.d(TAG, "EnsureMetadata: "+ buffer.position());
            throw new CorruptFileFormatException("The signature file appears to be corrupt.");
        }
        hasReadMetadata = true;
    }


    ArrayList<Long> Apply() throws CorruptFileFormatException, IOException {
        EnsureMetadata();
        long pair = 0;
        long lastIndex = 0;
        while (buffer.hasRemaining()) {
            byte b = buffer.get();

            // Log.d("OUD: ","Applying delta " + buffer.position() + ", " + buffer.limit());

            if (b == BinaryFormat.CopyCommand) {
                long start = buffer.getLong();
                long length = buffer.getLong();
                // Log.d("OUD: ","Copy from " + start + " for " + length);
                long numIndex ;
                if (length % chunkSize != 0) {
                    if (!buffer.hasRemaining()) numIndex = length / chunkSize;
                    else numIndex = length / chunkSize -1 ;
                }
                else {
                    numIndex = length / chunkSize -1;
                }
                for (long i = 0; i < numIndex; i++) {
                    tokenArray.add(start + i * chunkSize);
                }
            } else if (b == BinaryFormat.DataCommand) {
                long length = buffer.getLong();
                long soFar = 0;
                while (soFar < length) {
                    byte[] bytes = new byte[(int) Math.min(length - soFar, 1024 * 1024 * 4)];
                    buffer.get(bytes);
                    soFar += bytes.length;
                    // Log.d("OUD: ", "Write data" + Arrays.toString(bytes));
                }
            } else {
                // Log.d(TAG, "Apply: Command not detected");
            }
        }
        return tokenArray;
    }
}