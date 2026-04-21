package com.attendance.util;

import com.attendance.entity.Shift;
import java.time.LocalTime;

public class ShiftUtils {

    /**
     * Kiểm tra xem hai ca làm việc có bị chồng lấn thời gian hay không.
     * Hỗ trợ cả trường hợp ca làm việc xuyên đêm (ví dụ: 22h - 6h sáng mai).
     */
    public static boolean isOverlapping(Integer dayA, Shift shiftA, Integer dayB, Shift shiftB) {
        // Chuyển đổi thời gian của ca A sang phút tính từ đầu tuần (Thứ 2)
        long startA = getMinutesFromWeekStart(dayA, shiftA.getStartTime());
        long endA = getMinutesFromWeekStart(dayA, shiftA.getEndTime());
        if (isOvernight(shiftA)) {
            endA += 1440; // Cộng thêm 24 tiếng
        }

        // Chuyển đổi thời gian của ca B
        long startB = getMinutesFromWeekStart(dayB, shiftB.getStartTime());
        long endB = getMinutesFromWeekStart(dayB, shiftB.getEndTime());
        if (isOvernight(shiftB)) {
            endB += 1440;
        }

        long minutesInWeek = 7 * 1440; // 10080 phút
        
        // Kiểm tra chồng lấn theo 3 trường hợp của vòng tuần:
        // 1. Chồng lấn trong tuần hiện tại.
        // 2. Chồng lấn khi ca B được coi là của tuần sau (để check ca A cuối tuần này).
        // 3. Chồng lấn khi ca A được coi là của tuần sau (để check ca B cuối tuần này).
        return isOverlappingInterval(startA, endA, startB, endB) ||
               isOverlappingInterval(startA, endA, startB + minutesInWeek, endB + minutesInWeek) ||
               isOverlappingInterval(startA + minutesInWeek, endA + minutesInWeek, startB, endB);
    }

    private static boolean isOverlappingInterval(long s1, long e1, long s2, long e2) {
        // Công thức: Max(Start) < Min(End) -> Có chồng lấn
        // Không dùng <= vì hệ thống cho phép tiếp giáp (End == Start)
        return Math.max(s1, s2) < Math.min(e1, e2);
    }

    private static long getMinutesFromWeekStart(Integer dayOfWeek, LocalTime time) {
        // Quy ước: Thứ 2 (1) là ngày 0, Chủ nhật (7) là ngày 6.
        return (long)(dayOfWeek - 1) * 1440 + time.getHour() * 60 + time.getMinute();
    }

    private static boolean isOvernight(Shift shift) {
        return shift.getEndTime().isBefore(shift.getStartTime());
    }
}



