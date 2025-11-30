package com.reminder.wifireminder;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "routine_table")
public class Routine_Table implements Parcelable {

    @NonNull
    @PrimaryKey
    public String BSSID;

    public String name;

    public String SSID;

    public int sound_mode;

    public int volume;

    public int gamma;

    public String Todo_list;

    public String g_date;

    public String m_date;

    public Routine_Table(
            @NonNull String BSSID,
            String name,
            String SSID,
            int sound_mode,
            int volume,
            int gamma,
            String Todo_list,
            String g_date,
            String m_date
    ) {
        this.BSSID = BSSID;
        this.name = name;
        this.SSID = SSID;
        this.sound_mode = sound_mode;
        this.volume = volume;
        this.gamma = gamma;
        this.Todo_list = Todo_list;
        this.g_date = g_date;
        this.m_date = m_date;
    }

    protected Routine_Table(Parcel in) {
        BSSID = in.readString();
        name = in.readString();
        SSID = in.readString();
        sound_mode = in.readInt();
        volume = in.readInt();
        gamma = in.readInt();
        Todo_list = in.readString();
        g_date = in.readString();
        m_date = in.readString();
    }

    public static final Creator<Routine_Table> CREATOR = new Creator<>() {
        @Override
        public Routine_Table createFromParcel(Parcel in) {
            return new Routine_Table(in);
        }

        @Override
        public Routine_Table[] newArray(int size) {
            return new Routine_Table[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(BSSID);
        dest.writeString(name);
        dest.writeString(SSID);
        dest.writeInt(sound_mode);
        dest.writeInt(volume);
        dest.writeInt(gamma);
        dest.writeString(Todo_list);
        dest.writeString(g_date);
        dest.writeString(m_date);
    }
}
