package com.jeet.service;

import java.util.List;

import com.jeet.api.Devices;
import com.jeet.api.Logins;
import com.jeet.api.Tokens;
import com.jeet.broadcasting.eventModel.AddMovie;
import com.jeet.broadcasting.eventModel.EventState;
import com.jeet.db.DAO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class BookingHandlerImpl {

	@Inject
	DAO dao;

	public List<Devices> getDevice(String uuid) {
		return dao.getDevices(uuid);
	}

	public Logins getUser(String user) {
		return dao.getUser(user);
	}

	public int getNewUser(String newuser) {
		return dao.getNewUser(newuser);
	}

	public int getNewEmail(String newemail) {
		return dao.getNewEmail(newemail);
	}

	public Logins getUuid(String uuid) {
		return dao.getUuid(uuid);
	}

	public Tokens getToken(String token1) {
		return dao.getToken(token1);
	}

	public Tokens getToken2(String token1) {
		return dao.getToken2(token1);
	}

	@Transactional
	public void saveNewEvent(AddMovie event) {
		dao.saveNewEvent(event);
	}

	public List<EventState> getNewEventsForUser(String uuid) {
		return dao.getNewEventsForUser(uuid);
	}
}