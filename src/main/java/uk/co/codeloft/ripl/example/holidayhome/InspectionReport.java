package uk.co.codeloft.ripl.example.holidayhome;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import uk.co.codeloft.ripl.core.ChildCreatedEvent;
import uk.co.codeloft.ripl.core.ChildEntity;

import java.time.LocalDate;

public class InspectionReport extends ChildEntity {

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
     */
    public InspectionReport(ChildCreatedEvent<HolidayHome, HolidayHome, InspectionReport, InspectionReport.Kernel> event, Kernel kernel) {
        super(event);
        this.kernel = kernel;
        this.expiry = LocalDate.now().plusDays(120);
    }

    private String asString() {
        return String.format("Expiry: %s%n", this.expiry == null ? "null" : this.expiry.toString());
    }

    public String toString() {
        return super.toString() + this.kernel.toString() + this.asString();
    }
}
