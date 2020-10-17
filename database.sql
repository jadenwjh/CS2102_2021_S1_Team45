CREATE DATABASE pcs_database;

--\c into pcs_database

CREATE TABLE Users (
	username VARCHAR PRIMARY KEY NOT NULL,
	email VARCHAR,
	profile VARCHAR NOT NULL,
	address VARCHAR NOT NULL,
	phoneNum INTEGER NOT NULL
);

CREATE TABLE Consumer (
	username VARCHAR PRIMARY KEY REFERENCES Users(username) ON DELETE cascade,
	creditCard INTEGER,
	bankAcc INTEGER NOT NULL
);

CREATE TABLE PCSadmin (
	username VARCHAR PRIMARY KEY REFERENCES Users(username) ON DELETE cascade
);

CREATE TABLE PetOwner (
	username VARCHAR PRIMARY KEY REFERENCES Consumers(username) ON DELETE cascade
);

CREATE TABLE CareTaker (
	username VARCHAR PRIMARY KEY REFERENCES Consumers(username) ON DELETE cascade
  maxslots INTEGER NOT NULL,
);

CREATE TABLE FullTimer (
	username VARCHAR PRIMARY KEY REFERENCES CareTaker(username) ON DELETE cascade
);

CREATE TABLE PartTimer (
	username VARCHAR PRIMARY KEY REFERENCES CareTaker(username) ON DELETE cascade
);

CREATE TABLE Pet (
	petowner VARCHAR REFERENCES PetOwner(username) ON DELETE CASCADE,
	petname VARCHAR NOT NULL,
	profile VARCHAR NOT NULL,
	specialReq VARCHAR,
	PRIMARY KEY (petowner, petname)
);

--parttimers 
CREATE TABLE Availability (
	caretaker VARCHAR REFERENCES PartTimer(username) ON DELETE CASCADE,
	avail DATE NOT NULL,
	PRIMARY KEY (caretaker, avail)
);

--fulltimers
CREATE TABLE Unavailability (
	caretaker VARCHAR REFERENCES FullTimer(username) ON DELETE CASCADE,
	avail DATE NOT NULL,
	PRIMARY KEY (caretaker, avail)
);

-- #######################
-- caretakerid, avail
-- 		1 jan 
-- 		2 jan
-- 		3 jan 
-- #######################

CREATE TABLE PetType (
	category VARCHAR PRIMARY KEY NOT NULL,
	baseprice FLOAT(4) NOT NULL
);

CREATE TABLE Manages (
	admin VARCHAR REFERENCES PCSadmin(username),
	caretaker VARCHAR REFERENCES CareTakers(username),
	PRIMARY KEY (admin, caretaker)
); 

CREATE TABLE Bid (
	petowner VARCHAR NOT NULL,
	petname VARCHAR NOT NULL,
	caretaker VARCHAR NOT NULL,
	sdate DATE NOT NULL,
  edate DATE NOT NULL, 
	FOREIGN KEY(petowner, petname) REFERENCES Pets(petowner, petname),
	FOREIGN KEY(caretaker, sdate) REFERENCES Availability(caretaker, avail),
	transferType VARCHAR NOT NULL, 
    paymentType VARCHAR NOT NULL, 
    price FLOAT(4) NOT NULL,
    isPaid BOOLEAN NOT NULL, 
    isWin BOOLEAN NOT NULL, 
    review VARCHAR NOT NULL, 
    rating VARCHAR NOT NULL
	
);
