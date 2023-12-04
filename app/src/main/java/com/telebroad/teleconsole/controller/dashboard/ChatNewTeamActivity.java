package com.telebroad.teleconsole.controller.dashboard;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.chat.ChatWebSocket;
import com.telebroad.teleconsole.chat.client.SetMessage;
import com.telebroad.teleconsole.chat.client.pubMessage;
import com.telebroad.teleconsole.db.models.ChannelDB;
import com.telebroad.teleconsole.databinding.ActivityChatAddTeamBinding;
import java.util.HashMap;
import java.util.Map;

public class ChatNewTeamActivity extends AppCompatActivity {
    private ActivityChatAddTeamBinding binding;
    private SetMessage setMessage;
    private boolean shouldDisable = false;
    private String channelDB;
    private final Gson gson = new Gson();
    private ChannelDB ChannelDBModels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatAddTeamBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        shouldDisable = getIntent().getBooleanExtra("shouldDisable",false);
            channelDB  = getIntent().getStringExtra("channelDB");
            ChannelDBModels = gson.fromJson(channelDB, new TypeToken<ChannelDB>() {}.getType());
        if (!shouldDisable) {
            binding.btnCreate.setOnClickListener(v -> {
                if (!binding.editTextDescription.getText().toString().isEmpty() && !binding.editTextName.getText().toString().isEmpty()) {
                    if (setMessage != null) {
                        pubMessage pubMessage = new pubMessage();
                        pubMessage.setId("newTopic");
                        pubMessage.setTopic("new");
                        pubMessage.setSet(setMessage);
                        ChatWebSocket.getInstance().sendObject("sub", pubMessage);
                        setMessage = null;
                        finish();
                    } else {
                        Toast.makeText(this, "Please select one of the below options", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Must have a name and description", Toast.LENGTH_SHORT).show();
                }
            });
            binding.radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                if (!binding.editTextDescription.getText().toString().isEmpty() && !binding.editTextName.getText().toString().isEmpty()) {
                    switch (checkedId) {
                        case R.id.rbPrivacy:
                            Map<String, String> defacs = new HashMap<>();
                            defacs.put("anon", "n");
                            defacs.put("auth", "RWPSD");
                            Map<String, String> publicInfo = new HashMap<>();
                            publicInfo.put("description", binding.editTextDescription.getText().toString());
                            publicInfo.put("fn", binding.editTextName.getText().toString());
                            SetMessage.Desc desc = new SetMessage.Desc(defacs, publicInfo);
                            setMessage = new SetMessage(desc);
                            break;
                        case R.id.rbPublic:
                            Map<String, String> defacs1 = new HashMap<>();
                            defacs1.put("anon", "n");
                            defacs1.put("auth", "JRWPSD");
                            Map<String, String> publicInfo1 = new HashMap<>();
                            publicInfo1.put("description", binding.editTextDescription.getText().toString());
                            publicInfo1.put("fn", binding.editTextName.getText().toString());
                            SetMessage.Desc desc1 = new SetMessage.Desc(defacs1, publicInfo1);
                            setMessage = new SetMessage(desc1);
                            break;
                    }
                } else {
                    Toast.makeText(this, "Must have a name and description", Toast.LENGTH_SHORT).show();
                }
            });
        }else {
            binding.editTextName.setText(ChannelDBModels.getName());
            binding.editTextDescription.setText(ChannelDBModels.getDescription());
            binding.editTextName.setEnabled(false);
            binding.editTextDescription.setEnabled(false);
            binding.btnCreate.setEnabled(false);
            if (ChannelDBModels.isNotPrivate()){
                binding.rbPrivacy.setChecked(false);
                binding.rbPrivacy.setEnabled(false);
                binding.rbPublic.setChecked(true);
                binding.rbPublic.setEnabled(false);
            }else {
                binding.rbPrivacy.setChecked(true);
                binding.rbPrivacy.setEnabled(false);
                binding.rbPublic.setChecked(false);
                binding.rbPublic.setEnabled(false);
            }
        }
    }
    @Override
    protected void onPause() {super.onPause();overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);}
}