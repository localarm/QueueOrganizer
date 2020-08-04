package com.pavel.queueorganizer;

public class Notify<T> {
    private final Executor executor;
    private final T currentClient;
    private T secondClient;
    private String currentClientName;

    public Notify(Executor executor, T currentClient) {
        this.executor = executor;
        this.currentClient = currentClient;
    }

    public void setSecondClient(T secondClient) {
        this.secondClient = secondClient;
    }

    public Executor getExecutor() {
        return executor;
    }

    public T getCurrentClient() {
        return currentClient;
    }

    public T getSecondClient() {
        return secondClient;
    }

    public String getCurrentClientName() {
        return currentClientName;
    }

    public void setCurrentClientName(String currentClientName) {
        this.currentClientName = currentClientName;
    }
}
