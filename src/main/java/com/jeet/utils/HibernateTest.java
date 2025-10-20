package com.jeet.utils;

import java.util.ArrayList;
import java.util.List;

import com.jeet.broadcasting.eventModel.EventState;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.jeet.api.Devices;
import com.jeet.api.Logins;
import com.jeet.db.DAO;
import com.jeet.db.HibernateUtil;
import com.jeet.service.BookingHandlerImpl;

public class HibernateTest {

	public static void main(String[] args) throws Exception {

		SessionFactory factory = HibernateUtil.getSessionFactory();

		Statistics stats = factory.getStatistics(); 
		stats.setStatisticsEnabled(true);		

		BookingHandlerImpl bh = new BookingHandlerImpl();

		List<EventState> events = bh.getNewEventsForUser("GG");
		List<Devices> device = bh.getDevice("65f63602-6ddf-11e5-8441-71caa0c5f788");

		JSONObject json = new JSONObject();
		json.put("device", device);
		System.out.println("hello");

		Logins user = bh.getUser("II");
		
		if(user.getId() !=0) {
			System.out.println("hello");
		} else {
			
			System.out.println("no user");
		}
		
		int newuser_ = bh.getNewEmail("igeorge1982@gmail.com");

		System.out.println(newuser_);

		
        stats.logSummary();
        stats.getCacheRegionStatistics("movies");
        

	}
	
}