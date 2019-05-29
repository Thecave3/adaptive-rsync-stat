package com.aenduo.health.sdkexample.errors;

public class CorruptFileFormatException extends Throwable {
    public CorruptFileFormatException(String error) {
        System.out.println(error);
    }
}
