package com.pavel.queueorganizer;

/** Сущность, описывающая исполнителя*/
public class Executor {
    /** имя исполнителя*/
    private String name;
    /** внешний ключ к таблице client*/
    private long clientId;
    /** внешний ключ к таблице queue*/
    private long queueId;
    /** статус обслуживания клиента*/
    private boolean serveClient;
    /** статус активности исполнителя*/
    private boolean activeNow;
    /** статус, указывающий является ли клиент исполнителем очереди*/
    private boolean invalid;
    /** id клиента, который обслуживается исполнителем на данный момент */
    private long servingClient;
    /** статус ожидания нового клиента*/
    private boolean waiting;

    public void setName(String name) {
        this.name = name;
    }

    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

    public void setQueueId(long queueId) {
        this.queueId = queueId;
    }

    public void setServeClient(boolean serveClient) {
        this.serveClient = serveClient;
    }

    public void setActiveNow(boolean activeNow) { this.activeNow = activeNow; }

    public void setInvalid(boolean invalid) {
        this.invalid = invalid;
    }

    public void setServingClient(long servingClient) {
        this.servingClient = servingClient;
    }

    public String getName() {
        return name;
    }

    public long getClientId() {
        return clientId;
    }

    public long getQueueId() {
        return queueId;
    }

    public boolean isServeClient() {
        return serveClient;
    }

    public boolean isActiveNow() { return activeNow; }

    public boolean isInvalid() {
        return invalid;
    }

    public long getServingClient(){
        return servingClient;
    }

    public boolean isWaiting() {
        return waiting;
    }

    public void setWaiting(boolean waiting) {
        this.waiting = waiting;
    }
}


