package uk.co.codeloft.ripl.example.holidayhome;

import uk.co.codeloft.ripl.core.ChildEntity;

import java.time.LocalDate;

public class InspectionReport extends ChildEntity<HolidayHome> {

    public static enum InspectionGrade {
        PERFECT,
        EXCELLENT,
        INADEQUATE,
    }

    private InspectionGrade grade;
    private LocalDate reportDate;
    private String inspectorName;
}
