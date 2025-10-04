package com.example.bankcards.exception.customexceptions;

public class CorruptedDataException extends RuntimeException {
    public CorruptedDataException(String message) {
        super(message);
    }
}
