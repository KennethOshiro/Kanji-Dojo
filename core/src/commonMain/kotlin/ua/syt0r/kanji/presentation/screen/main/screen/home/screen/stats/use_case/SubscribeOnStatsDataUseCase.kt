package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.use_case

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import ua.syt0r.kanji.core.RefreshableData
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.refreshableDataFlow
import ua.syt0r.kanji.core.srs.LetterPracticeType
import ua.syt0r.kanji.core.srs.LetterSrsManager
import ua.syt0r.kanji.core.srs.VocabPracticeType
import ua.syt0r.kanji.core.time.TimeUtils
import ua.syt0r.kanji.core.user_data.practice.ReviewHistoryItem
import ua.syt0r.kanji.core.user_data.practice.ReviewHistoryRepository
import ua.syt0r.kanji.presentation.LifecycleState
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

interface SubscribeOnStatsDataUseCase {
    operator fun invoke(lifecycleState: StateFlow<LifecycleState>): Flow<RefreshableData<StatsData>>
}

data class StatsData(
    val today: LocalDate,
    val yearlyPractices: Map<LocalDate, Int>,
    val todayReviews: Int,
    val todayTimeSpent: Duration,
    val totalReviews: Int,
    val totalTimeSpent: Duration,
    val uniqueLettersStudied: Int,
    val uniqueWordsStudied: Int
)

class DefaultSubscribeOnStatsDataUseCase(
    private val letterSrsManager: LetterSrsManager,
    private val reviewHistoryRepository: ReviewHistoryRepository,
    private val timeUtils: TimeUtils
) : SubscribeOnStatsDataUseCase {

    override fun invoke(
        lifecycleState: StateFlow<LifecycleState>
    ): Flow<RefreshableData<StatsData>> {
        return refreshableDataFlow(
            dataChangeFlow = letterSrsManager.dataChangeFlow,
            lifecycleState = lifecycleState,
            valueProvider = { getStats() }
        )
    }

    private suspend fun getStats(): StatsData {
        Logger.logMethod()
        val today = timeUtils.getCurrentDate()

        val timeZone = TimeZone.currentSystemDefault()
        val yearStart = LocalDate(today.year, 1, 1).atStartOfDayIn(timeZone)
        val yearEnd = LocalDate(today.year + 1, 1, 1).atStartOfDayIn(timeZone)

        val yearlyReviewsToDateMap = reviewHistoryRepository.getReviews(yearStart, yearEnd)
            .map { it to it.timestamp.toLocalDateTime(timeZone).date }

        val dateToReviews: Map<LocalDate, List<ReviewHistoryItem>> = yearlyReviewsToDateMap
            .groupBy { it.second }
            .toList()
            .associate { it.first to it.second.map { it.first } }

        val todayReviews = dateToReviews[today] ?: emptyList()

        return StatsData(
            today = today,
            yearlyPractices = dateToReviews.mapValues { (_, practices) -> practices.size },
            todayReviews = todayReviews.size,
            todayTimeSpent = todayReviews.map { it.duration }
                .fold(Duration.ZERO) { acc, duration ->
                    acc.plus(duration.coerceAtMost(SingleReviewDurationLimit))
                },
            totalReviews = reviewHistoryRepository
                .getTotalReviewsCount()
                .toInt(),
            totalTimeSpent = reviewHistoryRepository
                .getTotalPracticeTime(SingleReviewDurationLimit.inWholeMilliseconds),
            uniqueLettersStudied = reviewHistoryRepository
                .getUniqueReviewItemsCount(LetterPracticeType.srsPracticeTypeValues)
                .toInt(),
            uniqueWordsStudied = reviewHistoryRepository
                .getUniqueReviewItemsCount(VocabPracticeType.srsPracticeTypeValues)
                .toInt()
        )
    }

    companion object {
        private val SingleReviewDurationLimit = 1.minutes
    }

}