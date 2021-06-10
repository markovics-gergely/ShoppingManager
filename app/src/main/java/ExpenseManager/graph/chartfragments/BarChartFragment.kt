package ExpenseManager.graph.chartfragments

import ExpenseManager.R
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class BarChartFragment : Fragment() {

    inner class formatter() : ValueFormatter(){
        val keystring = arrayListOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        override fun getFormattedValue(value: Float): String {
            if(value >= 0 && value < keystring.size)
                return keystring[value.toInt()]
            else return ""
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val rootView = inflater.inflate(R.layout.fragment_bar_chart, container, false)

        if (arguments != null) {
            val args = arguments
            val exentries = args?.getParcelableArrayList<BarEntry>(getString(R.string.ex_bar_entries))
            val inentries = args?.getParcelableArrayList<BarEntry>(getString(R.string.in_bar_entries))

            val format = formatter()
            val inDataSet = BarDataSet(inentries, getString(R.string.income))
            inDataSet.color = ContextCompat.getColor(requireContext(), R.color.colorPrimaryDarkGraph)
            inDataSet.setDrawValues(false)

            val exDataSet = BarDataSet(exentries, getString(R.string.expense))
            exDataSet.color = ContextCompat.getColor(requireContext(), R.color.colorPrimaryGraph)
            exDataSet.setDrawValues(false)

            val data = BarData(inDataSet, exDataSet)
            val barchart: BarChart = rootView.findViewById(R.id.chart)

            barchart.data = data
            barchart.data.barWidth = 0.38f
            val xAxis = barchart.xAxis
            xAxis.setCenterAxisLabels(true)
            xAxis.setDrawGridLines(false)
            xAxis.valueFormatter = format
            xAxis.position = XAxis.XAxisPosition.BOTTOM_INSIDE
            xAxis.isGranularityEnabled = true
            xAxis.granularity = 1f
            xAxis.axisMinimum = 0f
            xAxis.axisMaximum = 12f

            barchart.groupBars(0f, 0.24f, 0f)

            barchart.description.isEnabled = false
            barchart.legend.isEnabled = false
            barchart.invalidate()
        }
        return rootView
    }
}