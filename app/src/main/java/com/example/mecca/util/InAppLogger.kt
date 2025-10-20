package com.example.mecca.util

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object InAppLogger {
    private val logList = mutableListOf<Pair<String, LogLevel>>()
    private val listeners = mutableListOf<(List<Pair<String, LogLevel>>) -> Unit>()

    enum class LogLevel { DEBUG, ERROR }

    fun d(message: String) = log(LogLevel.DEBUG, message)
    fun e(message: String) = log(LogLevel.ERROR, message)

    private fun log(level: LogLevel, message: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val entry = "$timestamp  $message"
        logList.add(entry to level)
        if (logList.size > 500) logList.removeAt(0)
        listeners.forEach { it(logList.toList()) }

        // still go to Android's Logcat too
        when (level) {
            LogLevel.DEBUG -> Log.d("MESSA-DEBUG", message)
            LogLevel.ERROR -> Log.e("MESSA-DEBUG", message)
        }
    }

    fun observe(onUpdate: (List<Pair<String, LogLevel>>) -> Unit) {
        listeners.add(onUpdate)
        onUpdate(logList.toList())
    }

    fun stopObserving(onUpdate: (List<Pair<String, LogLevel>>) -> Unit) {
        listeners.remove(onUpdate)
    }
}
@Composable
fun LogConsole() {
    val logs = remember { mutableStateListOf<Pair<String, InAppLogger.LogLevel>>() }

    DisposableEffect(Unit) {
        val observer: (List<Pair<String, InAppLogger.LogLevel>>) -> Unit = { newList ->
            logs.clear()
            logs.addAll(newList)
        }
        InAppLogger.observe(observer)
        onDispose { InAppLogger.stopObserving(observer) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(8.dp)
    ) {
        items(logs) { (line, level) ->
            val color = when (level) {
                InAppLogger.LogLevel.DEBUG -> Color.Green
                InAppLogger.LogLevel.ERROR -> Color.Red
            }
            Text(text = line, color = color, fontSize = 12.sp)
        }
    }
}
