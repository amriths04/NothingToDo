package com.example.nothingtasks;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.TaskListViewHolder> {

    public interface OnListClickListener {
        void onListClick(TaskList list);
    }

    private List<TaskList> taskLists;
    private final OnListClickListener clickListener;

    // Constructor accepting the click listener
    public TaskListAdapter(OnListClickListener listener) {
        this.clickListener = listener;
    }

    public void setTaskLists(List<TaskList> taskLists) {
        this.taskLists = taskLists;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
        return new TaskListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskListViewHolder holder, int position) {
        TaskList list = taskLists.get(position);
        holder.title.setText(list.getName());

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onListClick(list);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (taskLists != null) ? taskLists.size() : 0;
    }

    static class TaskListViewHolder extends RecyclerView.ViewHolder {
        TextView title;

        public TaskListViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.listName);

        }
    }
}
