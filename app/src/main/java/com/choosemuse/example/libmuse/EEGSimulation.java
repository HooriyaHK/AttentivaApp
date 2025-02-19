package com.choosemuse.example.libmuse;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class EEGSimulation extends AppCompatActivity {
    public static void main(String[] args) {
        int bufferCapacity = 60; // Store 60 samples for 1 minute (1 sample per second)
        int numChannels = 4; // Number of EEG channels
        CircularBuffer eegBuffer = new CircularBuffer(bufferCapacity, numChannels);
        CircularBuffer fftBuffer = new CircularBuffer(bufferCapacity, numChannels);

        System.out.println("Starting EEG simulation with " + bufferCapacity + " buffer capacity and " + numChannels + " channels.");

        // Timer to simulate EEG data collection and analysis every second
        Timer timer = new Timer();

        // Timer task for periodic EEG data sampling
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    System.out.println("TimerTask is running..."); // Debug log

                    // Simulate getting new data from Muse SDK
                    float[] newSample = getSample(numChannels);
                    eegBuffer.addSample(newSample);

                    // Perform FFT-like processing on the most recent sample
                    float[] fftResult = simpleFrequencyAnalysis(newSample);

                    // Add the result to the FFT buffer
                    fftBuffer.addSample(fftResult);

                    // Check the focus state based on wavebands
                    String focusState = determineFocusState(fftResult);

                    // Print EEG buffer, FFT buffer, and focus state for debugging
                    System.out.println("New EEG Sample: " + Arrays.toString(newSample));
                    System.out.println("New FFT Result: " + Arrays.toString(fftResult));
                    System.out.println("User is currently: " + focusState);
                } catch (Exception e) {
                    System.out.println("Exception occurred in TimerTask: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }, 0, 1000); // Run every 1 second

        // Sleep to allow TimerTask to execute for 5 seconds before the main thread exits
        try {
            Thread.sleep(5000); // Allow TimerTask to run for 5 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Main method is exiting...");
    }

    // Simulate getting EEG sample data from Muse SDK
    private static float[] getSample(int numChannels) {
        Random random = new Random();
        float[] sample = new float[numChannels];
        for (int i = 0; i < numChannels; i++) {
            sample[i] = random.nextFloat(); // Generate random EEG values
        }
        System.out.println("Generated new sample: " + Arrays.toString(sample));
        return sample;
    }

    // Simulate simple frequency analysis (FFT-like)
    private static float[] simpleFrequencyAnalysis(float[] sample) {
        int n = sample.length;
        float[] magnitudes = new float[n];

        for (int i = 0; i < n; i++) {
            magnitudes[i] = Math.abs(sample[i]); // Use magnitude of the values
        }

        System.out.println("FFT-like analysis results: " + Arrays.toString(magnitudes));
        return magnitudes;
    }

    // Determine focus state based on FFT result (magnitude of frequencies)
    private static String determineFocusState(float[] fftResult) {
        // Ensure we have enough channels in the fftResult array (in this case, 4)
        if (fftResult.length < 4) {
            throw new IllegalArgumentException("Not enough frequency bands in FFT result.");
        }

        float alphaBandThreshold = 0.3f; // Threshold for alpha waves (relaxed state)
        float betaBandThreshold = 0.7f; // Threshold for beta waves (focused state)
        float gammaBandThreshold = 0.5f; // Threshold for gamma waves (high cognitive processing)

        // Define frequency band indices based on FFT result size
        int alphaStart = 0, alphaEnd = 1;  // Assuming first channel for Alpha
        int betaStart = 1, betaEnd = 2;    // Assuming second channel for Beta
        int gammaStart = 2, gammaEnd = 3;  // Assuming third channel for Gamma

        // Calculate average magnitudes in each frequency band
        float alphaMagnitude = calculateBandMagnitude(fftResult, alphaStart, alphaEnd);
        float betaMagnitude = calculateBandMagnitude(fftResult, betaStart, betaEnd);
        float gammaMagnitude = calculateBandMagnitude(fftResult, gammaStart, gammaEnd);

        String focusState = "Neutral"; // Default to Neutral
        if (betaMagnitude > betaBandThreshold && gammaMagnitude > gammaBandThreshold) {
            focusState = "Focused";
        } else if (alphaMagnitude > alphaBandThreshold) {
            focusState = "Unfocused";
        }

        System.out.println("Focus state determined: " + focusState);
        return focusState;
    }

    // Calculate the average magnitude in a specific frequency band
    private static float calculateBandMagnitude(float[] fftResult, int startIdx, int endIdx) {
        if (startIdx < 0 || endIdx > fftResult.length) {
            throw new IllegalArgumentException("Invalid frequency band indices.");
        }

        float sum = 0;
        int count = 0;

        for (int i = startIdx; i < endIdx; i++) {
            sum += fftResult[i];
            count++;
        }

        float average = (count == 0) ? 0 : sum / count;
        System.out.println("Calculated band magnitude from indices " + startIdx + " to " + endIdx + ": " + average);
        return average;
    }
}

class CircularBuffer {
    private float[][] buffer;
    private int capacity;
    private int numChannels;
    private int index;

    public CircularBuffer(int capacity, int numChannels) {
        this.capacity = capacity;
        this.numChannels = numChannels;
        this.buffer = new float[capacity][numChannels];
        this.index = 0;
    }

    public void addSample(float[] sample) {
        if (sample.length != numChannels) {
            throw new IllegalArgumentException("Sample size does not match the number of channels.");
        }
        buffer[index] = sample;
        index = (index + 1) % capacity;
    }

    public float[] getSample(int index) {
        return buffer[index];
    }
}