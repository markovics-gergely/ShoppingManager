package ExpenseManager.graph.chartfragments

import ExpenseManager.R
import ExpenseManager.account.data.AccountItem
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.android.synthetic.main.fragment_pie_chart.*
import kotlin.math.round
import kotlin.math.roundToInt

class PieChartFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val rootView = inflater.inflate(R.layout.fragment_pie_chart, container, false)

        if (arguments != null) {
            val args = arguments
            val entries = args?.getParcelableArrayList<PieEntry>(getString(R.string.pie_entries))
            val sum = args?.getFloat(getString(R.string.pie_sum))
            if(sum != null && entries?.size == 2){
                entries.get(0).label = String.format("%.2f", entries.get(0).value / sum * 100) + " %"
                entries.get(1).label = String.format("%.2f", entries.get(1).value / sum * 100) + " %"
            }

            val dataset = PieDataSet(entries, getString(R.string.pie_chart))
            dataset.valueTextSize = 40f
            dataset.valueTextColor = ContextCompat.getColor(requireContext(), R.color.white)

            val colorlist: ArrayList<Int> = arrayListOf(
                ContextCompat.getColor(requireContext(), R.color.colorPrimaryGraph),
                ContextCompat.getColor(requireContext(), R.color.colorPrimaryDarkGraph))
            dataset.colors = colorlist

            val piechart: PieChart = rootView.findViewById(R.id.chart)
            piechart.data = PieData(dataset)
            piechart.holeRadius = 0f
            piechart.setEntryLabelTextSize(30f)
            piechart.description.isEnabled = false
            piechart.legend.isEnabled = false
            piechart.setEntryLabelTextSize(20f)
            piechart.setEntryLabelColor(Color.BLACK)
            piechart.invalidate()
        }
        return rootView
    }
}