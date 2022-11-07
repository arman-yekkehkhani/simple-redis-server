package org.example.redis;

import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.*;

public class WriteAheadLogger {
    public static final String LOG_PATH = "write_ahead_log.csv";

    private static BlockingDeque<String[]> persistenceQueue = new LinkedBlockingDeque<>();

    private WriteAheadLogger() {
        ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
        ses.scheduleAtFixedRate(() -> {
            try (FileWriter writer = new FileWriter(LOG_PATH, true);
                 ICSVWriter csvWriter = new CSVWriterBuilder(writer)
                         .build()) {
                while (!persistenceQueue.isEmpty()) {
                    csvWriter.writeNext(persistenceQueue.poll());
                }
            } catch (IOException e) {
                System.out.println("An error encountered while persisting logs.");
                e.printStackTrace();
            }
        }, 100, 100, TimeUnit.MILLISECONDS);
    }

    private static WriteAheadLogger instance;

    public static WriteAheadLogger getInstance() {
        if (instance == null)
            instance = new WriteAheadLogger();
        return instance;
    }

    public void log(int dbIdx, String command, String key, String value, Long expirationTime) {
        String[] line = new String[5];
        line[0] = String.valueOf(dbIdx);
        line[1] = command;
        line[2] = key;
        line[3] = value == null ? "" : value;
        line[4] = expirationTime == null ? "" : String.valueOf(expirationTime);

        persistenceQueue.add(line);
    }
}
