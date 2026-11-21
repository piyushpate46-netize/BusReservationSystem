# Bus Reservation System

**College Project PS No.:** 19  
**Application type:** Java console application  
**Language:** Java

## Project overview

The Bus Reservation System is a menu-driven application for viewing buses, searching routes, booking and cancelling tickets, managing a waiting queue, and saving data between runs. It demonstrates Java collections, methods, searching, sorting, and file handling.

## Project folder structure

```text
BusReservationSystem/
├── .gitignore                                  # Ignore compiled/build and IDE files
├── Main.java                                   # Application source code
├── buses.txt                                   # Bus inventory and seat capacities
├── bookings.txt                                # Saved bookings, cancellations, and waiting entries
├── Bus_Reservation_System_Project_Report.docx  # Editable college project report
├── Bus_Reservation_System_Project_Report.pdf   # PDF copy of the complete report
└── README.md                                   # Project documentation
```

## Requirements and run instructions

- Install a Java Development Kit (JDK) and make `java` and `javac` available in the terminal.
- Open a terminal in the project folder. The program reads and writes data files relative to the current working directory.
- Compile and start the application:

```text
javac Main.java
java Main
```

Select menu option **6. Exit** to save current data before closing the application.

## Introduction

Bus reservations require tracking routes, seat capacity, passenger bookings, and cancellations. This project models those basic operations through a console interface. Text files preserve bus and booking information between application runs.

## Objectives

- Display buses and remaining seats.
- Search routes by destination text.
- Book tickets while respecting seat capacity.
- Queue passengers in first-in, first-out order when a bus is full.
- Assign a cancelled seat to the next waiting passenger.
- Generate sequential ticket IDs starting from `TKT101`.
- Sort buses by fare and persist data using text files.

## Technologies used

- **Java** for application logic and console input/output.
- **ArrayList** for buses and booking records.
- **Queue** with a `LinkedList` implementation for waiting passengers.
- **Java file I/O** (`BufferedReader`, `BufferedWriter`, and `Files`) for loading and saving data.
- **Comparator** for fare sorting.

## Algorithm

1. Load bus records from `buses.txt` and saved booking records from `bookings.txt`.
2. Determine the next ticket number from the saved ticket IDs.
3. Display the menu and read the user's choice. Invalid numeric input is handled without ending the program.
4. For a booking, find the requested bus. If a seat is available, create a confirmed ticket. If full, add the passenger to the waiting queue.
5. For cancellation, find a confirmed ticket and mark it cancelled. If the bus has waiting passengers, promote the earliest one and assign a new ticket ID.
6. For searching, match destination text against each route without case sensitivity. For sorting, order buses by ascending fare.
7. On exit, save buses and booking states to their respective files.

## Features and menu options

1. **View All Buses** — shows bus ID, route, fare, and seats remaining.
2. **Search Bus by Destination** — finds routes containing the entered text, ignoring case.
3. **Book Ticket** — books a seat or adds the passenger to the bus's waiting queue.
4. **Cancel Ticket** — cancels a confirmed ticket and promotes the next waiting passenger when possible.
5. **Sort Buses by Fare** — displays buses from lowest to highest fare.
6. **Exit** — saves bus and booking data and exits.

Ticket IDs increase sequentially. Cancelled records remain in the data file as history. Waiting records receive a ticket ID only when a seat is assigned. Available seats are calculated from total capacity minus confirmed bookings.

## Data files

### `buses.txt`

Each row uses the format `BusID,Route,Fare,TotalSeats`:

```text
B101,Raipur-Bhopal,700,2
B102,Raipur-Nagpur,450,2
```

### `bookings.txt`

Each row uses `TicketID,PassengerName,BusID,Status`. Status is `BOOKED`, `WAITING`, or `CANCELLED`. The supplied project folder contains an existing saved booking record. The program loads records at startup and saves current booking states when the user selects Exit. Keep this file with the project to retain its saved reservation state.

## Test cases

The college scenarios were compiled and executed:

| Test | Scenario | Result |
|---|---|---|
| 1. Normal booking | Book B101 for Riya using fresh data | `TKT101` assigned; 1 seat remains |
| 2. Last seat | Book B101 for Aman after Riya | `TKT102` assigned; 0 seats remain |
| 3. Invalid bus | Attempt to book B999 | `Bus B999 not found.` |
| 4. Waiting, cancellation, sorting | Fill B101, queue Dev, cancel TKT101, sort fares | Dev receives `TKT103`; B102 (450) sorts before B101 (700) |

The save-and-reload check restored the confirmed bookings and recalculated B101 as full after the application restarted.

## Sample output

```text
Ticket booked. TKT101 | Riya | B101 Raipur-Bhopal
Seats left: 1
Bus full. Dev added to waiting queue.
Ticket TKT101 cancelled. Seat given to Dev: TKT103
Buses sorted by fare:
B102 | 450
B101 | 700
```

## Conclusion

This project demonstrates a basic reservation workflow with capacity checks, sequential ticketing, waiting-list promotion, fare sorting, and file-based persistence. The application is kept in one beginner-readable Java source file with separate methods for its main operations.

## Uploading to GitHub

1. Create an empty repository on GitHub.
2. Open a terminal in this folder and run `git init` if the folder is not already a Git repository.
3. Stage the project files: `git add .gitignore Main.java buses.txt bookings.txt README.md Bus_Reservation_System_Project_Report.docx Bus_Reservation_System_Project_Report.pdf`.
4. Commit the files: `git commit -m "Add bus reservation system college project"`.
5. Add the repository URL: `git remote add origin <repository-url>`.
6. Push the branch: `git push -u origin main` (use your branch name if it differs).
