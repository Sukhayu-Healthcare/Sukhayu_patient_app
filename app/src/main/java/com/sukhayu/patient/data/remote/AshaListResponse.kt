// Update the property name from ashaWorkers to ashas
data class AshaListResponse(
    val ashas: List<AshaWorker>
)

data class AshaWorker(
    val asha_id: String,
    val asha_name: String,
    val asha_phone: String,
    val village: String?,
    val district: String?,
    val taluka: String?,
    val profile_pic: String?
)
