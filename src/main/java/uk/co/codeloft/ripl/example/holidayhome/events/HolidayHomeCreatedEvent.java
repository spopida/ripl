package uk.co.codeloft.ripl.example.holidayhome.events;

import lombok.Getter;
import uk.co.codeloft.ripl.core.CreatedEvent;
import uk.co.codeloft.ripl.example.holidayhome.HolidayHome;
import uk.co.codeloft.ripl.example.holidayhome.commands.CreateHolidayHomeCommand;

@Getter
public class HolidayHomeCreatedEvent extends CreatedEvent<HolidayHome> {

    private HolidayHome.Kernel kernel;

    public HolidayHomeCreatedEvent(CreateHolidayHomeCommand trigger, HolidayHome.Kernel kernel) {
        super(trigger);
        this.kernel = kernel;
    }

    @Override
    public HolidayHome apply(HolidayHome h) {
        return h;
        //return new HolidayHome(this, this.kernel);
    }
}
