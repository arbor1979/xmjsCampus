package com.dandian.campus.xmjs.widget;

import com.dandian.campus.xmjs.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

public class MyTimePickerDialog extends AlertDialog
implements OnClickListener, OnTimeChangedListener {

/**
* The callback interface used to indicate the user is done filling in
* the time (they clicked on the 'Set' button).
*/
public interface OnTimeSetListener {

/**
 * @param view The view associated with this listener.
 * @param hourOfDay The hour that was set.
 * @param minute The minute that was set.
 */
void onTimeSet(TimePicker view,String radioSel, int hourOfDay, int minute);
}

private static final String HOUR = "hour";
private static final String MINUTE = "minute";
private static final String IS_24_HOUR = "is24hour";

private final TimePicker mTimePicker;
private final RadioButton radio1;
private final RadioButton radio2;
private final OnTimeSetListener mCallback;

int mInitialHourOfDay;
int mInitialMinute;
boolean mIs24HourView;

/**
* @param context Parent.
* @param callBack How parent is notified.
* @param hourOfDay The initial hour.
* @param minute The initial minute.
* @param is24HourView Whether this is a 24 hour view, or AM/PM.
*/
public MyTimePickerDialog(Context context,
    OnTimeSetListener callBack,String radioSel,
    int hourOfDay, int minute, boolean is24HourView) {
this(context, 0, callBack,radioSel, hourOfDay, minute, is24HourView);
}

/**
* @param context Parent.
* @param theme the theme to apply to this dialog
* @param callBack How parent is notified.
* @param hourOfDay The initial hour.
* @param minute The initial minute.
* @param is24HourView Whether this is a 24 hour view, or AM/PM.
*/
public MyTimePickerDialog(Context context,
    int theme,
    OnTimeSetListener callBack,String radioSel,
    int hourOfDay, int minute, boolean is24HourView) {
super(context, theme);
mCallback = callBack;
mInitialHourOfDay = hourOfDay;
mInitialMinute = minute;
mIs24HourView = is24HourView;

setIcon(0);
setTitle("设置提醒时间");

Context themeContext = getContext();
setButton(BUTTON_POSITIVE, "设置", this);

LayoutInflater inflater =
        (LayoutInflater) themeContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
View view = inflater.inflate(R.layout.dialog_time_picker, null);
setView(view);
mTimePicker = (TimePicker) view.findViewById(R.id.timePicker);
radio1= (RadioButton) view.findViewById(R.id.radio0);
radio2= (RadioButton) view.findViewById(R.id.radio1);
if(radioSel.equals("当天"))
	radio1.setChecked(true);
else
	radio2.setChecked(true);
// initialize state
mTimePicker.setIs24HourView(mIs24HourView);
mTimePicker.setCurrentHour(mInitialHourOfDay);
mTimePicker.setCurrentMinute(mInitialMinute);
mTimePicker.setOnTimeChangedListener(this);
}

public void onClick(DialogInterface dialog, int which) {
tryNotifyTimeSet();
}

public void updateTime(int hourOfDay, int minutOfHour) {
mTimePicker.setCurrentHour(hourOfDay);
mTimePicker.setCurrentMinute(minutOfHour);
}

public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
/* do nothing */
}

private void tryNotifyTimeSet() {
if (mCallback != null) {
    mTimePicker.clearFocus();
    String selStr;
    if(radio1.isChecked())
    	selStr=(String) radio1.getText();
    else
    	selStr=(String) radio2.getText();
    mCallback.onTimeSet(mTimePicker, selStr,mTimePicker.getCurrentHour(),
            mTimePicker.getCurrentMinute());
}
}

@Override
protected void onStop() {
//tryNotifyTimeSet();
super.onStop();
}

@Override
public Bundle onSaveInstanceState() {
Bundle state = super.onSaveInstanceState();
state.putInt(HOUR, mTimePicker.getCurrentHour());
state.putInt(MINUTE, mTimePicker.getCurrentMinute());
state.putBoolean(IS_24_HOUR, mTimePicker.is24HourView());
return state;
}

@Override
public void onRestoreInstanceState(Bundle savedInstanceState) {
super.onRestoreInstanceState(savedInstanceState);
int hour = savedInstanceState.getInt(HOUR);
int minute = savedInstanceState.getInt(MINUTE);
mTimePicker.setIs24HourView(savedInstanceState.getBoolean(IS_24_HOUR));
mTimePicker.setCurrentHour(hour);
mTimePicker.setCurrentMinute(minute);
}
}
