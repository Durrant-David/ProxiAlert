package edu.byui.team06.proxialert.view;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import edu.byui.team06.proxialert.R;
import edu.byui.team06.proxialert.database.model.ProxiDB;

/**@author David Durrant, Chase Busacker
 * @version  1.0
 * @since 1.0
 * <p>TaskAdapter holds the list of tasks that is located
 * in MainActivity</p>
 * <p>the embedded class MyViewHolder
 *  formats the view of each task in the list.
 *  </p>
 */

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.MyViewHolder>{

    private List<ProxiDB> _taskList;
    //

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView task;
        public TextView dot;
        public TextView address;
        public TextView dueDate;


        //
        public MyViewHolder(View view) {
            super(view);
            task = view.findViewById(R.id.task);
            dot = view.findViewById(R.id.dot);
            address = view.findViewById(R.id.address);
            dueDate = view.findViewById(R.id.dueDate);

        }
    }


    public TaskAdapter(List<ProxiDB> taskList) {
        _taskList = taskList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_list_row, parent, false);

        return new MyViewHolder(itemView);
    }


   /**
     * onBindViewHolder
     * This where each list item is created. Not sure where it is called
     * @param holder - The task item itself.
     * @param position - location on the screen
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ProxiDB proxiDB = _taskList.get(position);

        holder.task.setText(proxiDB.getTask());



        //Date d = Calendar.getInstance().getTime();
        //SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        //String formattedDate = df.format(d);
        // Displaying dot from HTML character code

        if(proxiDB.getComplete().equals("true"))
        {
            holder.dot.setTextColor(Color.parseColor("#888888"));
            holder.task.setPaintFlags(holder.task.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.address.setPaintFlags(holder.address.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.dueDate.setPaintFlags(holder.dueDate.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        else {
            //If an item is no longer not completed we have to "unstrike" through them because if we resort items,
            //items that are not complete and in the same position as the striked through will become striked through
            holder.task.setPaintFlags(holder.task.getPaintFlags() & ~ Paint.STRIKE_THRU_TEXT_FLAG);
            holder.address.setPaintFlags(holder.address.getPaintFlags() & ~ Paint.STRIKE_THRU_TEXT_FLAG);
            holder.dueDate.setPaintFlags(holder.dueDate.getPaintFlags() & ~ Paint.STRIKE_THRU_TEXT_FLAG);
            holder.dot.setTextColor(Color.parseColor("#00c6ae"));
            long timeStamp = Long.parseLong(proxiDB.getTimeStamp());
            if (timeStamp + 86400000 <= System.currentTimeMillis()) {
                holder.dot.setTextColor(Color.parseColor("#ff0000"));
            }
        }
        holder.dot.setText(Html.fromHtml("&#8226;"));
        holder.dueDate.setText(proxiDB.getDueDate());
        holder.address.setText(proxiDB.getAddress());
    }

    @Override
    public int getItemCount() {
        return _taskList.size();
    }

}
