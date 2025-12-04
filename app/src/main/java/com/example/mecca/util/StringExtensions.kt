package com.example.mecca.util

import com.example.mecca.formModules.ConditionState
import com.example.mecca.formModules.YesNoState

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
