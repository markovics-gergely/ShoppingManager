package ExpenseManager.account.data;

import androidx.room.*

@Dao
public interface AccountItemDao {
    @Query("SELECT * FROM accountitem")
    fun getAll(): List<AccountItem>

    @Insert
    fun insert(accountItems: AccountItem): Long

    @Update
    fun update(accountItem: AccountItem)

    @Delete
    fun deleteItem(accountItem: AccountItem)

    @Query("DELETE FROM accountitem")
    fun deleteAllItem()
}