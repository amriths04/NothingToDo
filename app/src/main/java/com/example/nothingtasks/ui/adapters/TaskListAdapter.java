package com.example.nothingtasks.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nothingtasks.R;
import com.example.nothingtasks.data.model.TaskList;
import com.example.nothingtasks.ui.helpers.ReminderCounterHelper;

import java.util.List;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.TaskListViewHolder> {

    public interface OnListClickListener {
        void onListClick(TaskList list);
    }

    private List<TaskList> taskLists;
    private final OnListClickListener clickListener;

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
        holder.desc.setText(list.getDescription());

        // Get first capital letter for the icon
        String name = list.getName();
        String firstLetter = (name != null && !name.isEmpty()) ? name.substring(0, 1).toUpperCase() : "?";
        holder.icon.setText(firstLetter);

        // Set reminder count only if > 0
        ReminderCounterHelper.bindCount(holder.count, list.getReminderCount());

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
        TextView title, desc, icon, count;

        public TaskListViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.listName);
            desc = itemView.findViewById(R.id.listDesc);
            icon = itemView.findViewById(R.id.listIconLetter);
            count = itemView.findViewById(R.id.listReminderCount);
        }
    }
}
