package uk.co.codeloft.ripl.example.holidayhome;

import lombok.Getter;
import lombok.Setter;
import uk.co.codeloft.ripl.core.ChildCreatedEvent;
import uk.co.codeloft.ripl.core.ChildEntity;

/**
 * A trivial class used to demonstrate a child of a child
 */
@Getter
@Setter
public class InspectionIssue extends ChildEntity {

    private String issueDescription;

    public InspectionIssue(ChildCreatedEvent<HolidayHome, InspectionReport, InspectionIssue, String> event, String description) {
        super(event);
        this.issueDescription = description;
    }

    private String asString() {
        return String.format("Issue: %s%n", this.issueDescription == null ? "null" : this.issueDescription);
    }

    public String toString() {
        return super.toString() + this.asString();
    }

}
