
CREATE TABLE Users (
	username VARCHAR PRIMARY KEY NOT NULL,
	email VARCHAR,
	profile VARCHAR NOT NULL,
	address VARCHAR NOT NULL,
	phoneNum INTEGER NOT NULL
);

CREATE TABLE Consumers (
	username VARCHAR PRIMARY KEY REFERENCES Users(username) ON DELETE cascade,
	creditCard INTEGER,
	bankAcc INTEGER NOT NULL
);

CREATE TABLE PCSadmins (
	username VARCHAR PRIMARY KEY REFERENCES Users(username) ON DELETE cascade
);

CREATE TABLE PetOwners (
	username VARCHAR PRIMARY KEY REFERENCES Consumers(username) ON DELETE cascade
);

CREATE TABLE CareTakers (
	username VARCHAR PRIMARY KEY REFERENCES Consumers(username) ON DELETE cascade,
  maxslots INTEGER NOT NULL
);

CREATE TABLE FullTimers (
	username VARCHAR PRIMARY KEY REFERENCES CareTakers(username) ON DELETE cascade
);

CREATE TABLE PartTimers (
	username VARCHAR PRIMARY KEY REFERENCES CareTakers(username) ON DELETE cascade
);

CREATE TABLE Pets (
	petowner VARCHAR REFERENCES PetOwners(username) ON DELETE CASCADE,
	petname VARCHAR NOT NULL,
	profile VARCHAR NOT NULL,
	specialReq VARCHAR,
	PRIMARY KEY (petowner, petname)
);

--parttimers 
CREATE TABLE Availability (
	caretaker VARCHAR REFERENCES PartTimers(username) ON DELETE CASCADE,
	avail DATE NOT NULL,
	PRIMARY KEY (caretaker, avail)
);

--fulltimers
CREATE TABLE Unavailability (
	caretaker VARCHAR REFERENCES FullTimers(username) ON DELETE CASCADE,
	avail DATE NOT NULL,
	PRIMARY KEY (caretaker, avail)
);

-- #######################
-- caretakerid, avail
-- 		1 jan 
-- 		2 jan
-- 		3 jan 
-- #######################

CREATE TABLE PetTypes (
	category VARCHAR PRIMARY KEY NOT NULL,
	baseprice FLOAT(4) NOT NULL
);

CREATE TABLE Manages (
	admin VARCHAR REFERENCES PCSadmins(username),
	caretaker VARCHAR REFERENCES CareTakers(username),
	PRIMARY KEY (admin, caretaker)
); 

CREATE TABLE Bids (
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
