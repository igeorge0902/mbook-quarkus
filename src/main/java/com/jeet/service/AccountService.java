package com.jeet.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.jeet.api.Devices;
import com.jeet.api.Logins;
import com.jeet.db.DAO;
import com.jeet.rest.responsemapper.AccountMapper;
import com.jeet.rest.responserecord.account.UserResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class AccountService {

    @Inject
    DAO dao;

    @Inject
    AccountMapper accountMapper;

    public JsonNode parseErrorDetails(Object attribute) {
        return accountMapper.parseErrorDetails(attribute);
    }

    public UserResponse toUserResponse(Logins login, JsonNode errorDetails) {
        return accountMapper.toUserResponse(login, errorDetails);
    }

    public List<Devices> getDevices(String uuid) {
        return dao.getDevices(uuid);
    }

    public Logins getUser(String user) {
        return dao.getUser(user);
    }

    public int countUsersLike(String newuser) {
        return dao.getNewUser(newuser);
    }

    public int countEmailsLike(String newemail) {
        return dao.getNewEmail(newemail);
    }
}
