package com.jeet.service;

import com.jeet.broadcasting.eventModel.AddMovie;
import com.jeet.broadcasting.eventModel.EventState;
import com.jeet.db.DAO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class NewsService {

    @Inject
    DAO dao;

    public List<EventState> getNewEventsForUser(String user) {
        return dao.getNewEventsForUser(user);
    }

    @Transactional
    public void saveNewEvent(AddMovie event) {
        dao.saveNewEvent(event);
    }
}
