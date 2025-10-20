package com.jeet.api;

import jakarta.persistence.*;

@Entity
@Table(name="Tokens")
public class Tokens {
	@Id
	@GeneratedValue(strategy= GenerationType.AUTO)
    protected int id;
    protected String deviceId;
    protected String token1;
    protected String token2;
    protected String TIME_;
    
    /**
     * Gets the value of the Id property.
     * 
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the value of the Id property.
     * 
     */
    public void setId(int value) {
        this.id = value;
    }
    
    /**
     * Gets the value of the deviceId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDevice() {
        return deviceId;
    }

    /**
     * Sets the value of the deviceId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDevice(String value) {
        this.deviceId = value;
    }

    /**
     * Gets the value of the token1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getToken1() {
        return token1;
    }

    /**
     * Sets the value of the token1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setToken1(String value) {
        this.token1 = value;
    }

    /**
     * Gets the value of the token2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getToken2() {
        return token2;
    }

    /**
     * Sets the value of the token2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setToken2(String value) {
        this.token2 = value;
    }

	/**
	 * @return the deviceId
	 */
	public String getDeviceId() {
		return deviceId;
	}

	/**
	 * @param deviceId the deviceId to set
	 */
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	/**
	 * @return the tIME_
	 */
	public String getTIME_() {
		return TIME_;
	}

	/**
	 * @param tIME_ the tIME_ to set
	 */
	public void setTIME_(String tIME_) {
		TIME_ = tIME_;
	}

}


