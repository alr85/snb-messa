import java.time.LocalDate
import java.time.DayOfWeek
import java.time.temporal.ChronoUnit

object MasterPasswordGenerator {

    private const val SECRET_SAUCE = 555986

    fun generateWeeklyPassword(): String {
        // Get current date
        val currentDate = LocalDate.now()

        // Calculate ISO-like week number manually
        val firstDayOfYear = LocalDate.of(currentDate.year, 1, 1)
        val firstMonday = firstDayOfYear.with(DayOfWeek.MONDAY)

        // If the year starts before the first Monday, adjust
        val adjustedStart = if (firstMonday.isAfter(firstDayOfYear)) firstMonday.minusWeeks(1) else firstMonday

        val daysBetween = ChronoUnit.DAYS.between(adjustedStart, currentDate)
        val weekOfYear = (daysBetween / 7 + 1).toInt()

        println("Week of Year: $weekOfYear")

        // Get the current year
        val currentYear = currentDate.year
        println("Current Year: $currentYear")

        // Calculate the multiplier
        val multiplier = (weekOfYear + SECRET_SAUCE) % 17 + 3
        println("Multiplier: $multiplier")

        // Calculate the raw value
        val rawValue = (weekOfYear * SECRET_SAUCE * multiplier) / (currentYear % 100)
        println("Raw Value: $rawValue")

        // Take the last 6 digits and pad if necessary
        return rawValue.toString().takeLast(6).padStart(6, '0')
    }
}
