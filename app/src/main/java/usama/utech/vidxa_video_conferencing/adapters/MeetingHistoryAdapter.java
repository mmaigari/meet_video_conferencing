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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.daimajia.swipe.implments.SwipeItemRecyclerMangerImpl;
import com.daimajia.swipe.util.Attributes;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import usama.utech.vidxa_video_conferencing.R;
import usama.utech.vidxa_video_conferencing.models.MeetingHistory;
import usama.utech.vidxa_video_conferencing.utils.Constants;
import usama.utech.vidxa_video_conferencing.utils.SharedObjectsAndAppController;

public class MeetingHistoryAdapter extends RecyclerSwipeAdapter<MeetingHistoryAdapter.ViewHolder> {

    ArrayList<MeetingHistory> list;
    Context context;
    OnItemClickListener onItemClickListener;
    SwipeItemRecyclerMangerImpl mItemManger;

    public MeetingHistoryAdapter(ArrayList<MeetingHistory> list, Context context) {
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

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meeting_history, parent, false);
        return new ViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        final MeetingHistory bean = list.get(position);

        if (!TextUtils.isEmpty(bean.getMeeting_id())) {
            holder.txtName.setText(bean.getMeeting_id());
        } else {
            holder.txtName.setText("");
        }

        if (!TextUtils.isEmpty(bean.getStartTime())) {

            String date = SharedObjectsAndAppController.convertDateFormat(bean.getStartTime()
                    , Constants.DateFormats.DATETIME_FORMAT_24, Constants.DateFormats.DATE_FORMAT_DD_MMM_YYYY);

            String time = SharedObjectsAndAppController.convertDateFormat(bean.getStartTime()
                    , Constants.DateFormats.DATETIME_FORMAT_24, Constants.DateFormats.TIME_FORMAT_12);

            holder.txtDate.setText(date + ", " + time);

            if (date.equalsIgnoreCase(SharedObjectsAndAppController.getTodaysDate(Constants.DateFormats.DATE_FORMAT_DD_MMM_YYYY))) {
                holder.btnJoin.setVisibility(View.VISIBLE);
            } else {
                holder.btnJoin.setVisibility(View.GONE);
            }

        } else {
            holder.txtDate.setText("");
        }

        if (!TextUtils.isEmpty(bean.getStartTime()) && !TextUtils.isEmpty(bean.getEndTime())) {

            //HH converts hour in 24 hours format (0-23), day calculation
            @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat(Constants.DateFormats.DATETIME_FORMAT_24);

            Date d1 = null;
            Date d2 = null;

            try {
                d1 = format.parse(bean.getStartTime());
                d2 = format.parse(bean.getEndTime());

                //in milliseconds
                long diff = Objects.requireNonNull(d2).getTime() - Objects.requireNonNull(d1).getTime();

                long diffSeconds = diff / 1000 % 60;
                long diffMinutes = diff / (60 * 1000) % 60;
                long diffHours = diff / (60 * 60 * 1000) % 24;
                long diffDays = diff / (24 * 60 * 60 * 1000);

                if (diffHours > 0) {
                    holder.txtDuration.setText(String.format("%s:%s:%s", SharedObjectsAndAppController.pad(Integer.parseInt(String.valueOf(diffHours))), SharedObjectsAndAppController.pad(Integer.parseInt(String.valueOf(diffMinutes))), SharedObjectsAndAppController.pad(Integer.parseInt(String.valueOf(diffSeconds)))));
                } else if (diffMinutes > 0) {
                    holder.txtDuration.setText(String.format("%s:%s", SharedObjectsAndAppController.pad(Integer.parseInt(String.valueOf(diffMinutes))), SharedObjectsAndAppController.pad(Integer.parseInt(String.valueOf(diffSeconds)))));
                } else if (diffSeconds > 0) {
                    holder.txtDuration.setText(String.format("%s sec(s)", SharedObjectsAndAppController.pad(Integer.parseInt(String.valueOf(diffSeconds)))));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            holder.txtDuration.setText("-");
        }

        holder.llDelete.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                mItemManger.closeItem(position);
                onItemClickListener.onDeleteClickListener(position, list.get(position));
            }
        });

        holder.btnJoin.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                mItemManger.closeItem(position);
                onItemClickListener.onJoinClickListener(position, list.get(position));
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
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public interface OnItemClickListener {
        void onItemClickListener(int position, MeetingHistory bean);

        void onDeleteClickListener(int position, MeetingHistory bean);

        void onJoinClickListener(int position, MeetingHistory bean);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtName;
        Button btnJoin;
        TextView txtDuration;
        TextView txtDate;
        LinearLayout llMain;
        LinearLayout llDelete;

        public ViewHolder(View itemView) {
            super(itemView);


            btnJoin = (MaterialButton) itemView.findViewById(R.id.btnJoin);
            txtName = (MaterialTextView) itemView.findViewById(R.id.txtName);
            txtDate = (MaterialTextView) itemView.findViewById(R.id.txtDate);
            txtDuration = (MaterialTextView) itemView.findViewById(R.id.txtDuration);
            llDelete = (LinearLayout) itemView.findViewById(R.id.llDelete);
            llMain = (LinearLayout) itemView.findViewById(R.id.llMain);

        }
    }
}



