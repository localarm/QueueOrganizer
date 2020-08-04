package com.pavel.queueorganizer;

import java.sql.Timestamp;

public class ServedClient {
    private final String name;
    private final Timestamp startTime;
    private final Timestamp endTime;

    public ServedClient(String name, Timestamp startTime, Timestamp endTime) {
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getName() {
        return name;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }
}
