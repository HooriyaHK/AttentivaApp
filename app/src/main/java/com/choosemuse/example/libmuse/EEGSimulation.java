package com.choosemuse.example.libmuse;

import com.choosemuse.libmuse.*;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class EEGSimulation {
    private static CircularBuffer eegBuffer;
    private static CircularBuffer fftBuffer;

    public void main(String[] args) {
        int bufferCapacity = 60; // Store 60 samples for 1 minute (1 sample per second)
        int numChannels = 4;     // Number of EEG channels

        eegBuffer = new CircularBuffer(bufferCapacity, numChannels);
        fftBuffer = new CircularBuffer(bufferCapacity, numChannels);

        System.out.println("Starting EEG simulation with " + bufferCapacity + " buffer capacity and " + numChannels + " channels.");

        // Initialize Muse Manager
        MuseManagerAndroid manager = MuseManagerAndroid.getInstance();
        manager.setContext(null); // Replace `null` with your Application/Activity context

        // Search and connect to Muse
        manager.startListening();
        List<Muse> availableMuse = manager.getMuses();
        if (availableMuse.isEmpty()) {
            System.out.println("No Muse devices found.");
            return;
        }

        Muse muse = availableMuse.get(0); // Connect to the first available Muse
        System.out.println("Connecting to Muse device: " + muse.getName());
        setupMuse(muse);

        // Timer to process data every second
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    // Get the latest EEG sample from the buffer
                    float[] newSample = getSampleFromBuffer(eegBuffer);
                    if (newSample == null) {
                        System.out.println("No data available in the buffer yet.");
                        return;
                    }

                    // Perform FFT and focus analysis
                    float[] fftResult = simpleFrequencyAnalysis(newSample);
                    fftBuffer.addSample(fftResult);
                    String focusState = determineFocusState(fftResult);

                    System.out.println("EEG Sample: " + Arrays.toString(newSample));
                    System.out.println("FFT Result: " + Arrays.toString(fftResult));
                    System.out.println("User is currently: " + focusState);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 1000);
    }

    private static void setupMuse(Muse muse) {
        muse.registerConnectionListener(new MuseConnectionListener() {
            @Override
            public void receiveMuseConnectionPacket(MuseConnectionPacket p, Muse muse) {
                switch (p.getCurrentConnectionState()) {
                    case CONNECTED:
                        System.out.println("Muse is connected!");
                        break;
                    case DISCONNECTED:
                        System.out.println("Muse is disconnected!");
                        break;
                    default:
                        break;
                }
            }
        });

        muse.registerDataListener(new MuseDataListener() {
            @Override
            public void receiveMuseDataPacket(MuseDataPacket p, Muse muse) {
                if (p.packetType() == MuseDataPacketType.EEG) {
                    float[] values = new float[(int) p.valuesSize()];
                    for (int i = 0; i < values.length; i++) {
                        values[i] = (float) p.getEegChannelValue(Eeg.values()[i]);
                    }
                    eegBuffer.addSample(values);
                }
            }

            @Override
            public void receiveMuseArtifactPacket(MuseArtifactPacket p, Muse muse) {
                // Handle artifacts if needed (e.g., blinks, jaw clenches)
            }
        }, MuseDataPacketType.EEG);

        muse.runAsynchronously();
    }

    // Get the latest sample from the EEG buffer
    private static float[] getSampleFromBuffer(CircularBuffer buffer) {
        float[] sample = buffer.getSample((buffer.capacity() - 1) % buffer.capacity());
        if (sample == null) return null;

        float[] result = new float[sample.length];
        for (int i = 0; i < sample.length; i++) {
            result[i] = (float) sample[i];
        }
        return result;
    }

    // Simple FFT-like analysis
    private static float[] simpleFrequencyAnalysis(float[] sample) {
        int n = sample.length;
        float[] magnitudes = new float[n];
        for (int i = 0; i < n; i++) {
            magnitudes[i] = Math.abs(sample[i]); // Use magnitude of the values
        }
        return magnitudes;
    }

    // Determine focus state based on FFT result
    private static String determineFocusState(float[] fftResult) {
        float alphaBandThreshold = 0.3f;
        float betaBandThreshold = 0.7f;
        float gammaBandThreshold = 0.5f;

        int alphaStart = 0, alphaEnd = 1;
        int betaStart = 1, betaEnd = 2;
        int gammaStart = 2, gammaEnd = 3;

        float alphaMagnitude = calculateBandMagnitude(fftResult, alphaStart, alphaEnd);
        float betaMagnitude = calculateBandMagnitude(fftResult, betaStart, betaEnd);
        float gammaMagnitude = calculateBandMagnitude(fftResult, gammaStart, gammaEnd);

        if (betaMagnitude > betaBandThreshold && gammaMagnitude > gammaBandThreshold) {
            return "Focused";
        } else if (alphaMagnitude > alphaBandThreshold) {
            return "Unfocused";
        }
        return "Neutral";
    }

    private static float calculateBandMagnitude(float[] fftResult, int startIdx, int endIdx) {
        float sum = 0;
        int count = 0;

        for (int i = startIdx; i < endIdx; i++) {
            sum += fftResult[i];
            count++;
        }
        return (count == 0) ? 0 : sum / count;
    }

    public class CircularBuffer {
        private float[][] buffer;
        public int capacity;
        private int numChannels;
        private int index;

        public CircularBuffer(int capacity, int numChannels) {
            this.capacity = capacity;
            this.numChannels = numChannels;
            this.buffer = new float[capacity][numChannels];
            this.index = 0;
        }

        public void addSample(float[] values) {
            if (values.length != numChannels) {
                throw new IllegalArgumentException("Sample size does not match the number of channels.");
            }
            buffer[index] = values;
            index = (index + 1) % capacity;
        }

        public float[] getSample(int index) {
            return buffer[index];
        }

        public int capacity() {
            return capacity;
        }

    }

}
