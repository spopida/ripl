package uk.co.codeloft.ripl.example.holidayhome.commands;

import uk.co.codeloft.ripl.core.ChildEntity;
import uk.co.codeloft.ripl.core.CreateChildCommand;
import uk.co.codeloft.ripl.core.Event;
import uk.co.codeloft.ripl.example.holidayhome.HolidayHome;
import uk.co.codeloft.ripl.example.holidayhome.InspectionReport;
import uk.co.codeloft.ripl.example.holidayhome.events.InspectionReportCreatedEvent;

public class CreateInspectionReportCommand extends CreateChildCommand<HolidayHome, HolidayHome, InspectionReport> {

    private final InspectionReport.Kernel kernel;

    public CreateInspectionReportCommand(HolidayHome parent, String role, InspectionReport.Kernel kernel) {
        this(parent, parent, role, kernel);
    }

    public CreateInspectionReportCommand(HolidayHome root, HolidayHome parent, String role, InspectionReport.Kernel kernel) {
        super(root, parent, role, InspectionReport.class);
        this.kernel = kernel;
    }


    @Override
    public InspectionReportCreatedEvent getEvent() {
        return new InspectionReportCreatedEvent(this, this.kernel, this.getRoot(), this.getRole(), this.getParent());
    }
}
