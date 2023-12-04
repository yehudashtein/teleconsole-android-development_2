package com.telebroad.teleconsole.helpers;

import android.media.ToneGenerator;

import java.util.HashMap;
import java.util.Map;

import static android.media.AudioManager.STREAM_MUSIC;
import static android.media.AudioManager.STREAM_VOICE_CALL;
import static android.media.ToneGenerator.*;

public class DTMFPlayer {

    private static DTMFPlayer instance;
    private Map<Character, Integer> dtmfTones = new HashMap<>();
    public static DTMFPlayer getInstance(){
        if (instance == null){
            instance = new DTMFPlayer();
        }
        return instance;
    }

    private ToneGenerator toneGenerator;
    private DTMFPlayer(){
        toneGenerator = new ToneGenerator(STREAM_VOICE_CALL, 50);
        dtmfTones.put('0', TONE_DTMF_0);
        dtmfTones.put('1', TONE_DTMF_1);
        dtmfTones.put('2', TONE_DTMF_2);
        dtmfTones.put('3', TONE_DTMF_3);
        dtmfTones.put('4', TONE_DTMF_4);
        dtmfTones.put('5', TONE_DTMF_5);
        dtmfTones.put('6', TONE_DTMF_6);
        dtmfTones.put('7', TONE_DTMF_7);
        dtmfTones.put('8', TONE_DTMF_8);
        dtmfTones.put('9', TONE_DTMF_9);
        dtmfTones.put('*', TONE_DTMF_S);
        dtmfTones.put('#', TONE_DTMF_P);
        //dtmfTones.put('0', TONE_DTMF_0);
    }

    public void play(char c){
        Integer tone = dtmfTones.get(c);
        if (tone == null){
            tone = TONE_DTMF_A;
        }
        toneGenerator.startTone(tone, 80);
    }

    public void play(String string){
        for (char ch : string.toCharArray()){
            play(ch);
        }
    }

}
