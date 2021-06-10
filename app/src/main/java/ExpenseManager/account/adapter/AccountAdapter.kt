package ExpenseManager.account.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import ExpenseManager.R
import ExpenseManager.account.data.AccountItem
import java.text.SimpleDateFormat
import java.util.*


class AccountAdapter(private val listener: AccountAdapter.AccountItemClickListener) :
    RecyclerView.Adapter<AccountAdapter.AccountViewHolder>()  {

    private val items = mutableListOf<AccountItem>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountAdapter.AccountViewHolder {
        val itemView: View = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_account_list, parent, false)
        return AccountViewHolder(itemView)
    }

    val formatter = SimpleDateFormat("yy-MM-dd")
    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        val item = items[position]
        holder.nameTextView.text = item.name
        holder.dateTextView.text = formatter.format(item.date.time)
        holder.categoryTextView.text = item.shopCategory.name
        holder.priceTextView.text = item.estimatedPrice.toString() + " Ft"
        holder.iconImageView.setImageResource(getImageResource(item.shopCategory, holder.iconImageView.context))
        holder.expenseImageView.setImageResource(getExpenseImageResource(item.isExpense))

        holder.item = item
    }

    override fun getItemCount(): Int {
        return items.size
    }

    interface AccountItemClickListener {
        fun onItemChanged(item: AccountItem)
        fun onItemRemoved(item: AccountItem)
        fun onAllItemRemoved()
        fun onItemEditOpened(item: AccountItem)
    }

    inner class AccountViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val expenseImageView: ImageView
        val iconImageView: ImageView
        val dateTextView: TextView
        val nameTextView: TextView
        val categoryTextView: TextView
        val priceTextView: TextView
        val removeButton: ImageButton
        val editButton: ImageButton

        var item: AccountItem? = null

        init {
            expenseImageView = itemView.findViewById(R.id.AccountIsExpense)
            iconImageView = itemView.findViewById(R.id.AccountItemIconImageView)
            dateTextView = itemView.findViewById(R.id.AccountItemDateTextView)
            nameTextView = itemView.findViewById(R.id.AccountItemNameTextView)
            categoryTextView = itemView.findViewById(R.id.AccountItemCategoryTextView)
            priceTextView = itemView.findViewById(R.id.AccountItemPriceTextView)
            removeButton = itemView.findViewById(R.id.AccountItemRemoveButton)
            editButton = itemView.findViewById(R.id.AccountItemChangeButton)
            removeButton.setOnClickListener {
                item?.let { it ->
                    removeItem(it)
                    listener.onItemRemoved(it)
                }
            }
            editButton.setOnClickListener{
                item?.let { it ->
                    listener.onItemEditOpened(it)
                }
            }
        }
    }

    @DrawableRes
    private fun getImageResource(shopCategory: AccountItem.ShopCategory, context: Context) : Int{
        return context.resources.getIdentifier("shopcategories_" + shopCategory.toString().toLowerCase(
            Locale.ROOT), "drawable", context.packageName)
    }

    @DrawableRes
    private fun getExpenseImageResource(isExpense: Boolean) = when (isExpense) {
        true -> R.drawable.loss
        false -> R.drawable.profits
    }

    fun addItem(item: AccountItem) {
        items.add(item)
        items.sort()
        notifyItemInserted(items.indexOf(item))
    }

    fun update(accountItems: List<AccountItem>) {
        items.clear()
        Collections.sort(accountItems)
        items.addAll(accountItems)
        notifyDataSetChanged()
    }

    fun removeItem(item: AccountItem){
        val i = items.indexOf(item)
        items.removeAt(i)
        notifyItemRemoved(i)
    }

    fun removeAllItem(){
        val size = items.size
        items.clear()
        notifyItemRangeRemoved(0, size)
    }

    fun itemUpdated(oldItem: AccountItem, newItem: AccountItem){
        val id = items.indexOf(oldItem)
        items[id] = newItem
        notifyItemChanged(id)
        if(oldItem.date != newItem.date){
            items.sort()
            val newid = items.indexOf(newItem)
            notifyItemMoved(id, newid)
        }
    }

    fun getItems() : MutableList<AccountItem>{
        return items
    }

    fun isOverLimit(limit: Int) : Boolean{
        for(item in items) if(item.isExpense && item.estimatedPrice > limit) return true
        return false
    }
}