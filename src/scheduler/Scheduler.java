package scheduler;

import scheduler.db.ConnectionManager;
import scheduler.model.Caregiver;
import scheduler.model.Patient;
import scheduler.model.Vaccine;
import scheduler.model.Appointment;
import scheduler.util.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class Scheduler {

    // objects to keep track of the currently logged-in user
    // Note: it is always true that at most one of currentCaregiver and currentPatient is not null
    //       since only one user can be logged-in at a time
    private static Caregiver currentCaregiver = null;
    private static Patient currentPatient = null;

    public static void main(String[] args) {
        // printing greetings text
        System.out.println();
        System.out.println("Welcome to the COVID-19 Vaccine Reservation Scheduling Application!");
        System.out.println("*** Please enter one of the following commands ***");
        System.out.println("> create_patient <username> <password>");  //TODO: implement create_patient (Part 1)
        System.out.println("> create_caregiver <username> <password>");
        System.out.println("> login_patient <username> <password>");  // TODO: implement login_patient (Part 1)
        System.out.println("> login_caregiver <username> <password>");
        System.out.println("> search_caregiver_schedule <date>");  // TODO: implement search_caregiver_schedule (Part 2)
        System.out.println("> reserve <date> <vaccine>");  // TODO: implement reserve (Part 2)
        System.out.println("> upload_availability <date>");
        System.out.println("> cancel <appointment_id>");  // TODO: implement cancel (extra credit)
        System.out.println("> add_doses <vaccine> <number>");
        System.out.println("> show_appointments");  // TODO: implement show_appointments (Part 2)
        System.out.println("> logout");  // TODO: implement logout (Part 2)
        System.out.println("> quit");
        System.out.println();

        // read input from user
        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("> ");
            String response = "";
            try {
                response = r.readLine();
            } catch (IOException e) {
                System.out.println("Please try again!");
            }
            // split the user input by spaces
            String[] tokens = response.split(" ");
            // check if input exists
            if (tokens.length == 0) {
                System.out.println("Please try again!");
                continue;
            }
            // determine which operation to perform
            String operation = tokens[0];
            // Inside the main method
            try {
                if (operation.equals("create_patient")) {
                    createPatient(tokens);
                } else if (operation.equals("create_caregiver")) {
                    createCaregiver(tokens);
                } else if (operation.equals("login_patient")) {
                    loginPatient(tokens);
                } else if (operation.equals("login_caregiver")) {
                    loginCaregiver(tokens);
                } else if (operation.equals("search_caregiver_schedule")) {
                    searchCaregiverSchedule(tokens);
                } else if (operation.equals("reserve")) {
                    reserve(tokens);
                } else if (operation.equals("upload_availability")) {
                    uploadAvailability(tokens);
                } else if (operation.equals("cancel")) {
                    cancel(tokens);
                } else if (operation.equals("add_doses")) {
                    addDoses(tokens);
                } else if (operation.equals("show_appointments")) {
                    showAppointments(tokens);
                } else if (operation.equals("logout")) {
                    logout(tokens);
                } else if (operation.equals("quit")) {
                    System.out.println("Bye!");
                    return;
                } else {
                    System.out.println("Invalid operation name!");
                }
            } catch (SQLException e) {
                System.out.println("An error occurred during the operation: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static void createPatient(String[] tokens) {
        // create_patient <username> <password>
        // check 1: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Failed to create user.");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];
        // check 2: check if the username has been taken already
        if (usernameExistsPatient(username)) {
            System.out.println("Username taken, try again!");
            return;
        }
        byte[] salt = Util.generateSalt();
        byte[] hash = Util.generateHash(password, salt);
        // create the patient
        try {
            Patient patient = new Patient.PatientBuilder(username, salt, hash).build();
            // save to patient information to our database
            patient.saveToDB();
            System.out.println("Created user " + username);
        } catch (SQLException e) {
            System.out.println("Failed to create user.");
            e.printStackTrace();
        }
    }
    private static boolean usernameExistsPatient(String username) {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String selectUsername = "SELECT * FROM Patients WHERE Username = ?";
        try {
            PreparedStatement statement = con.prepareStatement(selectUsername);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            // returns false if the cursor is not before the first record or if there are no rows in the ResultSet.
            return resultSet.isBeforeFirst();
        } catch (SQLException e) {
            System.out.println("Error occurred when checking username");
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }
        return true;
    }

    private static void createCaregiver(String[] tokens) {
        // create_caregiver <username> <password>
        // check 1: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Failed to create user.");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];
        // check 2: check if the username has been taken already
        if (usernameExistsCaregiver(username)) {
            System.out.println("Username taken, try again!");
            return;
        }
        byte[] salt = Util.generateSalt();
        byte[] hash = Util.generateHash(password, salt);
        // create the caregiver
        try {
            Caregiver caregiver = new Caregiver.CaregiverBuilder(username, salt, hash).build(); 
            // save to caregiver information to our database
            caregiver.saveToDB();
            System.out.println("Created user " + username);
        } catch (SQLException e) {
            System.out.println("Failed to create user.");
            e.printStackTrace();
        }
    }

    private static boolean usernameExistsCaregiver(String username) {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String selectUsername = "SELECT * FROM Caregivers WHERE Username = ?";
        try {
            PreparedStatement statement = con.prepareStatement(selectUsername);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            // returns false if the cursor is not before the first record or if there are no rows in the ResultSet.
            return resultSet.isBeforeFirst();
        } catch (SQLException e) {
            System.out.println("Error occurred when checking username");
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }
        return true;
    }

    private static void loginPatient(String[] tokens) {
        // login_patient <username> <password>
        // check 1: if someone's already logged-in, they need to log out first
        if (currentCaregiver != null || currentPatient != null) {
            System.out.println("User already logged in.");
            return;
        }
        // check 2: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Login failed.");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];

        Patient patient = null;
        try {
            patient = new Patient.PatientGetter(username, password).get();
        } catch (SQLException e) {
            System.out.println("Login failed.");
            e.printStackTrace();
        }
        // check if the login was successful
        if (patient == null) {
            System.out.println("Login failed.");
        } else {
            System.out.println("Logged in as: " + username);
            currentPatient = patient;
        }
    }

    private static void loginCaregiver(String[] tokens) {
        // login_caregiver <username> <password>
        // check 1: if someone's already logged-in, they need to log out first
        if (currentCaregiver != null || currentPatient != null) {
            System.out.println("User already logged in.");
            return;
        }
        // check 2: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Login failed.");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];

        Caregiver caregiver = null;
        try {
            caregiver = new Caregiver.CaregiverGetter(username, password).get();
        } catch (SQLException e) {
            System.out.println("Login failed.");
            e.printStackTrace();
        }
        // check if the login was successful
        if (caregiver == null) {
            System.out.println("Login failed.");
        } else {
            System.out.println("Logged in as: " + username);
            currentCaregiver = caregiver;
        }
    }

    private static void searchCaregiverSchedule(String[] tokens) {
        if (currentCaregiver == null && currentPatient == null) {
            System.out.println("Please login first!");
            return;
        }

        if (tokens.length != 2) {
            System.out.println("Please try again!");
            return;
        }

        try {
            Date date = Date.valueOf(tokens[1]);
            List<String> results = searchCaregiverScheduleFromDB(date);

            if (results.isEmpty()) {
                System.out.println("No caregivers available for the given date.");
            } else {
                // Print the results
                System.out.println("CaregiverUsername DosesLeft");
                for (String result : results) {
                    System.out.println(result);
                }
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Please enter a valid date!");
        }
    }

    private static List<String> searchCaregiverScheduleFromDB(Date date) {
        List<String> results = new ArrayList<>();
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String sqlQuery = "SELECT Availabilities.Username, COUNT(*) AS DosesLeft " +
                "FROM Availabilities " +
                "LEFT JOIN Appointments ON Availabilities.Username = Appointments.CaregiverUsername " +
                "                       AND Availabilities.Time = Appointments.Date " +
                "WHERE Availabilities.Time = ? " +
                "      AND Appointments.AppointmentID IS NULL " +
                "GROUP BY Availabilities.Username " +
                "ORDER BY Availabilities.Username;";

        try {
            PreparedStatement statement = con.prepareStatement(sqlQuery);
            statement.setDate(1, date);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String username = resultSet.getString("Username");
                int dosesLeft = resultSet.getInt("DosesLeft");
                results.add(username + " " + dosesLeft);
            }
        } catch (SQLException e) {
            System.out.println("Error occurred when searching caregiver schedule");
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }

        return results;
    }

    private static void reserve(String[] tokens) throws SQLException {
        if (currentPatient == null) {
            System.out.println("Please login as a patient!");
            return;
        }

        if (tokens.length != 3) {
            System.out.println("Please try again!");
            return;
        }

        try {
            Date date = Date.valueOf(tokens[1]);
            String vaccineName = tokens[2];

            boolean reservationSuccess = reserveAppointment(date, vaccineName);

            if (reservationSuccess) {
                System.out.println("Appointment ID: " + currentPatient.getLatestAppointmentID() +
                        ", Caregiver username: " + currentPatient.getCaregiverUsername());
            } else {
                System.out.println("Reservation failed.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Please enter a valid date!");
        }
    }

    private static boolean reserveAppointment(Date date, String vaccineName) {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String updateAvailabilityQuery = "UPDATE Availabilities " +
                "SET Time = ?, Username = ?, DosesLeft = DosesLeft - 1 " +
                "WHERE Time = ? AND Username = ? AND DosesLeft > 0;";

        String insertAppointmentQuery = "INSERT INTO Appointments (AppointmentID, Date, CaregiverUsername, PatientUsername, VaccineName) " +
                "VALUES (?, ?, ?, ?, ?);";

        try {
            con.setAutoCommit(false);

            PreparedStatement updateStatement = con.prepareStatement(updateAvailabilityQuery);
            updateStatement.setDate(1, date);
            updateStatement.setString(2, currentPatient.getCaregiverUsername());
            updateStatement.setDate(3, date);
            updateStatement.setString(4, currentPatient.getCaregiverUsername());
            int rowsUpdated = updateStatement.executeUpdate();

            if (rowsUpdated == 0) {
                System.out.println("Not enough available doses!");
                return false;
            }


            // Insert Appointment
            PreparedStatement insertStatement = con.prepareStatement(insertAppointmentQuery);
            int appointmentID = generateUniqueAppointmentID();
            insertStatement.setInt(1, appointmentID);
            insertStatement.setDate(2, date);
            insertStatement.setString(3, currentPatient.getCaregiverUsername());
            insertStatement.setString(4, currentPatient.getUsername());
            insertStatement.setString(5, vaccineName);
            insertStatement.executeUpdate();

            con.commit();

            currentPatient.setLatestAppointmentID(appointmentID);

            System.out.println("Reservation successful!");
            return true;
        } catch (SQLException e) {
            try {
                con.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.out.println("Error occurred during reservation.");
            e.printStackTrace();
            return false;
        } finally {
            // Set auto-commit back to true
            try {
                con.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            cm.closeConnection();
        }
    }

    private static int generateUniqueAppointmentID() throws SQLException {
        return Appointment.getUniqueAppointmentID();
    }

    private static void uploadAvailability(String[] tokens) {
        // upload_availability <date>
        // check 1: check if the current logged-in user is a caregiver
        if (currentCaregiver == null) {
            System.out.println("Please login as a caregiver first!");
            return;
        }
        // check 2: the length for tokens need to be exactly 2 to include all information (with the operation name)
        if (tokens.length != 2) {
            System.out.println("Please try again!");
            return;
        }
        String date = tokens[1];
        try {
            Date d = Date.valueOf(date);
            currentCaregiver.uploadAvailability(d);
            System.out.println("Availability uploaded!");
        } catch (IllegalArgumentException e) {
            System.out.println("Please enter a valid date!");
        } catch (SQLException e) {
            System.out.println("Error occurred when uploading availability");
            e.printStackTrace();
        }
    }

    private static void addDoses(String[] tokens) {
        // add_doses <vaccine> <number>
        // check 1: check if the current logged-in user is a caregiver
        if (currentCaregiver == null) {
            System.out.println("Please login as a caregiver first!");
            return;
        }
        // check 2: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Please try again!");
            return;
        }
        String vaccineName = tokens[1];
        int doses = Integer.parseInt(tokens[2]);
        Vaccine vaccine = null;
        try {
            vaccine = new Vaccine.VaccineGetter(vaccineName).get();
        } catch (SQLException e) {
            System.out.println("Error occurred when adding doses");
            e.printStackTrace();
        }
        // check 3: if getter returns null, it means that we need to create the vaccine and insert it into the Vaccines
        //          table
        if (vaccine == null) {
            try {
                vaccine = new Vaccine.VaccineBuilder(vaccineName, doses).build();
                vaccine.saveToDB();
            } catch (SQLException e) {
                System.out.println("Error occurred when adding doses");
                e.printStackTrace();
            }
        } else {
            try {
                vaccine.increaseAvailableDoses(doses);
            } catch (SQLException e) {
                System.out.println("Error occurred when adding doses");
                e.printStackTrace();
            }
        }
        System.out.println("Doses updated!");
    }

    private static void showAppointments(String[] tokens) {
        if (currentCaregiver == null && currentPatient == null) {
            System.out.println("Please login first!");
            return;
        }

        if (tokens.length != 1) {
            System.out.println("Please try again!");
            return;
        }

        try {
            List<Appointment> appointments;
            if (currentPatient != null) {
                appointments = Appointment.getAppointmentsForPatient(currentPatient.getUsername());
            } else {
                appointments = Appointment.getAppointmentsForCaregiver(currentCaregiver.getUsername());
            }

            if (appointments.isEmpty()) {
                System.out.println("No appointments found.");
            } else {

                System.out.println("AppointmentID Date CaregiverUsername PatientUsername VaccineName");
                for (Appointment appointment : appointments) {
                    System.out.println(appointment);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error occurred when retrieving appointments");
            e.printStackTrace();
        }
    }


    private static void logout(String[] tokens) {
        if (currentCaregiver == null && currentPatient == null) {
            System.out.println("Please login first!");
        } else {
            System.out.println("Successfully logged out!");
            currentCaregiver = null;
            currentPatient = null;
        }
    }

    private static void cancel(String[] tokens) {
        if (currentPatient != null) {
            cancelAppointmentForPatient(tokens);
        } else if (currentCaregiver != null) {
            cancelAppointmentForCaregiver(tokens);
        } else {
            System.out.println("Please log in first!");
        }
    }

    private static void cancelAppointmentForPatient(String[] tokens) {
        if (tokens.length != 2) {
            System.out.println("Please try again!");
            return;
        }

        try {
            int appointmentID = Integer.parseInt(tokens[1]);

            boolean cancellationSuccess = cancelAppointmentForPatient(appointmentID);

            if (cancellationSuccess) {
                System.out.println("Appointment canceled successfully!");
            } else {
                System.out.println("Cancellation failed.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid appointment ID!");
        }
    }

    private static boolean cancelAppointmentForPatient(int appointmentID) {
        try {
            Appointment appointment = Appointment.getAppointmentById(appointmentID);
            if (appointment != null && appointment.getPatientUsername().equals(currentPatient.getUsername())) {
                appointment.removeFromDB();
                return true;
            } else {
                System.out.println("Invalid appointment ID or you don't have permission to cancel this appointment.");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Error occurred during cancellation.");
            e.printStackTrace();
            return false;
        }
    }

    private static void cancelAppointmentForCaregiver(String[] tokens) {
        if (tokens.length != 2) {
            System.out.println("Please try again!");
            return;
        }

        try {
            int appointmentID = Integer.parseInt(tokens[1]);

            boolean cancellationSuccess = cancelAppointmentForCaregiver(appointmentID);

            if (cancellationSuccess) {
                System.out.println("Appointment canceled successfully!");
            } else {
                System.out.println("Cancellation failed.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid appointment ID!");
        }
    }

    private static boolean cancelAppointmentForCaregiver(int appointmentID) {
        try {
            Appointment appointment = Appointment.getAppointmentById(appointmentID);
            if (appointment != null && appointment.getCaregiverUsername().equals(currentCaregiver.getUsername())) {
                appointment.removeFromDB();
                return true;
            } else {
                System.out.println("Invalid appointment ID or you don't have permission to cancel this appointment.");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Error occurred during cancellation.");
            e.printStackTrace();
            return false;
        }
    }


}
