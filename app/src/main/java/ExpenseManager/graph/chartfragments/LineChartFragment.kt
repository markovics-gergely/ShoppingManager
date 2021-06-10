package ExpenseManager.graph.chartfragments

import ExpenseManager.R
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs


class LineChartFragment : Fragment() {

    inner class formatter(val date: Date, val formatter: SimpleDateFormat) : ValueFormatter(){
        override fun getFormattedValue(value: Float): String {
            val cal: Calendar = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.DATE, value.toInt())
            return formatter.format(cal.time)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val rootView = inflater.inflate(R.layout.fragment_line_chart, container, false)

        if (arguments != null) {
            val args = arguments
            val exentries = args?.getParcelableArrayList<Entry>(getString(R.string.ex_line_entries))
            val inentries = args?.getParcelableArrayList<Entry>(getString(R.string.in_line_entries))
            val firstdatestring = args?.getString(getString(R.string.line_first_date))

            val formatter = SimpleDateFormat("MM-dd")
            val firstdate = formatter.parse(firstdatestring)

            val format = formatter(firstdate, formatter)

            val inDataSet = LineDataSet(inentries, getString(R.string.income))
            inDataSet.setCircleColor(ContextCompat.getColor(requireContext(), R.color.colorPrimaryDarkGraph))
            inDataSet.color = ContextCompat.getColor(requireContext(), R.color.colorPrimaryDarkGraph)
            inDataSet.setDrawValues(false)

            val exDataSet = LineDataSet(exentries, getString(R.string.expense))
            exDataSet.setCircleColor(ContextCompat.getColor(requireContext(), R.color.colorPrimaryGraph))
            exDataSet.color = ContextCompat.getColor(requireContext(), R.color.colorPrimaryGraph)
            exDataSet.setDrawValues(false)

            val data = LineData()
            data.addDataSet(inDataSet)
            data.addDataSet(exDataSet)

            val linechart: LineChart = rootView.findViewById(R.id.chart)

            linechart.data = data
            data.setValueFormatter(format)
            val xAxis = linechart.xAxis
            xAxis.valueFormatter = format
            xAxis.setCenterAxisLabels(true)
            xAxis.setDrawGridLines(false)
            xAxis.position = XAxis.XAxisPosition.BOTTOM_INSIDE
            xAxis.isGranularityEnabled = true
            xAxis.granularity = 1f
            xAxis.axisMinimum = 0f

            linechart.description.isEnabled = false
            linechart.legend.isEnabled = false
            linechart.invalidate()
        }
        return rootView
    }
}