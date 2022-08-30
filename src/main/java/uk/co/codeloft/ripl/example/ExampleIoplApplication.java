package uk.co.codeloft.ripl.example;

import uk.co.codeloft.ripl.core.*;
import uk.co.codeloft.ripl.example.holidayhome.commands.CreateHolidayHomeCommand;
import uk.co.codeloft.ripl.example.holidayhome.Booking;
import uk.co.codeloft.ripl.example.holidayhome.HolidayHome;
import uk.co.codeloft.ripl.example.holidayhome.InspectionIssue;
import uk.co.codeloft.ripl.example.holidayhome.InspectionReport;
import uk.co.codeloft.ripl.example.holidayhome.commands.CreateInspectionReportCommand;

import java.time.LocalDate;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public class ExampleIoplApplication {

    public static void main(String[] args) {

        // Create a new InMemoryRepository for the HolidayHome aggregate
        // TODO: Inject this somehow

        InMemoryRepository<HolidayHome> repository = new InMemoryRepository<>();


        // Declare parent-child relationships - allow inspection reports as children of the aggregate root,
        // and allow inspection issues as children of inspection reports.

        Entity.allowRelationship(HolidayHome.class, InspectionReport.class, "is documented by");
        Entity.allowRelationship(HolidayHome.class, Booking.class, "is booked by");
        Entity.allowRelationship(InspectionReport.class, InspectionIssue.class, "contains");

        // Build a HolidayHome kernel
        HolidayHome.Kernel kernel = HolidayHome.Kernel.builder()
                .houseNumberOrName("Rosebud Cottage")
                .street("Main road")
                .postalTownOrCity("Garden City")
                .postCode("GC11 2AB")
                .numberOfBedrooms(3)
                .ownerName("Catherine Sage")
                .build();

        // Make a 'create' command
        CreateHolidayHomeCommand createCmd = new CreateHolidayHomeCommand(kernel);

        // Make a command template to update the number of bedrooms with a pre-condition and an event action
        BiPredicate<HolidayHome, Integer> noMoreThanTenBeds = (t, b) -> b <= 10;                                // Pre-condition for mutating number of beds
        BiConsumer<HolidayHome, Integer> changeNumberOfBeds = (h, b) -> h.getKernel().setNumberOfBedrooms(b);   // Function to change number of beds
        UpdateCommandTemplate<HolidayHome, Integer> setNumberOfBeds = new UpdateCommandTemplate<>(noMoreThanTenBeds, changeNumberOfBeds);

        // TODO: DEEP COPY !!
        // Create a HolidayHome and do stuff with it
        HolidayHome h1 = doCommand(createCmd, repository);
        print(h1);

        HolidayHome h2 = doCommand(setNumberOfBeds.using(h1, 6), repository);
        print(h2);

        HolidayHome h3 = doCommand(setNumberOfBeds.using(h2, 8), repository);
        print(h3);

        HolidayHome h4 = doCommand(setNumberOfBeds.using(h3, 11), repository);
        print(h4);

        // Here's an example where an UpdateCommandTemplate instance has been defined as a static variable in
        // the aggregate root class - this offers a nicer level of encapsulation
        HolidayHome h5 = doCommand(HolidayHome.SET_OWNER.using(h1,"Catherine Thyme"), repository);
        print(h5);

        // Now create some children
        InspectionReport.Kernel firstReport = InspectionReport.Kernel.builder()
                .grade(InspectionReport.InspectionGrade.EXCELLENT)
                .inspectorName("Ivor Beadyeye")
                .reportDate(LocalDate.now())
                .build();

        // Create a report - here we are targeting h5 as the parent and the ultimate root here
        CreateInspectionReportCommand createReport1 = new CreateInspectionReportCommand(h5,"is documented by", firstReport);
        HolidayHome h6 = doCommand(createReport1, repository);

        InspectionReport.Kernel secondReport = InspectionReport.Kernel.builder()
                .grade(InspectionReport.InspectionGrade.PERFECT)
                .inspectorName("Belinda Overlook")
                .reportDate(LocalDate.now().plusDays(60))
                .build();

        // Create a report - here we are targeting h5 as the parent and the ultimate root here
        CreateInspectionReportCommand createReport2 = new CreateInspectionReportCommand(h5,"is documented by", secondReport);
        HolidayHome h7 = doCommand(createReport2, repository);

        print(h7);

        // Enhance toString() functionality so that entire aggregates can be printed
        //
        // CreateChildCommand<HolidayHome> createIssue = new CreateChildCommand<>(report, "contains", issueKernel)
        // InspectionIssue issue = repository.apply(createIssue);
        //
        // BiPredicate<InspectionReport, StatusEnum> statusNotClosed = (target, intent) -> target.getStatus() != Status.CLOSED;
        // BiConsumer<InspectionReport, StatusEnum> changeStatus = (result, intent) -> report.setStatus(intent);
        //
        // ChildUpdateCommandTemplate<HolidayHome> updateReportStatus = new ChildUpdateCommandTemplate<>(
        //      statusNotClosed,
        //      changeStatus);
        //
        // h = repository.apply(
        //      updateReportStatus.using(
        //          report,
        //          Status.PENDING_REVIEW));
        //
        // Not totally comfortable with the Repository acting as a command executor
        //
        // Then - really need to
        // - Stop using raw types
        // - Write unit tests

        System.exit(0);
    }

    // TODO: This is too fancy for a simple example
    private static HolidayHome doCommand(Command<HolidayHome> cmd, InMemoryRepository repository) {
        HolidayHome result = null;

        // Initialise the result to the current command target (if there is one)
        if (UpdateCommand.class.isAssignableFrom(cmd.getClass()))
            result = (HolidayHome)((UpdateCommand)cmd).getTarget();

        try {
            result = (HolidayHome) repository.apply(cmd);
        } catch (Command.PreConditionException ex) {
            //TODO: use a logger to shut lint up
            System.out.println(String.format("Command ignored because: %s%n", ex.getMessage()));
        }
        return result;
    }

    private static void print(HolidayHome h) {
        if (h != null) System.out.print(h.toString());
    }
}