package com.jeet.api;

import jakarta.persistence.*;

@Entity
@Table(name="devices")
public class Devices {
	@Id
	@GeneratedValue(strategy= GenerationType.AUTO)
    protected int id;
    protected String deviceId;
    protected String uuid;

//TODO: get back the device list
    
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
     * Gets the value of the uuid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Sets the value of the uuid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUuid(String value) {
        this.uuid = value;
    }

}


