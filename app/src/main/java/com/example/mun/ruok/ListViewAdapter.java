package com.example.mun.ruok;

import android.content.Context;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

public class ListViewAdapter extends ArrayAdapter {
    Context context;
    int resource;
    String[] values;
    LayoutInflater inflater;

    public ListViewAdapter(Context context, int resource, String[] values) {
        super(context, resource, values) ;

        this.context = context;
        this.resource = resource;
        this.values = values;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return values.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = inflater.inflate(resource, parent, false);
        }

        TextView textView = (TextView) convertView.findViewById(R.id.textView1);
        textView.setText(values[position]);
        textView.setTag(position);
        android.support.v7.widget.SwitchCompat switchCompat = (android.support.v7.widget.SwitchCompat) convertView.findViewById(R.id.fit_switch);

        if(position == 3) {
            switchCompat.setVisibility(View.VISIBLE);
            switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked) {
                        Toast.makeText(getContext(), "운동을 시작합니다.",Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "운동을 종료합니다.",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        return convertView;
    }
}
