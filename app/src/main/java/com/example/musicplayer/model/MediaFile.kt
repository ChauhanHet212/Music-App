package com.example.musicplayer.model

import android.os.Parcel
import android.os.Parcelable

data class MediaFile(
    val id: String?,
    val displayName: String?,
    val size: String?,
    val duration: String?,
    val artist: String?,
    val path: String?
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(p0: Parcel, p1: Int) {
        p0.writeString(id)
        p0.writeString(displayName)
        p0.writeString(size)
        p0.writeString(duration)
        p0.writeString(artist)
        p0.writeString(path)
    }

    companion object CREATOR : Parcelable.Creator<MediaFile> {
        override fun createFromParcel(parcel: Parcel): MediaFile {
            return MediaFile(parcel)
        }

        override fun newArray(size: Int): Array<MediaFile?> {
            return arrayOfNulls(size)
        }
    }
}
