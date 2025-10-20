package com.jeet.broadcasting.eventModel;

import com.jeet.api.Logins;
import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "news_settings")
public class NewsSettings implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    public int id;
    public String newsScreening;
    @ManyToOne(cascade={CascadeType.PERSIST, CascadeType.REFRESH}, fetch=FetchType.EAGER)
    @JoinColumn(name="logins_id")
    protected Logins user;
}