package com.example.mun.ruok;

import android.content.Context;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mun.ruok.Service.SensorService;

import static com.example.mun.ruok.Service.SensorService.sFit_mode;

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
       Switch switchCompat = (Switch) convertView.findViewById(R.id.fit_switch);

        if(position == 3) {
            switchCompat.setVisibility(View.VISIBLE);
            switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked && !sFit_mode) {
                        SensorService.fitStart();
                        Toast.makeText(getContext(), "운동을 시작합니다.",Toast.LENGTH_SHORT).show();
                    } else if(!isChecked) {
                        sFit_mode = false;
                        SensorService.timer.cancel();
                        Toast.makeText(getContext(), "운동을 종료합니다.",Toast.LENGTH_SHORT).show();
                    }
                }
            });
            if(sFit_mode) {
                switchCompat.setChecked(true);
            } else {
                switchCompat.setChecked(false);
            }
        }
        return convertView;
    }
}
