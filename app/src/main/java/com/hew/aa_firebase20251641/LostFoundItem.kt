package com.hew.aa_firebase20251641
data class LostFoundItem(
    var id: Long = -1L,
    var type: String = TYPE_LOST,
    var name: String = "",
    var place: String = "",
    var date: String = "",
    var status: String = STATUS_OPEN,
    var description: String = "",
    var ownerId: String = "",
    var createdAt: Long = 0L
) {
    companion object {
        const val TYPE_LOST = "LOST"
        const val TYPE_FOUND = "FOUND"
        const val STATUS_OPEN = "OPEN"
        const val STATUS_COMPLETED = "COMPLETED"
    }
}
