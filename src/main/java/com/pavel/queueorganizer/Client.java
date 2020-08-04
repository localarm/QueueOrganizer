package com.pavel.queueorganizer;

public class Client {
    /** имя клиента*/
    private String firstName;
    /** фамилия клиента*/
    private String lastName;
    /** id клиента в базе данных*/
    private long id;

    public Client( long id, String firstName) {
        this.id = id;
        this.firstName = firstName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public long getId() {
        return id;
    }
}
