package com.example.lifelog.batch

import com.example.lifelog.insight.AggregatedInsight
import com.example.lifelog.insight.AggregatedInsightRepository
import com.example.lifelog.log.structured.StructuredEventRepository
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.infrastructure.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

@Configuration
class WeeklyAggregationJobConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val structuredEventRepository: StructuredEventRepository,
    private val aggregatedInsightRepository: AggregatedInsightRepository,
) {
    @Bean
    fun weeklyAggregationJob(): Job =
        JobBuilder("weeklyAggregationJob", jobRepository)
            .start(weeklyAggregationStep())
            .build()

    @Bean
    fun weeklyAggregationStep(): Step =
        StepBuilder("weeklyAggregationStep", jobRepository)
            .tasklet { _, _ ->
                val now = LocalDate.now()
                val weekStart = now.with(DayOfWeek.MONDAY)
                val weekEnd = weekStart.plusDays(6)

                if (aggregatedInsightRepository.existsByWeekStartDate(weekStart)) {
                    return@tasklet RepeatStatus.FINISHED
                }

                val zone = ZoneId.systemDefault()

                val startInstant = weekStart.atStartOfDay(zone).toInstant()
                val endInstant =
                    weekEnd
                        .plusDays(1)
                        .atStartOfDay(zone)
                        .minusNanos(1)
                        .toInstant()

                val counts = structuredEventRepository.countByCategoryBetween(startInstant, endInstant)

                val total = counts.sumOf { it.count }

                val categoryCounts = counts.associate { it.category to it.count }

                aggregatedInsightRepository.save(
                    AggregatedInsight(
                        weekStartDate = weekStart,
                        weekEndDate = weekEnd,
                        totalEventCount = total,
                        categoryCounts = categoryCounts,
                    ),
                )

                RepeatStatus.FINISHED
            }.transactionManager(transactionManager)
            .build()
}
