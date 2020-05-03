package example.digitallife.DB;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface Account_DAO {
    @Insert
    void insertAccount(Account account);

    @Update
    void updateAccount(Account account);

    @Delete
    void deleteAccount(Account account);

    @Query("SELECT * FROM accounts")
    List<Account> getAllAccounts();

    @Query("SELECT * FROM accounts WHERE id IN (:idAccount)")
    Account findById(int idAccount);

    @Query("SELECT * FROM accounts WHERE name LIKE (:name)")
    List<Account> selectLike_byName(String name);
}
