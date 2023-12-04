package com.telebroad.teleconsole.controller;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.viewmodels.FileViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import static android.widget.Toast.*;
import static com.google.common.base.Strings.isNullOrEmpty;

public class FilesActivity extends AppCompatActivity {

    public static final String EXTRA_FOLDER = "com.telebroad.teleconsole.filesactivity.folder";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files);

        String filename = getIntent().getExtras().getString(EXTRA_FOLDER);
       // android.util.Log.d("Files01", "filename " + filename);
        if (isNullOrEmpty(filename)) {
            finish();
            return;
        }
        File folder = new File(filename);
        RecyclerView recyclerView = findViewById(R.id.files_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        setTitle(folder.getName());
        recyclerView.setAdapter(new FileRecyclerAdapter(folder.listFiles()));

    }

    class FileRecyclerAdapter extends RecyclerView.Adapter<FileRecyclerAdapter.FileItem> {

        File[] files;

        ArrayList<File> selectedFiles = new ArrayList<>();
        boolean isSelectedMode = false;

        FileRecyclerAdapter(File[] files) {
            this.files = files;
            Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
        }

        @NonNull
        @Override
        public FileRecyclerAdapter.FileItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new FileRecyclerAdapter.FileItem(LayoutInflater.from(FilesActivity.this).inflate(R.layout.item_file, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull FileRecyclerAdapter.FileItem holder, int position) {

            File file = files[position];

            FileViewModel fileViewModel = ViewModelProviders.of(FilesActivity.this).get(file.getName(), FileViewModel.class);
            fileViewModel.setFile(file);

            holder.nameView.setText(fileViewModel.getName());
            holder.iconView.setImageDrawable(fileViewModel.getIcon());
            holder.dateView.setText(fileViewModel.getDate());
            holder.sizeView.setText(fileViewModel.getSize());

            holder.itemView.setOnClickListener(v -> {
                if (file.isDirectory()){
                    Intent i = new Intent(FilesActivity.this, FilesActivity.class);
                    i.putExtra(FilesActivity.EXTRA_FOLDER, file.getAbsolutePath());
                    startActivity(i);
                }else {
                    try {
                        Intent clickIntent = fileViewModel.getOnClickIntent();
                        if (clickIntent != null){
                            startActivity(fileViewModel.getOnClickIntent());
                        }else{
                            Toast.makeText(FilesActivity.this, "Unable to find file", LENGTH_LONG).show();
                        }
                    } catch (ActivityNotFoundException anfe) {
                        makeText(FilesActivity.this, "There is no app that can open this file", LENGTH_LONG).show();
                    }
                }
            });

            holder.itemView.setOnLongClickListener(v -> {
                toggleSelected(file);
                return false;
            });
        }

        private void toggleSelected(File file){
            if (isSelected(file)){
                selectedFiles.remove(file);
            }else{
                selectedFiles.add(file);
            }
        }


        private boolean isSelected(File file){
            return selectedFiles.contains(file);
        }
        @Override
        public int getItemCount() {
            return files.length;
        }

        class FileItem extends RecyclerView.ViewHolder {

            ImageView iconView;
            TextView nameView, sizeView, dateView;

            FileItem(@NonNull View itemView) {
                super(itemView);
                iconView = itemView.findViewById(R.id.iconImageView);
                nameView = itemView.findViewById(R.id.nameTextView);
                sizeView = itemView.findViewById(R.id.sizeTextView);
                dateView = itemView.findViewById(R.id.dateTextView);
            }

//            private void updateBackground(File file) {
//                if (item != null){
//                    this.itemView.setBackgroundResource(selectedMessageModels.contains(item) ? R.color.oldColorPrimaryLight : R.drawable.bg_rectangle_white_ripple);
//                }
//            }
        }
    }

}
