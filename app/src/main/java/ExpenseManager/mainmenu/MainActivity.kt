package ExpenseManager.mainmenu

import ExpenseManager.R
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ExpenseManager.shoppinglist.ShoppingActivity
import ExpenseManager.account.AccountActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnShopping.setOnClickListener {
            val shoppingIntent = Intent(this, ShoppingActivity::class.java)
            startActivity(shoppingIntent)
        }

        btnAccount.setOnClickListener {
            val accountIntent = Intent(this, AccountActivity::class.java)
            startActivity(accountIntent)
        }
    }
}