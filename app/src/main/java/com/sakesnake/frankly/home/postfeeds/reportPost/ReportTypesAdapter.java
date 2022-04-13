package com.sakesnake.frankly.home.postfeeds.reportPost;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sakesnake.frankly.R;

import java.util.List;

public class ReportTypesAdapter extends RecyclerView.Adapter<ReportTypesAdapter.ReportTypesViewHolder> {

    private List<String> reportTypesList;

    private Context mContext;

    private SelectedAdapterPosition selectedAdapterPosition;

    public ReportTypesAdapter(List<String> reportTypesList, Context mContext, SelectedAdapterPosition selectedAdapterPosition) {
        this.reportTypesList = reportTypesList;
        this.mContext = mContext;
        this.selectedAdapterPosition = selectedAdapterPosition;
    }

    public class ReportTypesViewHolder extends RecyclerView.ViewHolder {
        TextView reportTypeTextView;
        public ReportTypesViewHolder(@NonNull View itemView) {
            super(itemView);
            reportTypeTextView = (TextView) itemView.findViewById(R.id.report_type_text_view);

            itemView.setOnClickListener(view -> {
                if (selectedAdapterPosition != null)
                    selectedAdapterPosition.selectedPosition(getAdapterPosition());
            });
        }
    }

    @NonNull
    @Override
    public ReportTypesAdapter.ReportTypesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ReportTypesViewHolder(LayoutInflater.from(mContext).inflate(R.layout.report_types_list_view,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ReportTypesAdapter.ReportTypesViewHolder holder, int position) {
        holder.reportTypeTextView.setText(reportTypesList.get(position));
    }

    @Override
    public int getItemCount() {
        return reportTypesList.size();
    }
}
