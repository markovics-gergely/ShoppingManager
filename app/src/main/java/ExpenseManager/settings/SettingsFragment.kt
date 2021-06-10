package ExpenseManager.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import ExpenseManager.R
import android.text.InputType
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceManager

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val accountlimit = findPreference<EditTextPreference>(getString(R.string.acc_limit))
        if(accountlimit != null){
            accountlimit.setOnBindEditTextListener {
                it.inputType = InputType.TYPE_CLASS_NUMBER
            }
        }

        val shoppinglimit = findPreference<EditTextPreference>(getString(R.string.shop_limit))
        if(shoppinglimit != null){
            shoppinglimit.setOnBindEditTextListener {
                it.inputType = InputType.TYPE_CLASS_NUMBER
            }
        }
    }

    companion object {
        const val TAG = "SettingsFragment"
    }
}