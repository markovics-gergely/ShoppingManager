package ExpenseManager.account

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.snackbar.Snackbar
import ExpenseManager.mainmenu.MainActivity
import ExpenseManager.R
import ExpenseManager.account.adapter.AccountAdapter
import ExpenseManager.account.data.AccountItem
import ExpenseManager.account.data.AccountListDatabase
import ExpenseManager.account.fragments.DatePickerDialogFragment
import ExpenseManager.account.fragments.NewAccountItemDialogFragment
import ExpenseManager.graph.GraphActivity
import ExpenseManager.settings.SettingsActivity
import kotlinx.android.synthetic.main.activity_account_manager.*
import kotlinx.android.synthetic.main.activity_account_manager.btnback
import kotlinx.android.synthetic.main.content_account.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

class AccountActivity : AppCompatActivity(), AccountAdapter.AccountItemClickListener,
        NewAccountItemDialogFragment.NewAccountItemDialogListener, DatePickerDialogFragment.OnDateSelectedListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AccountAdapter
    private lateinit var database: AccountListDatabase
    private var sum: Int = 0
    private var newItemDialog: NewAccountItemDialogFragment = NewAccountItemDialogFragment()
    private var newItemLimit = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_manager)
        setSupportActionBar(acctoolbar)
        supportActionBar?.setTitle(getString(R.string.accountmanager))
        fabacc.setOnClickListener{
            newItemDialog.show(
                supportFragmentManager,
                NewAccountItemDialogFragment.TAG
            )
        }
        btnback.setOnClickListener{
            val mainIntent = Intent(this, MainActivity::class.java)
            startActivity(mainIntent)
        }
        btncharts.setOnClickListener {
            if(adapter.getItems().size != 0){
                val graphIntent = Intent(this, GraphActivity::class.java)
                val array: ArrayList<AccountItem> = adapter.getItems() as ArrayList<AccountItem>
                graphIntent.putExtra(getString(R.string.items), ArrayList(adapter.getItems()))
                startActivity(graphIntent)
            }
            else Snackbar.make(findViewById(android.R.id.content), getString(R.string.nothing_to_graph), Snackbar.LENGTH_LONG).show()
        }
        database = Room.databaseBuilder(
            applicationContext,
            AccountListDatabase::class.java,
            "account-list"
        ).build()
        initRecyclerView()

        val b: Bundle? = intent.extras
        if(b != null){
            newItemDialog = NewAccountItemDialogFragment()
            newItemDialog.arguments = Bundle()
            newItemDialog.arguments?.putInt("sum", b.getInt("sum"))
            
            newItemDialog.show(
                supportFragmentManager,
                NewAccountItemDialogFragment.TAG
            )
        }
    }

    override fun onResume() {
        super.onResume()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        if(sharedPreferences.getBoolean(getString(R.string.account_enable), false)){
            val key = sharedPreferences.getString(getString(R.string.acc_limit), "")
            if(key != null) newItemLimit = Integer.valueOf(key)
        }
        else newItemLimit = 0
        Log.d("AccountManager", "AccountItem Limit: " + newItemLimit.toString())

        if(newItemLimit > 0 && adapter.isOverLimit(newItemLimit)){
            AlertDialog.Builder(this)
                .setMessage(getString(R.string.item_over_limit))
                .setPositiveButton(getString(R.string.ok)) { dialogInterface, i ->
                }
                .show()
        }
    }

    private fun initRecyclerView() {
        recyclerView = AccountRecyclerView
        adapter = AccountAdapter(this)
        loadItemsInBackground()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadItemsInBackground() {
        thread {
            val items = database.accountItemDao().getAll()
            initsum(items)

            runOnUiThread {
                adapter.update(items)
                updateSumText()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_account, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val settingsIntent = Intent(this, SettingsActivity::class.java)
                startActivity(settingsIntent)
                true
            }
            R.id.action_remove_all -> {
                if(adapter.itemCount != 0) {
                    AlertDialog.Builder(this)
                        .setMessage(getString(R.string.remove_all_warn))
                        .setPositiveButton(getString(R.string.yes)) { dialogInterface, i ->
                            adapter.removeAllItem()
                            onAllItemRemoved()
                        }
                        .setNegativeButton(getString(R.string.no), null)
                        .show()
                }
                else Snackbar.make(findViewById(android.R.id.content), getString(R.string.nothing_to_remove), Snackbar.LENGTH_LONG).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onItemChanged(item: AccountItem) {
        thread {
            database.accountItemDao().update(item)
            Log.d("AccountManager", "AccountItem update was successful")
        }
    }

    override fun onAccountItemCreated(newItem: AccountItem) {
        thread {
            if(newItemLimit <= 0 || (!newItem.isExpense || newItem.estimatedPrice <= newItemLimit)){
                val newId = database.accountItemDao().insert(newItem)
                val newAccountItem = newItem.copy(
                    id = newId
                )
                runOnUiThread {
                    adapter.addItem(newAccountItem)
                    val diff = if(newAccountItem.isExpense) -1 * newAccountItem.estimatedPrice else newAccountItem.estimatedPrice
                    sum += diff
                    updateSumText()
                }
            }
            else Snackbar.make(findViewById(android.R.id.content), getString(R.string.over_expense_limit), Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onItemRemoved(item: AccountItem) {
        thread {
            database.accountItemDao().deleteItem(item)
            Log.d("AccountManager", "AccountItem remove was successful")
            sum -= if(item.isExpense) -1 * item.estimatedPrice else item.estimatedPrice
            updateSumText()
        }
    }

    override fun onAllItemRemoved() {
        thread {
            database.accountItemDao().deleteAllItem()
            Log.d("AccountManager", "All AccountItem remove was successful")
            sum = 0
            updateSumText()
        }
    }

    override fun onItemEditOpened(item: AccountItem) {
        newItemDialog = NewAccountItemDialogFragment()
        newItemDialog.setRootItem(item)
        newItemDialog.show(
            supportFragmentManager,
            NewAccountItemDialogFragment.TAG
        )
    }

    override fun onAccountItemEdited(oldItem: AccountItem, newItem: AccountItem) {
        thread {
            if(newItemLimit <= 0 || (!newItem.isExpense || newItem.estimatedPrice <= newItemLimit)){
                database.accountItemDao().update(newItem)
                runOnUiThread {
                    adapter.itemUpdated(oldItem, newItem)
                    val oldp = if(oldItem.isExpense) -1 * oldItem.estimatedPrice else oldItem.estimatedPrice
                    val newp = if(newItem.isExpense) -1 * newItem.estimatedPrice else newItem.estimatedPrice
                    sum += newp - oldp
                    updateSumText()
                }
            }
            else Snackbar.make(findViewById(android.R.id.content), getString(R.string.edited_over_expense_limit), Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onCalendarPicked(dialog: NewAccountItemDialogFragment, date: Calendar) {
        val datepicker: DatePickerDialogFragment = DatePickerDialogFragment()
        datepicker.setDate(date)
        datepicker.show(supportFragmentManager, "NewAccountItemCalendarFragment")
        newItemDialog = dialog
    }

    override fun onDialogOver(wasValid: Boolean) {
        if(!wasValid) Snackbar.make(findViewById(android.R.id.content), getString(R.string.not_valid), Snackbar.LENGTH_LONG).show()
        newItemDialog = NewAccountItemDialogFragment()
    }

    private fun initsum(items: List<AccountItem>){
        sum = 0
        for(i in 0 until items.size){
            sum += if(items.get(i).isExpense) -1 * items.get(i).estimatedPrice else items.get(i).estimatedPrice
        }
    }
    private fun updateSumText(){
        sumText.setText(sum.toString() + " Ft")
    }

    override fun onDateSelected(year: Int, month: Int, day: Int) {
        val cal: Calendar = Calendar.getInstance()
        cal.set(year, month, day)
        newItemDialog.setDate(cal)
    }

}