package ExpenseManager.shoppinglist.data

import androidx.room.*

@Dao
interface ShoppingItemDao {
    @Query("SELECT * FROM shoppingitem")
    fun getAll(): List<ShoppingItem>

    @Insert
    fun insert(shoppingItems: ShoppingItem): Long

    @Update
    fun update(shoppingItem: ShoppingItem)

    @Delete
    fun deleteItem(shoppingItem: ShoppingItem)

    @Query("DELETE FROM shoppingitem")
    fun deleteAllItem()
}