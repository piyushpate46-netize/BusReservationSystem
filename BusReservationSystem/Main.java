import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;

/** Console-based bus reservation system for a beginner Java project. */
public class Main {
    private static final Path BUS_FILE = Paths.get("buses.txt");
    private static final Path BOOKING_FILE = Paths.get("bookings.txt");
    private static final List<Bus> buses = new ArrayList<>();
    private static final List<Ticket> bookings = new ArrayList<>();
    private static final Scanner scanner = new Scanner(System.in);
    private static int nextTicketNumber = 101;

    public static void main(String[] args) {
        loadBuses();
        loadBookings();
        while (true) {
            showMenu();
            int choice = readInt("Enter your choice: ");
            switch (choice) {
                case 1: displayBuses(buses); break;
                case 2:
                    System.out.print("Enter destination to search: ");
                    searchBuses(scanner.nextLine().trim());
                    break;
                case 3: bookTicket(); break;
                case 4: cancelTicket(); break;
                case 5: sortBusesByFare(); break;
                case 6:
                    saveData();
                    System.out.println("Data saved. Thank you for using Bus Reservation System.");
                    return;
                default: System.out.println("Invalid choice. Please enter a number from 1 to 6.");
            }
        }
    }

    private static void showMenu() {
        System.out.println("\n===== BUS RESERVATION SYSTEM =====");
        System.out.println("1. View All Buses\n2. Search Bus by Destination\n3. Book Ticket");
        System.out.println("4. Cancel Ticket\n5. Sort Buses by Fare\n6. Exit");
    }

    // Reads whole lines so invalid input never terminates the program.
    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try { return Integer.parseInt(input); }
            catch (NumberFormatException e) { System.out.println("Please enter a valid number."); }
        }
    }

    /** Loads bus records from buses.txt and uses the sample buses if the file is missing. */
    private static void loadBuses() {
        if (!Files.exists(BUS_FILE)) {
            System.out.println("buses.txt was not found. Creating it with the sample buses.");
            try {
                Files.write(BUS_FILE, List.of("B101,Raipur-Bhopal,700,2", "B102,Raipur-Nagpur,450,2"));
            } catch (IOException e) { System.out.println("Could not create buses.txt: " + e.getMessage()); }
        }
        try (BufferedReader reader = Files.newBufferedReader(BUS_FILE)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length != 4) { System.out.println("Skipping invalid bus row: " + line); continue; }
                try {
                    Bus bus = new Bus(fields[0].trim(), fields[1].trim(), Integer.parseInt(fields[2].trim()), Integer.parseInt(fields[3].trim()));
                    if (bus.fare < 0 || bus.totalSeats < 0) throw new NumberFormatException();
                    buses.add(bus);
                } catch (NumberFormatException e) { System.out.println("Skipping invalid bus row: " + line); }
            }
        } catch (IOException e) { System.out.println("Could not read buses.txt: " + e.getMessage()); }
    }

    /** Restores saved tickets and waiting passengers and continues ticket numbering. */
    private static void loadBookings() {
        if (!Files.exists(BOOKING_FILE)) return;
        try (BufferedReader reader = Files.newBufferedReader(BOOKING_FILE)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",", 4);
                if (fields.length != 4) { System.out.println("Skipping invalid booking row: " + line); continue; }
                Bus bus = findBus(fields[2].trim());
                String status = fields[3].trim();
                if (bus == null || (!status.equals("BOOKED") && !status.equals("WAITING") && !status.equals("CANCELLED"))) continue;
                Ticket ticket = new Ticket(fields[0].trim(), fields[1].trim(), bus.id, status);
                bookings.add(ticket);
                if (ticket.ticketId.startsWith("TKT")) {
                    try { nextTicketNumber = Math.max(nextTicketNumber, Integer.parseInt(ticket.ticketId.substring(3)) + 1); }
                    catch (NumberFormatException ignored) { }
                }
            }
        } catch (IOException e) { System.out.println("Could not read bookings.txt: " + e.getMessage()); }
    }

    /** Writes the bus list and all booking states to their text files on exit. */
    private static void saveData() {
        // Bus rows stay in the requested four-column format; seat counts are derived from active bookings.
        try (BufferedWriter writer = Files.newBufferedWriter(BUS_FILE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (Bus bus : buses) {
                writer.write(bus.id + "," + bus.route + "," + bus.fare + "," + bus.totalSeats);
                writer.newLine();
            }
        } catch (IOException e) { System.out.println("Could not save buses.txt: " + e.getMessage()); }
        try (BufferedWriter writer = Files.newBufferedWriter(BOOKING_FILE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (Ticket ticket : bookings) {
                writer.write(ticket.ticketId + "," + ticket.passengerName + "," + ticket.busId + "," + ticket.status);
                writer.newLine();
            }
        } catch (IOException e) { System.out.println("Could not save bookings.txt: " + e.getMessage()); }
    }

    /** Prints bus information and computes the current available seats for each bus. */
    private static void displayBuses(List<Bus> list) {
        if (list.isEmpty()) { System.out.println("No buses available."); return; }
        System.out.println("\nBus ID | Route | Fare | Seats left");
        for (Bus bus : list) System.out.println(bus.id + " | " + bus.route + " | " + bus.fare + " | " + seatsLeft(bus));
    }

    // Searches for a destination contained in a route, without regard to case.
    private static void searchBuses(String destination) {
        if (destination.isEmpty()) { System.out.println("Destination cannot be empty."); return; }
        List<Bus> matches = new ArrayList<>();
        for (Bus bus : buses) if (bus.route.toLowerCase().contains(destination.toLowerCase())) matches.add(bus);
        if (matches.isEmpty()) System.out.println("No buses found for destination: " + destination);
        else displayBuses(matches);
    }

    /** Books a free seat or appends the passenger to the bus's waiting list. */
    private static void bookTicket() {
        System.out.print("Enter Bus ID: ");
        String busId = scanner.nextLine().trim();
        Bus bus = findBus(busId);
        if (bus == null) { System.out.println("Bus " + busId + " not found."); return; }
        System.out.print("Enter Passenger Name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty() || name.contains(",")) { System.out.println("Please enter a passenger name without commas."); return; }
        if (seatsLeft(bus) <= 0) {
            bookings.add(new Ticket("", name, bus.id, "WAITING"));
            System.out.println("Bus full. " + name + " added to waiting queue.");
            return;
        }
        Ticket ticket = createTicket(name, bus);
        System.out.println("Ticket booked. " + ticket.ticketId + " | " + name + " | " + bus.id + " " + bus.route);
        System.out.println("Seats left: " + seatsLeft(bus));
    }

    /** Creates a confirmed booking using the next sequential ticket number. */
    private static Ticket createTicket(String name, Bus bus) {
        Ticket ticket = new Ticket("TKT" + nextTicketNumber++, name, bus.id, "BOOKED");
        bookings.add(ticket);
        return ticket;
    }

    /** Cancels a confirmed ticket and promotes the earliest waiting passenger, if present. */
    private static void cancelTicket() {
        System.out.print("Enter Ticket ID: ");
        String ticketId = scanner.nextLine().trim();
        Ticket ticket = null;
        for (Ticket item : bookings) if (item.status.equals("BOOKED") && item.ticketId.equalsIgnoreCase(ticketId)) { ticket = item; break; }
        if (ticket == null) { System.out.println("Booked ticket " + ticketId + " not found."); return; }
        ticket.status = "CANCELLED";
        Bus bus = findBus(ticket.busId);
        Queue<Ticket> queue = waitingQueue(bus);
        if (!queue.isEmpty()) {
            Ticket waiting = queue.remove();
            waiting.ticketId = "TKT" + nextTicketNumber++;
            waiting.status = "BOOKED";
            System.out.println("Ticket " + ticket.ticketId + " cancelled. Seat given to " + waiting.passengerName + ": " + waiting.ticketId);
        } else System.out.println("Ticket " + ticket.ticketId + " cancelled.");
    }

    /** Builds a FIFO queue of waiting records for the selected bus in booking order. */
    private static Queue<Ticket> waitingQueue(Bus bus) {
        Queue<Ticket> queue = new LinkedList<>();
        for (Ticket ticket : bookings) if (ticket.busId.equals(bus.id) && ticket.status.equals("WAITING")) queue.add(ticket);
        return queue;
    }

    /** Sorts the shared bus list by fare in ascending order and prints the result. */
    private static void sortBusesByFare() {
        buses.sort(Comparator.comparingInt(bus -> bus.fare));
        System.out.println("Buses sorted by fare:");
        for (Bus bus : buses) System.out.println(bus.id + " | " + bus.fare);
    }

    /** Finds a bus by ID, ignoring capitalization; returns null when it is absent. */
    private static Bus findBus(String id) {
        for (Bus bus : buses) if (bus.id.equalsIgnoreCase(id)) return bus;
        return null;
    }

    /** Calculates available seats from total capacity minus confirmed bookings. */
    private static int seatsLeft(Bus bus) {
        int booked = 0;
        for (Ticket ticket : bookings) if (ticket.busId.equals(bus.id) && ticket.status.equals("BOOKED")) booked++;
        return Math.max(0, bus.totalSeats - booked);
    }

    private static class Bus {
        final String id, route;
        final int fare, totalSeats;
        Bus(String id, String route, int fare, int totalSeats) { this.id = id; this.route = route; this.fare = fare; this.totalSeats = totalSeats; }
    }

    private static class Ticket {
        String ticketId;
        final String passengerName, busId;
        String status;
        Ticket(String ticketId, String passengerName, String busId, String status) {
            this.ticketId = ticketId; this.passengerName = passengerName; this.busId = busId; this.status = status;
        }
    }
}
