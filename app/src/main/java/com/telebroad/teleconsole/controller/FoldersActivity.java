package com.telebroad.teleconsole.controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.helpers.Utils;

import java.io.File;

public class FoldersActivity extends AppCompatActivity {

    boolean hasFiles;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folders);

        File teleconsoleFile = new File(Utils.getRootFolder().getAbsolutePath(),"TeleConsole");
        RecyclerView recyclerView = findViewById(R.id.files_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        if (teleconsoleFile.listFiles() != null){
            hasFiles = true;
            recyclerView.setAdapter(new FileRecyclerAdapter(teleconsoleFile.listFiles()));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!hasFiles){
            new AlertDialog.Builder(this, R.style.DialogStyle).
                    setTitle("No Files").
                    setMessage("You don't have any files yet").
                    setNeutralButton(android.R.string.ok, (DialogInterface dialog, int which) -> dialog.dismiss()).
                    setOnDismissListener(dialog -> finish()).create().show();
        }
    }

    class FileRecyclerAdapter extends RecyclerView.Adapter<FileRecyclerAdapter.FileItem> {

        File[] files;


        FileRecyclerAdapter (File[] files){
            this.files = files;
        }
        @NonNull
        @Override
        public FileItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(FoldersActivity.this ).inflate(R.layout.item_folder, parent, false);
            return new FileItem(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FileItem holder, int position) {
            holder.fileName.setText(files[position].getName());
            holder.itemView.setOnClickListener(v -> {
               // android.util.Log.d("Files01", "clicked");
                Intent i = new Intent(FoldersActivity.this, FilesActivity.class);
                i.putExtra(FilesActivity.EXTRA_FOLDER, files[position].getAbsolutePath());
                startActivity(i);
            });
        }

        @Override
        public int getItemCount() {
            return files == null ? 0 : files.length;
        }

        class FileItem extends RecyclerView.ViewHolder{
            TextView fileName;
            FileItem(@NonNull View itemView) {
                super(itemView);
                fileName = itemView.findViewById(R.id.folderName);
            }

        }
    }

}

