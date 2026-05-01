package com.jeet.db;

import com.jeet.api.Devices;
import com.jeet.api.Logins;
import com.jeet.api.Tokens;
import com.jeet.broadcasting.eventModel.AddMovie;
import com.jeet.broadcasting.eventModel.EventState;
import com.jeet.logging.LogLevel;
import com.jeet.logging.ObservedLog;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

@RequestScoped
public class DAO {

    @Inject
    Session session;  // Quarkus-managed Hibernate session

    @ObservedLog(category = "LOG-ENTITY", level = LogLevel.INFO)
    @Transactional
    public List<Devices> getDevices(String uuid) {
        String hql = "from Devices where uuid = :mUuid";
        List<Devices> list = session.createQuery(hql, Devices.class)
                .setParameter("mUuid", uuid)
                .getResultList();

        if (list.isEmpty()) {
            Devices dev = new Devices();
            dev.setDevice("The user does not have devices");
            dev.setId(0);
            dev.setUuid(uuid);
            list.add(dev);
        }
        return list;
    }

    @ObservedLog(category = "LOG-ENTITY", level = LogLevel.INFO)
    @Transactional
    public Logins getUser(String user) {
        String hql = "from Logins where user = :mUser";
        Logins result = session.createQuery(hql, Logins.class)
                .setParameter("mUser", user)
                .uniqueResult();

        if (result == null) {
            Logins ls = new Logins();
            ls.setId(0);
            ls.setUser(user);
            ls.setUuid("no UUID");
            ls.setEmail("no email");
            return ls;
        }
        return result;
    }

    @ObservedLog(category = "LOG-ENTITY", level = LogLevel.DEBUG)
    @Transactional
    public int getNewUser(String newuser) {
        String hql = "from Logins where user like :mUser";
        List<Logins> list = session.createQuery(hql, Logins.class)
                .setParameter("mUser", newuser + "%")
                .getResultList();
        return list.size();
    }

    @ObservedLog(category = "LOG-ENTITY", level = LogLevel.DEBUG)
    @Transactional
    public int getNewEmail(String newemail) {
        String hql = "from Logins where email like :mEmail";
        List<Logins> list = session.createQuery(hql, Logins.class)
                .setParameter("mEmail", newemail + "%")
                .getResultList();
        return list.size();
    }

    @ObservedLog(category = "LOG-ENTITY", level = LogLevel.DEBUG)
    @Transactional
    public Logins getUuid(String uuid) {
        String hql = "from Logins where uuid = :mUuid";
        return session.createQuery(hql, Logins.class)
                .setParameter("mUuid", uuid)
                .uniqueResult();
    }

    @ObservedLog(category = "LOG-ENTITY", level = LogLevel.DEBUG)
    @Transactional
    public Tokens getToken(String token1) {
        String hql = "from Tokens where token1 = :mToken1";
        return session.createQuery(hql, Tokens.class)
                .setParameter("mToken1", token1)
                .uniqueResult();
    }

    @ObservedLog(category = "LOG-ENTITY", level = LogLevel.DEBUG)
    @Transactional
    public Tokens getToken2(String token1) {
        String hql = "from Tokens where token1 = :mToken1";
        return session.createQuery(hql, Tokens.class)
                .setParameter("mToken1", token1)
                .uniqueResult();
    }

    @ObservedLog(category = "LOG-HIBERNATE-TX", level = LogLevel.INFO)
    @Transactional
    public void saveNewEvent(AddMovie newMovie) {
        session.persist(newMovie);
    }

    @ObservedLog(category = "LOG-HIBERNATE-TX", level = LogLevel.INFO)
    @Transactional
    public void saveNewEventState(AddMovie newMovie) {
        session.persist(newMovie);
    }

    @ObservedLog(category = "LOG-ENTITY", level = LogLevel.INFO)
    @Transactional
    public List<EventState> getNewEventsForUser(String user) {
        String hql = "select es from EventState es join es.logins lo " +
                "where lo.user = :mUser and es.state = 'not_read'";
        return session.createQuery(hql, EventState.class)
                .setParameter("mUser", user)
                .getResultList();
    }
}
