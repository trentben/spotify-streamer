/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.mjhassanpur.spotifystreamer.media;

import android.support.v4.media.MediaMetadataCompat;

import com.github.mjhassanpur.spotifystreamer.utils.LogHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Utility class to get a list of media items (music tracks).
 *
 * @see <a href="https://github.com/googlesamples/android-UniversalMusicPlayer/blob/master/mobile/src/main/java/com/example/android/uamp/model/MusicProvider.java"></a>
 */
public class MediaProvider {

    private static final String TAG = LogHelper.makeLogTag(MediaProvider.class);

    public static final String CUSTOM_METADATA_TRACK_SOURCE = "__SOURCE__";

    private Map<String, MediaMetadataCompat> mMusicListById;
    private final Map<Integer, String> mMusicListOrder;
    private List<Track> mMusicList;

    enum State {
        NON_INITIALIZED, INITIALIZED
    }

    private volatile State mCurrentState = State.NON_INITIALIZED;

    public MediaProvider() {
        mMusicListById = new HashMap<>();
        mMusicListOrder = new HashMap<>();
    }

    /**
     * Return the MediaMetadataCompat for the given mediaID.
     *
     * @param mediaId The unique media ID.
     */
    public MediaMetadataCompat getMusic(String mediaId) {
        return mMusicListById.containsKey(mediaId) ? mMusicListById.get(mediaId) : null;
    }

    public Iterable<MediaMetadataCompat> getMusicList() {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        return getSortedMusicList();
    }

    private ArrayList<MediaMetadataCompat> getSortedMusicList() {
        ArrayList<MediaMetadataCompat> sortedMusicList = new ArrayList<>();
        for (int i = 0, n = mMusicList.size(); i < n; i++) {
            String mediaId = mMusicListOrder.get(i);
            sortedMusicList.add(mMusicListById.get(mediaId));
        }
        return sortedMusicList;
    }

    public void setMusicList(List<Track> tracks) {
        if (tracks != null) {
            if (isEqual(tracks, mMusicList))
                return;
            mMusicList = tracks;
            mMusicListById.clear();
            mMusicListOrder.clear();
            for (int i = 0, n = tracks.size(); i < n; i++) {
                MediaMetadataCompat item = build(tracks.get(i));
                String mediaId = item.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
                mMusicListById.put(mediaId, item);
                mMusicListOrder.put(i, mediaId);
            }
            mCurrentState = State.INITIALIZED;
        }
    }

    public boolean isEqual(List<Track> arg0, List<Track> arg1) {
        if (arg0 != null && !arg0.isEmpty() && arg1 != null && !arg1.isEmpty()) {
            if (arg0.get(0).id.equals(arg1.get(0).id))
                return true;
        }
        return false;
    }

    private MediaMetadataCompat build(Track track) {
        LogHelper.d(TAG, "Found music track: ", track.name);
        // TODO: Move the track source out of MediaMetadataCompat
        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, track.id)
                .putString(CUSTOM_METADATA_TRACK_SOURCE, track.preview_url)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, track.album.name)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, track.artists.get(0).name)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 30000) // track previews are only 30 secs
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, track.album.images.get(0).url)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, track.name)
                .build();
    }
}
