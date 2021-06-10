package ExpenseManager.shoppinglist.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import ExpenseManager.R
import ExpenseManager.shoppinglist.data.ShoppingItem
import java.util.*

class ShoppingAdapter(private val listener: ShoppingItemClickListener) :
    RecyclerView.Adapter<ShoppingAdapter.ShoppingViewHolder>() {

    private val items = mutableListOf<ShoppingItem>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingViewHolder {
        val itemView: View = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_shopping_list, parent, false)
        return ShoppingViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ShoppingViewHolder, position: Int) {
        val item = items[position]
        holder.nameTextView.text = item.name
        holder.categoryTextView.text = item.category.name
        holder.priceTextView.text = item.estimatedPrice.toString() + " Ft"
        holder.iconImageView.setImageResource(getImageResource(item.category, holder.iconImageView.context))
        holder.isBoughtCheckBox.isChecked = item.isBought

        holder.item = item
    }

    override fun getItemCount(): Int {
        return items.size
    }

    interface ShoppingItemClickListener {
        fun onItemChanged(item: ShoppingItem)
        fun onItemRemoved(item: ShoppingItem)
        fun onAllItemRemoved()
        fun onItemEditOpened(item: ShoppingItem)
    }

    inner class ShoppingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val iconImageView: ImageView
        val nameTextView: TextView
        val categoryTextView: TextView
        val priceTextView: TextView
        val isBoughtCheckBox: CheckBox
        val removeButton: ImageButton
        val editButton: ImageButton

        var item: ShoppingItem? = null

        init {
            iconImageView = itemView.findViewById(R.id.ShoppingItemIconImageView)
            nameTextView = itemView.findViewById(R.id.ShoppingItemNameTextView)
            categoryTextView = itemView.findViewById(R.id.ShoppingItemCategoryTextView)
            priceTextView = itemView.findViewById(R.id.ShoppingItemPriceTextView)
            isBoughtCheckBox = itemView.findViewById(R.id.ShoppingItemIsBoughtCheckBox)
            removeButton = itemView.findViewById(R.id.ShoppingItemRemoveButton)
            editButton = itemView.findViewById(R.id.ShoppingItemChangeButton)
            isBoughtCheckBox.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _, isChecked ->
                item?.let {
                    item!!.isBought = isChecked
                    listener.onItemChanged(item!!)
                }
            })
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
    private fun getImageResource(itemCategory: ShoppingItem.Category, context: Context) : Int{
        return context.resources.getIdentifier("itemcategories_" + itemCategory.toString().toLowerCase(
            Locale.ROOT), "drawable", context.packageName)
    }

    fun addItem(item: ShoppingItem) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    fun update(shoppingItems: List<ShoppingItem>) {
        items.clear()
        items.addAll(shoppingItems)
        notifyDataSetChanged()
    }

    fun removeItem(item: ShoppingItem){
        val i = items.indexOf(item)
        items.removeAt(i)
        notifyItemRemoved(i)
    }

    fun removeAllItem(){
        val size = items.size
        items.clear()
        notifyItemRangeRemoved(0, size)
    }

    fun itemUpdated(oldItem: ShoppingItem, newItem: ShoppingItem){
        val id = items.indexOf(oldItem)
        items[id] = newItem
        notifyItemChanged(id)
    }

    fun allBought(): Boolean{
        for(item in items) if(!item.isBought) return false
        return true
    }
}