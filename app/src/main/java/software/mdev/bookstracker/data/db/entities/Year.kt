package software.mdev.bookstracker.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import software.mdev.bookstracker.other.Constants
import java.io.Serializable

@Entity(tableName = Constants.DATABASE_NAME_YEAR)
data class Year(
        @ColumnInfo(name = Constants.DATABASE_YEAR_ITEM_YEAR)
        var year: String = "0000",

        @ColumnInfo(name = Constants.DATABASE_YEAR_ITEM_BOOKS)
        var yearBooks: Int = 0,

        @ColumnInfo(name = Constants.DATABASE_YEAR_ITEM_PAGES)
        var yearPages: Int = 0,

        @ColumnInfo(name = Constants.DATABASE_YEAR_ITEM_RATING)
        var avgRating: Float = 0F,

        @ColumnInfo(name = Constants.DATABASE_YEAR_CHALLENGE_BOOKS)
        var yearChallengeBooks: Int? = null,

        @ColumnInfo(name = Constants.DATABASE_YEAR_CHALLENGE_PAGES)
        var yearChallengePages: Int = 0,

        @ColumnInfo(name = Constants.DATABASE_YEAR_QUICKEST_BOOK)
        var yearQuickestBook: String = "null",

        @ColumnInfo(name = Constants.DATABASE_YEAR_QUICKEST_BOOK_VAL)
        var yearQuickestBookVal: String = "null",

        @ColumnInfo(name = Constants.DATABASE_YEAR_LONGEST_BOOK)
        var yearLongestBook: String = "null",

        @ColumnInfo(name = Constants.DATABASE_YEAR_LONGEST_BOOK_VAL)
        var yearLongestBookVal: Int = 0,

        @ColumnInfo(name = Constants.DATABASE_YEAR_AVG_READING_TIME)
        var yearAvgReadingTime: String = "null",

        @ColumnInfo(name = Constants.DATABASE_YEAR_AVG_PAGES)
        var yearAvgPages: Int = 0,

        @ColumnInfo(name = Constants.DATABASE_YEAR_SHORTEST_BOOK)
        var yearShortestBook: String = "null",

        @ColumnInfo(name = Constants.DATABASE_YEAR_SHORTEST_BOOK_VAL)
        var yearShortestBookVal: Int = 0,

        @ColumnInfo(name = Constants.DATABASE_YEAR_BOOKS_BY_MONTH)
        var yearBooksByMonth: Array<Int> = arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),

        @ColumnInfo(name = Constants.DATABASE_YEAR_PAGES_BY_MONTH)
        var yearPagesByMonth: Array<Int> = arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)

): Serializable{
        @PrimaryKey(autoGenerate = false)
        var id: Int? = null
        override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as Year

                if (!yearBooksByMonth.contentEquals(other.yearBooksByMonth)) return false
                if (!yearPagesByMonth.contentEquals(other.yearPagesByMonth)) return false

                return true
        }

        override fun hashCode(): Int {
                var result = yearBooksByMonth.contentHashCode()
                result = 31 * result + yearPagesByMonth.contentHashCode()
                return result
        }
}