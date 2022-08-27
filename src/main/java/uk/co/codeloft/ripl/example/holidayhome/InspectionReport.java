package uk.co.codeloft.ripl.example.holidayhome;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import uk.co.codeloft.ripl.core.ChildEntity;
import uk.co.codeloft.ripl.example.holidayhome.events.InspectionReportCreatedEvent;

import java.time.LocalDate;

public class InspectionReport extends ChildEntity<HolidayHome> {

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
                    "Address: %s, %s, %s%n",
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

    public InspectionReport(InspectionReportCreatedEvent evt, Kernel kernel) {
        super(evt);
        this.kernel = kernel;
    }

}
