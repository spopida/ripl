package uk.co.codeloft.ripl.example.holidayhome;

import uk.co.codeloft.ripl.core.*;

import java.time.LocalDate;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public class HolidayHomeFactory extends AggregateRootFactory<HolidayHome> {

    //--- COMMAND TEMPLATES --------------------------------------------------------------------------------------------

    // TODO: Convert these comments to javadoc-style

    // The template for creating a HolidayHome has no pre-conditions that apply to the kernel (k), and
    // requires the HolidayHome constructor
    private final CreateCommandTemplate<HolidayHome, HolidayHome.Kernel> createHolidayHome =
            new CreateCommandTemplate<>(this, k -> true, HolidayHome::new);

    // Make a command template to update the number of bedrooms with a pre-condition and an event action
    private final UpdateCommandTemplate<HolidayHome, Integer> setNumberOfBeds = new UpdateCommandTemplate<>(
            this,
            (target, beds) -> beds <= 10,
            (home, beds) -> home.getKernel().setNumberOfBedrooms(beds));

    // A command template to set the owner (including a pre-condition and an action)
    public final UpdateCommandTemplate<HolidayHome, String> setOwner = new UpdateCommandTemplate<>(
            this,
            (target, owner) -> !owner.isBlank(),
            (result, owner) -> result.getKernel().setOwnerName(owner));

    // Now we create a child command template.  This is parameterized with 4 types:
    // - The type of the aggregate root
    // - The type of the immediate parent
    // - The type of child that is being created
    // - The type of the kernel for the child
    // And the constructor for the template takes a predicate on the kernel, and a constructor
    public final CreateChildCommandTemplate<HolidayHome, HolidayHome, InspectionReport, InspectionReport.Kernel> createRpt =
            new CreateChildCommandTemplate<>(
                    this,
                    k -> k.getReportDate().isBefore(LocalDate.now()),
                    InspectionReport::new);

    public final UpdateChildCommandTemplate<HolidayHome, InspectionReport, String> changeInspectorName =
            new UpdateChildCommandTemplate<>(
                    this,
                    "is documented by",
                    (target, name) -> !name.isBlank(),
                    (report, name) -> report.getKernel().setInspectorName(name));


    public final CreateChildCommandTemplate<HolidayHome, InspectionReport, InspectionIssue, String> createIssue =
            new CreateChildCommandTemplate<>(
                    this,
                    d -> true,
                    InspectionIssue::new);

    //--- METHODS --------------------------------------------------------------------------------------------

    public HolidayHomeFactory(AggregateRootRepository<HolidayHome> repo) {
        super(repo);
    }

    public HolidayHome create(HolidayHome.Kernel kernel) throws Command.PreConditionException {
        return super.perform(createHolidayHome.using(kernel));
    }

    public HolidayHome setNumberOfBeds(String targetId, int noOfBeds) throws Command.PreConditionException, InvalidCommandTargetException {
        return super.perform(setNumberOfBeds.using(this.getLatest(targetId), noOfBeds));
    }

    public HolidayHome setOwner(String targetId, String newOwner) throws Command.PreConditionException, InvalidCommandTargetException {
        return super.perform(setOwner.using(this.getLatest(targetId), newOwner));
    }

    public HolidayHome createInspectionReport(String rootId, InspectionReport.Kernel kernel, String role)
            throws Command.PreConditionException, InvalidCommandTargetException {
        HolidayHome root = this.getLatest(rootId);

        return super.perform(createRpt.using(root, root, kernel, role));
    }

    public HolidayHome createInspectionIssue(String rootId, String parentReportId, String issue) throws
            Command.PreConditionException,
            InvalidCommandTargetException,
            InvalidObjectTypeException,
            InvalidEntityIdException {
        HolidayHome root = this.getLatest(rootId);
        InspectionReport parent = (InspectionReport) root.getDescendent(parentReportId, InspectionReport.class)
                .orElseThrow(() -> new InvalidEntityIdException(parentReportId));
        return super.perform(createIssue.using(root, parent, issue, "contains" ));
    }

    public HolidayHome changeInspectorName(String rootId, String reportId, String newName) throws
            Command.PreConditionException,
            InvalidCommandTargetException,
            InvalidEntityIdException,
            InvalidObjectTypeException {
        HolidayHome root = this.getLatest(rootId);
        InspectionReport parent = (InspectionReport) root.getDescendent(reportId, InspectionReport.class)
                .orElseThrow(() -> new InvalidEntityIdException(reportId));

        return super.perform(changeInspectorName.using(this.getLatest(rootId), parent, newName));
    }
}
