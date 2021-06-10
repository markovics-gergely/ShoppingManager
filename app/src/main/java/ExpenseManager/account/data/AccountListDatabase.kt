package ExpenseManager.account.data;

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [AccountItem::class], version = 1)
@TypeConverters(value = [AccountItem.ShopCategory::class])
abstract class AccountListDatabase : RoomDatabase() {
    abstract fun accountItemDao(): AccountItemDao
}