package ExpenseManager.shoppinglist

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.snackbar.Snackbar
import ExpenseManager.mainmenu.MainActivity
import ExpenseManager.R
import ExpenseManager.R.menu.menu_shopping
import ExpenseManager.account.AccountActivity
import ExpenseManager.settings.SettingsActivity
import ExpenseManager.shoppinglist.adapter.ShoppingAdapter
import ExpenseManager.shoppinglist.data.ShoppingItem
import ExpenseManager.shoppinglist.data.ShoppingListDatabase
import ExpenseManager.shoppinglist.fragments.NewShoppingItemDialogFragment
import kotlinx.android.synthetic.main.activity_shopping.*
import kotlinx.android.synthetic.main.content_shopping.*
import kotlin.concurrent.thread

class ShoppingActivity : AppCompatActivity(), ShoppingAdapter.ShoppingItemClickListener,
    NewShoppingItemDialogFragment.NewShoppingItemDialogListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ShoppingAdapter
    private lateinit var database: ShoppingListDatabase
    private var sum: Int = 0
    private var newItemLimit = 0
    private var newAccountItemLimit = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping)
        setSupportActionBar(shoptoolbar)
        supportActionBar?.setTitle(getString(R.string.shoppinglist))
        fabshop.setOnClickListener{
            NewShoppingItemDialogFragment().show(
                supportFragmentManager,
                NewShoppingItemDialogFragment.TAG
            )
        }
        btnback.setOnClickListener{
            val mainIntent = Intent(this, MainActivity::class.java)
            startActivity(mainIntent)
        }
        btnclear.setOnClickListener {
            if(adapter.getItemCount() == 0) Snackbar.make(findViewById(android.R.id.content), getString(R.string.noitem), Snackbar.LENGTH_LONG).show()
            else if(adapter.allBought()){
                if(newAccountItemLimit <= 0 || sum <= newAccountItemLimit){
                    AlertDialog.Builder(this)
                        .setMessage(getString(R.string.clearandsave))
                        .setPositiveButton(getString(R.string.yes)) { dialogInterface, i ->
                            val saveIntent = Intent(this, AccountActivity::class.java)
                            val b: Bundle = Bundle()
                            b.putInt(getString(R.string.sum), sum)
                            saveIntent.putExtras(b)
                            startActivity(saveIntent)

                            adapter.removeAllItem()
                            onAllItemRemoved()
                        }
                        .setNegativeButton(getString(R.string.no), null)
                        .show()
                }
                else Snackbar.make(findViewById(android.R.id.content), getString(R.string.overaccountlimit), Snackbar.LENGTH_LONG).show()
            }
            else Snackbar.make(findViewById(android.R.id.content), getString(R.string.noteverybought), Snackbar.LENGTH_LONG).show()
        }

        database = Room.databaseBuilder(
            applicationContext,
            ShoppingListDatabase::class.java,
            "shopping-list"
        ).build()
        initRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        if(sharedPreferences.getBoolean(getString(R.string.account_enable), false)){
            val key = sharedPreferences.getString(getString(R.string.acc_limit), "")
            if(key != null) newAccountItemLimit = Integer.valueOf(key)
        }
        else newAccountItemLimit = 0
        Log.d("ShoppingActivity", "AccountItem Limit: " + newAccountItemLimit.toString())

        if(sharedPreferences.getBoolean(getString(R.string.shopping_enable), false)){
            val key = sharedPreferences.getString(getString(R.string.shop_limit), "")
            if(key != null) newItemLimit = Integer.valueOf(key)
        }
        else newItemLimit = 0
        Log.d("ShoppingActivity", "ShoppingItem Limit: " + newItemLimit.toString())

        if(sum > newItemLimit && newItemLimit > 0){
            AlertDialog.Builder(this)
                .setMessage(getString(R.string.sum_over_limit))
                .setPositiveButton(getString(R.string.ok)) { dialogInterface, i ->
                }
                .show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(menu_shopping, menu)
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
                if(adapter.itemCount != 0){
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

    private fun initRecyclerView() {
        recyclerView = ShoppingRecyclerView
        adapter = ShoppingAdapter(this)
        loadItemsInBackground()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadItemsInBackground() {
        thread {
            val items = database.shoppingItemDao().getAll()
            initsum(items)

            runOnUiThread {
                adapter.update(items)
                updateSumText()
            }
        }
    }

    override fun onItemChanged(item: ShoppingItem) {
        thread {
            database.shoppingItemDao().update(item)
            Log.d("ShoppingActivity", "ShoppingItem update was successful")
        }
    }

    override fun onShoppingItemCreated(newItem: ShoppingItem) {
        thread {
            if(newItemLimit <= 0 || sum + newItem.estimatedPrice <= newItemLimit){
                val newId = database.shoppingItemDao().insert(newItem)
                val newShoppingItem = newItem.copy(
                    id = newId
                )
                runOnUiThread {
                    adapter.addItem(newShoppingItem)
                    sum += newShoppingItem.estimatedPrice
                    updateSumText()
                }
            }
            else Snackbar.make(findViewById(android.R.id.content), getString(R.string.over_shopping_limit), Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onItemRemoved(item: ShoppingItem) {
        thread {
            database.shoppingItemDao().deleteItem(item)
            Log.d("ShoppingActivity", "ShoppingItem remove was successful")
            sum -= item.estimatedPrice
            updateSumText()
        }
    }

    override fun onAllItemRemoved() {
        thread {
            database.shoppingItemDao().deleteAllItem()
            Log.d("ShoppingActivity", "All ShoppingItem remove was successful")
            sum = 0
            updateSumText()
        }
    }

    override fun onItemEditOpened(item: ShoppingItem) {
        val dialog = NewShoppingItemDialogFragment()
        dialog.setRootItem(item)
        dialog.show(
            supportFragmentManager,
            NewShoppingItemDialogFragment.TAG
        )
    }

    override fun onShoppingItemEdited(oldItem: ShoppingItem, newItem: ShoppingItem) {
        thread {
            if(newItemLimit <= 0 || sum + newItem.estimatedPrice - oldItem.estimatedPrice <= newItemLimit){
                val item = newItem.copy(
                    id = oldItem.id
                )
                database.shoppingItemDao().update(item)
                runOnUiThread {
                    adapter.itemUpdated(oldItem, newItem)
                    sum += newItem.estimatedPrice - oldItem.estimatedPrice
                    updateSumText()
                }
            }
            else Snackbar.make(findViewById(android.R.id.content), getString(R.string.edited_over_shopping_limit), Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onDialogOver(wasValid: Boolean) {
        if(!wasValid) Snackbar.make(findViewById(android.R.id.content), getString(R.string.not_valid), Snackbar.LENGTH_LONG).show()
    }

    private fun initsum(items: List<ShoppingItem>){
        sum = 0
        for(i in 0 until items.size){
            sum += items.get(i).estimatedPrice
        }
    }
    private fun updateSumText(){
        sumText.setText(sum.toString() + " Ft")
    }
}