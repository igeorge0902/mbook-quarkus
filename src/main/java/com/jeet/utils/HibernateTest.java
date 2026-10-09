package com.jeet.utils;

import com.jeet.api.Devices;
import com.jeet.api.Logins;
import com.jeet.db.DAO;
import com.jeet.broadcasting.eventModel.EventState;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

import java.util.List;

@QuarkusMain(name = "hibernate-test")
public class HibernateTest implements QuarkusApplication {

    @Inject
    DAO dao;

    @Inject
    EntityManagerFactory entityManagerFactory;

    @Override
    public int run(String... args) {
        Statistics stats = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        stats.setStatisticsEnabled(true);
        List<EventState> events = dao.getNewEventsForUser("GG");
        List<Devices> device = dao.getDevices("65f63602-6ddf-11e5-8441-71caa0c5f788");

        System.out.println(events);
        System.out.println(device);

        Logins user = dao.getUser("II");
        if (user.getId() != 0) {
            System.out.println("hello");
        } else {
            System.out.println("no user");
        }

        int newuser = dao.getNewEmail("igeorge1982@gmail.com");
        System.out.println(newuser);

        stats.logSummary();
        stats.getCacheRegionStatistics("movies");
        return 0;
    }
}