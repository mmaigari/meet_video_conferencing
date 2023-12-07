package usama.utech.vidxa_video_conferencing.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.daimajia.swipe.implments.SwipeItemRecyclerMangerImpl;
import com.daimajia.swipe.util.Attributes;
import com.google.android.material.button.MaterialButton;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import usama.utech.vidxa_video_conferencing.R;
import usama.utech.vidxa_video_conferencing.models.Schedule;
import usama.utech.vidxa_video_conferencing.utils.Constants;
import usama.utech.vidxa_video_conferencing.utils.SharedObjectsAndAppController;

public class SchedulerAdapter extends RecyclerSwipeAdapter<SchedulerAdapter.ViewHolder> {

    ArrayList<Schedule> list;
    Context context;
    OnItemClickListener onItemClickListener;
    SwipeItemRecyclerMangerImpl mItemManger;

    public SchedulerAdapter(ArrayList<Schedule> list, Context context) {
        this.list = list;
        this.context = context;
        mItemManger = new SwipeItemRecyclerMangerImpl(this);
        setMode(Attributes.Mode.Single);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }

    @Override
    public @NotNull ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.itemview_schedule, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final @NotNull ViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        final Schedule bean = list.get(position);

        if (!TextUtils.isEmpty(bean.getTitle())) {
            holder.txtName.setText(bean.getTitle());
            holder.txtNameInitial.setText(bean.getTitle().substring(0, 1));
        }

        if (!TextUtils.isEmpty(bean.getDate())) {
            holder.txtDate.setText(bean.getDate());
            if (Constants.checkDateisFuture(bean.getDate())) {
                holder.btnStart.setVisibility(View.VISIBLE);
            } else {
                holder.btnStart.setVisibility(View.GONE);
            }
        } else {
            holder.btnStart.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(bean.getStartTime()) && !TextUtils.isEmpty(bean.getDuration())) {
            String startTime = SharedObjectsAndAppController.convertDateFormat(bean.getStartTime(), Constants.DateFormats.TIME_FORMAT_24
                    , Constants.DateFormats.TIME_FORMAT_12);

            holder.txtTime.setText(String.format("Starts at %s (%s Mins)", startTime, bean.getDuration()));

            holder.txtTime.setVisibility(View.VISIBLE);
        } else {
            holder.txtTime.setVisibility(View.GONE);
        }

        holder.btnStart.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                mItemManger.closeItem(position);
                onItemClickListener.onStartClickListener(position, list.get(position));
            }
        });
        holder.llDelete.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                mItemManger.closeItem(position);
                onItemClickListener.onDeleteClickListener(position, list.get(position));


            }
        });

        holder.llMain.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClickListener(position, list.get(position));
            }
        });

        mItemManger.bindView(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NotNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public interface OnItemClickListener {
        void onItemClickListener(int position, Schedule bean);

        void onDeleteClickListener(int position, Schedule bean);

        void onStartClickListener(int position, Schedule bean);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtName;
        TextView txtNameInitial;
        TextView txtDate;
        TextView txtTime;
        Button btnStart;
        LinearLayout llMain;
        LinearLayout llDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            llMain = (LinearLayout) itemView.findViewById(R.id.llMain);
            txtNameInitial = (TextView) itemView.findViewById(R.id.txtNameInitial);
            txtName = (TextView) itemView.findViewById(R.id.txtName);
            txtDate = (TextView) itemView.findViewById(R.id.txtDate);
            txtTime = (TextView) itemView.findViewById(R.id.txtTime);
            llDelete = (LinearLayout) itemView.findViewById(R.id.llDelete);
            btnStart = (MaterialButton) itemView.findViewById(R.id.btnStart);

        }
    }
}



