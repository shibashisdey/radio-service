package com.shibashis.radio.streaming;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class BroadcastService {

    private final List<OutputStream> listeners = new CopyOnWriteArrayList<>();
    private final List<File> queue = new ArrayList<>();

    private static final int BUFFER_SIZE = 4096;
    private volatile int currentSongIndex = 0;

    public BroadcastService() {
        queue.add(new File("C:\\Users\\shibashis\\Downloads\\Music\\Papaoutai (Afro Soul).mp3"));
        // Add more songs if needed
    }

    public void addListener(OutputStream os) {
        listeners.add(os);
        System.out.println("Listener added: " + listeners.size());
    }

    public void removeListener(OutputStream os) {
        listeners.remove(os);
        try { os.close(); } catch (Exception ignored) {}
        System.out.println("Listener removed: " + listeners.size());
    }

    @PostConstruct
    public void start() {
        Thread playbackThread = new Thread(this::playbackLoop);
        playbackThread.setDaemon(true);
        playbackThread.start();
        System.out.println("Playback engine started.");
    }

    private void playbackLoop() {

        while (true) {

            File currentSong = queue.get(currentSongIndex);
            System.out.println("Playing: " + currentSong.getName());

            try (BufferedInputStream input =
                         new BufferedInputStream(new FileInputStream(currentSong))) {

                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;

                while ((bytesRead = input.read(buffer)) != -1) {

                    if (!listeners.isEmpty()) {

                        for (OutputStream os : listeners) {
                            try {
                                os.write(buffer, 0, bytesRead);
                            } catch (IOException e) {
                                removeListener(os);
                            }
                        }
                    }

                    // Small steady pacing — DO NOT overcalculate
                    Thread.sleep(15);
                }

                System.out.println("Song finished.");

            } catch (Exception e) {
                e.printStackTrace();
            }

            // Move to next song (loop queue)
            currentSongIndex = (currentSongIndex + 1) % queue.size();
        }
    }
}
