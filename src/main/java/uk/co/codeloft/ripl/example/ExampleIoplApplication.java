package uk.co.codeloft.ripl.example;

import uk.co.codeloft.ripl.core.Command;
import uk.co.codeloft.ripl.core.Entity;
import uk.co.codeloft.ripl.core.InMemoryRepository;
import uk.co.codeloft.ripl.core.IntUpdateCommandTemplate;
import uk.co.codeloft.ripl.example.holidayhome.commands.CreateHolidayHomeCommand;
import uk.co.codeloft.ripl.example.holidayhome.Booking;
import uk.co.codeloft.ripl.example.holidayhome.HolidayHome;
import uk.co.codeloft.ripl.example.holidayhome.InspectionIssue;
import uk.co.codeloft.ripl.example.holidayhome.InspectionReport;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public class ExampleIoplApplication {

    public static void main(String[] args) {

        // Create a new InMemoryRepository for the HolidayHome aggregate
        // TODO: Inject this somehow

        InMemoryRepository<HolidayHome> repository = new InMemoryRepository<>();

        // Declare parent-child relationships - allow inspection reports as children of the aggregate root,
        // and allow inspection issues as children of inspection reports.

        Entity.allowCollection(HolidayHome.class, InspectionReport.class, "is documented by");
        Entity.allowCollection(HolidayHome.class, Booking.class, "is booked by");
        Entity.allowCollection(InspectionReport.class, InspectionIssue.class, "documents");

        // Build a HolidayHome kernel
        HolidayHome.Kernel kernel = HolidayHome.Kernel.builder()
                .houseNumberOrName("Rosebud Cottage")
                .street("Main road")
                .postalTownOrCity("Garden City")
                .postCode("GC11 2AB")
                .numberOfBedrooms(3)
                .ownerName("Catherine Sage")
                .build();

        // Make a 'create' command and tell the repository to execute the command, receiving an event
        // by way of response.  The create command might have some pre-conditions based on the attributes
        // that are mandatory for a holiday home
        CreateHolidayHomeCommand createCmd = new CreateHolidayHomeCommand(kernel);

        // Declare pre-conditions for mutating the number of bedrooms - in this case the number of beds must be <= 10
        BiPredicate<HolidayHome, Integer> noMoreThanTenBeds = (t, b) -> b <= 10;

        // Declare functionality that can be used to change the number of bedrooms when pre-conditions are met
        BiConsumer<HolidayHome, Integer> changeNumberOfBeds = (h, b) -> h.getKernel().setNumberOfBedrooms(b);

        // Create a command template that can be used multiple times with different parameters
        IntUpdateCommandTemplate<HolidayHome> updateNumberOfBeds = new IntUpdateCommandTemplate<>(noMoreThanTenBeds, changeNumberOfBeds);

        // Create a HolidayHome and do stuff with it
        try {
            HolidayHome h = (HolidayHome) repository.apply(createCmd);
            System.out.print(h.toString());

            h = (HolidayHome) repository.apply(updateNumberOfBeds.using(h, 6));
            System.out.print(h.toString());

            h = (HolidayHome) repository.apply(updateNumberOfBeds.using(h, 8));
            System.out.print(h.toString());

            h = (HolidayHome) repository.apply(updateNumberOfBeds.using(h, 11));
            System.out.print(h.toString());

            // Create an 'update' command that adds an inspection report to the aggregate root
            //
            // AddChildCommand addChildCmd = new AddChildCommand(h.getId(), inspectionReportKernel);
            // h.attemptCommand(new AddChildCommand(h.getId(), "is documented by", inspectionReportKernel));
            //
            // Not totally comfortable with the Repository acting as a command executor
        } catch (Command.PreConditionException ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
        }

        System.exit(0);
    }
}
