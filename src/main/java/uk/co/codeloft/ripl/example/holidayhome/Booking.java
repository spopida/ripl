package uk.co.codeloft.ripl.example.holidayhome;

import lombok.Getter;
import lombok.Setter;
import uk.co.codeloft.ripl.core.ChildEntity;

import java.time.LocalDate;

@Getter @Setter
public class Booking extends ChildEntity {

    public Booking() {
        // TODO:
        super(null);
    }
    private LocalDate bookedFrom;
    private LocalDate bookedUntil;
    private String partyName;
}
