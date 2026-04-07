package com.project.run_to_own.service;

import com.project.run_to_own.model.User;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class StreakService {

    /**
     * Calculates a user's current and best streaks by analyzing their entire activity history.
     * This is a computationally intensive method and should only be run once during a full history sync.
     */
    public void calculateStreaksFromHistory(User user, List<Map<String, Object>> allActivities) {
        if (allActivities == null || allActivities.isEmpty()) {
            user.setCurrentStreak(0);
            return;
        }

        // Ensure activities are processed in the order they occurred
        allActivities.sort(Comparator.comparing(a -> Instant.parse((String) a.get("start_date"))));

        int currentStreak = 0;
        int bestStreak = 0;
        LocalDate lastActivityDate = null;

        for (Map<String, Object> activity : allActivities) {
            // We only care about activities with distance for streaks
            if (activity.get("distance") instanceof Number && ((Number) activity.get("distance")).doubleValue() > 0) {

                LocalDate activityDate = Instant.parse((String) activity.get("start_date")).atZone(ZoneOffset.UTC).toLocalDate();

                if (lastActivityDate != null) {
                    long daysBetween = ChronoUnit.DAYS.between(lastActivityDate, activityDate);
                    if (daysBetween == 1) {
                        currentStreak++; // Consecutive day, continue the streak
                    } else if (daysBetween > 1) {
                        currentStreak = 1; // Streak was broken, start a new one
                    }
                    // If daysBetween is 0 (multiple activities on the same day), do nothing.
                } else {
                    currentStreak = 1; // The very first activity starts a streak of 1.
                }

                if (currentStreak > bestStreak) {
                    bestStreak = currentStreak;
                }
                lastActivityDate = activityDate;
            }
        }

        // After checking all activities, we need to see if the current streak is still active.
        // A streak is only "current" if the last activity was today or yesterday.
        if (lastActivityDate != null && ChronoUnit.DAYS.between(lastActivityDate, LocalDate.now(ZoneOffset.UTC)) > 1) {
            currentStreak = 0; // The streak has ended.
        }

        user.setLastActivityDate(lastActivityDate);
        user.setCurrentStreak(currentStreak);
        user.setBestStreak(bestStreak);
    }
}