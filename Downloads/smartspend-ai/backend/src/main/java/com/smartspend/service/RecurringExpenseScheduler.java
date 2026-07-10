package com.smartspend.service;

import com.smartspend.entity.Expense;
import com.smartspend.entity.Notification;
import com.smartspend.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Runs once a day and fires a reminder notification for every recurring
 * expense whose next occurrence date has arrived, then advances that
 * expense's nextRecurrenceDate to the following cycle.
 */
@Component
@RequiredArgsConstructor
public class RecurringExpenseScheduler {

    private final ExpenseRepository expenseRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 6 * * *") // every day at 06:00 server time
    @Transactional
    public void sendRecurringReminders() {
        List<Expense> dueToday = expenseRepository.findByRecurringTrueAndNextRecurrenceDate(LocalDate.now());

        for (Expense expense : dueToday) {
            notificationService.create(expense.getUser(),
                    "Recurring Expense Reminder",
                    String.format("Your recurring expense \"%s\" (%s) of %.2f is due today.",
                            expense.getCategory().getName(),
                            expense.getNotes() == null ? "" : expense.getNotes(),
                            expense.getAmount()),
                    Notification.NotificationType.RECURRING_REMINDER);

            expense.setNextRecurrenceDate(advance(expense));
            expenseRepository.save(expense);
        }
    }

    private LocalDate advance(Expense expense) {
        LocalDate current = expense.getNextRecurrenceDate();
        return switch (expense.getRecurrenceFrequency()) {
            case DAILY -> current.plusDays(1);
            case WEEKLY -> current.plusWeeks(1);
            case MONTHLY -> current.plusMonths(1);
            case YEARLY -> current.plusYears(1);
        };
    }
}
