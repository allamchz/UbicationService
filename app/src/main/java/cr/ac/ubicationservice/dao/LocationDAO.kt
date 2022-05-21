package cr.ac.ubicationservice.dao

import androidx.room.*
import cr.ac.ubicationservice.entity.Location

@Dao
interface LocationDAO {


    @Insert
    fun insert (location: Location)


    @Query ("select * from location_table")
    fun query(): List<Location>


}