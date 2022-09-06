package uk.co.codeloft.ripl.example;

import uk.co.codeloft.ripl.core.*;
import uk.co.codeloft.ripl.example.holidayhome.Booking;
import uk.co.codeloft.ripl.example.holidayhome.HolidayHome;
import uk.co.codeloft.ripl.example.holidayhome.InspectionIssue;
import uk.co.codeloft.ripl.example.holidayhome.InspectionReport;
import uk.co.codeloft.ripl.example.holidayhome.commands.CreateInspectionReportCommand;

import java.time.LocalDate;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

public class ExampleRiplApplication {

    private static final Logger LOGGER;

    static {
        LOGGER = Logger.getLogger(ExampleRiplApplication.class.getName());
        LOGGER.addHandler(new ConsoleHandler());
    }
    public static void main(String[] args) {

        AggregateRootRepository<HolidayHome> repo = new InMemoryAggregateRootRepository<>();
        AggregateRootManager<HolidayHome, HolidayHome.Kernel> manager = new AggregateRootManager<>(repo);

        // Declare parent-child relationships - allow inspection reports as children of the aggregae root,
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


        // The template for creating a HolidayHome has no pre-conditions that apply to the kernel (k), and
        // requires the HolidayHome constructor
        CreateCommandTemplate<HolidayHome, HolidayHome.Kernel> createHolidayHome =
                new CreateCommandTemplate<>(k -> true, HolidayHome::new);

        // We get an actual command from the template by using the kernel we created above
        CreateCommand<HolidayHome, HolidayHome.Kernel> createRosebudCottage = createHolidayHome.using(kernel);


        HolidayHome rosebudCottage = null;
        try {
            rosebudCottage = manager.perform(createRosebudCottage);
            print(rosebudCottage);

            // Let's see if we can get it from the repo
            rosebudCottage = repo.getLatest(rosebudCottage.getId());
            print(rosebudCottage);

            // Make a command template to update the number of bedrooms with a pre-condition and an event action
            BiPredicate<HolidayHome, Integer> noMoreThanTenBeds = (t, b) -> b <= 10;                                // Pre-condition for mutating number of beds
            BiConsumer<HolidayHome, Integer> changeNumberOfBeds = (h, b) -> h.getKernel().setNumberOfBedrooms(b);   // Function to change number of beds
            UpdateCommandTemplate<HolidayHome, Integer> setNumberOfBeds = new UpdateCommandTemplate<>(noMoreThanTenBeds, changeNumberOfBeds);

            rosebudCottage = manager.perform(setNumberOfBeds.using(rosebudCottage, 6));
            print(rosebudCottage);

            // TODO: Prove that we get an exception if we violate the pre-condition


            // Here's an example where an UpdateCommandTemplate instance has been defined as a static variable in
            // the aggregate root class - this offers a nicer level of encapsulation or at least cohesion
            rosebudCottage = manager.perform(HolidayHome.SET_OWNER.using(rosebudCottage,"Catherine Thyme"));
            print(rosebudCottage);

            CreateChildCommandTemplate<HolidayHome, HolidayHome, InspectionReport, InspectionReport.Kernel> createRpt =
                    new CreateChildCommandTemplate<>(
                            k -> k.getReportDate().isBefore(LocalDate.now()),
                            InspectionReport::new);

            // Now create some children
            InspectionReport.Kernel firstReport = InspectionReport.Kernel.builder()
                    .grade(InspectionReport.InspectionGrade.EXCELLENT)
                    .inspectorName("Ivor Beadyeye")
                    .reportDate(LocalDate.now())
                    .build();

            rosebudCottage = manager.perform(createRpt.using(rosebudCottage, rosebudCottage, firstReport, "is documented by"));
            print(rosebudCottage);

        } catch (Command.PreConditionException e) {
            System.out.printf(e.getMessage());
        }


        // CHANGE EVENT / REPO FUNCTIONALITY - EVENT APPLY() SHOULD TAKE TARGET PARAM FUNCTIONALITY;
        // GET RID OF ALL TODOs
        // SOLVE DEEP COPY QUESTION
        // ELIMINATE THE NEED FOR BESPOKE CREATE COMMANDS (use template pattern as for updates)
        // SUPPORT CHILD UPDATES
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

    private static void print(HolidayHome h) { if (h != null) System.out.println(h.toString()); }
}