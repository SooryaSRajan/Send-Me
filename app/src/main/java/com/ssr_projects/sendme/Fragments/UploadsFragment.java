package com.ssr_projects.sendme.Fragments;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.icu.text.DateFormat;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ssr_projects.sendme.Adapter.UploadRecyclerAdapter;
import com.ssr_projects.sendme.MainActivity;
import com.ssr_projects.sendme.R;
import com.ssr_projects.sendme.Values;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import static android.app.Activity.RESULT_OK;
import static com.ssr_projects.sendme.Values.requestFilePickerCode;

public class UploadsFragment extends Fragment {

    ArrayList<HashMap<String, String>> arrayList;
    UploadRecyclerAdapter uploadRecyclerAdapter;

    public UploadsFragment() {
        this.arrayList = new ArrayList<>();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        this.uploadRecyclerAdapter = new UploadRecyclerAdapter(this.arrayList, getActivity());
        View view = inflater.inflate(R.layout.fragment_uploads, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.upload_recycler_view);
        FloatingActionButton button = view.findViewById(R.id.send_data);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(uploadRecyclerAdapter);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                try {
                    startActivityForResult(intent, requestFilePickerCode);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getActivity(), "Please install a file manager", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == requestFilePickerCode && resultCode == RESULT_OK){
            if(data != null){
                Uri uri = data.getData();

                Log.e(getClass().getName(), "onActivityResult: Extension" + GetFileExtension(uri));
                float dataSize = 0;
                try {
                    InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);
                    Log.e(getClass().getName(), "onActivityResult: " + inputStream.available() );
                    dataSize = bytesToMeg(inputStream.available());
                    Log.e(getClass().getName(), "onActivityResult: " +  getFileName(uri));
                    Log.e(getClass().getName(), "onActivityResult: " + dataSize );
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ContentResolver cr = getActivity().getContentResolver();
                String mime = null, fileName = null;
                if (uri != null) {
                    fileName = getFileName(uri);
                    mime = cr.getType(uri);
                }

                Random rand = new Random();
                Values.requestList.get(0).replace("KEY", String.valueOf(rand.nextInt()));

                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("NAME", fileName);
                hashMap.put("DATE", DateFormat.getDateInstance().format(new Date()));
                hashMap.put("SIZE", dataSize + " mb");
                hashMap.put("URI", String.valueOf(uri));
                hashMap.put("MIME", mime);
                hashMap.put("URI", uri.toString());

                arrayList.add(hashMap);
                Values.requestList.add(hashMap);
                Log.e(getClass().getName(), "onActivityResult: name " + arrayList.get(arrayList.size() - 1).get("NAME"));
                uploadRecyclerAdapter.notifyItemInserted(arrayList.size() - 1);

                Log.e(getClass().getName(), "onActivityResult: MIME " + mime );
                Log.e(getClass().getName(), "onActivityResult: FILE " + fileName );

            }
        }
    }

    private static final float  MEGABYTE = 1024L * 1024L;

    public static float bytesToMeg(long bytes) {
        Log.e("LOG ", "bytesToMeg: " + bytes );
        return bytes / MEGABYTE ;
    }

    public String GetFileExtension(Uri uri)
    {
        ContentResolver contentResolver = getActivity().getContentResolver();
        MimeTypeMap mimeTypeMap=MimeTypeMap.getSingleton();
        // Return file Extension
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }


}