package com.google.mlkit.vision.demo.java.texttospeech;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

public class TextToSpeechUtil {
    TextToSpeech instance = null;

    public TextToSpeechUtil(Context context) {
        instance = new TextToSpeech(context, status -> {
            if (status != TextToSpeech.ERROR) {
                instance.setLanguage(Locale.UK);
            }
        });
    }

    public void speak(String message, boolean isFromCountdown) {
        if (!isFromCountdown && instance.isSpeaking()) {
            return;
        }

        if (isFromCountdown) {
            instance.speak(message, TextToSpeech.QUEUE_FLUSH, null);
        } else {
            instance.speak(message, TextToSpeech.QUEUE_ADD, null);
        }
    }

}
