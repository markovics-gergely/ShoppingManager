package ExpenseManager.account.fragments;

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.fragment.app.DialogFragment
import ExpenseManager.R
import ExpenseManager.account.data.AccountItem
import java.text.SimpleDateFormat
import java.util.*

class NewAccountItemDialogFragment : DialogFragment() {
    private lateinit var nameEditText: EditText
    private lateinit var estimatedPriceEditText: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var isExpenseCheckBox: CheckBox
    private lateinit var datePick: ImageButton
    private lateinit var dateTextView: TextView
    private var date: Calendar = Calendar.getInstance()
    private val formatter = SimpleDateFormat("yy MMMM dd")

    interface NewAccountItemDialogListener {
        fun onAccountItemCreated(newItem: AccountItem)
        fun onAccountItemEdited(oldItem: AccountItem, newItem: AccountItem)
        fun onCalendarPicked(dialog: NewAccountItemDialogFragment, date: Calendar)
        fun onDialogOver(wasValid: Boolean)
    }

    private lateinit var listener: NewAccountItemDialogListener
    private var rootitem: AccountItem? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? NewAccountItemDialogListener
            ?: throw RuntimeException("Activity must implement the NewAccountItemDialogListener interface!")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.new_account_item)
            .setView(getContentView())
            .setPositiveButton(R.string.ok) { _, _ ->
                if (isValid()) {
                    if(rootitem == null)
                        listener.onAccountItemCreated(getAccountItem())
                    else{
                        listener.onAccountItemEdited(rootitem!!, getAccountItem())
                    }
                }
                rootitem = null
                listener.onDialogOver(isValid())
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
        if(rootitem != null) setAccountItem(rootitem!!)
        else date = Calendar.getInstance()

        datePick.setOnClickListener{
            listener.onCalendarPicked(this, date)
        }
        dateTextView.text = formatter.format(date.time)

        if (arguments != null) {
                val args = arguments
            val sum= args?.getInt(getString(R.string.sum))
            Log.d("dialogbundle", sum.toString())

            estimatedPriceEditText.setText(sum.toString())
            isExpenseCheckBox.isChecked = true
        }

        return dialog
    }

    companion object {
        const val TAG = "NewAccountItemDialogFragment"
    }

    private fun getContentView(): View {
        val contentView =
            LayoutInflater.from(context).inflate(R.layout.dialog_new_account_item, null)
        nameEditText = contentView.findViewById(R.id.AccountItemNameEditText)
        estimatedPriceEditText = contentView.findViewById(R.id.AccountItemEstimatedPriceEditText)
        categorySpinner = contentView.findViewById(R.id.AccountItemCategorySpinner)
        categorySpinner.setAdapter(
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                resources.getStringArray(R.array.acccategory_items)
            )
        )
        isExpenseCheckBox = contentView.findViewById(R.id.AccountItemIsExpense)
        dateTextView = contentView.findViewById(R.id.AccountItemDateText)
        datePick = contentView.findViewById(R.id.AccountItemDatePick)

        return contentView
    }

    private fun isValid() = nameEditText.text.isNotEmpty() && estimatedPriceEditText.text.isNotEmpty() && estimatedPriceEditText.text.toString().toInt() >= 0

    private fun getAccountItem() : AccountItem {
        return AccountItem(
            id = rootitem?.id,
            name = nameEditText.text.toString(),
            estimatedPrice = try {
                estimatedPriceEditText.text.toString().toInt()
            } catch (e: NumberFormatException) {
                0
            },
            shopCategory = AccountItem.ShopCategory.getByOrdinal(categorySpinner.selectedItemPosition)
                ?: AccountItem.ShopCategory.SuperMarket,
            isExpense = isExpenseCheckBox.isChecked,
            date = date
        )
    }

    private fun setAccountItem(item: AccountItem) {
        nameEditText.setText(item.name)
        estimatedPriceEditText.setText(item.estimatedPrice.toString())
        categorySpinner.setSelection(item.shopCategory.ordinal)
        isExpenseCheckBox.isChecked = item.isExpense
        dateTextView.text = formatter.format(item.date.time)
        date = item.date
    }

    fun setRootItem(item: AccountItem){
        rootitem = item
    }

    fun setDate(cal: Calendar){
        date = cal
        dateTextView.text = formatter.format(date.time)
    }
}

