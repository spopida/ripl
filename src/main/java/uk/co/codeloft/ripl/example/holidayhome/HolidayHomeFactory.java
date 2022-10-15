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
            new CreateCommandTemplate<>(k -> true, HolidayHome::new);

    // Make a command template to update the number of bedrooms with a pre-condition and an event action
    private final UpdateCommandTemplate<HolidayHome, Integer> setNumberOfBeds = new UpdateCommandTemplate<>(
            (target, beds) -> beds <= 10,
            (home, beds) -> home.getKernel().setNumberOfBedrooms(beds));

    // A command template to set the owner (including a pre-condition and an action)
    public final UpdateCommandTemplate<HolidayHome, String> setOwner = new UpdateCommandTemplate<>(
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
                    k -> k.getReportDate().isBefore(LocalDate.now()),
                    InspectionReport::new);

    public final UpdateChildCommandTemplate<HolidayHome, InspectionReport, String> changeInspectorName =
            new UpdateChildCommandTemplate<>( "is documented by",
                    (target, name) -> !name.isBlank(),
                    (report, name) -> report.getKernel().setInspectorName(name));

    // THE SOLUTION TO THE ABOVE PROBLEM:
    // 1. The template should never have required the root and parent to be passed ... these should have been part of the call to the using method
    // 2. But having the pass the root, parent, and target entities is overkill anyway.  The problem is that this doesn't scale to situations
    //    Where there are multiple levels of relationship.  I think we should consider just passing the root and the target - no intermediate parents
    // 3. Also, if we are switching to not passing target entities - using string ids instead - then we need some kind of id (a string one), so that
    //    have a way of referring to targets.

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

    // A Re-think:
    //
    // The controlling application receives piecemeal updates externally sourced (probably in JSON format).
    // It needs to convert these into objects, but not entities - these are incoming requests THAT MAY FAIL.  So, we
    // use String Ids to target updates
    //
    // We can require that for updates to children, the
    // aggregate root id is always given, along with the child id.  If the child id is omitted, then we assume the update is to the
    // root.
    //
    // For a creation instruction, the client must pass:
    // - the id of the root
    // - the id of the immediate parent (nesting doesn't matter)
    // - the kernel of the child
    // - the relationship to the parent
    //
    // For an update instruction, the client must pass:
    // - the id of the root
    // - the id of affected child (nesting doesn't matter)
    // - the update object
    //
    // To support all of this, whenever a child (however deeply nested) is added to a parent, a flatmap contained
    // in the root is needed.  This means that wherever the child lives in the entity hierarchy, a reference to
    // it can be obtained from the root object in combination with its id.
    //
    // Impact:
    // - Hold a flatmap of children keyed by childId in the Aggregate Root instance
    // - When Entity.addChild() is called, the root's flatmap is updated
    // - When handling an update command, we get the latest version
}
