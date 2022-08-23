package uk.co.codeloft.ripl.example.holidayhome.commands;

import lombok.Getter;
import lombok.Setter;
import uk.co.codeloft.ripl.core.CreateCommand;
import uk.co.codeloft.ripl.example.holidayhome.HolidayHome;
import uk.co.codeloft.ripl.example.holidayhome.events.HolidayHomeCreatedEvent;

/**
 * A command to instruct the creation of a new HolidayHome account.  The creation command needs to contain all
 * mandatory attributes, without which the aggregate cannot be created.  In this example class we keep things
 * simple; there are no pre-conditions for example.
 */
@Getter @Setter
public class CreateHolidayHomeCommand extends CreateCommand<HolidayHome> {

    private HolidayHome.Kernel kernel;

    public CreateHolidayHomeCommand(HolidayHome.Kernel kernel) {
        super();
        this.kernel = kernel;
    }

    @Override
    public HolidayHomeCreatedEvent getEvent() {
        return new HolidayHomeCreatedEvent(this, this.kernel);
    }
}
