package com.github.mjhassanpur.spotifystreamer.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.mjhassanpur.spotifystreamer.DividerItemDecoration;
import com.github.mjhassanpur.spotifystreamer.R;
import com.github.mjhassanpur.spotifystreamer.Types;
import com.github.mjhassanpur.spotifystreamer.activities.TopTracksActivity;
import com.github.mjhassanpur.spotifystreamer.adapters.ArtistAdapter;
import com.github.mjhassanpur.spotifystreamer.listeners.RecyclerItemClickListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import retrofit.RetrofitError;

public class ArtistSearchFragment extends Fragment {
    @InjectView(R.id.artist_recycler_view) RecyclerView mRecyclerView;
    @InjectView(R.id.message_container) View mDefaultMessageView;
    private ArtistAdapter mArtistAdapter;
    private List<Artist> mArtistList;
    private Gson mGson;
    private SpotifyService mSpotifyService;
    private final String KEY_ARTIST = "artist";
    private final String KEY_ARTISTS = "artists";
    private final static String LOG_TAG = "ArtistSearchFragment";

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSpotifyService = new SpotifyApi().getService();
        mGson = new Gson();
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artist_search, container, false);
        ButterKnife.inject(this, view);
        setupRecyclerView();
        if (savedInstanceState == null) {
            mRecyclerView.setAdapter(new ArtistAdapter(new ArrayList<Artist>()));
            showDefaultSearchMessage();
        } else {
            String json = savedInstanceState.getString(KEY_ARTISTS);
            if (json != null) {
                mArtistList = mGson.fromJson(json, Types.ARTIST_LIST);
                updateArtistAdapter(mArtistList);
            }
        }
        return view;
    }

    @Override public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_ARTISTS, mGson.toJson(mArtistList, Types.ARTIST_LIST));
    }

    private void setupRecyclerView() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), null));
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new OnItemClickListener()));
    }

    public void searchArtists(String query) {
        if (query != null && !query.trim().isEmpty()) {
            new SearchArtistsTask().execute(query);
        }
    }

    private boolean updateArtistAdapter(List<Artist> artists) {
        if (artists != null && !artists.isEmpty()) {
            mArtistAdapter = new ArtistAdapter(new ArrayList<>(artists));
            mRecyclerView.setAdapter(mArtistAdapter);
            showArtistList();
            return true;
        }
        return false;
    }

    public void showArtistList() {
        mDefaultMessageView.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    public void showDefaultSearchMessage() {
        mRecyclerView.setVisibility(View.GONE);
        mDefaultMessageView.setVisibility(View.VISIBLE);
    }

    private class OnItemClickListener extends RecyclerItemClickListener.SimpleOnItemClickListener {
        @Override public void onItemClick(View childView, int position) {
            Intent intent = new Intent(getActivity(), TopTracksActivity.class);
            Artist artist = mArtistList.get(position);
            intent.putExtra(KEY_ARTIST, mGson.toJson(artist, Types.ARTIST));
            startActivity(intent);
        }
    }

    private class SearchArtistsTask extends AsyncTask<String, Void, Void> {
        @Override protected Void doInBackground(String... params) {
            String query = params[0];
            try {
                mArtistList = mSpotifyService.searchArtists(query).artists.items;
            } catch (RetrofitError e) {
                Log.e(LOG_TAG, "An error occurred when attempting to retrieve artists");
            }
            return null;
        }

        @Override protected void onPostExecute(Void aVoid) {
            if (!updateArtistAdapter(mArtistList)) {
                mRecyclerView.setAdapter(new ArtistAdapter(new ArrayList<Artist>()));
                showDefaultSearchMessage();
                Toast.makeText(getActivity(), "No artists found. Please refine search.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
