package uk.co.codeloft.ripl.example.holidayhome.events;

import uk.co.codeloft.ripl.core.AggregateRoot;
import uk.co.codeloft.ripl.core.ChildCreatedEvent;
import uk.co.codeloft.ripl.core.ChildEntity;
import uk.co.codeloft.ripl.core.Entity;
import uk.co.codeloft.ripl.example.holidayhome.HolidayHome;
import uk.co.codeloft.ripl.example.holidayhome.InspectionReport;
import uk.co.codeloft.ripl.example.holidayhome.commands.CreateInspectionReportCommand;

public class InspectionReportCreatedEvent extends ChildCreatedEvent<HolidayHome, HolidayHome, InspectionReport> {

    private final InspectionReport.Kernel kernel;
    private final HolidayHome root;
    private final HolidayHome parent;
    private final String role;

    public InspectionReportCreatedEvent(
            CreateInspectionReportCommand trigger,
            InspectionReport.Kernel kernel,
            HolidayHome root,
            String role,
            HolidayHome parent) {
        super(trigger);
        this.kernel = kernel;
        this.root = root;
        this.parent = parent;
        this.role = role;
    }

    @Override
    public AggregateRoot apply() {
        // Get the aggregate root (not the immediate parent) targeted by the command

        // Clone it (Deep Copy)
        this.parent.getRoot().evolve();

        // Create a new InspectionReport from the kernel
        InspectionReport rpt = new InspectionReport(this, this.kernel, null);

        // Get the latest version of the target parent entity from the repo

        // Add the report to the new aggregate
        try {
            this.parent.addChild(this.role, rpt);
        } catch (Entity.InvalidRelationshipInstanceException e) {

        }
        // Return the ultimate parent
        return parent.getRoot();
    }
}
