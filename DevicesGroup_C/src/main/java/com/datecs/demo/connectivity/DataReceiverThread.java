package com.datecs.demo.connectivity;

import static com.datecs.demo.MainActivity.log;

import java.io.IOException;
import java.io.InputStream;

public class DataReceiverThread extends Thread {
    private InputStream inputStream;
    private volatile boolean isRunning = true;

    public DataReceiverThread(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public void run() {
        byte[] buffer = new byte[1024]; // Adjust buffer size as needed
        int bytesRead;

        while (isRunning) {
            try {
                if (inputStream.available() > 0) {
                    bytesRead = inputStream.read(buffer);

                    if (bytesRead > 0) {
                        String receivedData = new String(buffer, 0, bytesRead, "UTF-8"); // Use the appropriate encoding
                        // Handle the received data

                        // Check if there is data available to read
                        int availableBytes = inputStream.available();

                        if (availableBytes > 0) {
                            // Data is available to read, create a buffer and read it
//                            byte[] buffer = new byte[availableBytes];
//                            int bytesRead = inputStream.read(buffer);

                            // Convert the read bytes to a String using an appropriate character encoding
                            String text = new String(buffer, 0, bytesRead, "UTF-8"); // Use the correct encoding

                            // Now 'text' contains the text content from the InputStream.
                            log("Received text: " + text);
                        } else {
                            // No data available to read at this moment
                            log("No data available.");
                        }
                    }
                } else {
                    // No data available, you can optionally add a short sleep to avoid busy-waiting
                    log("Sleep Data available.");
                    Thread.sleep(500); // Sleep for 100 milliseconds (adjust as needed)
                }
            } catch (IOException e) {
                // Handle I/O error
            } catch (InterruptedException e) {
                // Handle thread interruption
            }
        }
    }

    public void stopThread() {
        isRunning = false;
    }
}