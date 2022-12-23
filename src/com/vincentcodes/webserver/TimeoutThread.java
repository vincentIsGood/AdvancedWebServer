package com.vincentcodes.webserver;

import java.util.function.Consumer;

public class TimeoutThread extends Thread {
    public final long timeoutMs;
    public final Consumer<Boolean> handler;

    private boolean stopRequested;

    /**
     * @param timeoutMs
     * @param handler handle whats happen after timeout. It accepts a 
     * boolean indicating whether the thread is requested to stop (ie. 
     * thread is stopping)
     */
    public TimeoutThread(long timeoutMs, Consumer<Boolean> handler) {
        this.timeoutMs = timeoutMs;
        this.handler = handler;
    }

    @Override
    public void run() {
        try{Thread.sleep(timeoutMs);}catch(InterruptedException e){}
        handler.accept(stopRequested);
        return;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    public Consumer<Boolean> getHandler() {
        return handler;
    }

    /**
     * Try to stop the thread
     */
    public void tryStop(){
        stopRequested = true;
        interrupt();
    }
    
}
