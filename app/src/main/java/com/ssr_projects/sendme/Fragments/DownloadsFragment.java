package com.ssr_projects.sendme.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ssr_projects.sendme.Adapter.UploadRecyclerAdapter;
import com.ssr_projects.sendme.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class DownloadsFragment extends Fragment {

    UploadRecyclerAdapter uploadRecyclerAdapter;
    ArrayList<HashMap<String, String>> downloadsArrayList;


    public DownloadsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        downloadsArrayList = new ArrayList<>();
        View view = inflater.inflate(R.layout.fragment_downloads, container, false);
        this.uploadRecyclerAdapter = new UploadRecyclerAdapter(this.downloadsArrayList, getActivity());
        RecyclerView recyclerView = view.findViewById(R.id.download_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(uploadRecyclerAdapter);
        return view;
    }


    public void addData(final HashMap<String, String> hashMap){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                downloadsArrayList.add(hashMap);
                uploadRecyclerAdapter.notifyItemInserted(downloadsArrayList.size() - 1);
            }
        });
    }
}