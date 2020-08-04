package com.pavel.queueorganizer;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/** Сущность, описывающая клиента, находящегося в очереди*/
public class ClientInQueue {
    /**внешний ключ к таблице client*/
    private final long clientId;
    /**внешний ключ к таблице queue*/
    private final long queueId;
    /**время начала обслуживания клиента в очередь*/
    private Timestamp startTime;
    /**время завершения обслуживания в очереди*/
    private Timestamp endTime;
    /**место клиента в очереди*/
    private long place;
    /**id исполнителя обслужившего клиента */
    private long servedById;
    /**флаг оповещений*/
    private boolean notificationStatus;
    /**флаг завершения очереди*/
    private boolean complete;

    public ClientInQueue(long clientId, long queueId) {
        this.clientId = clientId;
        this.queueId = queueId;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public void setPlace(long place) {
        this.place = place;
    }

    public void setNotificationStatus(boolean notificationStatus) {
        this.notificationStatus = notificationStatus;
    }

    public void setServedById(long servedById) {
        this.servedById = servedById;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public long getClientId() {
        return clientId;
    }

    public long getQueueId() {
        return queueId;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public long getPlace() {
        return place;
    }

    public boolean getNotificationStatus() {
        return notificationStatus;
    }

    public long getServedById() {
        return servedById;
    }

    public boolean isComplete() {
        return complete;
    }
}
