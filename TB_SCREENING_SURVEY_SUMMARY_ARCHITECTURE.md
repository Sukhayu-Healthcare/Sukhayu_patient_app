## TB Screening Survey Summary Integration - Visual Architecture Guide

---

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    ASHA Patient App                              │
└─────────────────────────────────────────────────────────────────┘
                              │
                    ┌─────────┴─────────┐
                    │                   │
        ┌───────────▼──────────┐    ┌──▼──────────────┐
        │ TB Screening Flow    │    │ View Surveys    │
        │                      │    │ Flow            │
        └───────────┬──────────┘    └──┬──────────────┘
                    │                   │
        ┌───────────▼──────────────┐    │
        │ TbScreeningActivity      │    │
        │ (TB Screening Form)      │    │
        └───────────┬──────────────┘    │
                    │                   │
        ┌───────────▼──────────────┐    │
        │ TbScreeningViewModel     │    │
        │ - saveTbScreening()      │    │
        │ - syncPendingTbScreenings│    │
        └───────────┬──────────────┘    │
                    │                   │
        ┌───────────▼──────────────────────────────┐
        │ TbScreeningRepository  (UPDATED)         │
        │ - createOrUpdateTbScreening(entity,ashaId)
        │ - markAsSynced(id, ashaId)               │
        └───────┬─────────────────┬────────────────┘
                │                 │
        ┌───────▼────────┐  ┌────▼──────────┐
        │ TbScreeningDao │  │ SurveySummaryDao (UPDATED)
        │                │  │                │
        │ Save to        │  │ Save to        │
        │ tb_screenings  │  │ survey_summary │
        └───────┬────────┘  └────┬───────────┘
                │                │
        ┌───────▼────────────────▼──────────┐
        │      Room Database (Local)        │
        │                                   │
        │  ┌──────────────────────────┐   │
        │  │ tb_screenings table      │   │
        │  │ - id (PK)               │   │
        │  │ - patientId             │   │
        │  │ - name                  │   │
        │  │ - isSynced              │   │
        │  │ - [symptoms, risk...]   │   │
        │  └──────────────┬───────────┘   │
        │                 │               │
        │                 │ surveyLocalId │
        │                 │ (reference)   │
        │                 │               │
        │  ┌──────────────▼───────────┐   │
        │  │ survey_summary table      │   │
        │  │ - summaryId (PK)         │   │
        │  │ - surveyLocalId (FK)     │   │
        │  │ - surveyType             │   │
        │  │ - patientName            │   │
        │  │ - isSynced               │   │
        │  │ - ashaId (INDEX)         │   │
        │  └──────────────┬───────────┘   │
        │                 │               │
        └─────────────────┼───────────────┘
                          │
                ┌─────────▼─────────┐
                │ Flow<List<...>>   │
                │ Changes detected  │
                └─────────┬─────────┘
                          │
            ┌─────────────▼──────────────┐
            │ AshaViewSurveysViewModel   │
            │ (NO CHANGES NEEDED)        │
            │ - Observes: getAllForAsha()│
            └─────────────┬──────────────┘
                          │
            ┌─────────────▼──────────────┐
            │ SurveySummaryAdapter       │
            │ (NO CHANGES NEEDED)        │
            │ Updates RecyclerView       │
            └─────────────┬──────────────┘
                          │
            ┌─────────────▼──────────────┐
            │ View Surveys Screen        │
            │ Shows TB screenings        │
            │ with Pending/Synced status │
            └────────────────────────────┘
```

---

## 📊 Data Flow Sequence Diagram

### Scenario 1: Save TB Screening

```
User                TbScreening        TbScreening         TB
Activity            ViewModel          Repository          Database
  │                   │                   │                  │
  │ saveTbScreening() │                   │                  │
  ├──────────────────>│                   │                  │
  │                   │ getAshaId()       │                  │
  │                   │ TokenManager ✓    │                  │
  │                   │                   │                  │
  │                   │ createOrUpdate()  │                  │
  │                   ├──────────────────>│                  │
  │                   │                   │ upsertTbScreening()
  │                   │                   ├─────────────────>│
  │                   │                   │                  │ Save to
  │                   │                   │                  │ tb_screenings
  │                   │                   │<─────────────────┤
  │                   │                   │                  │
  │                   │                   │ insertOrUpdate() │
  │                   │                   │ (summary)        │
  │                   │                   ├─────────────────>│
  │                   │                   │                  │ Save to
  │                   │                   │                  │ survey_summary
  │                   │                   │<─────────────────┤
  │                   │<──────────────────┤                  │
  │<──────────────────┤                   │                  │
  │ Toast: "Saved"    │                   │                  │
  │                   │                   │                  │
  │                   ┌───────────────────────────────────┐  │
  │                   │ Flow<List> emits new survey       │  │
  │                   └───────────────────────────────────┘  │
  │                                                            │
  │                   View Surveys updates:                  │
  │                   - New survey visible                   │
  │                   - Status: "Pending"                    │
  │                   - Counts: Pending=1, Synced=0         │
  │
```

### Scenario 2: Sync TB Screening

```
User                TbScreening        TbScreening         TB
Dashboard           ViewModel          Repository          Database      Backend API
  │                   │                   │                  │              │
  │ Click Sync        │                   │                  │              │
  ├──────────────────>│                   │                  │              │
  │                   │ getAshaId() ✓     │                  │              │
  │                   │                   │                  │              │
  │                   │ getPending()      │                  │              │
  │                   ├──────────────────>│                  │              │
  │                   │                   │ getUnsyncedTb()  │              │
  │                   │                   ├─────────────────>│              │
  │                   │                   │<─────────────────┤ (unsynced)   │
  │                   │<──────────────────┤                  │              │
  │                   │ [pending list]    │                  │              │
  │                   │                   │                  │              │
  │                   │ For each pending: │                  │              │
  │                   │                   │                  │              │
  │                   │ toTbFirstRequest()│                  │              │
  │                   │ ────────────────┐ │                  │              │
  │                   │ ────────────────>│ │                  │              │
  │                   │                  │ POST request      │              │
  │                   │                  ├──────────────────────────────────>│
  │                   │                  │                  │              │ Validate
  │                   │                  │                  │              │ Save to DB
  │                   │                  │<──────────────────────────────────┤
  │                   │                  │ 200 OK            │              │
  │                   │                  │                  │              │
  │                   │ markAsSynced()   │                  │              │
  │                   ├──────────────────>│                  │              │
  │                   │                   │ markTbSynced()   │              │
  │                   │                   ├─────────────────>│              │
  │                   │                   │ UPDATE tb_screenings
  │                   │                   │ isSynced=true    │              │
  │                   │                   │<─────────────────┤              │
  │                   │                   │                  │              │
  │                   │                   │ markSummarySync()│              │
  │                   │                   ├─────────────────>│              │
  │                   │                   │ UPDATE survey_summary
  │                   │                   │ isSynced=true    │              │
  │                   │                   │<─────────────────┤              │
  │                   │<──────────────────┤                  │              │
  │<──────────────────┤                   │                  │              │
  │ Toast: "Synced"   │                   │                  │              │
  │                   │                   │                  │              │
  │                   ┌──────────────────────────────────┐   │              │
  │                   │ Flow<List> emits change         │   │              │
  │                   └──────────────────────────────────┘   │              │
  │                                                            │
  │                   View Surveys updates:                   │
  │                   - Survey status: "Synced"               │
  │                   - Counts: Pending=0, Synced=1           │
  │
```

---

## 🔄 State Transitions

```
┌──────────────────────────────────────────────────────────────┐
│           TB Screening State Machine                         │
└──────────────────────────────────────────────────────────────┘

              ┌─────────────────────┐
              │   Not Created       │
              │   (Initial State)   │
              └──────────┬──────────┘
                         │
            User fills TB form & saves
                         │
                         ▼
              ┌─────────────────────────────────────────┐
              │   LOCAL (isSynced=false)                │
              │   - Saved in tb_screenings              │
              │   - Summary in survey_summary           │
              │   - View: "Pending"                     │
              │   - Searchable in View Surveys          │
              └──────────────┬──────────────────────────┘
                             │
                ┌────────────┴─────────────┐
                │                          │
         User clicks Sync            Offline (no sync)
                │                          │
                ▼                          │
         Internet check                   │
              │                            │
         ┌────┴────┐                      │
      No│          │Yes                  │
        │          │                      │
        │          ▼                      │
        │     POST to backend API         │
        │          │                      │
        │     ┌────┴────┐                 │
        │  Fail│        │Success          │
        │      │        │                 │
        │      ▼        ▼                 │
        │   [Stay Local] [Mark Synced]   │
        │      │        │                │
        │      │   ┌────▼─────────────┐  │
        │      │   │ SYNCED (true)    │  │
        │      │   │ - tb_screenings  │  │
        │      │   │   isSynced=true  │  │
        │      │   │ - survey_summary │  │
        │      │   │   isSynced=true  │  │
        │      │   │ - View: "Synced" │  │
        │      │   │ - Not editable   │  │
        │      │   └──────────────────┘  │
        │      │        │                │
        │      └────────┬────────────────┘
        │               │
        └───────────────┘
         (Retry on next sync)
```

---

## 🎯 Component Responsibility Matrix

| Component | Responsibility | Changes |
|-----------|----------------|---------|
| **TbScreeningActivity** | Collect TB screening data from user | ❌ None |
| **TbScreeningViewModel** | Handle save/sync logic, get ashaId | ✅ Enhanced |
| **TbScreeningRepository** | Orchestrate DB operations | ✅ Enhanced |
| **TbScreeningDao** | Raw TB screening DB ops | ❌ None |
| **SurveySummaryMappers** | Convert TB entity → Summary entity | ✅ New |
| **SurveySummaryDao** | Survey summary DB ops | ✅ Enhanced |
| **AshaLocalDatabase** | Database instance | ❌ None |
| **Room DB Tables** | Persist data locally | ✅ Uses existing |
| **AshaViewSurveysActivity** | Display all surveys | ❌ None |
| **AshaViewSurveysViewModel** | Load survey summaries | ❌ None |
| **SurveySummaryAdapter** | Render survey list | ❌ None |
| **Backend API** | Receive TB screening submissions | ❌ None |

---

## 💾 Database Schema Relationship

```
┌──────────────────────────────────────────────────────┐
│              Room Database Schema                    │
├──────────────────────────────────────────────────────┤
│                                                      │
│  ┌──────────────────────────────────────────────┐  │
│  │ tb_screenings                                │  │
│  ├──────────────────────────────────────────────┤  │
│  │ PK  id: String                               │  │
│  │     patientId: String                        │  │
│  │     name: String                             │  │
│  │     ageYears: Int                            │  │
│  │     sex: String                              │  │
│  │     mobileNumber: String?                    │  │
│  │     addressVillage: String                   │  │
│  │     dateOfScreening: String                  │  │
│  │     cough2WeeksOrMore: Boolean               │  │
│  │     coughWithBlood: Boolean                  │  │
│  │     fever2WeeksOrMore: Boolean               │  │
│  │     ... more symptoms ...                    │  │
│  │     isSynced: Boolean  ◄─────┐              │  │
│  │     createdAt: Long           │              │  │
│  │     updatedAt: Long           │              │  │
│  └──────────────────────────────────────────────┘  │
│                                                      │
│                          ┌───────────────────────┐  │
│                          │  Relationship Logic:  │  │
│                          │  When TB screening    │  │
│                          │  saved/synced, also   │  │
│                          │  save/update summary  │  │
│                          └───────────────────────┘  │
│                                                      │
│  ┌──────────────────────────────────────────────┐  │
│  │ survey_summary                               │  │
│  ├──────────────────────────────────────────────┤  │
│  │ PK  summaryId: String                        │  │
│  │     surveyLocalId: String  ◄──────┐          │  │
│  │                                   │          │  │
│  │  FK ┌─ References tb_screenings.id           │  │
│  │     │                                        │  │
│  │     serverId: Long?                          │  │
│  │     patientId: String                        │  │
│  │     patientName: String?                     │  │
│  │     patientPhone: String?                    │  │
│  │     surveyType: String  ("TB_SCREENING")     │  │
│  │     surveyDate: Long                         │  │
│  │     village: String?                         │  │
│  │     status: String                           │  │
│  │     isSynced: Boolean  ◄──────┐              │  │
│  │     ashaId: String (INDEX)                   │  │
│  │                                              │  │
│  │  Indexes:                                    │  │
│  │  - (ashaId)                                  │  │
│  │  - (patientId)                               │  │
│  │  - (surveyType)                              │  │
│  │  - (surveyDate)                              │  │
│  └──────────────────────────────────────────────┘  │
│                                                      │
└──────────────────────────────────────────────────────┘

Legend:
  PK = Primary Key
  FK = Foreign Key
  ◄─── = Synchronized field
  INDEX = Database index for fast queries
```

---

## 🔌 Integration Points

### 1. During TB Screening Save
```
TbScreeningViewModel.saveTbScreening(entity)
    │
    ├─ TokenManager.getUserId() ─→ Get ASHA ID
    │
    └─ TbScreeningRepository.createOrUpdateTbScreening(entity, ashaId)
        │
        ├─ TbScreeningDao.upsertTbScreening(entity)
        │   └─→ Room: INSERT/UPDATE to tb_screenings
        │
        └─ fromTbScreening(entity, ashaId, false) ✅ MAPPER
            └─ SurveySummaryDao.insertOrUpdate(summary)
                └─→ Room: INSERT/UPDATE to survey_summary
```

### 2. During Backend Sync
```
TbScreeningViewModel.syncPendingTbScreenings()
    │
    ├─ TokenManager.getUserId() ─→ Get ASHA ID
    │
    ├─ TbScreeningRepository.getUnsyncedTbScreenings()
    │
    └─ For each pending:
        ├─ ApiClient.submitTbFirst(request)
        │   └─→ HTTP POST to backend
        │
        └─ TbScreeningRepository.markAsSynced(id, ashaId)
            │
            ├─ TbScreeningDao.markTbScreeningAsSynced(id)
            │   └─→ Room: UPDATE tb_screenings SET isSynced=true
            │
            └─ SurveySummaryDao.markSummaryAsSynced(id, ashaId)
                └─→ Room: UPDATE survey_summary SET isSynced=true
```

### 3. View Surveys Display
```
AshaViewSurveysViewModel (unchanged)
    │
    └─ SurveySummaryDao.getAllForAsha(ashaId)
        │
        └─ Flow<List<SurveySummaryEntity>>
            │
            └─ Includes all survey types:
                ├─ TB_SCREENING ✅ NEW
                ├─ ANC_FIRST_VISIT (future)
                └─ GENERAL_SURVEY (future)
                    │
                    └─ AshaViewSurveysAdapter
                        └─ RecyclerView displays surveys
```

---

## ✅ Pre-Deployment Checklist

```
┌─────────────────────────────────────────────┐
│ Code Generation                             │
├─────────────────────────────────────────────┤
│ ✅ SurveySummaryMappers.kt created         │
│ ✅ SurveySummaryDao.kt updated             │
│ ✅ TbScreeningRepository.kt updated        │
│ ✅ TbScreeningViewModel.kt updated         │
│ ✅ All imports added                       │
│ ✅ All syntax valid                        │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│ Compilation                                 │
├─────────────────────────────────────────────┤
│ ⏳ gradle build (run in terminal)          │
│   Expected: BUILD SUCCESSFUL               │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│ Unit Testing                                │
├─────────────────────────────────────────────┤
│ ⏳ Test mapper: fromTbScreening()          │
│ ⏳ Test: SurveySummaryEntity fields       │
│ ⏳ Test: markSummaryAsSynced() query      │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│ Integration Testing                         │
├─────────────────────────────────────────────┤
│ ⏳ Save TB Screening                       │
│   Expected: Appears in View Surveys        │
│ ⏳ Verify sync counts                      │
│   Expected: Pending +1, Synced 0           │
│ ⏳ Sync to backend                         │
│   Expected: Status → Synced               │
│ ⏳ Verify counts update                    │
│   Expected: Pending 0, Synced +1           │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│ End-to-End Testing                          │
├─────────────────────────────────────────────┤
│ ⏳ Full user flow (save → sync → display) │
│ ⏳ Multiple surveys                        │
│ ⏳ Search/filter with TB screenings        │
│ ⏳ No crashes in logcat                    │
└─────────────────────────────────────────────┘
```

---

## 🎓 Learning Points

### Pattern 1: Mapper Function
**Why:** Separates concerns (conversion logic)
**Where:** `SurveySummaryMappers.fromTbScreening()`
**Benefit:** Reusable, testable, clear intent

### Pattern 2: Repository
**Why:** Single source of truth for data operations
**Where:** `TbScreeningRepository`
**Benefit:** Encapsulates complexity, manages multiple DAOs

### Pattern 3: Safe Null Handling
**Why:** Prevents crashes with optional dependencies
**Where:** `ashaId?.let { surveySummaryDao?.let { ... } }`
**Benefit:** Backward compatible, graceful degradation

### Pattern 4: Flow-Based Reactivity
**Why:** Automatic UI updates when data changes
**Where:** `SurveySummaryDao.getAllForAsha()` returns Flow
**Benefit:** No manual refresh needed, always in sync

---

**Architecture Guide Complete ✅**
**All visual diagrams and integration points documented**
**Ready for implementation and testing**

