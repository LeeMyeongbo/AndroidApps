package com.alarm.newsalarmkt.database

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "AlarmDataTable")
class AlarmData : Parcelable {
    @PrimaryKey
    var id: Int = 0

    @ColumnInfo(name = "Topic")
    var alarmTopic: String = ""

    @ColumnInfo(name = "Voice")
    var voiceIdx: Int = 0

    @ColumnInfo(name = "Pitch")
    var pitch: Float = 0f

    @ColumnInfo(name = "Tempo")
    var tempo: Float = 0f

    @ColumnInfo(name = "VolumeSize")
    var volumeSize: Int = 0

    @ColumnInfo(name = "VibIntensity")
    var vibIntensity: Int = 0

    @ColumnInfo(name = "Date")
    var specificDateInMillis: Long = 0

    @ColumnInfo(name = "WeekBit")
    var periodicWeekBit: Int = 0

    @ColumnInfo(name = "Active")
    var isActive: Boolean = false

    constructor(`in`: Parcel) {
        id = `in`.readInt()
        alarmTopic = `in`.readString().toString()
        voiceIdx = `in`.readInt()
        pitch = `in`.readFloat()
        tempo = `in`.readFloat()
        volumeSize = `in`.readInt()
        vibIntensity = `in`.readInt()
        specificDateInMillis = `in`.readLong()
        periodicWeekBit = `in`.readInt()
        isActive = `in`.readBoolean()
    }

    constructor(id: Int) {
        this.id = id
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeString(alarmTopic)
        dest.writeInt(voiceIdx)
        dest.writeFloat(pitch)
        dest.writeFloat(tempo)
        dest.writeInt(volumeSize)
        dest.writeInt(vibIntensity)
        dest.writeLong(specificDateInMillis)
        dest.writeInt(periodicWeekBit)
        dest.writeBoolean(isActive)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is AlarmData) {
            return false
        }
        return id == other.id
    }

    override fun hashCode(): Int {
        return id
    }

    override fun toString(): String {
        return ("AlarmData(id=" + id + ", alarmTopic=" + alarmTopic + ", voiceIdx=" + voiceIdx
                + ", pitch=" + pitch + ", tempo=" + tempo + ", volumeSize=" + volumeSize
                + ", vibIntensity=" + vibIntensity + ", specificDateInMillis=" + specificDateInMillis
                + ", periodicWeekBit=" + periodicWeekBit + ", isActive=" + isActive + ")")
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<AlarmData?> = object : Parcelable.Creator<AlarmData?> {
            override fun createFromParcel(source: Parcel): AlarmData {
                return AlarmData(source)
            }

            override fun newArray(size: Int): Array<AlarmData?> {
                return arrayOfNulls(size)
            }
        }
    }
}
