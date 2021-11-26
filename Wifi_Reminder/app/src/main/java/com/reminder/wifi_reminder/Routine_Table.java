package com.reminder.wifi_reminder;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "routine_table")
public class Routine_Table implements Parcelable {
    @NonNull
    @PrimaryKey
    public String BSSID;            // wifi BSSID (라우터의 물리적 주소, 키로 사용)

    public String name;             // 루틴 이름

    public String SSID;             // wifi SSID (사용하는 wifi 이름)

    public int sound_mode;          // 소리 모드

    public int volume;              // 음량

    public int gamma;               // 밝기

    public String Todo_list;        // 해야할 일

    public String g_date;           // 생성된 날짜

    public String m_date;           // 수정된 날짜

    public Routine_Table(@NonNull String BSSID, String name, String SSID, int sound_mode, int volume, int gamma, String Todo_list
                        , String g_date, String m_date) {
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

    public static final Creator<Routine_Table> CREATOR = new Creator<Routine_Table>() {
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