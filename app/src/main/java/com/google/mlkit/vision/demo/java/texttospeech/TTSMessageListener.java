package com.google.mlkit.vision.demo.java.texttospeech;

public interface TTSMessageListener {
    void messageReceived(String message, boolean isFromCountdown);
}
