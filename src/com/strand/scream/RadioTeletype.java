package com.strand.scream;

import java.util.Formatter;

import com.strand.global.StrandLog;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class RadioTeletype {

    public final static String CALL_SIGN = "CUSF";

    // (CENTRE_FREQ + FREQ_SHIFT/2) / BAUD_RATE must be integer
    public final static int CENTRE_FREQ = 1075;
    public final static int FREQ_SHIFT = 350;
    public final static int BAUD_RATE = 50;

    // 8000Hz, 11025Hz, 44100Hz all work
    public final static int SAMPLE_RATE = 44100;

    // Length (bits) of lock-on tone before messages
    public final static int LOCK_ON = 300;

    // Minimum time (ms) between transmissions
    public final static int MIN_GAP = 5000;

    private final byte[] mark;
    private final byte[] space;
    
    private AudioTrack track;

    // Initialises the mark and space 16 bit sound arrays
    public RadioTeletype() {
        
        int markFreq = CENTRE_FREQ + FREQ_SHIFT / 2;
        int spaceFreq = CENTRE_FREQ - FREQ_SHIFT / 2;

        mark = generateTone(markFreq);
        space = generateTone(spaceFreq);

    }

    // Generates a tone of particular frequency (1 bit long at specified baud
    // rate)
    private byte[] generateTone(double freq) {

        int numSamples = SAMPLE_RATE / BAUD_RATE;
        double[] sample = new double[numSamples];
        byte[] output = new byte[2 * numSamples];

        // Create sine-wave sample
        double x = 2 * Math.PI * freq / SAMPLE_RATE;
        for (int i = 0; i < numSamples; i++) {
            sample[i] = Math.sin(x * i);
        }

        // Convert to 16 bit WAV PCM sound array
        int j = 0;
        for (final double dVal : sample) {
            final short val = (short) (dVal * Short.MAX_VALUE);
            output[j++] = (byte) (val & 0x00ff); // Lower byte
            output[j++] = (byte) ((val & 0xff00) >>> 8); // Upper byte
        }

        return output;
    }

    private void streamAudio(String text) {

        boolean[] bits = createBits(text);
        
        int minSize =AudioTrack.getMinBufferSize(44100,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT );
        track = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, 
                AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, 
                minSize, AudioTrack.MODE_STREAM);

        track.play();
        
        // Lock-on bits
        for (int k = 0; k < LOCK_ON; k++) {
            track.write(mark, 0, mark.length);
        }

        // Data bits
        for (boolean bit : bits) {
            byte[] tone = bit ? mark : space;
            track.write(tone, 0, tone.length);
        }

        track.stop();
    }

    // Takes string and returns RS232 (serial) bits
    private boolean[] createBits(String msg) {
        byte[] bytes = msg.getBytes();
        boolean[] bits = new boolean[bytes.length * (Byte.SIZE + 3)];

        int j = 0;

        for (byte b : bytes) {

            bits[j++] = false; // Start bit

            int val = b;
            for (int i = 0; i < 8; i++) {
                // Least-significant bit first
                bits[j++] = ((val & 1) == 0) ? false : true;
                val >>= 1;
            }

            // Stop bits
            bits[j++] = true;
            bits[j++] = true;

        }

        return bits;
    }

    private String crc(String s) {
        char x = 0xFFFF;

        char[] chars = s.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            x = (char) (x ^ (chars[i] << 8));

            for (int j = 0; j < 8; j++) {
                if ((x & 0x8000) > 0) {
                    x = (char) ((x << 1) ^ 0x1021);
                } else {
                    x <<= 1;
                }
            }

        }

        Formatter formatter = new Formatter();
        formatter.format("%04X", (int) x);

        return formatter.out().toString();
    }
    
    public void play(Context context) {
        String msg = "$$" + CALL_SIGN + ",";
        msg += context.getString(R.string.rtty_message);
        msg += "*" + crc(msg);
        StrandLog.d(ScreamService.TAG, "Radio teletype generation and playback commencing");
        streamAudio(msg);
    }
    
    public void stop() {
        if (track != null) {
            track.flush();
            track.release();
            track = null;
        }
    }

}
