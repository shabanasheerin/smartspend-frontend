package com.smartspend.service;

import com.smartspend.dto.goal.GoalContributionRequest;
import com.smartspend.dto.goal.GoalRequest;
import com.smartspend.entity.SavingsGoal;
import com.smartspend.entity.User;
import com.smartspend.exception.BadRequestException;
import com.smartspend.exception.ResourceNotFoundException;
import com.smartspend.repository.SavingsGoalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoalServiceTest {

    @Mock private SavingsGoalRepository savingsGoalRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private GoalService goalService;

    private User buildUser() {
        User u = new User();
        u.setId(1L);
        return u;
    }

    private SavingsGoal buildGoal(User user, BigDecimal target, BigDecimal saved) {
        SavingsGoal goal = SavingsGoal.builder()
                .user(user)
                .title("Buy Laptop")
                .targetAmount(target)
                .savedAmount(saved)
                .status(SavingsGoal.GoalStatus.IN_PROGRESS)
                .build();
        goal.setId(1L);
        return goal;
    }

    @Test
    void create_returnsGoalWithZeroProgress() {
        User user = buildUser();
        GoalRequest request = new GoalRequest("Buy Laptop", new BigDecimal("1000"), null);

        when(savingsGoalRepository.save(any(SavingsGoal.class))).thenAnswer(inv -> {
            SavingsGoal g = inv.getArgument(0);
            g.setId(1L);
            return g;
        });

        var response = goalService.create(user, request);

        assertEquals("Buy Laptop", response.getTitle());
        assertEquals(0, BigDecimal.ZERO.compareTo(response.getProgressPercentage()));
        assertEquals(SavingsGoal.GoalStatus.IN_PROGRESS, response.getStatus());
    }

    @Test
    void contribute_marksGoalAchieved_whenTargetReached() {
        User user = buildUser();
        SavingsGoal goal = buildGoal(user, new BigDecimal("1000"), new BigDecimal("900"));

        when(savingsGoalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(savingsGoalRepository.save(any(SavingsGoal.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = goalService.contribute(user, 1L, new GoalContributionRequest(new BigDecimal("100")));

        assertEquals(SavingsGoal.GoalStatus.ACHIEVED, response.getStatus());
    }

    @Test
    void contribute_throwsBadRequestException_whenGoalAlreadyAchieved() {
        User user = buildUser();
        SavingsGoal goal = buildGoal(user, new BigDecimal("1000"), new BigDecimal("1000"));
        goal.setStatus(SavingsGoal.GoalStatus.ACHIEVED);

        when(savingsGoalRepository.findById(1L)).thenReturn(Optional.of(goal));

        assertThrows(BadRequestException.class,
                () -> goalService.contribute(user, 1L, new GoalContributionRequest(new BigDecimal("50"))));
    }

    @Test
    void contribute_throwsResourceNotFoundException_whenGoalBelongsToAnotherUser() {
        User owner = buildUser();
        User otherUser = new User();
        otherUser.setId(2L);

        SavingsGoal goal = buildGoal(owner, new BigDecimal("1000"), BigDecimal.ZERO);

        when(savingsGoalRepository.findById(1L)).thenReturn(Optional.of(goal));

        assertThrows(ResourceNotFoundException.class,
                () -> goalService.contribute(otherUser, 1L, new GoalContributionRequest(new BigDecimal("50"))));
    }
}
