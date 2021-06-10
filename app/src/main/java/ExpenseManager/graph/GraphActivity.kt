package ExpenseManager.graph

import ExpenseManager.mainmenu.MainActivity
import ExpenseManager.R
import ExpenseManager.account.data.AccountItem
import ExpenseManager.graph.chartfragments.BarChartFragment
import ExpenseManager.graph.chartfragments.LineChartFragment
import ExpenseManager.graph.chartfragments.PieChartFragment
import ExpenseManager.settings.SettingsActivity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.github.mikephil.charting.data.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_graph.*
import java.text.SimpleDateFormat
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class GraphActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private var lineFragment: LineChartFragment? = null
    private var barFragment: BarChartFragment? = null
    private var pieFragment: PieChartFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)
        setSupportActionBar(graphtoolbar)
        supportActionBar?.setTitle(getString(R.string.graph_manager))

        btnhome.setOnClickListener {
            val mainIntent = Intent(this, MainActivity::class.java)
            startActivity(mainIntent)
        }

        btnback.setOnClickListener {
            finish()
        }

        val navbar: BottomNavigationView = findViewById(R.id.bottomgraphbar)
        navbar.setOnNavigationItemSelectedListener(this)
        navbar.menu.findItem(R.id.line).setChecked(true)
        navbar.itemIconTintList = null

        val items = intent.getParcelableArrayListExtra<AccountItem>(getString(R.string.items)) as ArrayList<AccountItem>
        Log.d("GraphActivity", items.size.toString() + " item got")

        loadPieEntries(items)
        loadLineEntries(items)
        loadBarEntries(items)

        bottomgraphbar.selectedItemId = R.id.line
    }

    private fun loadPieEntries(items: ArrayList<AccountItem>){
        val pieEntries: ArrayList<Entry> = ArrayList()
        var incomesum: Float = 0f
        var expensesum: Float = 0f
        for(item in items)
            if(item.isExpense) expensesum += item.estimatedPrice
            else incomesum += item.estimatedPrice
        pieEntries.add(PieEntry(expensesum, getString(R.string.expense)))
        pieEntries.add(PieEntry(incomesum, getString(R.string.income)))

        pieFragment = PieChartFragment()
        pieFragment!!.arguments = Bundle()
        pieFragment!!.arguments?.putParcelableArrayList(getString(R.string.pie_entries), pieEntries)
        pieFragment!!.arguments?.putFloat(getString(R.string.pie_sum), expensesum + incomesum)
    }

    private fun loadLineEntries(items: ArrayList<AccountItem>){
        val expensedate = ArrayList<Calendar>()
        val expensevalues = ArrayList<Int>()
        for(i in items.size - 1 downTo 0){
            if(items.get(i).isExpense){
                val date = Calendar.getInstance()
                date.set(Calendar.DAY_OF_YEAR, items.get(i).date.get(Calendar.DAY_OF_YEAR))
                val value: Int = items.get(i).estimatedPrice
                if(expensedate.contains(date)){
                    expensevalues.set(expensedate.indexOf(date), expensevalues.get(expensedate.indexOf(date)) + value)
                }
                else{
                    expensedate.add(date)
                    expensevalues.add(value)
                }
            }
        }

        val incomedate = ArrayList<Calendar>()
        val incomevalues = ArrayList<Int>()
        for(i in items.size - 1 downTo 0){
            if(!items.get(i).isExpense){
                val date = Calendar.getInstance()
                date.set(Calendar.DAY_OF_YEAR, items.get(i).date.get(Calendar.DAY_OF_YEAR))
                val value: Int = items.get(i).estimatedPrice
                if(incomedate.contains(date)){
                    incomevalues.set(incomedate.indexOf(date), incomevalues.get(incomedate.indexOf(date)) + value)
                }
                else{
                    incomedate.add(date)
                    incomevalues.add(value)
                }
            }
        }
        val formatter = SimpleDateFormat("MM-dd")

        val alldate = ArrayList<Calendar>(expensedate)
        for(cal in incomedate) if(!alldate.contains(cal)) alldate.add(cal)
        Collections.sort(alldate)
        val firststring = formatter.format(alldate.first().time)

        val weightedX = ArrayList<Int>()
        for(cal in alldate) {
            val diff = TimeUnit.DAYS.convert(cal.timeInMillis - alldate.first().timeInMillis, TimeUnit.MILLISECONDS)
            weightedX.add(diff.toInt())
        }

        val exentries = ArrayList<Entry>()
        val inentries = ArrayList<Entry>()

        for(cal in alldate){
            if(incomedate.contains(cal)){
                inentries.add(Entry(weightedX.get(alldate.indexOf(cal)).toFloat(), incomevalues.get(incomedate.indexOf(cal)).toFloat()))
            }
            if(expensedate.contains(cal)){
                exentries.add(Entry(weightedX.get(alldate.indexOf(cal)).toFloat(), expensevalues.get(expensedate.indexOf(cal)).toFloat()))
            }
        }

        lineFragment = LineChartFragment()
        lineFragment!!.arguments = Bundle()
        lineFragment!!.arguments?.putParcelableArrayList(getString(R.string.ex_line_entries), exentries)
        lineFragment!!.arguments?.putParcelableArrayList(getString(R.string.in_line_entries), inentries)
        lineFragment!!.arguments?.putString(getString(R.string.line_first_date), firststring)
    }

    private fun loadBarEntries(items: ArrayList<AccountItem>){
        val expensemonthvalue = ArrayList<Int>()
        val incomemonthvalues = ArrayList<Int>()
        for (i in 0..11) expensemonthvalue.add(0)
        for (i in 0..11) incomemonthvalues.add(0)
        for(i in items.size - 1 downTo 0){
            val date = items.get(i).date
            if(date.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR)){
                val value: Int = items.get(i).estimatedPrice
                if(items.get(i).isExpense){
                    expensemonthvalue.set(date.get(Calendar.MONTH), expensemonthvalue.get(date.get(Calendar.MONTH)) + value)
                }
                else {
                    incomemonthvalues.set(date.get(Calendar.MONTH), incomemonthvalues.get(date.get(Calendar.MONTH)) + value)
                }
            }
        }
        val exentries = ArrayList<BarEntry>()
        val inentries = ArrayList<BarEntry>()

        for(i in 0..11){
            exentries.add(BarEntry(i.toFloat(), expensemonthvalue.get(i).toFloat()))
        }
        for(i in 0..11){
            inentries.add(BarEntry(i.toFloat(), incomemonthvalues.get(i).toFloat()))
        }
        barFragment = BarChartFragment()
        barFragment!!.arguments = Bundle()
        barFragment!!.arguments?.putParcelableArrayList(getString(R.string.ex_bar_entries), exentries)
        barFragment!!.arguments?.putParcelableArrayList(getString(R.string.in_bar_entries), inentries)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        when(item.itemId){
            R.id.bar -> {
                transaction.replace(R.id.chart_container, barFragment!!)
                transaction.commit()
                return true
            }
            R.id.line -> {
                transaction.replace(R.id.chart_container, lineFragment!!)
                transaction.commit()
                return true
            }
            R.id.pie -> {
                transaction.replace(R.id.chart_container, pieFragment!!)
                transaction.commit()
                return true
            }
            else -> return true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_graph, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val settingsIntent = Intent(this, SettingsActivity::class.java)
                startActivity(settingsIntent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}