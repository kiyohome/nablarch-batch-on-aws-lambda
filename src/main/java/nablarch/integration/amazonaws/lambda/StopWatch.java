package nablarch.integration.amazonaws.lambda;

import nablarch.core.log.Logger;

public class StopWatch {
    private String target;
    private long beginTime;
    private long time;
    public StopWatch(String target) {
        this.target = target;
    }
    public void begin() {
        beginTime = System.currentTimeMillis();
    }
    public void finish() {
        time = System.currentTimeMillis() - beginTime;
    }
    public void log(Logger logger) {
        logger.logInfo(this.toString());
    }
    @Override
    public String toString() {
        return String.format("%s [%s] seconds", target, String.valueOf(time / 1000));
    }
}
