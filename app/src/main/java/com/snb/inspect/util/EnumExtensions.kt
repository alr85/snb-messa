package com.snb.inspect.util

import com.snb.inspect.formModules.ConditionState
import com.snb.inspect.formModules.YesNoState

/**
 * Extension functions to convert Enum states to strings for persistence.
 * Converts [YesNoState.UNSPECIFIED] and [ConditionState.UNSPECIFIED] to "NA" 
 * to ensure database compatibility.
 */

fun YesNoState.toSafeString(): String {
    return if (this == YesNoState.UNSPECIFIED) "NA" else this.name
}

fun ConditionState.toSafeString(): String {
    return if (this == ConditionState.UNSPECIFIED) "NA" else this.name
}
