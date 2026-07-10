package com.smartspend.service;

import com.smartspend.dto.goal.GoalContributionRequest;
import com.smartspend.dto.goal.GoalRequest;
import com.smartspend.dto.goal.GoalResponse;
import com.smartspend.entity.Notification;
import com.smartspend.entity.SavingsGoal;
import com.smartspend.entity.User;
import com.smartspend.exception.BadRequestException;
import com.smartspend.exception.ResourceNotFoundException;
import com.smartspend.repository.SavingsGoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final SavingsGoalRepository savingsGoalRepository;
    private final NotificationService notificationService;

    @Transactional
    public GoalResponse create(User user, GoalRequest request) {
        SavingsGoal goal = SavingsGoal.builder()
                .user(user)
                .title(request.getTitle())
                .targetAmount(request.getTargetAmount())
                .savedAmount(BigDecimal.ZERO)
                .targetDate(request.getTargetDate())
                .status(SavingsGoal.GoalStatus.IN_PROGRESS)
                .build();

        return toResponse(savingsGoalRepository.save(goal));
    }

    @Transactional
    public GoalResponse update(User user, Long goalId, GoalRequest request) {
        SavingsGoal goal = findOwned(user, goalId);
        goal.setTitle(request.getTitle());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setTargetDate(request.getTargetDate());

        return toResponse(savingsGoalRepository.save(goal));
    }

    @Transactional
    public GoalResponse contribute(User user, Long goalId, GoalContributionRequest request) {
        SavingsGoal goal = findOwned(user, goalId);

        if (goal.getStatus() != SavingsGoal.GoalStatus.IN_PROGRESS) {
            throw new BadRequestException("Cannot contribute to a goal that is not in progress");
        }

        goal.setSavedAmount(goal.getSavedAmount().add(request.getAmount()));

        if (goal.getSavedAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(SavingsGoal.GoalStatus.ACHIEVED);
            notificationService.create(user, "Goal Achieved!",
                    "Congratulations! You've reached your \"" + goal.getTitle() + "\" savings goal.",
                    Notification.NotificationType.GOAL_ACHIEVED);
        }

        return toResponse(savingsGoalRepository.save(goal));
    }

    @Transactional
    public void delete(User user, Long goalId) {
        savingsGoalRepository.delete(findOwned(user, goalId));
    }

    @Transactional
    public GoalResponse abandon(User user, Long goalId) {
        SavingsGoal goal = findOwned(user, goalId);
        goal.setStatus(SavingsGoal.GoalStatus.ABANDONED);
        return toResponse(savingsGoalRepository.save(goal));
    }

    public List<GoalResponse> listForUser(User user) {
        return savingsGoalRepository.findByUser(user).stream().map(this::toResponse).toList();
    }

    private GoalResponse toResponse(SavingsGoal goal) {
        BigDecimal progress = goal.getTargetAmount().compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : goal.getSavedAmount().multiply(BigDecimal.valueOf(100))
                        .divide(goal.getTargetAmount(), 2, RoundingMode.HALF_UP);

        LocalDate estimatedCompletion = switch (goal.getStatus()) {
            case ACHIEVED -> LocalDate.now();
            case IN_PROGRESS -> goal.getTargetDate();
            case ABANDONED -> null;
        };

        return GoalResponse.builder()
                .id(goal.getId())
                .title(goal.getTitle())
                .targetAmount(goal.getTargetAmount())
                .savedAmount(goal.getSavedAmount())
                .progressPercentage(progress)
                .targetDate(goal.getTargetDate())
                .estimatedCompletionDate(estimatedCompletion)
                .status(goal.getStatus())
                .build();
    }

    private SavingsGoal findOwned(User user, Long goalId) {
        SavingsGoal goal = savingsGoalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Savings goal not found"));
        if (!goal.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Savings goal not found");
        }
        return goal;
    }
}
