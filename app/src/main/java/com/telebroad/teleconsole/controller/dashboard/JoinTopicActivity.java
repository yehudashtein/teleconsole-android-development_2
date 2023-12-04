package com.telebroad.teleconsole.controller.dashboard;

import static android.content.Intent.EXTRA_STREAM;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.chat.ChatWebSocket;
import com.telebroad.teleconsole.chat.client.JoinTopicModel;
import com.telebroad.teleconsole.chat.client.SubMessage;
import com.telebroad.teleconsole.db.ChatDatabase;
import com.telebroad.teleconsole.db.models.ChannelDB;
import com.telebroad.teleconsole.databinding.ActivityJoinTopicBinding;
import com.telebroad.teleconsole.databinding.AddTeamItemBinding;
import com.telebroad.teleconsole.databinding.ChatAddUserBinding;
import com.telebroad.teleconsole.helpers.SettingsHelper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class JoinTopicActivity extends AppCompatActivity {
    private ActivityJoinTopicBinding binding;
    private AddTeamAdapter adapter;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityJoinTopicBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Gson gson = new Gson();
        imageUri = getIntent().getParcelableExtra(EXTRA_STREAM);
        String joinTopicJson = getIntent().getStringExtra("getOnlyGrp");
        String shareImageList = getIntent().getStringExtra("shareList");
        List<ChannelDB> joinTopicModelsShare = gson.fromJson(shareImageList, new TypeToken<List<ChannelDB>>() {}.getType());
        List<JoinTopicModel> joinTopicModels = gson.fromJson(joinTopicJson, new TypeToken<List<JoinTopicModel>>() {}.getType());
        if (joinTopicModels != null && joinTopicModels.size()>0 && "getOnlyP2p".equals(joinTopicModels.get(0).getId())){
            getSupportActionBar().setTitle("Start a direct chat");
        }
        if (joinTopicModelsShare != null && joinTopicModelsShare.size()>0 ){
            getSupportActionBar().setTitle("Share image with...");
            List<JoinTopicModel> shareTopicModel = joinTopicModelsShare.stream().map(JoinTopicModel::createInstance).collect(Collectors.toList());
            Toast.makeText(this, shareTopicModel.get(0).getUser(), Toast.LENGTH_SHORT).show();
            adapter = new AddTeamAdapter(new DIFF_CALLBACK(),null);
            binding.rvGroups.setLayoutManager(new LinearLayoutManager(JoinTopicActivity.this));
            binding.rvGroups.setAdapter(adapter);
            adapter.submitList(shareTopicModel);
        }
        List<JoinTopicModel> joinTopicModelList = new ArrayList<>();
        ListenableFuture<List<ChannelDB>> HasJoined = ChatDatabase.getInstance().channelDao().getTeamChannelsListenableFuture();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Futures.addCallback(HasJoined,
                    new FutureCallback<List<ChannelDB>>() {
                        @Override
                        public void onSuccess(List<ChannelDB> result) {
                            if (joinTopicModels != null) {
                                adapter = new AddTeamAdapter(new DIFF_CALLBACK(), result);
                                binding.rvGroups.setLayoutManager(new LinearLayoutManager(JoinTopicActivity.this));
                                binding.rvGroups.setAdapter(adapter);
                                adapter.submitList(joinTopicModels);
                            }
                        }

                        @Override
                        public void onFailure(Throwable t) {}
                    },this.getMainExecutor());
        }
//        AddTeamAdapter adapter = new AddTeamAdapter(new DIFF_CALLBACK(),JoinedTopics);
//        binding.rvGroups.setLayoutManager(new LinearLayoutManager(this));
//        binding.rvGroups.setAdapter(adapter);
//        adapter.submitList(joinTopicModels);

        binding.editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                joinTopicModelList.clear();
                if (joinTopicModels != null) {
                    for (JoinTopicModel s1 : joinTopicModels) {
                        if (s1.getFn().toLowerCase().contains(s.toString().toLowerCase())) {
                            joinTopicModelList.add(s1);
                        }
                    }
                    if (adapter != null) {
                        adapter.submitList(joinTopicModelList);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (binding.editTextSearch.getText().toString().isEmpty()) {
                    adapter.submitList(joinTopicModels);
                }
            }
        });
    }
    static class DIFF_CALLBACK extends DiffUtil.ItemCallback<JoinTopicModel>{

        @Override
        public boolean areItemsTheSame(@NonNull JoinTopicModel oldItem, @NonNull JoinTopicModel newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull JoinTopicModel oldItem, @NonNull JoinTopicModel newItem) {
            return oldItem.getFn().equals(newItem.getFn());
        }
    }
    class AddTeamAdapter extends ListAdapter<JoinTopicModel,AddTeamAdapter.RootViewHolder>{
        private List<JoinTopicModel> joinTopicModels;
        private List<ChannelDB> JoinedTopics = new ArrayList<>();
        protected AddTeamAdapter(@NonNull DiffUtil.ItemCallback<JoinTopicModel> diffCallback,List<ChannelDB> JoinedTopics) {
            super(diffCallback);
            if (joinTopicModels != null) {
                Set<ChannelDB> uniqueSet = new HashSet<>(JoinedTopics);
                this.JoinedTopics = new ArrayList<>(uniqueSet);
                notifyDataSetChanged();
            }
        }

        @Override
        public int getItemViewType(int position) {
            if ("getOnlyP2p".equals(getCurrentList().get(position).getId())|| "shareList".equals(getCurrentList().get(position).getId())) return 1;
          //  else if ("getAllTopics".equals(getCurrentList().get(position).getId()))return 2;
            else return super.getItemViewType(position);
        }

        @Override
        public void submitList(@Nullable List<JoinTopicModel> list) {
            super.submitList(list);
            joinTopicModels = list;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public RootViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == 1)
               return new AddUserViewHolder(ChatAddUserBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
            else return new JoinTeamViewHolder(AddTeamItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }
        private void onBindViewHolder(@NonNull AddUserViewHolder holder, int position){
            JoinTopicModel joinTopicModel = getCurrentList().get(position);
            if (joinTopicModel.getPhoto() != null && joinTopicModel.getPhoto().equals("") || joinTopicModel.getPhoto()==null
            && joinTopicModel.getUser().startsWith("usr"))
                holder.binding.imageView32.setImageDrawable(SettingsHelper.grtFirstTwoInitialsWithColor(joinTopicModel.getFn()));
            else if (joinTopicModel.getUser().startsWith("grp")){
                if (joinTopicModel.getMode().toLowerCase().contains("j")){
                    holder.binding.imageView32.setImageResource(R.drawable.ic_outline_group_24);
                }else {
                    holder.binding.imageView32.setImageResource(R.drawable.ic_outline_lock_24);
                }
            }
            else {
                if (joinTopicModel.getPhoto().contains("}")) {
                    String ref = joinTopicModel.getPhoto().substring(joinTopicModel.getPhoto().indexOf("=") + 1).replace("}", "");
                    Glide.with(JoinTopicActivity.this).load(ref).circleCrop().into(holder.binding.imageView32);
                }else if (joinTopicModel.getPhoto() != null)Glide.with(JoinTopicActivity.this).load(joinTopicModel.getPhoto()).circleCrop().into(holder.binding.imageView32);
            }
            holder.binding.textView46.setText(joinTopicModel.getFn());
            holder.binding.getRoot().setOnClickListener(v -> {
                ChatWebSocket.getInstance().subscribe(joinTopicModel.getUser());
                Intent intent = new Intent(JoinTopicActivity.this ,ChatActivity.class);
                intent.putExtra(ChatActivity.CURRENT_CHAT_EXTRA, joinTopicModel.getUser());
                intent.putExtra(Intent.EXTRA_STREAM,imageUri);
                startActivity(intent);
                JoinTopicActivity.this.finish();
            });
        }
        private void onBindViewHolder(@NonNull JoinTeamViewHolder holder, int position){
            JoinTopicModel joinTopicModel = getCurrentList().get(position);
            holder.binding.textView46.setText(joinTopicModel.getFn());
            if (joinTopicModels.get(position).getSubCount() == 1){
                holder.binding.txtNomMembers.setText(joinTopicModel.getSubCount()+" Member");
            }else {
                holder.binding.txtNomMembers.setText(joinTopicModel.getSubCount()+" Members");
            }
            String mode = joinTopicModel.getMode();
            if ("JRWPASD".equals(mode) ||"JRWPS".equals(mode) ||"JRWPAD".equals(mode)||"JRWPSD".equals(mode)|| "JRWPASDO".equals(mode) ){
                holder.binding.imageView32.setImageResource(R.drawable.ic_outline_group_24);
            }else {
                holder.binding.imageView32.setImageResource(R.drawable.ic_outline_lock_24);
            }
            boolean isJoined = false;
            for (ChannelDB c : JoinedTopics) {
                if (joinTopicModel.getTopic().equals(c.getTopic())) {
                    isJoined = true;
                    break;
                }
            }
            if (isJoined) {
                holder.binding.joined.setVisibility(View.VISIBLE);
                holder.binding.joined.setText("Joined");
                holder.binding.buttonJoin.setVisibility(View.GONE);
                holder.binding.buttonView.setVisibility(View.GONE);
                holder.binding.btnAccess.setVisibility(View.VISIBLE);
            } else {
                holder.binding.joined.setVisibility(View.GONE);
                holder.binding.buttonJoin.setVisibility(View.VISIBLE);
                holder.binding.buttonView.setVisibility(View.VISIBLE);
                holder.binding.btnAccess.setVisibility(View.GONE);
            }
            holder.binding.btnAccess.setOnClickListener(v -> {
                ChatWebSocket.getInstance().subscribe(joinTopicModel.getTopic());
                Intent intent = new Intent(JoinTopicActivity.this, ChatActivity.class);
                intent.putExtra(ChatActivity.CURRENT_CHAT_EXTRA,joinTopicModel.getTopic());
                startActivity(intent);
            });
            holder.binding.buttonJoin.setOnClickListener(v -> {
                SubMessage subMessage = new SubMessage();
                subMessage.setId("joinNewTeam");
                subMessage.setTopic(joinTopicModel.getTopic());
                ChatWebSocket.getInstance().sendObject("sub",subMessage);
                holder.binding.joined.setVisibility(View.VISIBLE);
                holder.binding.joined.setText("Joined");
                holder.binding.buttonJoin.setVisibility(View.GONE);
                holder.binding.buttonView.setVisibility(View.GONE);
                holder.binding.btnAccess.setVisibility(View.VISIBLE);
            });
            holder.binding.buttonView.setOnClickListener(v -> {

            });
        }

        @Override
        public void onBindViewHolder(@NonNull RootViewHolder holder, int position) {
            if (holder instanceof JoinTeamViewHolder) onBindViewHolder((JoinTeamViewHolder) holder, position);
            else onBindViewHolder((AddUserViewHolder) holder, position);
        }
      class JoinTeamViewHolder extends RootViewHolder {
           private final AddTeamItemBinding binding;
           private JoinTeamViewHolder(AddTeamItemBinding binding) {super(binding.getRoot());
           this.binding = binding;
          }
      }
        class AddUserViewHolder extends RootViewHolder {
            private final ChatAddUserBinding binding;
            private AddUserViewHolder(ChatAddUserBinding binding) {
                super(binding.getRoot());
                this.binding = binding;}
        }
        class RootViewHolder extends RecyclerView.ViewHolder {
            public RootViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }
}