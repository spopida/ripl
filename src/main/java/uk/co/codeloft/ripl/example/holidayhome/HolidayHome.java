package uk.co.codeloft.ripl.example.holidayhome;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import uk.co.codeloft.ripl.core.AggregateRoot;
import uk.co.codeloft.ripl.core.CreatedEvent;
import uk.co.codeloft.ripl.core.UpdateCommandTemplate;

@Getter
public class HolidayHome extends AggregateRoot {

    //-- Static Members --//

    /**
     * A class that is used to represent the initial attributes of a HolidayHome required for first-time construction.
     * Note that there should be no complex construction logic - this class is deliberately anaemic.  Any business
     * logic associated with construction should be contained in the create command.
     */
    @Builder
    @Getter
    @Setter
    public static class Kernel {
        /**
         * Address attributes
         */
        private String houseNumberOrName;
        private String street;
        private String postalTownOrCity;
        private String postCode;

        /**
         * The owner of the holiday home
         */
        private String ownerName;

        /**
         * The number of bedrooms available for use by guests
         */
        private int numberOfBedrooms;

        /**
         * Returns a String representation of this object
         * @return a representation of the object as a String
         */
        public String toString() {
            StringBuilder result = new StringBuilder();
            result.append(String.format(
                    "Address: %s, %s, %s, %s%n",
                    this.houseNumberOrName,
                    this.street,
                    this.postalTownOrCity,
                    this.postCode));
            result.append(String.format("Owner: %s%n", this.ownerName));
            result.append(String.format("No. of bedrooms: %d%n", this.numberOfBedrooms));
            return result.toString();
        }
    }

    // A command template to set the owner (including a pre-condition and an action)
    public static final UpdateCommandTemplate<HolidayHome, String> SET_OWNER = new UpdateCommandTemplate<>(
            HolidayHome::checkOwner,
            //(target, owner) -> !owner.isBlank(),
            (result, owner) -> result.getKernel().setOwnerName(owner));

    //-- Non-static Members --//

    /**
     * The kernel of an instance, i.e. a bunch of attributes needed as a pre-requisite for construction
     */
    private final Kernel kernel;

    /**
     * Construct a HolidayHome
     * @param evt the event resulting from successful execution of the create command
     * @param kernel the kernel of the aggregate root entity (core attributes)
     */
    public HolidayHome(CreatedEvent<HolidayHome, HolidayHome.Kernel> evt, Kernel kernel) {
        super(evt);
        this.kernel = kernel;
    }

    private String asString() {
        return this.getKernel().toString();
    }

    public String toString() {
        return
                String.format("%n-.-.-.-.-.-.-.-.-.-.-.%n") +
                super.toString() +
                this.getKernel().toString() +
                this.allChildren();
    }

    //-- Pre-condition predicates --//
    public static boolean checkOwner(HolidayHome current, String newOwner) {
        return !newOwner.isBlank();
    }
}
