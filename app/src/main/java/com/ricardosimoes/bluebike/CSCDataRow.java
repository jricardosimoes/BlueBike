package com.ricardosimoes.bluebike;

import java.sql.Timestamp;

/**
 * Created by ricardo on 23/12/14.
 */
public class CSCDataRow {
    private int id;
    private Timestamp timestamp;
    private int cadence;
    private float speed;

    public CSCDataRow(int id, Timestamp timestamp, int cadence, float speed) {
        this.id = id;
        this.timestamp = timestamp;
        this.cadence = cadence;
        this.speed = speed;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public int getCadence() {
        return cadence;
    }

    public void setCadence(int cadence) {
        this.cadence = cadence;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    @Override
    public String toString() {
        return id + "\t" + timestamp + "\t" + cadence + "\t" + speed +  ';';
    }

    public String header() {
        return "id\ttimestamp\tcadence\tspeed;";
    }
}
