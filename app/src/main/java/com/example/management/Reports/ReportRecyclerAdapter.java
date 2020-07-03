package com.example.management.Reports;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.management.R;

import java.util.ArrayList;

public class ReportRecyclerAdapter extends RecyclerView.Adapter<ReportRecyclerAdapter.ReportViewHolder> {

    private Context context;
    private boolean reportType;/*true - Remaining, false - Transaction*/
    private ArrayList<String[]> reportList;/*0 - item, 1 - count income, 2 - count outgo(or remaining)*/

    public ReportRecyclerAdapter(Context context, ArrayList<String[]> reportList, boolean reportType) {
        this.context = context;
        this.reportList = reportList;
        this.reportType = reportType;;
    }

    public static class ReportViewHolder extends RecyclerView.ViewHolder {

        public TextView tvItem, income, outgo;

        public ReportViewHolder(@NonNull View itemView, boolean reportRemaining) {
            super(itemView);

            tvItem = itemView.findViewById(R.id.tv_report_item);
            income = itemView.findViewById(R.id.tv_income);
            outgo = itemView.findViewById(R.id.tv_outgo);

            if (reportRemaining) {
                income.setVisibility(View.INVISIBLE);
            }

        }
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.reports_recycler_items, parent, false);
        return new ReportRecyclerAdapter.ReportViewHolder(view, reportType);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        holder.tvItem.setText(reportList.get(position)[0]);
        holder.income.setText(reportList.get(position)[1]);
        holder.outgo.setText(reportList.get(position)[2]);
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

}
