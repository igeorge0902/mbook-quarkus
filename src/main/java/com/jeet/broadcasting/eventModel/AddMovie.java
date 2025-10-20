package com.jeet.broadcasting.eventModel;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "events")
public class AddMovie implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	public int id;
	public String movie;
	public String date;
	public String venue;
	public String screeningId;
	public String screeningDatesId;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getMovie() {
		return movie;
	}

	public void setMovie(String movie) {
		this.movie = movie;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getVenue() {
		return venue;
	}

	public void setVenue(String venue) {
		this.venue = venue;
	}

	public String getScreeningId() {
		return screeningId;
	}

	public void setScreeningId(String screeningId) {
		this.screeningId = screeningId;
	}

	public String getScreeningDatesId() {
		return screeningDatesId;
	}

	public void setScreeningDatesId(String screeningDatesId) {
		this.screeningDatesId = screeningDatesId;
	}

}
