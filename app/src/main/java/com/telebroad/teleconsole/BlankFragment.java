package com.telebroad.teleconsole;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.telebroad.teleconsole.controller.dashboard.ChatActivity;

import java.io.IOException;
import java.util.Objects;


public class BlankFragment extends Fragment {
    public BlankFragment() {

    }

    private ActivityResultLauncher<Intent> fileLauncher;
    private int takeFlags;
    private FilesUri filesUri;
    public void setFilesUri(FilesUri filesUri) {
        this.filesUri = filesUri;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fileLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK ) {
                Intent data = result.getData();
                if (data != null) {
                    Uri uri = data.getData();
                    requireActivity(). getContentResolver().takePersistableUriPermission(uri, takeFlags);
                    String mimeType = requireActivity().getContentResolver().getType(uri);
                    if (mimeType != null && mimeType.startsWith("image/") ||mimeType != null&& mimeType.startsWith("video/")) {
                        Toast.makeText(getActivity(), "Please select the camara icon on the chat screen to get images and videos ", Toast.LENGTH_SHORT).show();
                        return;
                    }else{
                        try {
                            filesUri.getFilesUri(uri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_blank, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        CardView cardView = view.findViewById(R.id.cardViewImages);
        cardView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            takeFlags = intent.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            fileLauncher.launch(intent);
        });
        ImageView imageView = view.findViewById(R.id.hideFragment);
        imageView.setOnClickListener(v1 -> {
            ChatActivity chatActivity = (ChatActivity) getActivity();
            chatActivity.getBinding().fragmentChat.setVisibility(View.GONE);
        });
    }
    @FunctionalInterface
    public interface FilesUri{
        void getFilesUri(Uri uri) throws IOException;
    }
}