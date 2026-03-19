package com.snb.inspect.dataClasses.mappers

import com.snb.inspect.dataClasses.NoticeCloud
import com.snb.inspect.dataClasses.NoticeLocal

fun NoticeCloud.toLocal(): NoticeLocal? {

    // If the ID is null, the notice is unusable.
    val id = noticeId ?: return null

    return NoticeLocal(
        noticeId = id,

        title = title?.trim().takeUnless { it.isNullOrEmpty() }
            ?: "Notice",

        body = body?.trim().takeUnless { it.isNullOrEmpty() }
            ?: "",

        dateAdded = dateAdded,
        createdBy = createdBy,

        isActive = isActive ?: true,
        isPinned = isPinned ?: false
    )
}
