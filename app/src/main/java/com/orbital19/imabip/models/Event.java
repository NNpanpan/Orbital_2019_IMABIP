package com.orbital19.imabip.models;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.orbital19.imabip.teams.models.Team;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Event implements Serializable, Comparable<Event> {

    public static String availableEventCollection = "AvailableEvents";
    public static String contactKey = "Contact";
    public static String descriptionKey = "Description";
    public static String hostIDKey = "Host"; // can be idividual or team
    public static String nameKey = "Name";
    public static String typeKey = "Type";
    public static String venueKey = "Venue";
    public static String evTimeKey = "Time";
    public static String partySizeKey = "PartySize";
    public static String enrolledKey = "Enrolled";
    public static String playersKey = "Players";
    public static String idKey = "ID";
    public static String byTeamKey = "ByTeam";
    public static String teamSlotsKey = "TeamSlots"; // each entry is mapped teamName - slots


    private String ID;
    private ArrayList<String> Contact = new ArrayList<>();
    private String Description;
    private String HostID;
    private String Name;
    private String Type;
    private String Venue;
    private String EvTime;
    private Long PartySize;
    private Long Enrolled;
    private ArrayList<String> Players;
    private HashMap<String, Integer> monthValues = new HashMap<>();
    private boolean ByTeam;
    private long millisValue = 0;


    public Event(ArrayList<String> contact, String desc, String host, String name, String type, String venue,
                 String time, Long size, Long enrolled, boolean byTeam) {
        Contact.add(0, contact.get(0)); // email
        Contact.add(1, contact.get(1)); // phone
        Description = desc;
        HostID = host;
        Name = name;
        Type = type;
        Venue = venue;
        EvTime = time;
        PartySize = size;
        Enrolled = enrolled;
        ID = Contact.get(0).substring(0, 6) + host + time.substring(0, 3)
                + time.substring(4, 6) + time.substring(10, 15) + time.substring(15) + type + venue;
        Players = new ArrayList<>();
        ByTeam = byTeam;
    }

    public ArrayList<String> getContact() { return Contact; }
    public String getDescription() { return Description; }
    public String getHost() { return HostID; }
    public String getName() { return Name; }
    public String getVenue() { return Venue; }
    public String getType() { return Type; }
    public String getTime() { return EvTime; }
    public Long getPartySize() { return PartySize; }
    public Long getEnrolled() { return Enrolled; }
    public String getID() { return ID; }

    public void createEntry() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> event = new HashMap<>();

        event.put(contactKey, Contact);
        event.put(descriptionKey, Description);
        event.put(hostIDKey, HostID);
        event.put(nameKey, Name);
        event.put(venueKey, Venue);
        event.put(typeKey, Type);
        event.put(evTimeKey, EvTime);
        event.put(partySizeKey, PartySize);
        event.put(enrolledKey, Enrolled);
        event.put(idKey, ID);
        event.put(byTeamKey, ByTeam);
        event.put(teamSlotsKey, new HashMap<String, Long>());
        event.put(playersKey, new ArrayList<>());

        db.collection(availableEventCollection).document(ID).set(event);
    }

    public void toUserHistory(String email) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> event = new HashMap<>();

        event.put(contactKey, Contact);
        event.put(descriptionKey, Description);
        event.put(hostIDKey, HostID);
        event.put(nameKey, Name);
        event.put(venueKey, Venue);
        event.put(typeKey, Type);
        event.put(evTimeKey, EvTime);
        event.put(partySizeKey, PartySize);
        event.put(enrolledKey, Enrolled);
        event.put(idKey, ID);
        event.put(byTeamKey, ByTeam);
        event.put(teamSlotsKey, new HashMap<String, Long>());
        event.put(playersKey, new ArrayList<>());

        db.collection(User.usersCollection).document(email).collection(User.historyCollection)
                .document(ID).set(event);
    }

    public static HashMap<String, Integer> setMonthsMap() {
        HashMap<String, Integer> monthValue = new HashMap<>();
        monthValue.put("Jan", 1);
        monthValue.put("Feb", 2);
        monthValue.put("Mar", 3);
        monthValue.put("Apr", 4);
        monthValue.put("May", 5);
        monthValue.put("Jun", 6);
        monthValue.put("Jul", 7);
        monthValue.put("Aug", 8);
        monthValue.put("Sep", 9);
        monthValue.put("Oct", 10);
        monthValue.put("Nov", 11);
        monthValue.put("Dec", 12);
        return monthValue;
    }

    public int compareTo(Event ev) {
        monthValues = setMonthsMap();

        String s1 = this.EvTime;
        String s2 = ev.EvTime;

        if (!s1.substring(0, 3).equals(s2.substring(0, 3))) {
            // different months
            Integer m1 = monthValues.get(s1.substring(0, 3));
            Integer m2 = monthValues.get(s2.substring(0, 3));
            return m1 - m2;
        } else {
            // same month
            Integer d1 = Integer.parseInt(s1.substring(4, 6));
            Integer d2 = Integer.parseInt(s2.substring(4, 6));
            if (!d1.equals(d2)) // different dates
                return d1 - d2;
            else { // same date
                String h1 = s1.substring(10, 15);
                String ap1 = s1.substring(15);
                String h2 = s2.substring(10, 15);
                String ap2 = s2.substring(15);
                if (!ap1.equals(ap2))
                    return ap1.compareTo(ap2);
                else {
                    return h1.compareTo(h2);
                }
            }
        }
    }

    public long getTimeInMilis() {

        if (millisValue == 0) {
            HashMap<String, Integer> month = setMonthsMap();
            int hour = Integer.parseInt(EvTime.substring(10, 12));
            int amPM = EvTime.substring(15).equals("AM") ? 0 : 1;
            hour += amPM * 12;

            int min = Integer.parseInt(EvTime.substring(13, 15));


            Calendar appointment = (new Calendar.Builder()).setDate(2019,
                    month.get(EvTime.substring(0, 3)) - 1, Integer.parseInt(EvTime.substring(4, 6)))
                    .setTimeOfDay(hour, min, 0).build();

            millisValue = appointment.getTimeInMillis();
        }

        return millisValue;
    }

    /*
        Calculate the delay till one hour before game
     */
    public long delayOne() {

        return getTimeInMilis() - 60*60*1000 - Calendar.getInstance().getTimeInMillis();
    }

    /*
        Calculate the delay till 30 mins before game
     */
    public long delayTwo() {

        return getTimeInMilis() - 30*60*1000 - Calendar.getInstance().getTimeInMillis();
    }

    /*
        Calculate the delay till the starting point
     */
    public long delayExact() {

        return getTimeInMilis() - Calendar.getInstance().getTimeInMillis();
    }
}
