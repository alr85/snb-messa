package com.snb.inspect.calibrationViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snb.inspect.util.CheckweigherAccuracyCalculator
import com.snb.inspect.util.CheckweigherAccuracyResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class CheckweigherAccuracyInputs(
    val tNomText: String = "",

    // 10 passes as text so your UI can stay simple
    val passTexts: List<String> = List(10) { "" },

    val staticScaleText: String = "",
    val checkweigherText: String = ""
)

class CheckweigherAccuracyViewModel : ViewModel() {

    private val _inputs = MutableStateFlow(CheckweigherAccuracyInputs())
    val inputs: StateFlow<CheckweigherAccuracyInputs> = _inputs

    val result: StateFlow<CheckweigherAccuracyResult?> =
        inputs
            .map { i ->
                val tNom = i.tNomText.toDoubleOrNull() ?: return@map null
                
                // REJECT < 5g: Return null so no calculation is performed and UI shows initial state
                if (tNom < 5.0) return@map null

                val passes = i.passTexts.map { it.toDoubleOrNull() }
                val staticScale = i.staticScaleText.toDoubleOrNull()
                val checkweigher = i.checkweigherText.toDoubleOrNull()

                CheckweigherAccuracyCalculator.calculate(
                    tNom,
                    passes,
                    staticScale,
                    checkweigher
                )
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun setTNom(text: String) {
        val filtered = text.filter { it.isDigit() || it == '.' }
        _inputs.value = _inputs.value.copy(tNomText = filtered)
    }

    fun setPass(index: Int, text: String) {
        val old = _inputs.value.passTexts
        val newList = old.toMutableList().also { it[index] = text }
        _inputs.value = _inputs.value.copy(passTexts = newList)
    }

    fun setStaticScale(text: String) {
        _inputs.value = _inputs.value.copy(staticScaleText = text)
    }

    fun setCheckweigher(text: String) {
        _inputs.value = _inputs.value.copy(checkweigherText = text)
    }

    fun clearAll() {
        _inputs.value = CheckweigherAccuracyInputs()
    }
}
