CREATE TABLE Caregivers (
    Username varchar(255),
    Salt BINARY(16),
    Hash BINARY(16),
    PRIMARY KEY (Username)
);

CREATE TABLE Patients (
    Username varchar(255),
    Salt BINARY(16),
    Hash BINARY(16),
    PRIMARY KEY (Username)
);

CREATE TABLE Availabilities (
    Time DATE,
    Username VARCHAR(255) REFERENCES Caregivers(Username),
    DosesLeft INT,
    PRIMARY KEY (Time, Username)
);

CREATE TABLE Vaccines (
    Name varchar(255),
    Doses int,
    PRIMARY KEY (Name)
);

CREATE TABLE Appointments (
    AppointmentID INT,
    Date date,
    CaregiverUsername varchar(255) REFERENCES Caregivers(Username),
    PatientUsername varchar(255) REFERENCES Patients(Username),
    VaccineName varchar(255) REFERENCES Vaccines(Name),
	PRIMARY KEY (AppointmentID)
);
