package com.dandian.campus.xmjs.fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.dandian.campus.xmjs.activity.EditUserInfoActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.DatePicker;

@SuppressLint("SimpleDateFormat")
public class BirthdayPickerFragment extends DialogFragment implements
		OnDateSetListener {
	String birthday;
	String year;
	String month;
	String day;
	Calendar calendar;
	DatePickerDialog dialog;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		calendar = Calendar.getInstance();
		birthday = getArguments().getString("birthday");

		if (birthday.equals("nodata")) {
			dialog = new DatePickerDialog(getActivity(), this, Calendar.YEAR,
					Calendar.MONTH, Calendar.DAY_OF_MONTH);
			dialog.setButton2("取消", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.dismiss();
				}
			});
		} else {
			String[] dates = birthday.split("-");
			year = dates[0];
			month = dates[1];
			day = dates[2];
			System.out.println("--------------" + year + month + day
					+ Integer.parseInt(year) + Integer.parseInt(month)
					+ Integer.parseInt(day));
			dialog = new DatePickerDialog(getActivity(), this,
					Integer.parseInt(year), Integer.parseInt(month),
					Integer.parseInt(day));
			dialog.setButton2("取消", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.dismiss();
				}
			});
		}

		return dialog;
	}

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
		// TODO Auto-generated method stub
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, monthOfYear);
		calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String time = df.format(calendar.getTime());
		EditUserInfoActivity.birthday.setText(time);
	}

}
