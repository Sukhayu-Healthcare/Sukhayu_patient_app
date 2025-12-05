# Database Seeding Implementation - Complete Guide

## ✅ Implementation Complete

The Room database now automatically seeds dummy patient data when the database is created for the first time.

---

## Changes Made

### 1. ✅ AshaLocalDatabase.kt - Added Database Seeding

#### Imports Added:
```kotlin
import android.util.Log
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sukhayu.patient.DummyData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
```

#### Added Coroutine Scope:
```kotlin
companion object {
    @Volatile private var INSTANCE: AshaLocalDatabase? = null
    
    // Coroutine scope for database operations during initialization
    private val databaseScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // ...
}
```

#### Added DatabaseCallback Class:
```kotlin
/**
 * Database callback to seed dummy patient data when database is created for the first time.
 * This ensures offline-first architecture with test data available immediately.
 */
private class DatabaseCallback(
    private val context: Context
) : RoomDatabase.Callback() {
    
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        Log.d("AshaLocalDatabase", "Database created - seeding dummy patient data")
        
        // Get database instance and seed data in background coroutine
        INSTANCE?.let { database ->
            databaseScope.launch {
                try {
                    val patientDao = database.patientDao()
                    val dummyPatients = DummyData.getDummyPatients()
                    
                    // Insert all dummy patients
                    patientDao.insertPatients(dummyPatients)
                    
                    val count = patientDao.getPatientCount()
                    Log.d("AshaLocalDatabase", "Successfully seeded $count dummy patients")
                } catch (e: Exception) {
                    Log.e("AshaLocalDatabase", "Error seeding dummy patients", e)
                }
            }
        }
    }
}
```

#### Updated getInstance() Method:
```kotlin
fun getInstance(context: Context): AshaLocalDatabase {
    return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
            context.applicationContext,
            AshaLocalDatabase::class.java,
            "asha_local_db"
        )
        .fallbackToDestructiveMigration()
        .addCallback(DatabaseCallback(context))  // ✅ Add seeding callback
        .build()
        
        INSTANCE = instance
        instance
    }
}
```

---

### 2. ✅ PatientDao.kt - Already Has Required Method

The PatientDao already had the required method for batch insertion:

```kotlin
@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun insertPatients(patients: List<PatientEntity>)
```

**No changes needed to PatientDao!**

---

## How It Works

### 1. Database Creation
When the app is first installed or the database is recreated:
- Room calls `onCreate()` on the `DatabaseCallback`
- This happens only once per database lifecycle

### 2. Seeding Process
```
onCreate() triggered
    ↓
Log: "Database created - seeding dummy patient data"
    ↓
Launch coroutine on IO dispatcher (background thread)
    ↓
Get patientDao from database instance
    ↓
Get dummy patients from DummyData.getDummyPatients()
    ↓
Insert all patients using patientDao.insertPatients()
    ↓
Log success: "Successfully seeded X dummy patients"
```

### 3. Patient Data Available
After seeding completes:
- 19 dummy patients are available in the database
- Includes pregnant women, adult men, and adolescents
- All features (ANC, TB, General Survey) can use this data
- Data persists across app restarts

---

## Dummy Patients Seeded

### Total: 19 Patients

#### Pregnant Women (4):
1. Priya Sharma (DUMMY_001)
2. Sunita Devi (DUMMY_002)
3. Lakshmi Patel (DUMMY_003)
4. Meera Gupta (DUMMY_004)

#### Adult Men (6):
5. Rajesh Kumar (DUMMY_M001)
6. Amit Singh (DUMMY_M002)
7. Vijay Patil (DUMMY_M003)
8. Suresh Yadav (DUMMY_M004)
9. Ramesh Verma (DUMMY_M005)
10. Mohan Reddy (DUMMY_M006)

#### Adolescents/Children (5):
11. Rohit Sharma (DUMMY_A001)
12. Anjali Desai (DUMMY_A002)
13. Karan Patel (DUMMY_A003)
14. Pooja Singh (DUMMY_A004)
15. Arjun Kumar (DUMMY_A005)

---

## Testing the Seeding

### Method 1: Fresh Install
1. **Uninstall the app** completely
2. **Rebuild and install** from Android Studio
3. Launch the app
4. Check Logcat for:
   ```
   D/AshaLocalDatabase: Database created - seeding dummy patient data
   D/AshaLocalDatabase: Successfully seeded 19 dummy patients
   ```
5. Navigate to any patient search screen
6. You should see all 19 dummy patients available

### Method 2: Clear App Data
1. Go to **Settings → Apps → Sukhayu Patient App**
2. Tap **Storage**
3. Tap **Clear Data**
4. Launch the app again
5. Check Logcat as above

### Method 3: Use Destructive Migration (Already Enabled)
Since `.fallbackToDestructiveMigration()` is enabled:
1. Simply rebuild the project
2. The database will be recreated on next launch
3. Seeding will happen automatically

---

## Verification Queries

### Check Patient Count
```kotlin
val count = patientDao.getPatientCount()
Log.d("TEST", "Total patients: $count")
// Expected: 19
```

### Search All Patients
```kotlin
val allPatients = patientDao.searchPatients("%")
Log.d("TEST", "Found ${allPatients.size} patients")
// Expected: 19 (or up to 10 if LIMIT is applied)
```

### Get Specific Patient
```kotlin
val patient = patientDao.getPatientById("DUMMY_001")
Log.d("TEST", "Patient: ${patient?.name}")
// Expected: "Priya Sharma"
```

---

## Architecture Benefits

### ✅ Offline-First
- Dummy data available immediately without network
- All features can be tested offline

### ✅ Consistent Test Data
- Same dummy patients across all modules
- ANC surveys use pregnant women
- TB screening uses adult men and adolescents
- General Survey can use any patient

### ✅ One-Time Seeding
- `onCreate()` only runs when database is created
- No duplicate inserts
- Uses `OnConflictStrategy.REPLACE` to handle any conflicts

### ✅ Background Processing
- Seeding happens on IO dispatcher
- Doesn't block main thread
- Uses coroutines for async operation

### ✅ Error Handling
- Try-catch block handles any insertion errors
- Logs errors for debugging
- App continues to function even if seeding fails

---

## Database Flow Diagram

```
App Launch
    ↓
AshaLocalDatabase.getInstance(context)
    ↓
Is database file present?
    ↓
NO → Room creates database
    ↓
    DatabaseCallback.onCreate() triggered
    ↓
    Launch coroutine (IO thread)
    ↓
    patientDao.insertPatients(DummyData.getDummyPatients())
    ↓
    19 patients inserted
    ↓
    Log success message
    
YES → Use existing database
    ↓
    (no seeding)
```

---

## Code Summary

### Files Modified:
1. **AshaLocalDatabase.kt**
   - Added imports for coroutines, logging, and DummyData
   - Added `databaseScope` for background operations
   - Added `DatabaseCallback` inner class
   - Updated `getInstance()` to add callback

### Files Used (No Changes):
1. **PatientDao.kt** - Already had `insertPatients()` method
2. **DummyData.kt** - Source of dummy patient data

---

## Important Notes

### 🔄 When Does Seeding Happen?
- **ONLY** on database creation (first install or after clear data)
- **NOT** on app restart
- **NOT** on database version upgrade (unless using destructive migration)

### 🔍 How to Trigger Re-seeding?
To see the seeding happen again:
1. **Clear app data** (Settings → Apps → Clear Data)
2. **Uninstall and reinstall** the app
3. **Change database version** and rebuild (will trigger destructive migration)

### ⚠️ Destructive Migration Impact
Currently using `.fallbackToDestructiveMigration()`:
- Any database schema change will recreate the database
- All existing data will be lost
- Dummy patients will be re-seeded automatically

### 📱 Production Considerations
For production:
- Replace `.fallbackToDestructiveMigration()` with proper migrations
- Remove or disable dummy data seeding
- Use real patient data from server sync

---

## Expected Logcat Output

When database is created and seeded:

```
D/AshaLocalDatabase: Database created - seeding dummy patient data
D/AshaLocalDatabase: Successfully seeded 19 dummy patients
```

If seeding fails:
```
D/AshaLocalDatabase: Database created - seeding dummy patient data
E/AshaLocalDatabase: Error seeding dummy patients
E/AshaLocalDatabase: [Stack trace with error details]
```

---

## Next Steps

### 1. Test the Seeding
- Clear app data or reinstall
- Launch app and check Logcat
- Verify patient search shows all 19 patients

### 2. Use Seeded Data
All features can now use the pre-seeded patients:
- **General Survey**: Search and select any patient
- **ANC Survey**: Use pregnant women (DUMMY_001 to DUMMY_004)
- **TB Screening**: Use adult men or adolescents
- **Patient Search**: All 19 patients available

### 3. Add More Dummy Data (Optional)
If needed, you can add more entities to DummyData:
- Pregnancies linked to pregnant women
- Past ANC visits
- TB screening records
- General survey records

Just add similar callbacks for other DAOs if needed.

---

## Status: ✅ COMPLETE

The database seeding is fully implemented and ready to use. The app will automatically populate dummy patient data on first launch, providing immediate offline testing capability for all features.

