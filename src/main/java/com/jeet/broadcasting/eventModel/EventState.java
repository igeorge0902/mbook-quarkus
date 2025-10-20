package com.jeet.broadcasting.eventModel;

import com.jeet.api.Logins;
import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "events_states")
public class EventState implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    public int id;
    @ManyToOne(cascade={CascadeType.PERSIST, CascadeType.REFRESH}, fetch=FetchType.EAGER)
    @JoinColumn(name="events_id")
    protected AddMovie event;
    @ManyToOne(cascade={CascadeType.PERSIST, CascadeType.REFRESH}, fetch=FetchType.EAGER)
    @JoinColumn(name="logins_id")
    protected Logins logins;
    protected String state;

    public int getId() {
        return id;
    }

    public AddMovie getEvent() {
        return event;
    }

    public Logins getLogins() {
        return logins;
    }

    public String getState() {
        return state;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setEvent(AddMovie event) {
        this.event = event;
    }

    public void setLogins(Logins logins) {
        this.logins = logins;
    }

    public void setState(String state) {
        this.state = state;
    }
}
