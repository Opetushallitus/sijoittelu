package fi.vm.sade.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TempDockerDB {

    private static final Logger LOG = LoggerFactory.getLogger(TempDockerDB.class);
    private static final String dbName                      = "sijoittelu";
    private static final String containerName               = "sijoittelu-postgres";

    private static final int port                           = 5433;

    private static final int startStopRetries               = 100;
    private static final int startStopRetryIntervalMillis   = 100;

    private static boolean databaseIsRunning() {
        try {
            Process p = Runtime.getRuntime().exec("docker exec " + containerName + " pg_isready -q -t 1 -h localhost -U oph -d " + dbName);
            return p.waitFor() == 0;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    };

    private static void startDatabaseContainer() {
        LOG.info("Starting PostgreSQL container (localhost:" + port + "):");

        try {
            Process p = Runtime.getRuntime().exec("docker start " + containerName);
            p.waitFor();
            String error = new String(p.getErrorStream().readAllBytes());
            if (!tryTimes(() -> databaseIsRunning(), startStopRetries, startStopRetryIntervalMillis)) {
                throw new RuntimeException("postgres not accepting connections in port " + port);
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void start() {
        try {
            if(!databaseIsRunning()) {
                startDatabaseContainer();
            }
        } finally {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> stop()));
        }
    }

    private static void stop() {
        try {
            LOG.info("Killing PostgreSQL container");
            Process p = Runtime.getRuntime().exec("docker stop " + containerName);
            p.waitFor();
        } catch(Exception e) {
            LOG.warn("PostgreSQL container didn't stop gracefully");
        }
    }

    private interface Function<T> {
        T apply();
    }

    private static boolean tryTimes(Function<Boolean> runnable, int times, int interval) throws InterruptedException {
        if(times==0) {
            return false;
        }
        if(runnable.apply()) {
            return true;
        }
        Thread.sleep(interval);
        return tryTimes(runnable, times-1, interval);
    }
}
