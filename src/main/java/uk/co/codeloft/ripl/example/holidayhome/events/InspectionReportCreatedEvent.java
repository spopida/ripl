package uk.co.codeloft.ripl.example.holidayhome.events;

import uk.co.codeloft.ripl.core.AggregateRoot;
import uk.co.codeloft.ripl.core.ChildCreatedEvent;
import uk.co.codeloft.ripl.core.ChildEntity;
import uk.co.codeloft.ripl.example.holidayhome.HolidayHome;
import uk.co.codeloft.ripl.example.holidayhome.InspectionReport;
import uk.co.codeloft.ripl.example.holidayhome.commands.CreateInspectionReportCommand;

public class InspectionReportCreatedEvent extends ChildCreatedEvent<HolidayHome, HolidayHome, InspectionReport> {

    private InspectionReport.Kernel kernel;

    public InspectionReportCreatedEvent(CreateInspectionReportCommand trigger, InspectionReport.Kernel kernel) {
        super(trigger);
        this.kernel = kernel;
    }

    @Override
    public AggregateRoot apply() {
        // Get the aggregate root (not the immediate parent) targeted by the command

        // Clone it (Deep Copy)

        // Create a new InspectionReport from the kernel, with the immediate parent
        InspectionReport rpt = new InspectionReport(this, this.kernel, null);

        // Return the ultimate parent
        return null;
    }
}
