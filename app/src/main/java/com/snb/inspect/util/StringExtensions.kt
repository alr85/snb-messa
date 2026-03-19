package com.snb.inspect.util

import com.snb.inspect.formModules.ConditionState
import com.snb.inspect.formModules.YesNoState

fun String.toYesNoState(): YesNoState {
    return when (this.uppercase()) {
        "YES" -> YesNoState.YES
        "NO" -> YesNoState.NO
        "NA" -> YesNoState.NA
        else -> YesNoState.UNSPECIFIED
    }
}

fun String.toConditionState(): ConditionState {
    return when (this.uppercase()) {
        "GOOD" -> ConditionState.GOOD
        "SATISFACTORY" -> ConditionState.SATISFACTORY
        "POOR" -> ConditionState.POOR
        "NA" -> ConditionState.NA
        else -> ConditionState.UNSPECIFIED
    }
}
