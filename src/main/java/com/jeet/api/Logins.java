package com.jeet.api;

import jakarta.persistence.*;

@Entity
@Table(name="logins")
public class Logins {
	@Id
	@GeneratedValue(strategy= GenerationType.AUTO)
    protected int id;
    protected String user;
    protected String uuid;
    protected String email;
    protected String profilePicture;


    public int getId() {
        return id;
    }

    public void setId(int value) {
        this.id = value;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String value) {
        this.user = value;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String value) {
        this.uuid = value;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String value) {
        this.email = value;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String value) {
        this.profilePicture = value;
    }
}


