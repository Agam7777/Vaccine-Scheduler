package scheduler.model;

import scheduler.db.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Appointment {

    private final int appointmentID;
    private final java.sql.Date date;
    private final String caregiverUsername;
    private final String patientUsername;
    private final String vaccineName;

    private Appointment(int appointmentID, Date date, String caregiverUsername, String patientUsername, String vaccineName) {
        this.appointmentID = appointmentID;
        this.date = date;
        this.caregiverUsername = caregiverUsername;
        this.patientUsername = patientUsername;
        this.vaccineName = vaccineName;
    }

    public int getAppointmentID() {
        return appointmentID;
    }

    public java.sql.Date getDate() {
        return date;
    }

    public String getCaregiverUsername() {
        return caregiverUsername;
    }

    public String getPatientUsername() {
        return patientUsername;
    }

    public String getVaccineName() {
        return vaccineName;
    }

    public void saveToDB() throws SQLException {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String addAppointment = "INSERT INTO Appointments VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement statement = con.prepareStatement(addAppointment);
            statement.setInt(1, this.appointmentID);
            statement.setDate(2, this.date);
            statement.setString(3, this.caregiverUsername);
            statement.setString(4, this.patientUsername);
            statement.setString(5, this.vaccineName);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Failed to save appointment to the database.");
        } finally {
            cm.closeConnection();
        }
    }

    public static int getUniqueAppointmentID() throws SQLException {
        return generateUniqueAppointmentID();
    }

    private static int generateUniqueAppointmentID() throws SQLException {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String selectMaxID = "SELECT MAX(AppointmentID) FROM Appointments";
        try {
            PreparedStatement statement = con.prepareStatement(selectMaxID);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) + 1;
            }
        } catch (SQLException e) {
            throw new SQLException("Failed to generate a unique appointment ID.");
        } finally {
            cm.closeConnection();
        }
        return 1;
    }
    public static List<Appointment> getAppointmentsForPatient(String patientUsername) throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String sqlQuery = "SELECT * FROM Appointments WHERE PatientUsername = ?";
        try {
            PreparedStatement statement = con.prepareStatement(sqlQuery);
            statement.setString(1, patientUsername);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int appointmentID = resultSet.getInt("AppointmentID");
                Date date = resultSet.getDate("Date");
                String caregiverUsername = resultSet.getString("CaregiverUsername");
                String vaccineName = resultSet.getString("VaccineName");

                Appointment appointment = new Appointment(appointmentID, date, caregiverUsername, patientUsername, vaccineName);
                appointments.add(appointment);
            }
        } finally {
            cm.closeConnection();
        }

        return appointments;
    }

    public static List<Appointment> getAppointmentsForCaregiver(String caregiverUsername) throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String sqlQuery = "SELECT * FROM Appointments WHERE CaregiverUsername = ?";
        try {
            PreparedStatement statement = con.prepareStatement(sqlQuery);
            statement.setString(1, caregiverUsername);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int appointmentID = resultSet.getInt("AppointmentID");
                Date date = resultSet.getDate("Date");
                String patientUsername = resultSet.getString("PatientUsername");
                String vaccineName = resultSet.getString("VaccineName");

                Appointment appointment = new Appointment(appointmentID, date, caregiverUsername, patientUsername, vaccineName);
                appointments.add(appointment);
            }
        } finally {
            cm.closeConnection();
        }

        return appointments;
    }

    public static Appointment getAppointmentById(int appointmentID) throws SQLException {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String sqlQuery = "SELECT * FROM Appointments WHERE AppointmentID = ?";
        try {
            PreparedStatement statement = con.prepareStatement(sqlQuery);
            statement.setInt(1, appointmentID);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int id = resultSet.getInt("AppointmentID");
                Date date = resultSet.getDate("Date");
                String caregiverUsername = resultSet.getString("CaregiverUsername");
                String patientUsername = resultSet.getString("PatientUsername");
                String vaccineName = resultSet.getString("VaccineName");

                return new Appointment(id, date, caregiverUsername, patientUsername, vaccineName);
            } else {
                return null;
            }
        } finally {
            cm.closeConnection();
        }
    }

    public void removeFromDB() throws SQLException {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String deleteAppointment = "DELETE FROM Appointments WHERE AppointmentID = ?";
        try {
            PreparedStatement statement = con.prepareStatement(deleteAppointment);
            statement.setInt(1, this.appointmentID);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Failed to cancel appointment from the database.");
        } finally {
            cm.closeConnection();
        }
    }

}
