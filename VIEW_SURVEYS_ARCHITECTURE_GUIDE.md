# View Surveys - Data Flow & Architecture Visual Guide

## Complete Data Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                     ASHA VIEW SURVEYS SCREEN                        │
│                    Real Data Implementation                          │
└─────────────────────────────────────────────────────────────────────┘

    USER ACTIONS
    ────────────

    [User Opens Activity]
           │
           ▼
    ┌─────────────────┐
    │  Activity Init  │
    │  onCreate()     │
    └────────┬────────┘
             │
             ├─────────────────────────────────┐
             │                                 │
             ▼                                 ▼
    ┌──────────────────┐        ┌──────────────────────┐
    │ Initialize Views │        │ Create ViewModel     │
    │ - RecyclerView   │        │ (loaded via Provider)│
    │ - SearchView     │        └────────┬─────────────┘
    │ - Spinner        │                 │
    │ - Adapter        │                 ▼
    └──────────────────┘        ┌─────────────────────┐
                                │ ViewModel Init      │
                                │ refreshAll() called │
                                └────────┬────────────┘
                                         │
                                         ▼
                        ┌────────────────────────────────┐
                        │  ROOM DATABASE ACCESS          │
                        │  ────────────────────────────  │
                        │  dao.getAllForAsha(ashaId)    │
                        │  returns: Flow<List<Entity>>  │
                        │                                │
                        │  + countSyncedForAsha()       │
                        │  + countPendingForAsha()      │
                        │                                │
                        │  [survey_summary table]       │
                        │  ├─ summaryId                 │
                        │  ├─ patientName               │
                        │  ├─ surveyType (e.g., TB_...) │
                        │  ├─ surveyDate (timestamp)    │
                        │  ├─ village                   │
                        │  ├─ status                    │
                        │  ├─ isSynced (boolean)        │
                        │  └─ ashaId                    │
                        └────────────┬───────────────────┘
                                     │
                                     ▼
                        ┌────────────────────────────────┐
                        │ ENTITY → UI MODEL MAPPING      │
                        │ ─────────────────────────────  │
                        │ toUiModel() function:          │
                        │                                │
                        │ 1. Format Date                 │
                        │    surveyDate (Long)           │
                        │    ↓                           │
                        │    SimpleDateFormat("dd MMM")  │
                        │    ↓                           │
                        │    "15 Dec 2024" (String)      │
                        │                                │
                        │ 2. Format Type                 │
                        │    surveyType ("TB_SCREENING") │
                        │    ↓                           │
                        │    friendlyType()              │
                        │    ↓                           │
                        │    "TB Screening" (String)     │
                        │                                │
                        │ 3. Map Fields                  │
                        │    patientName (String)        │
                        │    village (String)            │
                        │    status (String)             │
                        │    isSynced (Boolean)          │
                        │                                │
                        │ Result: SurveySummaryUiModel   │
                        └────────────┬───────────────────┘
                                     │
                                     ▼
                    ┌─────────────────────────────────────┐
                    │ LIVEDATA EMISSION                   │
                    │ ─────────────────────────────────── │
                    │ _surveys.value = uiModelList        │
                    │ _syncSummaryText.value = "Synced:..." │
                    └────────────┬────────────────────────┘
                                 │
                                 ▼
            ┌────────────────────────────────────────┐
            │ ACTIVITY OBSERVER                      │
            │ ──────────────────────────────────── │
            │ viewModel.surveys.observe()            │
            │   ├─ Store in fullSurveyList           │
            │   └─ Call applyFilters()               │
            │                                        │
            │ viewModel.syncSummaryText.observe()    │
            │   └─ Update tvSyncSummary              │
            └────────────┬──────────────────────────┘
                         │
                         ▼


    FILTERING LOGIC
    ───────────────

    ┌──────────────────────────────────────────────────────────┐
    │ USER INPUT → FILTER APPLICATION                          │
    ├──────────────────────────────────────────────────────────┤
    │                                                          │
    │  SearchView Input                                        │
    │  "Priya"                                                 │
    │  │                                                      │
    │  ├─→ onQueryTextChange()                               │
    │  │   └─→ applyFilters()                                │
    │  │                                                      │
    │  └─→ searchQuery = "priya" (lowercase)                 │
    │      │                                                  │
    │      ▼                                                  │
    │      Filter condition:                                  │
    │      survey.patientName.lowercase().contains(searchQ) │
    │           OR                                            │
    │      survey.surveyType.lowercase().contains(searchQ)   │
    │                                                          │
    ├─────────────────────────────────────────────────────────┤
    │                                                          │
    │  Spinner Selection                                       │
    │  "TB Screening"                                          │
    │  │                                                      │
    │  ├─→ onItemSelected()                                  │
    │  │   └─→ applyFilters()                                │
    │  │                                                      │
    │  └─→ selectedType = "TB Screening"                     │
    │      │                                                  │
    │      ▼                                                  │
    │      Filter condition:                                  │
    │      selectedType == "All Types"  (show all)           │
    │           OR                                            │
    │      survey.surveyType == selectedType                 │
    │                                                          │
    └──────────────────────────────────────────────────────────┘


    ┌──────────────────────────────────────────────────────────┐
    │ COMBINED FILTER LOGIC                                    │
    ├──────────────────────────────────────────────────────────┤
    │                                                          │
    │  applyFilters() function:                               │
    │  ┌──────────────────────────────────────────────────┐  │
    │  │ val filteredList = fullSurveyList.filter { sv → │  │
    │  │   val matchesSearch = searchQuery.isEmpty() ||   │  │
    │  │       sv.patientName.contains(searchQuery) ||    │  │
    │  │       sv.surveyType.contains(searchQuery)        │  │
    │  │                                                  │  │
    │  │   val matchesType = selectedType == "All Types"  │  │
    │  │       || sv.surveyType == selectedType           │  │
    │  │                                                  │  │
    │  │   matchesSearch && matchesType  ← AND LOGIC     │  │
    │  │ }                                                 │  │
    │  └──────────────────────────────────────────────────┘  │
    │                                                          │
    │  Example:                                               │
    │  ┌─ Full List: 10 surveys                              │
    │  │  - Priya (TB Screening) ✓                          │
    │  │  - Priya (ANC Follow-up) ✗                         │
    │  │  - Anjali (TB Screening) ✗                         │
    │  │  - Geeta (ANC First Visit) ✗                       │
    │  │  - Priya (TB Follow-up) ✗                          │
    │  │  - ...                                              │
    │  │                                                      │
    │  └─ Filtered List: 1 survey (matches both filters)     │
    │                                                          │
    └──────────────────────────────────────────────────────────┘


    ┌──────────────────────────────────────────────────────────┐
    │ RECYCLERVIEW UPDATE                                      │
    ├──────────────────────────────────────────────────────────┤
    │                                                          │
    │  adapter.submitList(filteredList)                       │
    │         │                                               │
    │         ▼                                               │
    │  DiffCallback.areItemsTheSame() → Calculate diff       │
    │         │                                               │
    │         ▼                                               │
    │  onBindViewHolder() → Update only changed items        │
    │         │                                               │
    │         ▼                                               │
    │  Display filtered surveys in RecyclerView              │
    │  ├─ Patient Name: "Priya Singh"                        │
    │  ├─ Survey Type: "TB Screening"                        │
    │  ├─ Date: "15 Dec 2024"                                │
    │  ├─ Village: "Village A"                               │
    │  ├─ Status: "COMPLETED"                                │
    │  └─ Sync Icon: ✓ (synced)                              │
    │                                                          │
    └──────────────────────────────────────────────────────────┘
```

---

## Architecture Layers

```
┌────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                      │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐ │
│  │ AshaViewSurveysActivity                              │ │
│  ├──────────────────────────────────────────────────────┤ │
│  │ • UI View bindings (SearchView, Spinner, RecyclerV) │ │
│  │ • Observe LiveData from ViewModel                    │ │
│  │ • Handle user interactions (SearchView, Spinner)     │ │
│  │ • Apply filters and update adapter                   │ │
│  │ • No business logic, no Room access                  │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐ │
│  │ SurveySummaryAdapter + SurveySummaryUiModel          │ │
│  ├──────────────────────────────────────────────────────┤ │
│  │ • Display UI models in RecyclerView                  │ │
│  │ • Handle click events                                │ │
│  │ • Calculate diffs for efficient updates              │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                            │
└────────────────────────────────────────────────────────────┘
                           ▲
                           │
          ┌────────────────┴───────────────┐
          │                                │
          ▼                                ▼
┌──────────────────────────┐   ┌──────────────────────────┐
│  PRESENTATION INTERFACE  │   │   PRESENTATION INTERFACE │
│  (LiveData Observers)    │   │   (Search + Filter UI)   │
└──────────────┬───────────┘   └──────────────┬───────────┘
               │                              │
               └──────────────┬───────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                  VIEWMODEL LAYER                           │
│                                                            │
│  ┌─────────────────────────────────────────────────────┐  │
│  │ AshaViewSurveysViewModel                            │  │
│  ├─────────────────────────────────────────────────────┤  │
│  │ • Load data from Room (no backend calls)            │  │
│  │ • Map SurveySummaryEntity to SurveySummaryUiModel   │  │
│  │ • Format dates (dd MMM yyyy)                        │  │
│  │ • Format type names (friendly names)                │  │
│  │ • Calculate sync counts                             │  │
│  │ • Expose LiveData<surveys>                          │  │
│  │ • Expose LiveData<syncSummaryText>                  │  │
│  │ • Business logic only, no Android references       │  │
│  └─────────────────────────────────────────────────────┘  │
│                                                            │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                   DATA LAYER (ROOM)                         │
│                                                            │
│  ┌─────────────────────────────────────────────────────┐  │
│  │ SurveySummaryDao                                     │  │
│  ├─────────────────────────────────────────────────────┤  │
│  │ • getAllForAsha(ashaId): Flow<List<Entity>>         │  │
│  │ • countSyncedForAsha(ashaId): Flow<Int>             │  │
│  │ • countPendingForAsha(ashaId): Flow<Int>            │  │
│  │ • insertOrUpdate(summary): suspend                  │  │
│  └─────────────────────────────────────────────────────┘  │
│                                                            │
│  ┌─────────────────────────────────────────────────────┐  │
│  │ SurveySummaryEntity (Database Schema)               │  │
│  ├─────────────────────────────────────────────────────┤  │
│  │ • Table: survey_summary                             │  │
│  │ • Indexed on: ashaId, patientId, surveyType, date  │  │
│  │ • Fields: summaryId, surveyLocalId, serverId, ...  │  │
│  └─────────────────────────────────────────────────────┘  │
│                                                            │
│  ┌─────────────────────────────────────────────────────┐  │
│  │ AshaLocalDatabase (SQLite Database)                 │  │
│  ├─────────────────────────────────────────────────────┤  │
│  │ • Singleton instance                                │  │
│  │ • Manages all local data persistence                │  │
│  │ • Room ORM (Object Relational Mapping)              │  │
│  └─────────────────────────────────────────────────────┘  │
│                                                            │
└─────────────────────────────────────────────────────────────┘
                       ▲
                       │
                       ▼
            ┌──────────────────────┐
            │  LOCAL STORAGE       │
            │  (SQLite Database)   │
            │                      │
            │  Persistent          │
            │  Offline-ready       │
            │  Fast access         │
            │                      │
            └──────────────────────┘
```

---

## State Management Flow

```
┌─────────────────────────────────────────────────────────────┐
│                    APPLICATION STATE                        │
└─────────────────────────────────────────────────────────────┘

┌──────────────┐
│  INITIAL     │
│  Load from DB│
│              │
│  fullSurvey  │  [Survey1, Survey2, Survey3, ...]
│  List: Empty │
└──────────┬───┘
           │
           ▼
┌──────────────────┐
│  SURVEYS LOADED  │
│                  │
│  fullSurveyList: │  [S1(Priya,TB_SCREENING),
│  Populated       │   S2(Anjali,ANC_FIRST),
│                  │   S3(Priya,TB_FOLLOWUP),
│                  │   ...]
└──────────┬───────┘
           │
           ├─ No SearchView input
           ├─ Spinner: "All Types" (default)
           │
           ▼
┌──────────────────┐
│  DISPLAY ALL     │
│  (No filtering)  │
│                  │
│  Displayed:      │  [S1, S2, S3, ...]
│  All surveys     │
└──────────┬───────┘
           │
           ├─ User types "Priya" in SearchView
           │
           ▼
┌──────────────────────┐
│  SEARCH FILTER       │
│  ACTIVE              │
│                      │
│  searchQuery:        │  "priya"
│  selectedType:       │  "All Types"
│  Displayed:          │  [S1(Priya,TB_SCREENING),
│                      │   S3(Priya,TB_FOLLOWUP)]
└──────────┬───────────┘
           │
           ├─ User selects "TB Screening" in Spinner
           │
           ▼
┌──────────────────────────┐
│  COMBINED FILTERS        │
│  BOTH ACTIVE             │
│                          │
│  searchQuery:            │  "priya"
│  selectedType:           │  "TB Screening"
│  Displayed:              │  [S1(Priya,TB_SCREENING)]
│                          │  (only matches BOTH)
└──────────┬───────────────┘
           │
           ├─ User clears SearchView
           │
           ▼
┌──────────────────────────┐
│  TYPE FILTER ONLY        │
│                          │
│  searchQuery:            │  "" (empty)
│  selectedType:           │  "TB Screening"
│  Displayed:              │  [S1(Priya,TB_SCREENING)]
└──────────┬───────────────┘
           │
           ├─ User selects "All Types"
           │
           ▼
┌──────────────────────────┐
│  NO FILTERS              │
│  BACK TO FULL LIST       │
│                          │
│  searchQuery:            │  "" (empty)
│  selectedType:           │  "All Types"
│  Displayed:              │  [S1, S2, S3, ...]
│  (all surveys again)     │
└──────────────────────────┘
```

---

## Data Types & Mappings

```
┌─────────────────────────────────────────────────────────────┐
│                ENTITY → UI MODEL MAPPING                    │
└─────────────────────────────────────────────────────────────┘

SurveySummaryEntity (from Room)
│
├─ patientName: String? = "Priya Singh"
│  └─ UiModel.patientName: String = "Priya Singh"
│
├─ patientPhone: String? = "9876543210"
│  └─ (not used in UiModel)
│
├─ surveyType: String = "TB_SCREENING"
│  ├─ friendlyType() applied
│  └─ UiModel.surveyType: String = "TB Screening"
│
├─ surveyDate: Long = 1702656000000
│  ├─ SimpleDateFormat("dd MMM yyyy") applied
│  └─ UiModel.date: String = "15 Dec 2024"
│
├─ village: String? = "Village A"
│  └─ UiModel.village: String = "Village A"
│
├─ status: String = "COMPLETED"
│  └─ UiModel.status: String = "COMPLETED"
│
├─ isSynced: Boolean = true
│  └─ UiModel.isSynced: Boolean = true
│     └─ Adapter: shows cloud_done icon
│
└─ (summaryId, surveyLocalId, etc. not used in UI)


┌─────────────────────────────────────────────────────────────┐
│              SURVEY TYPE FORMATTING EXAMPLES                │
└─────────────────────────────────────────────────────────────┘

Database Value          →    Friendly Display
─────────────────────        ─────────────────
"TB_SCREENING"          →    "TB Screening"
"TB_FOLLOWUP"           →    "TB Follow-up"
"ANC_FIRST_VISIT"       →    "ANC First Visit"
"ANC_FOLLOWUP"          →    "ANC Follow-up"
"GENERAL_SURVEY"        →    "General Survey"
"OTHER_TYPE"            →    "Other Type" (auto-formatted)


┌─────────────────────────────────────────────────────────────┐
│               DATE FORMATTING EXAMPLES                      │
└─────────────────────────────────────────────────────────────┘

Timestamp (Long)        →    Formatted String
────────────────────        ────────────────
1702656000000           →    "15 Dec 2024"
1702569600000           →    "14 Dec 2024"
1702483200000           →    "13 Dec 2024"
1702396800000           →    "12 Dec 2024"

Format Pattern: "dd MMM yyyy"
  dd   = day (01-31)
  MMM  = month name (Jan, Feb, ..., Dec)
  yyyy = 4-digit year
```

---

## Sync Status Logic

```
┌─────────────────────────────────────────────────────────────┐
│              SYNC STATUS DETERMINATION                      │
└─────────────────────────────────────────────────────────────┘

Database Column: isSynced (Boolean)
│
├─ isSynced = true (uploaded to server)
│  ├─ Count: countSyncedForAsha()
│  ├─ Display in text: "Synced: X"
│  ├─ Adapter shows: ic_cloud_done icon
│  └─ Visual: ✓ Cloud icon (green/checkmark)
│
└─ isSynced = false (pending upload)
   ├─ Count: countPendingForAsha()
   ├─ Display in text: "Pending: Y"
   ├─ Adapter shows: ic_cloud_off icon
   └─ Visual: ☁ Cloud icon (gray/warning)


Example Display:
┌──────────────────────────────┐
│ Synced: 5 | Pending: 3       │  ← tv_sync_summary
├──────────────────────────────┤
│ Survey 1  TB Screening  ✓    │  ← isSynced=true
│ Survey 2  ANC First     ☁    │  ← isSynced=false
│ Survey 3  TB Follow-up  ✓    │  ← isSynced=true
│ Survey 4  ANC Follow-up ☁    │  ← isSynced=false
│ ...                          │
└──────────────────────────────┘
```

---

## No Backend Dependency

```
┌─────────────────────────────────────────────────────────────┐
│              OFFLINE-FIRST ARCHITECTURE                     │
│                                                            │
│  ✓ View Surveys loads ONLY from Room Database             │
│  ✓ NO API calls to backend server                         │
│  ✓ NO network dependency                                  │
│  ✓ Works completely offline                               │
│  ✓ Fast local database access                             │
│  ✓ No internet required                                   │
│                                                            │
│  Data Flow:  Room  →  ViewModel  →  Activity  →  UI       │
│              (Local)   (Logic)       (Display)  (User)     │
│                                                            │
│  No External Services:                                     │
│  ─ No backend API server                                  │
│  ─ No network requests                                    │
│  ─ No authentication needed for data display              │
│  ─ No internet permission required (for View Surveys)     │
│                                                            │
└─────────────────────────────────────────────────────────────┘
```

---

This architecture provides:
- ✅ **Clean separation** of concerns (MVVM)
- ✅ **Reactive updates** (LiveData)
- ✅ **Offline-first** (Room database)
- ✅ **Fast performance** (local SQLite)
- ✅ **Type safety** (Kotlin)
- ✅ **Null safety** (Elvis operators)
- ✅ **Testability** (ViewModel can be unit tested)
- ✅ **Maintainability** (clean code structure)

