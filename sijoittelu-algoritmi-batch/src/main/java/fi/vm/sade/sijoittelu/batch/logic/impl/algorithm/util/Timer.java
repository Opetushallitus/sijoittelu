package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

public class Timer {
    private final StopWatch watch;
    private final String task;
    private final Logger LOG;

    private Timer(String task, Class loggingClass) {
        this.watch = new StopWatch();
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

        t.watch.start(task);

        t.LOG.info(logMessage);

        return t;
    }

    public Timer stop() {
        return stop("");
    }

    public Timer stop(String logMsg) {
        this.watch.stop();

        final String logMessage = (this.task + " STOPPED ( " + this.watch.getTotalTimeMillis()  + "ms ) " + logMsg).trim();

        this.LOG.info(logMessage);

        return this;
    }
}
