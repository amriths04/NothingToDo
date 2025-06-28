package com.example.nothingtasks;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.TaskListViewHolder> {

    private List<TaskList> taskLists;

    public void setTaskLists(List<TaskList> taskLists) {
        this.taskLists = taskLists;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_list, parent, false);
        return new TaskListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskListViewHolder holder, int position) {
        TaskList list = taskLists.get(position);
        holder.nameText.setText(list.name);
        holder.descText.setText(list.description != null ? list.description : "");
    }

    @Override
    public int getItemCount() {
        return taskLists == null ? 0 : taskLists.size();
    }

    public static class TaskListViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, descText;

        public TaskListViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.listName);
            descText = itemView.findViewById(R.id.listDesc);
        }
    }
}
