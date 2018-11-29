package edu.byui.team06.proxialert.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.byui.team06.proxialert.R;
import edu.byui.team06.proxialert.database.model.ProxiDB;
import edu.byui.team06.proxialert.view.maps.MapsActivity;
/**@author
 * @version  1.0
 * @since
 * <p>TaskAdapter holds the list of tasks that is located
 * in MainActivity</p>
 * <p>the embedded class MyViewHolder
 *  formats the view of each task in the list.
 *  </p>
 * @param
 * @return
 */

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.MyViewHolder>{

    private List<ProxiDB> _taskList;
    //

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView task;
        public TextView dot;
        public TextView address;
        public TextView dueDate;
        final Button setLocation;


        //
        public MyViewHolder(View view) {
            super(view);
            task = view.findViewById(R.id.task);
            dot = view.findViewById(R.id.dot);
            address = view.findViewById(R.id.address);
            setLocation = view.findViewById(R.id.setLocation);
            dueDate = view.findViewById(R.id.dueDate);

        }
    }


    public TaskAdapter(List<ProxiDB> taskList) {
        _taskList = taskList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_list_row, parent, false);

        return new MyViewHolder(itemView);
    }


    /**@author
     * @version  1.0
     * @since
     * onBindViewHolder
     * This where each list item is created. Not sure where it is called
     * @param holder - The task item itself.
     * @param position - location on the screen
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        ProxiDB proxiDB = _taskList.get(position);

        holder.task.setText(proxiDB.getTask());

        //Date d = Calendar.getInstance().getTime();
        //SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        //String formattedDate = df.format(d);
        // Displaying dot from HTML character code

        long timeStamp = Long.parseLong(proxiDB.getTimeStamp());
         if(timeStamp +86400000 <= System.currentTimeMillis()) {
             holder.dot.setTextColor(Color.parseColor("#ff0000"));
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
