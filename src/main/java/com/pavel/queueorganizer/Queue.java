package com.pavel.queueorganizer;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/** Сущность, описывающая очередь*/
public class Queue {
    /** Название очереди*/
    private String name;
    /** Строка с временем работы очереди*/
    private String workHours;
    /** координата долготы очереди*/
    private float longitude;
    /** координаты широты очереди*/
    private float latitude;
    /** id очереди в базе данных*/
    private long id;
    /** id администратора очереди в таблице client, внешний ключ*/
    private long admin;
    /** статус очереди, по умолчанию true*/
    private boolean active;
    /** флаг, указывающий на закрытие очереди*/
    private boolean invalid;
    /** время с которого очередь начала работать*/
    private Timestamp startTime;
    /**время закрытия очереди*/
    private Timestamp endTime;
    /**последнее  место в очереди*/
    private long lastPlace;

    public Queue(String name, float longitude, float latitude, long admin, Timestamp startTime) {
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
        this.admin = admin;
        this.startTime = startTime;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setWorkHours(String workHours) {
        this.workHours = workHours;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setAdmin(long admin) {
        this.admin = admin;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setInvalid(boolean invalid) {
        this.invalid = invalid;
    }

    public void setStartTime(Timestamp startTime) { this.startTime = startTime; }

    public void setEndTime(Timestamp endTime) { this.endTime = endTime;}

    public void setLastPlace(long lastPlace) {
        this.lastPlace = lastPlace;
    }

    public String getName() { return name; }

    public String getWorkHours() { return workHours; }

    public float getLongitude() { return longitude; }

    public float getLatitude() { return latitude; }

    public long getId() { return id; }

    public long getAdmin() { return admin; }

    public boolean isActive() { return active; }

    public boolean isInvalid() { return invalid; }

    public Timestamp getStartTime() { return startTime; }

    public Timestamp getEndTime() { return endTime; }

    public long getLastPlace() {
        return lastPlace;
    }
}
