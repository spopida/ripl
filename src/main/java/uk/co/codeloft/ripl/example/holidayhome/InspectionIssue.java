package uk.co.codeloft.ripl.example.holidayhome;

import lombok.Getter;
import lombok.Setter;
import uk.co.codeloft.ripl.core.ChildEntity;

/**
 * A trivial class used to demonstrate a child of a child
 */
@Getter
@Setter
public class InspectionIssue extends ChildEntity {

    public InspectionIssue() {
        //TODO
        super(null);
    }
    private String issueDescription;
}
