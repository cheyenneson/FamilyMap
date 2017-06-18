package hawkes.fmc.model;

import java.util.ArrayList;

import hawkes.model.Event;

/**
 * Created by yo on 6/16/17.
 */

public class Filter {

    private String filterType;
    private boolean isOn;


    public Filter(String filterType) {
        this.filterType = filterType;
        isOn = true; // by default all filters are turned on (its event type will show on map)
    }

    public ArrayList<String> determineEventTypes() {
        Model model = Model.getModel();
        ArrayList<Event> events = model.getEvents();

        ArrayList<String> eventTypes = new ArrayList<>();

        for (Event event : events) {
            if (!eventTypes.contains(event.getEventType())) {
                eventTypes.add(event.getEventType());
            }
        }

//        for (String s : eventTypes) {
//            System.out.println(s + "\n\n\n\n\n");
//        }

        return eventTypes;

    }

    public String getFilterType() {
        return filterType;
    }

    public void setFilterType(String filterType) {
        this.filterType = filterType;
    }

    public boolean isOn() {
        return isOn;
    }

    public void setOn(boolean on) {
        isOn = on;
    }
}