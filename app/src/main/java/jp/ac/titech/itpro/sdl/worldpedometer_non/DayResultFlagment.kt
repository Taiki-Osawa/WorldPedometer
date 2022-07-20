package jp.ac.titech.itpro.sdl.worldpedometer_non

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class DayResultFlagment : Fragment(){
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dayresult, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val LENGTH_ONEWALK: Double = 0.7
        val PLAYER_HEIGHT_DEFAULT: Int = 170

        val bundle = arguments ?: return
        val pedometer_today = bundle.getInt(PEDOMETER_TODAY)
        val pedometer_total = bundle.getInt(PEDOMETER_TOTAL)
        val pedometer_all = bundle.getInt(PEDOMETER_ALL)
        val pedometer_todayTarget = bundle.getInt(PEDOMETER_DAILYTARGET)
        val lastday = bundle.getInt(LASTDAY)
        val lastmonth = bundle.getInt(LASTMONTH)
        val lastyear = bundle.getInt(LASTYEAR)
        val player_height_new = bundle.getInt(PLAYER_HEIGHT)
        val player_height = bundle.getInt(PLAYER_HEIGHT_PREV)

        val date_Results: TextView = view.findViewById(R.id.date_Results)
        date_Results.setText(
            lastyear.toString() + "/" +
            (lastmonth + 1).toString() + "/" +
            lastday.toString() + " Results"
        )

        val pedometer_field: TextView = view.findViewById(R.id.pedometer_field)
        pedometer_field.setText(pedometer_today.toString() + "歩")

        val percent_field: TextView = view.findViewById(R.id.percent_field)
        val percent = pedometer_today * 100 / pedometer_todayTarget
        percent_field.setText(percent.toString() + "%")

        val pedolength_field: TextView = view.findViewById(R.id.pedolength_field)
        val pedolength: Double = pedometer_today * LENGTH_ONEWALK
        pedolength_field.setText(pedolength.toInt().toString() + "m")

        val height_bonus_field: TextView = view.findViewById(R.id.height_bonus_field)
        val height_bonus: Double = player_height.toDouble() / PLAYER_HEIGHT_DEFAULT
        height_bonus_field.setText("×%.2f".format(height_bonus))

        val worldlength_field: TextView = view.findViewById(R.id.worldlength_field)
        val worldlength: Double = pedolength * height_bonus
        worldlength_field.setText(worldlength.toInt().toString() + "m")

        val total_pedometer_field: TextView = view.findViewById(R.id.total_pedometer_field)
        total_pedometer_field.setText(pedometer_total.toString() + "歩")

        val total_worldlength_field: TextView = view.findViewById(R.id.total_worldlength_field)
        total_worldlength_field.setText(pedometer_all.toString() + "m")

        val before_height: TextView = view.findViewById(R.id.before_height)
        val height_player_double: Double = player_height.toDouble() / 100
        before_height.setText("%.1f".format(height_player_double) + "m")

        val after_height: TextView = view.findViewById(R.id.after_height)
        val height_player_new_double: Double = player_height_new.toDouble() / 100
        after_height.setText("%.1f".format(height_player_new_double) + "m")
    }
}