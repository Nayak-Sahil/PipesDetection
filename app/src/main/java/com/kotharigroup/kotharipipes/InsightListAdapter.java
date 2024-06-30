package com.kotharigroup.kotharipipes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class InsightListAdapter extends ArrayAdapter<InsightListView> {

    public InsightListAdapter(@NonNull Context context, ArrayList<InsightListView> arrList) {
        super(context, 0, arrList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;

        // of the recyclable view is null then inflate the custom layout for the same
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.insight_list, parent, false);
        }

        InsightListView currentPosition = getItem(position);
        assert currentPosition != null;

        TextView curnInsightNameLbl = (TextView) view.findViewById(R.id.curnInsightNameLbl);
        TextView totalPipesCountLbl = (TextView) view.findViewById(R.id.totalPipeCountItemLbl);
        TextView dateLbl = (TextView) view.findViewById(R.id.dateLbl);

        curnInsightNameLbl.setText(currentPosition.getInsightName());
        dateLbl.setText(currentPosition.getInsightOndate());
        totalPipesCountLbl.setText(String.valueOf(currentPosition.getTotalPipesCount()));

        return view;
    }
}
