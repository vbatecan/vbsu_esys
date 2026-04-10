package com.group5.paul_esys.modules.registrar.model;

import java.time.LocalTime;

public record ScheduleSlot(String day, LocalTime startTime, LocalTime endTime, String label) {
}
