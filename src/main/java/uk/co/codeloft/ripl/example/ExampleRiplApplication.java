package uk.co.codeloft.ripl.example;

import uk.co.codeloft.ripl.core.*;
import uk.co.codeloft.ripl.example.holidayhome.*;

import java.time.LocalDate;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
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

        HolidayHomeFactory factory = new HolidayHomeFactory(repo);

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

        HolidayHome rosebudCottage = null;
        try {
            rosebudCottage = factory.create(kernel);
            print(rosebudCottage);

            // Let's see if we can get it from the repo
            rosebudCottage = repo.getLatest(rosebudCottage.getId());
            print(rosebudCottage);

            rosebudCottage = factory.setNumberOfBeds(rosebudCottage.getId(), 6);
            print(rosebudCottage);

            rosebudCottage = factory.setOwner(rosebudCottage.getId(), "Catherine Thyme");
            print(rosebudCottage);

            // Now create some children
            InspectionReport.Kernel firstReport = InspectionReport.Kernel.builder()
                    .grade(InspectionReport.InspectionGrade.EXCELLENT)
                    .inspectorName("Ivor Beadyeye")
                    .reportDate(LocalDate.now().minusDays(2L))
                    .build();

            InspectionReport.Kernel secondReport = InspectionReport.Kernel.builder()
                    .grade(InspectionReport.InspectionGrade.INADEQUATE)
                    .inspectorName("Ann Onimus")
                    .reportDate(LocalDate.now().minusDays(1L))
                    .build();


            rosebudCottage = factory.createInspectionReport(rosebudCottage.getId(), firstReport, "is documented by");
            print(rosebudCottage);

            rosebudCottage = factory.createInspectionReport(rosebudCottage.getId(), secondReport, "is documented by");
            print(rosebudCottage);


            // Now let's get the report we just added
            Predicate<InspectionReport> findPredicate = rpt -> rpt.getKernel().getInspectorName().equals("Ivor Beadyeye");
            List<InspectionReport> matchingReports = rosebudCottage.findChildren("is documented by", findPredicate);

            for (InspectionReport rpt : matchingReports) {
                rosebudCottage = factory.changeInspectorName(rosebudCottage.getId(), rpt.getId(), "Ivor Massive Beadyeye");
                rosebudCottage = factory.createInspectionIssue(rosebudCottage.getId(), rpt.getId(), "Dripping kitchen tap");
                print(rosebudCottage);
            }

            // This should throw an exception
            rosebudCottage = factory.setNumberOfBeds(rosebudCottage.getId(), 11);
            print(rosebudCottage);
        } catch (Exception e) {
            System.out.printf(e.getMessage());
            System.exit(1);
        }

        // GET RID OF ALL TODOs
        // SOLVE DEEP COPY QUESTION
        //
        // WHat if all updates go through the root?
        //
        // Light bulb ?  Each relationship must be unique - it can't just be a string
        // Could we have an enumeration?
        //
        // HOLIDAY_HOME_HAS_INSPECTION_REPORT
        // INSPECTION_REPORT_HAS_INSPECTION_ISSUE
        //
        //
        // First, not much changes - we need to move the root, the relationship
        // We should contemplate doing updates purely by id (the string uuid).
        System.exit(0);
    }

    private static void print(HolidayHome h) { if (h != null) System.out.println(h.toString()); }
}