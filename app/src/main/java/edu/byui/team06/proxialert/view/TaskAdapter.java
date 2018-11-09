package edu.byui.team06.proxialert.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import edu.byui.team06.proxialert.R;
import edu.byui.team06.proxialert.database.model.ProxiDB;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.MyViewHolder>{

    private Context _context;
    private List<ProxiDB> _taskList;
//
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView task;
        public TextView dot;
        public TextView address;
//
        public MyViewHolder(View view) {
            super(view);
            task = view.findViewById(R.id.task);
            dot = view.findViewById(R.id.dot);
            address = view.findViewById(R.id.address);
        }
    }


    public TaskAdapter(Context context, List<ProxiDB> taskList) {
        _context = context;
        _taskList = taskList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        ProxiDB proxiDB = _taskList.get(position);

        holder.task.setText(proxiDB.getTask());

        // Displaying dot from HTML character code
        holder.dot.setText(Html.fromHtml("&#8226;"));

        holder.address.setText(proxiDB.getAddress());
    }

    @Override
    public int getItemCount() {
        return _taskList.size();
    }
}