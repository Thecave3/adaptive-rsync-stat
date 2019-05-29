package com.aenduo.health.sdkexample;

import java.nio.charset.StandardCharsets;

class BinaryFormat {
    public static final byte[] SignatureHeader = "OCTOSIG".getBytes(StandardCharsets.US_ASCII);
    public static final byte[] DeltaHeader = "OCTODELTA".getBytes(StandardCharsets.US_ASCII);
    public static final byte[] EndOfMetadata = ">>>".getBytes(StandardCharsets.US_ASCII);
    public static final byte CopyCommand = 0x60;
    public static final byte DataCommand = (byte) 0x80;
    public static final byte Version = 0x01;
}