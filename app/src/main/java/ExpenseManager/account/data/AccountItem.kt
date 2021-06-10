package ExpenseManager.account.data

import android.os.Parcelable
import androidx.room.*
import kotlinx.android.parcel.Parcelize
import java.io.Serializable
import java.security.SecureRandom
import java.util.*

@Parcelize
@TypeConverters(Converters::class)
@Entity(tableName = "accountitem")
data class AccountItem(
    @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) val id: Long?,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "shop_category") val shopCategory: ShopCategory,
    @ColumnInfo(name = "estimated_price") val estimatedPrice: Int,
    @ColumnInfo(name = "date") val date: Calendar,
    @ColumnInfo(name = "is_expense") val isExpense: Boolean
) : Comparable<AccountItem>, Parcelable {
    @Parcelize
    enum class ShopCategory : Parcelable {
        Aldi, Alza, Birthday, BurgerKing, ClothingShop, Decathlon, DrugStore, ElectronicsShop, Euronics,
        FastFood, Fressnapf, FurnitureShop, Grocery, HnM, Ikea, Jysk, Kfc, Libri, Lidl, McDonalds,
        MediaMarkt, Pepco, Pharmacy, PocketMoney, Rossmann, Scholarship, Spar, SportsDirect, SuperMarket;
        companion object {
            @JvmStatic
            @TypeConverter
            fun getByOrdinal(ordinal: Int): ShopCategory? {
                var ret: ShopCategory? = null
                for (cat in values()) {
                    if (cat.ordinal == ordinal) {
                        ret = cat
                        break
                    }
                }
                return ret
            }

            @JvmStatic
            @TypeConverter
            fun toInt(category: ShopCategory): Int {
                return category.ordinal
            }
        }
    }

    override fun compareTo(other: AccountItem): Int {
        if(date > other.date) return -1
        else if(date == other.date) return 0
        else return 1
    }
}