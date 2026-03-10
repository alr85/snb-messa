package com.example.mecca.formModules

enum class YesNoState {
    YES,
    NO,
    NA,
    UNSPECIFIED
}

enum class ConditionState {
    GOOD,
    SATISFACTORY,
    POOR,
    NA,
    UNSPECIFIED
}

enum class PvRuleStatus {
    Pass, Fail, Incomplete, Warning, NA
}

data class PvRule(
    val description: String,
    val status: PvRuleStatus
)
