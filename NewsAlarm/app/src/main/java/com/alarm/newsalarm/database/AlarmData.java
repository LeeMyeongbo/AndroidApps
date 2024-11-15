package com.alarm.newsalarm.database;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "AlarmDataTable")
public class AlarmData implements Parcelable {

    @PrimaryKey
    private long id;

    @ColumnInfo(name = "Name")
    private String alarmName = "";

    @ColumnInfo(name = "Topic")
    private String alarmTopic = "";

    @ColumnInfo(name = "VolumeSize")
    private float volumeSize;

    @ColumnInfo(name = "VibIntensity")
    private int vibIntensity;

    @ColumnInfo(name = "Date")
    private long specificDateInMillis = -1;

    @ColumnInfo(name = "WeekBit")
    private byte periodicWeekBit;

    @ColumnInfo(name = "Hour")
    private int periodicTimeInMin = -1;

    @ColumnInfo(name = "Active")
    private boolean isActive = true;

    public static final Creator<AlarmData> CREATOR = new Creator<>() {

        @Override
        public AlarmData createFromParcel(Parcel source) {
            return new AlarmData(source);
        }

        @Override
        public AlarmData[] newArray(int size) {
            return new AlarmData[size];
        }
    };

    protected AlarmData(Parcel in) {
        id = in.readLong();
        alarmName = in.readString();
        alarmTopic = in.readString();
        volumeSize = in.readFloat();
        vibIntensity = in.readInt();
        specificDateInMillis = in.readLong();
        periodicWeekBit = in.readByte();
        periodicTimeInMin = in.readInt();
        isActive = in.readBoolean();
    }

    public AlarmData(
        long id, String alarmName, String alarmTopic, float volumeSize, int vibIntensity
    ) {
        setId(id);
        setAlarmName(alarmName);
        setAlarmTopic(alarmTopic);
        setVolumeSize(volumeSize);
        setVibIntensity(vibIntensity);
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setAlarmName(String name) {
        alarmName = name;
    }

    public String getAlarmName() {
        return alarmName;
    }

    public void setAlarmTopic(String topic) {
        alarmTopic = topic;
    }

    public String getAlarmTopic() {
        return alarmTopic;
    }

    public void setVolumeSize(float volumeSize) {
        this.volumeSize = volumeSize;
    }

    public float getVolumeSize() {
        return volumeSize;
    }

    public void setVibIntensity(int vibIntensity) {
        this.vibIntensity = vibIntensity;
    }

    public int getVibIntensity() {
        return vibIntensity;
    }

    public void setSpecificDateInMillis(long date) {
        this.specificDateInMillis = date;
    }

    public long getSpecificDateInMillis() {
        return specificDateInMillis;
    }

    public void setPeriodicWeekBit(byte bit) {
        this.periodicWeekBit = bit;
    }

    public byte getPeriodicWeekBit() {
        return periodicWeekBit;
    }

    public void setPeriodicTimeInMin(int periodicTimeInMin) {
        this.periodicTimeInMin = periodicTimeInMin;
    }

    public int getPeriodicTimeInMin() {
        return periodicTimeInMin;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public boolean isActive() {
        return isActive;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(alarmName);
        dest.writeString(alarmTopic);
        dest.writeFloat(volumeSize);
        dest.writeInt(vibIntensity);
        dest.writeLong(specificDateInMillis);
        dest.writeByte(periodicWeekBit);
        dest.writeInt(periodicTimeInMin);
        dest.writeBoolean(isActive);
    }
}
