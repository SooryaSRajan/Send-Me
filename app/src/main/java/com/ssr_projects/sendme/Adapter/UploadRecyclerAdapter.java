package com.ssr_projects.sendme.Adapter;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.ssr_projects.sendme.BuildConfig;
import com.ssr_projects.sendme.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class UploadRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<HashMap<String, String>> arrayList;
    Activity activity;

    public UploadRecyclerAdapter(ArrayList<HashMap<String, String>> arrayList, Activity activity) {
        this.arrayList = arrayList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new DataHolder(inflater.inflate(R.layout.upload_list_element, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Log.d(getClass().getName(), "onBindViewHolder: Name " + arrayList.get(position).get("NAME"));
        Log.d(getClass().getName(), "onBindViewHolder: Size " + arrayList.get(position).get("SIZE"));
        ((DataHolder) holder).fileNameView.setText(arrayList.get(position).get("NAME"));
        ((DataHolder) holder).fileSizeView.setText(arrayList.get(position).get("SIZE"));
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    private class DataHolder extends RecyclerView.ViewHolder {
        TextView fileNameView, fileSizeView;

        public DataHolder(View inflate) {
            super(inflate);
            fileNameView = inflate.findViewById(R.id.file_name);
            fileSizeView = inflate.findViewById(R.id.file_size);
            inflate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (arrayList.get(getAdapterPosition()).get("URI") != null) {
                        ContentResolver cr = activity.getContentResolver();

                        //Uri uri = Uri.fromFile(new File(arrayList.get(getAdapterPosition()).get("URI")));
                        Uri uri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".provider", new File(arrayList.get(getAdapterPosition()).get("URI")));
                        String mimeType = cr.getType(uri);

                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(uri, mimeType);
                        activity.startActivity(intent);
                    }
                }
            });

        }
    }

}

