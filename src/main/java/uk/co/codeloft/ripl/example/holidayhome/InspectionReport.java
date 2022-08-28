package uk.co.codeloft.ripl.example.holidayhome;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import uk.co.codeloft.ripl.core.ChildEntity;
import uk.co.codeloft.ripl.example.holidayhome.events.InspectionReportCreatedEvent;

import java.time.LocalDate;

public class InspectionReport extends ChildEntity<HolidayHome, HolidayHome> {

    //-- Static Members --//

    public static enum InspectionGrade {
        PERFECT,
        EXCELLENT,
        INADEQUATE,
    }

    @Builder
    @Getter
    @Setter
    public static class Kernel {
        private InspectionGrade grade;
        private LocalDate reportDate;
        private String inspectorName;

        /**
         * Returns a String representation of this object
         * @return a representation of the object as a String
         */
        public String toString() {
            StringBuilder result = new StringBuilder();
            result.append(String.format(
                    "Inspection Report: %s, %s, %s%n",
                    this.grade.toString(),
                    this.reportDate.toString(),
                    this.inspectorName));
            return result.toString();
        }
    }
    //-- Non-static Members --//
    /**
     * The kernel of an instance, i.e. a bunch of attributes needed as a pre-requisite for construction
     */
    private final InspectionReport.Kernel kernel;

    /**
     * The expiry date of the report - a derived field
     */
    private final LocalDate expiry;

    /**
     * Construct an InspectionReport
     * @param event the event that reflects the construction
     * @param kernel the kernel of the report, required to complete construction
     */
    public InspectionReport(InspectionReportCreatedEvent event, Kernel kernel, LocalDate expiry) {
        super(event);
        this.kernel = kernel;
        this.expiry = expiry;
    }

}
