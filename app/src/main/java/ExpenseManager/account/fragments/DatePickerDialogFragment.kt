package ExpenseManager.account.fragments

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class DatePickerDialogFragment : DialogFragment(), DatePickerDialog.OnDateSetListener{
    private lateinit var onDateSelectedListener: OnDateSelectedListener

    private val c: Calendar = Calendar.getInstance()
    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context !is OnDateSelectedListener){
            throw RuntimeException("The activity does not implement the OnDateSelectedListener interface")
        }
        onDateSelectedListener = context
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        return DatePickerDialog(requireContext(), this, year, month, day)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        onDateSelectedListener.onDateSelected(year, month, dayOfMonth)
    }

    interface OnDateSelectedListener {
        fun onDateSelected(year: Int, month: Int, day: Int)
    }

    companion object {
        const val TAG = "NewAccountItemCalendarFragment"
    }

    fun setDate(date: Calendar){
        c.set(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH))
    }
}