package uk.co.codeloft.ripl.example;

import uk.co.codeloft.ripl.core.*;
import uk.co.codeloft.ripl.example.holidayhome.Booking;
import uk.co.codeloft.ripl.example.holidayhome.HolidayHome;
import uk.co.codeloft.ripl.example.holidayhome.InspectionIssue;
import uk.co.codeloft.ripl.example.holidayhome.InspectionReport;

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

            // Now we create a child command template.  This is parameterized with 4 types:
            // - The type of the aggregate root
            // - The type of the parent
            // - The type of child that is being created
            // - The type of the kernel for the child
            // And the constructor for the template takes a predicate on the kernel, and a constructor
            CreateChildCommandTemplate<HolidayHome, HolidayHome, InspectionReport, InspectionReport.Kernel> createRpt =
                    new CreateChildCommandTemplate<>(
                            k -> k.getReportDate().isBefore(LocalDate.now()),
                            InspectionReport::new);

            // Now create some children
            InspectionReport.Kernel firstReport = InspectionReport.Kernel.builder()
                    .grade(InspectionReport.InspectionGrade.EXCELLENT)
                    .inspectorName("Ivor Beadyeye")
                    .reportDate(LocalDate.now().minusDays(1L))
                    .build();

            // Actual creation of the child requires a command that needs the root, parent, child kernel, and relationship
            // TODO: should the relationship be part of the template?  I think that might be better...but it's too cumbersome
            // because we don't know the classes involved in the relationship, without having the objects
            rosebudCottage = manager.perform(createRpt.using(rosebudCottage, rosebudCottage, firstReport, "is documented by"));
            print(rosebudCottage);

            // This should throw an exception
            rosebudCottage = manager.perform(setNumberOfBeds.using(rosebudCottage, 11));
            print(rosebudCottage);
        } catch (Command.PreConditionException e) {
            System.out.printf(e.getMessage());
            System.exit(1);
        }

        // Merge UpdateCommand with SimpleUpdateCommand and UpdatedEvent with SimpleUpdatedEvent
        // GET RID OF ALL TODOs
        // SOLVE DEEP COPY QUESTION
        //
        // SUPPORT CHILD UPDATES
        // - ChildUpdateCommandTemplate
        // - ChildUpdatedEvent
        // - UpdateChildCommand
        //
        // To support child updates we'll need classes that know the type of the root, parent, and child.  A CUC will
        // need to be informed of the AggregateRoot to be updated, the relationship to use, the child entity to be updated and the update to do
        //
        // We need to figure out how to refer to the updated entity in the ChildUpdatedEvent.  This would have to be by its (internal) id, so
        // that the event can be applied during re-hydration.  This begs the question of how to search for/retrieve entities. We need the ability to
        // retrieve a sub-set of entities that match a given predicate.  For example, we might want to find all inspection reports between 2 dates.
        // To do this we could provide a predicate to a findChild(parent, role, predicate) method.  This would return a List.
        //
        // Once we have an instance of a child, the CUC will check the pre-conditions, generate the event, and return a new copy of the aggregate
        // with the new event applied.  This means that the ChildUpdatedEvent needs to have a reference to the root, the parent, and the relationship (role)
        // and of course the object parameter that represents the source of the update.  It will also have some kind of a consumer function so that the
        // logic of the update can be applied.  Hopefully, this consumer function just needs the target (child) entity, and the object, so it can
        // be a BiConsumer.  The apply logic should be trivial.
        //
        // The
        //
        // CHANGE all Templates to Factories?

        System.exit(0);
    }

    private static void print(HolidayHome h) { if (h != null) System.out.println(h.toString()); }
}