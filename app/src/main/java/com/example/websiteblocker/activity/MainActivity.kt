package com.example.websiteblocker.activity

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.example.websiteblocker.R
import com.example.websiteblocker.services.SharedPrefHandler
import com.example.websiteblocker.utils.Constants
import com.example.websiteblocker.utils.Constants.Companion.ENB_SCHEDULE
import com.example.websiteblocker.utils.Constants.Companion.END_TIME
import com.example.websiteblocker.utils.Constants.Companion.START_TIME
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.Format
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private var LOGTAG : String = "MainActivity >>> : ";
    lateinit var mTimePicker: TimePickerDialog
    lateinit var addTimingView: LinearLayout
    lateinit var schedulerView: LinearLayout

    lateinit var startTimeView: TextView
    lateinit var endTimeView: TextView
    lateinit var editButton: TextView
    lateinit var deleteButton: TextView
    lateinit var modeDescView: TextView
    lateinit var modeSwitch: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
        loadScheduleTiming()
        modeSwitching()
    }

    private fun init(){
        addTimingView = findViewById(R.id.ll_add_view);
        schedulerView = findViewById(R.id.ll_schedule_view)
        startTimeView =  findViewById(R.id.tv_start_time)
        endTimeView =  findViewById(R.id.tv_end_time)
        editButton =  findViewById(R.id.tv_edit)
        deleteButton =  findViewById(R.id.tv_delete)
        modeDescView = findViewById(R.id.tv_mode_desc)
        modeSwitch = findViewById(R.id.sw_mode)
    }

    private fun modeSwitching() {
        //load initial state which mode
        var mode = SharedPrefHandler.getInstance(this, SharedPrefHandler.PrefFiles.USER_DETAILS_PREF)?.getBooleanValue(Constants.WHITE_MODE)
        modeSwitch.isChecked = mode!!
        //description setup
        if(mode){
            modeDescView.text = Html.fromHtml(Constants.BLACK_MODE_DESC)
        }else{
            modeDescView.text = Html.fromHtml(Constants.WHITE_MODE_DESC)
        }

        //action change
        modeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                //backList mode
                SharedPrefHandler.getInstance(this, SharedPrefHandler.PrefFiles.USER_DETAILS_PREF)?.add(Constants.WHITE_MODE, true)
                modeDescView.text = Html.fromHtml(Constants.BLACK_MODE_DESC)
            }else{
               //whiteList
                SharedPrefHandler.getInstance(this, SharedPrefHandler.PrefFiles.USER_DETAILS_PREF)?.add(Constants.WHITE_MODE, false)
                modeDescView.text = Html.fromHtml(Constants.WHITE_MODE_DESC)
            }
        }
    }

    private fun loadScheduleTiming(){
        if(SharedPrefHandler.getInstance(this, SharedPrefHandler.PrefFiles.USER_DETAILS_PREF)!!.getBooleanValue(ENB_SCHEDULE)){
          var strTime =  SharedPrefHandler.getInstance(this, SharedPrefHandler.PrefFiles.USER_DETAILS_PREF)?.getValue(Constants.START_TIME)
          var endTime = SharedPrefHandler.getInstance(this, SharedPrefHandler.PrefFiles.USER_DETAILS_PREF)?.getValue(Constants.END_TIME)

          //set text view
          startTimeView.text = strTime
          endTimeView.text = endTime

          //delete schedules all store data
          deleteButton.setOnClickListener {
              SharedPrefHandler.getInstance(this, SharedPrefHandler.PrefFiles.USER_DETAILS_PREF)!!.remove(ENB_SCHEDULE)
              SharedPrefHandler.getInstance(this, SharedPrefHandler.PrefFiles.USER_DETAILS_PREF)!!.remove(START_TIME)
              SharedPrefHandler.getInstance(this, SharedPrefHandler.PrefFiles.USER_DETAILS_PREF)!!.remove(END_TIME)
              if(checkSchedule()){
                  setVisibilityScheduleView(true)
                  loadScheduleTiming()
              }else{
                  setVisibilityScheduleView(false)
              }
          }

          //edit schedule
          editButton.setOnClickListener {
              val dialog = BottomSheetDialog(this)
              val view = layoutInflater.inflate(R.layout.timing_bottom_sheet, null)
              val startDateBtn = view.findViewById<LinearLayout>(R.id.ll_start)
              val endDateBtn = view.findViewById<LinearLayout>(R.id.ll_end)
              val startTimeView = view.findViewById<TextView>(R.id.tv_start_time)
              val headTitle = view.findViewById<TextView>(R.id.tv_head)
              val endTimeView = view.findViewById<TextView>(R.id.tv_end_time)
              val saveButton = view.findViewById<AppCompatButton>(R.id.save_btn)
              val btnClose = view.findViewById<ImageButton>(R.id.close_btn)

              headTitle.text = "Edit Timing"

              //set exist data on view
              startTimeView.text = strTime
              endTimeView.text = endTime

              //close dialog
              btnClose.setOnClickListener {
                  dialog.dismiss()
              }

              //select start time
              startDateBtn.setOnClickListener {
                  pickTime(startTimeView)
              }
              //end time selection
              endDateBtn.setOnClickListener {
                  pickTime(endTimeView)
              }
              //save data
              saveButton.setOnClickListener {
                  //save times
                  var strTimes : String = startTimeView.text.toString()
                  var endTimes : String = endTimeView.text.toString()
                  if(strTimes.isNotEmpty() && endTimes.isNotEmpty()){
                      SharedPrefHandler.getInstance(this, SharedPrefHandler.PrefFiles.USER_DETAILS_PREF)?.add(Constants.START_TIME, strTimes)
                      SharedPrefHandler.getInstance(this, SharedPrefHandler.PrefFiles.USER_DETAILS_PREF)?.add(Constants.END_TIME, endTimes)
                      Toast.makeText(this, "Edited schedule successfully", Toast.LENGTH_LONG).show()
                      dialog.dismiss();
                  }else{
                      Toast.makeText(this, "Unsupported time format", Toast.LENGTH_LONG).show()
                  }
              }
              dialog.setCancelable(false)
              dialog.setContentView(view)
              dialog.setOnDismissListener {
                  //check schedule exist
                  if(checkSchedule()){
                      setVisibilityScheduleView(true)
                      loadScheduleTiming()
                  }else{
                      setVisibilityScheduleView(false)
                  }
              }
              dialog.show()
          }

        }
    }

    private fun setVisibilityScheduleView(vis: Boolean){
        if(!vis){
            addTimingView.visibility = View.VISIBLE
            schedulerView.visibility = View.GONE
        }else {
            schedulerView.visibility = View.VISIBLE
            addTimingView.visibility = View.GONE
        }
    }

    fun scheduleTiming(view: View) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.timing_bottom_sheet, null)
        val startDateBtn = view.findViewById<LinearLayout>(R.id.ll_start)
        val endDateBtn = view.findViewById<LinearLayout>(R.id.ll_end)
        val startTimeView = view.findViewById<TextView>(R.id.tv_start_time)
        val endTimeView = view.findViewById<TextView>(R.id.tv_end_time)
        val saveButton = view.findViewById<AppCompatButton>(R.id.save_btn)
        val btnClose = view.findViewById<ImageButton>(R.id.close_btn)
        btnClose.setOnClickListener {
            dialog.dismiss()
        }


        startDateBtn.setOnClickListener {
            pickTime(startTimeView)
        }


        endDateBtn.setOnClickListener {
            pickTime(endTimeView)
        }

        saveButton.setOnClickListener {
            //save times
            var strTime : String = startTimeView.text.toString()
            var endTime : String = endTimeView.text.toString()
            if(strTime.isNotEmpty() && endTime.isNotEmpty()){
                SharedPrefHandler.getInstance(this, SharedPrefHandler.PrefFiles.USER_DETAILS_PREF)?.add(Constants.START_TIME, strTime)
                SharedPrefHandler.getInstance(this, SharedPrefHandler.PrefFiles.USER_DETAILS_PREF)?.add(Constants.END_TIME, endTime)
                SharedPrefHandler.getInstance(this, SharedPrefHandler.PrefFiles.USER_DETAILS_PREF)?.add(Constants.ENB_SCHEDULE, true)
                Toast.makeText(this, "Saved new schedule", Toast.LENGTH_LONG).show()
                dialog.dismiss();
            }else{
                Toast.makeText(this, "Unsupported time format", Toast.LENGTH_LONG).show()
            }
        }

        dialog.setCancelable(false)
        dialog.setContentView(view)

        dialog.setOnDismissListener {
            //check schedule exist
            if(checkSchedule()){
                setVisibilityScheduleView(true)
                loadScheduleTiming()
            }else{
                setVisibilityScheduleView(false)
            }
        }

        dialog.show()
    }

    private fun pickTime(textView: TextView) {
        val mcurrentTime = Calendar.getInstance()
        val hour = mcurrentTime.get(Calendar.HOUR_OF_DAY)
        val minute = mcurrentTime.get(Calendar.MINUTE)
        mTimePicker =  TimePickerDialog(
            this,
            { view, hourOfDay, minute -> textView.text = getTime(hourOfDay, minute) },
            hour, minute, false)

        mTimePicker.show();
    }

    private fun getTime(hr: Int, min: Int): String {
        val cal = Calendar.getInstance()
        cal[Calendar.HOUR_OF_DAY] = hr
        cal[Calendar.MINUTE] = min
        val formatter: Format = SimpleDateFormat("h:mm a")
        return formatter.format(cal.time)
    }

    private fun checkSchedule() : Boolean {
        return SharedPrefHandler.getInstance(this, SharedPrefHandler.PrefFiles.USER_DETAILS_PREF)!!.getBooleanValue(Constants.ENB_SCHEDULE)
    }

    override fun onStart() {
        // check accessibility permission
        if(!isAllowAccessibility()){
            // ask enable permission turn on
            askPermissionForAccessibility();
        }

        //check schedule exist
        if(checkSchedule()){
            setVisibilityScheduleView(true)
            loadScheduleTiming()
        }else{
            setVisibilityScheduleView(false)
        }
        super.onStart()
    }

    private fun askPermissionForAccessibility(){

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enable Accessibility")
        builder.setMessage("To block website please enable accessibility on your device")

        builder.setPositiveButton(android.R.string.yes) { dialog, which ->
            dialog.dismiss()
            val intent =  Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }

        builder.setNegativeButton(android.R.string.no) { dialog, which ->
            dialog.dismiss()
            Toast.makeText(applicationContext, "Accessibility not enabled", Toast.LENGTH_SHORT).show()
        }

        builder.show()

    }

    private fun isAllowAccessibility() : Boolean {
        var enable  = 0;
        try {
            enable = Settings.Secure.getInt(this.contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED);
        }catch (e : Settings.SettingNotFoundException){
            Log.d(LOGTAG, "Error finding setting, default accessibility to not found: " + e.message)
        }
        return  enable == 1;
    }

}