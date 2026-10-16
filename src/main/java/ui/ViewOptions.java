package ui;

/**
 * Holds the booking name and chronological sorting choice for viewbooking.
 */
public class ViewOptions {
    private final String name;
    private final boolean sortByTime;

    /**
     * Creates view options from the parsed booking name and sort choice.
     *
     * @param name booking name to display
     * @param sortByTime whether to display matching bookings chronologically
     */
    public ViewOptions(String name, boolean sortByTime) {
        this.name = name;
        this.sortByTime = sortByTime;
    }

    public String getName() {
        return name;
    }

    public boolean isSortByTime() {
        return sortByTime;
    }
}
