package ExpenseManager.shoppinglist.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import ExpenseManager.R
import ExpenseManager.shoppinglist.data.ShoppingItem


class NewShoppingItemDialogFragment : DialogFragment() {
    private lateinit var nameEditText: EditText
    private lateinit var estimatedPriceEditText: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var alreadyPurchasedCheckBox: CheckBox
    
    interface NewShoppingItemDialogListener {
        fun onShoppingItemCreated(newItem: ShoppingItem)
        fun onShoppingItemEdited(oldItem: ShoppingItem, newItem: ShoppingItem)
        fun onDialogOver(wasValid: Boolean)
    }

    private lateinit var listener: NewShoppingItemDialogListener
    private var rootitem: ShoppingItem? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? NewShoppingItemDialogListener
            ?: throw RuntimeException("Activity must implement the NewShoppingItemDialogListener interface!")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.new_shopping_item)
            .setView(getContentView())
            .setPositiveButton(R.string.ok) { _, _ ->
                if (isValid()) {
                    if(rootitem == null)
                        listener.onShoppingItemCreated(getShoppingItem())
                    else{
                        listener.onShoppingItemEdited(rootitem!!, getShoppingItem())
                    }
                }
                rootitem = null
                listener.onDialogOver(isValid())
            }
            .setNegativeButton(R.string.cancel, null)
            .create()

        if(rootitem != null) setShoppingItem(rootitem!!)
        return dialog
    }

    companion object {
        const val TAG = "NewShoppingItemDialogFragment"
    }

    private fun getContentView(): View {
        val contentView =
            LayoutInflater.from(context).inflate(R.layout.dialog_new_shopping_item, null)
        nameEditText = contentView.findViewById(R.id.ShoppingItemNameEditText)
        estimatedPriceEditText = contentView.findViewById(R.id.ShoppingItemEstimatedPriceEditText)
        categorySpinner = contentView.findViewById(R.id.ShoppingItemCategorySpinner)
        categorySpinner.setAdapter(
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                resources.getStringArray(R.array.shopcategory_items)
            )
        )
        alreadyPurchasedCheckBox = contentView.findViewById(R.id.ShoppingItemIsPurchasedCheckBox)
        return contentView
    }

    private fun isValid() = nameEditText.text.isNotEmpty() && (estimatedPriceEditText.text.isEmpty() || estimatedPriceEditText.text.toString().toInt() >= 0)

    private fun getShoppingItem() = ShoppingItem(
        id = rootitem?.id,
        name = nameEditText.text.toString(),
        estimatedPrice = try {
            estimatedPriceEditText.text.toString().toInt()
        } catch (e: NumberFormatException) {
            0
        },
        category = ShoppingItem.Category.getByOrdinal(categorySpinner.selectedItemPosition)
            ?: ShoppingItem.Category.Bakery,
        isBought = alreadyPurchasedCheckBox.isChecked
    )

    fun setShoppingItem(item: ShoppingItem) {
        nameEditText.setText(item.name)
        estimatedPriceEditText.setText(item.estimatedPrice.toString())
        categorySpinner.setSelection(item.category.ordinal)
        alreadyPurchasedCheckBox.isChecked = item.isBought
    }

    fun setRootItem(item: ShoppingItem){
        rootitem = item
    }
}