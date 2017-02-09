package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util;

import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class Timer {
    private final Stopwatch watch;
    private final String task;
    private final Logger LOG;

    private Timer(String task, Class loggingClass) {
        this.watch = Stopwatch.createUnstarted();
        this.LOG = LoggerFactory.getLogger(loggingClass);
        this.task = task;
    }

    public static Timer start(String task) {
        return Timer.start(task, "");
    }

    public static Timer start(String task, String logMsg) {
        return Timer.start(task, logMsg, Timer.class);
    }

    public static Timer start(String task, String logMsg, Class loggingClass) {
        final Timer t = new Timer(task, loggingClass);
        final String logMessage = (t.task + " STARTED " + logMsg).trim();

        t.watch.start();

        t.LOG.info(logMessage);

        return t;
    }

    public Timer stop() {
        return stop("");
    }

    public Timer stop(String logMsg) {
        this.watch.stop();

        final String logMessage = (this.task + " STOPPED ( " + this.watch.elapsed(TimeUnit.MICROSECONDS) + " μs ) " + logMsg).trim();

        this.LOG.info(logMessage);

        return this;
    }
}
