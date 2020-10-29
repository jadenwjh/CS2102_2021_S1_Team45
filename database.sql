DROP TABLE IF EXISTS InvalidatedBids;
DROP TABLE IF EXISTS Bids;
DROP TABLE IF EXISTS Manages;
DROP TABLE IF EXISTS Availability;
DROP TABLE IF EXISTS Pets;
DROP TABLE IF EXISTS AbleToCare;
DROP TABLE IF EXISTS PetTypes;
DROP TABLE IF EXISTS PartTimers;
DROP TABLE IF EXISTS FullTimers;
DROP TABLE IF EXISTS CareTakers;
DROP TABLE IF EXISTS PetOwners;
DROP TABLE IF EXISTS PCSadmins;
DROP TABLE IF EXISTS Consumers;
DROP TABLE IF EXISTS Users;

-- ==========
-- | Tables |
-- ==========
CREATE TABLE Users (
	username VARCHAR PRIMARY KEY,
	email VARCHAR,
	password VARCHAR NOT NULL,
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
	username VARCHAR PRIMARY KEY REFERENCES Consumers(username) ON DELETE CASCADE,
	manager VARCHAR NOT NULL REFERENCES PCSadmins(username)
);

CREATE TABLE FullTimers (
	username VARCHAR PRIMARY KEY REFERENCES CareTakers(username) ON DELETE cascade
);

CREATE TABLE PartTimers (
	username VARCHAR PRIMARY KEY REFERENCES CareTakers(username) ON DELETE cascade
);

CREATE TABLE PetTypes (
	category VARCHAR PRIMARY KEY NOT NULL,
	baseprice FLOAT(4) NOT NULL
);

CREATE TABLE AbleToCare (
	caretaker VARCHAR REFERENCES CareTakers(username),
	category VARCHAR REFERENCES PetTypes(category),
	feeperday FLOAT(4) NOT NULL,
	PRIMARY KEY(caretaker, category) 
);

CREATE TABLE Pets (
	petowner VARCHAR REFERENCES PetOwners(username) ON DELETE CASCADE,
	petname VARCHAR NOT NULL,
	profile VARCHAR NOT NULL,
	specialReq VARCHAR,
	category VARCHAR NOT NULL REFERENCES PetTypes(category),
	PRIMARY KEY (petowner, petname)
);


CREATE TABLE Availability (
	caretaker VARCHAR REFERENCES CareTakers(username) ON DELETE CASCADE,
	avail DATE NOT NULL,
	PRIMARY KEY (caretaker, avail)
);


CREATE TABLE Bids (
	petowner VARCHAR NOT NULL,
	petname VARCHAR NOT NULL,
	caretaker VARCHAR NOT NULL,
	avail DATE NOT NULL,
	
	edate DATE NOT NULL, 
	transferType VARCHAR NOT NULL, 
    paymentType VARCHAR NOT NULL CHECK(paymentType='creditcard' OR paymentType='cash'), 
    price FLOAT(4) NOT NULL,
    isPaid BOOLEAN NOT NULL, 
    status CHAR(1) NOT NULL CHECK(status='a' OR status='p' OR status='r'), /* a-accepted, p-pending, r-rejected */
    review VARCHAR, 
    rating INTEGER CHECK(rating>=1 AND rating <=5),
    
    FOREIGN KEY(petowner, petname) REFERENCES Pets(petowner, petname),
	FOREIGN KEY(caretaker, avail) REFERENCES Availability(caretaker, avail) ON DELETE CASCADE,
	PRIMARY KEY(petowner, petname, caretaker, avail)
	
);

CREATE TABLE InvalidatedBids (
	petowner VARCHAR NOT NULL,
	petname VARCHAR NOT NULL,
	caretaker VARCHAR NOT NULL,
	sdate DATE NOT NULL,
	
	edate DATE NOT NULL, 
	transferType VARCHAR NOT NULL, 
    paymentType VARCHAR NOT NULL CHECK(paymentType='creditcard' OR paymentType='cash'), 
    price FLOAT(4) NOT NULL,
    
    FOREIGN KEY(petowner, petname) REFERENCES Pets(petowner, petname),
	FOREIGN KEY(caretaker) REFERENCES CareTakers(username),
	PRIMARY KEY(petowner, petname, caretaker)
	
);


-- ============================
-- |   Procedures/Functions   |
-- ============================

-- Registration
-- ============


CREATE OR REPLACE PROCEDURE
addPCSadmin(username VARCHAR, email VARCHAR, password VARCHAR, profile VARCHAR,
		address VARCHAR, phone INTEGER) AS 
$$ 
	BEGIN 
		IF username NOT IN (SELECT U.username FROM Users U) THEN
			INSERT INTO Users VALUES(username, email, password, profile, address, phone);
		END IF;
		INSERT INTO PCSadmins VALUES(username);
	END; $$
LANGUAGE plpgsql;

CREATE OR REPLACE PROCEDURE
addPetOwner(username VARCHAR, email VARCHAR, password VARCHAR, profile VARCHAR,
		address VARCHAR, phone INTEGER, creditcard INTEGER, bankacc INTEGER) AS 
$$ 
	BEGIN 
		IF username NOT IN (SELECT U.username FROM Users U) THEN
			INSERT INTO Users VALUES(username, email, password, profile, address, phone);
		END IF;
		IF username NOT IN (SELECT C.username FROM Consumers C) THEN
			INSERT INTO Consumers VALUES(username, creditcard, bankacc);
		END IF;
		INSERT INTO PetOwners VALUES(username);
	END; $$
LANGUAGE plpgsql;

CREATE OR REPLACE PROCEDURE
addCareTaker(username VARCHAR, email VARCHAR, password VARCHAR, profile VARCHAR,
		address VARCHAR, phone INTEGER, creditcard INTEGER, bankacc INTEGER, isPartTime BOOLEAN, manager VARCHAR) AS 
$$ 
	BEGIN 
		IF username NOT IN (SELECT U.username FROM Users U) THEN
			INSERT INTO Users VALUES(username, email, password, profile, address, phone);
		END IF;
		IF username NOT IN (SELECT C.username FROM Consumers C) THEN
			INSERT INTO Consumers VALUES(username, creditcard, bankacc);
		END IF;
		INSERT INTO Caretakers VALUES(username, manager);
		IF isPartTime THEN
			INSERT INTO PartTimers VALUES(username);
		END IF;
	END; $$
LANGUAGE plpgsql;

CREATE OR REPLACE PROCEDURE
addAvailableDates(ctname VARCHAR, startdate DATE, enddate DATE) AS
$$ 
	DECLARE availDate DATE;
	BEGIN 
		availDate := startdate;
		WHILE availDate<= enddate LOOP 
			IF availDate NOT IN (SELECT A.avail FROM Availability A WHERE A.caretaker = ctname) THEN 
				INSERT INTO Availability VALUES(ctname, availDate);
			END IF;
			availDate := availDate + 1;
		END LOOP;
	END; $$
LANGUAGE plpgsql;


-- Checking/Updating fees per day
-- ==============================

-- Trigger for base fees for pet types
DROP TRIGGER IF EXISTS updateFeePerDay ON PetTypes; 
CREATE OR REPLACE FUNCTION updateBasePrice()
RETURNS TRIGGER AS $$ 
	BEGIN 
		UPDATE AbleToCare SET feeperday = NEW.baseprice
		WHERE category=NEW.category 
			AND (feeperday<NEW.baseprice OR feeperday>(SELECT computeMaxPriceMultiplier(caretaker))*NEW.baseprice);
		RETURN NEW; 
	END; $$ 
LANGUAGE plpgsql; 

CREATE TRIGGER updateFeePerDay
AFTER UPDATE ON PetTypes
FOR EACH ROW EXECUTE PROCEDURE updateBasePrice();

-- Caretakers' price setting
CREATE OR REPLACE FUNCTION mapAvgRatingToMultiplier(avgratings NUMERIC)
RETURNS NUMERIC AS $$
	BEGIN
		IF avgratings>4.7 THEN
			RETURN 1.2;
		END IF;
		IF avgratings>4.0 THEN
			RETURN 1.1;
		END IF;
		RETURN 1.0;
	END; $$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION computeMaxPriceMultiplier(ctname VARCHAR)
RETURNS NUMERIC AS $$
	DECLARE avgratings NUMERIC;
	BEGIN
		IF ctname NOT IN (SELECT caretaker FROM Bids) THEN
			RETURN 1.0;
		END IF;
		SELECT AVG(B.rating) INTO avgratings FROM Bids B WHERE B.caretaker=ctname;
		RETURN (SELECT mapAvgRatingToMultiplier(avgratings));
	END; $$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION computeUpdatedMaxPriceMultiplier(ctname VARCHAR, newrating INTEGER)
RETURNS NUMERIC AS $$
	DECLARE sumratings NUMERIC;
	DECLARE numratings NUMERIC;
	DECLARE avgratings NUMERIC;
	BEGIN
		IF ctname NOT IN (SELECT caretaker FROM Bids) THEN
			RETURN (SELECT mapAvgRatingToMultiplier(newrating));
		END IF;
		SELECT SUM(B.rating) INTO sumratings
			FROM Bids B WHERE B.caretaker=ctname;
		SELECT COUNT(B.rating) INTO numratings
			FROM Bids B WHERE B.caretaker=ctname;		
		sumratings := sumratings + newrating;
		numratings := numratings + 1;
		avgratings := sumratings/numratings;
		RETURN (SELECT mapAvgRatingToMultiplier(avgratings));
	END; $$
LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS checkIsValidFee ON AbleToCare; 
CREATE OR REPLACE FUNCTION isValidFee()
RETURNS TRIGGER AS $$
	DECLARE basefee NUMERIC;
	BEGIN 
		SELECT P.baseprice INTO basefee FROM PetTypes P WHERE P.category=NEW.category;
		IF NEW.feeperday<basefee THEN 
			RAISE EXCEPTION 'fee per day cannot be lower than base price';
			RETURN NULL;
		END IF;
		IF NEW.feeperday>(SELECT computeMaxPriceMultiplier(NEW.caretaker))*basefee THEN
			RAISE EXCEPTION 'fee per day cannot be above upper limit';
			RETURN NULL;
		END IF;
		RETURN NEW;
	END; $$
LANGUAGE plpgsql;

CREATE TRIGGER checkIsValidFee
BEFORE INSERT OR UPDATE ON AbleToCare
FOR EACH ROW EXECUTE PROCEDURE isValidFee();



-- Check max pets allowed by caretaker
-- ===================================

CREATE OR REPLACE FUNCTION computeMaxPet(ctname VARCHAR)
RETURNS INTEGER AS $$
	DECLARE avgratings NUMERIC;
	BEGIN
		IF ctname NOT IN (SELECT C.username FROM CareTakers C) THEN
			RAISE EXCEPTION 'no such caretaker in database';
			RETURN -1;
		END IF;
		IF ctname NOT IN (SELECT P.username FROM PartTimers P) THEN
			RETURN 5;
		END IF;
		SELECT AVG(B.rating) INTO avgratings FROM Bids B WHERE B.caretaker=ctname;
		IF avgratings>4.7 THEN
			RETURN 5;
		END IF;
		IF avgratings>4.0 THEN
			RETURN 4;
		END IF;
		IF avgratings>3.0 THEN
			RETURN 3;
		END IF;
		RETURN 2.0;
	END; $$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION computeUpdatedMaxPet(ctname VARCHAR, newrating INTEGER)
RETURNS INTEGER AS $$
	DECLARE sumratings NUMERIC;
	DECLARE numratings NUMERIC;
	DECLARE avgratings NUMERIC;
	BEGIN
		IF ctname NOT IN (SELECT C.username FROM CareTakers C) THEN
			RAISE EXCEPTION 'no such caretaker in database';
			RETURN -1;
		END IF;
		IF ctname NOT IN (SELECT P.username FROM PartTimers P) THEN
			RETURN 5;
		END IF;
		SELECT SUM(B.rating) INTO sumratings
			FROM Bids B WHERE B.caretaker=ctname;
		SELECT COUNT(B.rating) INTO numratings
			FROM Bids B WHERE B.caretaker=ctname;		
		sumratings := sumratings + newrating;
		numratings := numratings + 1;
		avgratings := sumratings/numratings;
		IF avgratings>4.7 THEN
			RETURN 5;
		END IF;
		IF avgratings>4.0 THEN
			RETURN 4;
		END IF;
		IF avgratings>3.0 THEN
			RETURN 3;
		END IF;
		RETURN 2.0;
	END; $$
LANGUAGE plpgsql;



-- Check caretaker availability
-- ============================

CREATE OR REPLACE FUNCTION isAvailable(ctname VARCHAR, datebooked DATE, maxpet INTEGER DEFAULT NULL)
RETURNS BOOLEAN AS $$
	BEGIN
		IF maxpet IS NULL THEN 
			maxpet := (SELECT computeMaxPet(ctname));
		END IF;
		RETURN (SELECT COUNT(*)
					FROM Bids B
					WHERE B.caretaker=ctname AND B.avail=datebooked AND B.status='a'
					)<maxpet
				AND 
				datebooked IN (SELECT A.avail FROM Availability A WHERE A.caretaker=ctname);
	END; $$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION getBasePrice(petcategory VARCHAR)
RETURNS NUMERIC AS $$
	BEGIN
		RETURN (SELECT P.baseprice FROM PetTypes P WHERE P.category=petcategory);
	END; $$
LANGUAGE plpgsql;



-- Managing bids
-- =============

CREATE OR REPLACE PROCEDURE enterBid(petowner VARCHAR, petname VARCHAR, 
			caretaker VARCHAR, sdate DATE, edate DATE, transfertype VARCHAR, 
			paymenttype VARCHAR, price NUMERIC) AS $$
	DECLARE availdate DATE;
	BEGIN 
		availdate := sdate;
		WHILE availdate <= edate LOOP 
			INSERT INTO Bids VALUES(petowner, petname, caretaker, availdate,
					edate, transfertype, paymenttype, price, FALSE,'p', NULL, NULL);
			availdate := availdate + 1;
		END LOOP;
	END; $$
LANGUAGE plpgsql;


CREATE OR REPLACE PROCEDURE approveRejectBid(POname VARCHAR, nameOfPet VARCHAR, 
			CTname VARCHAR, availdate DATE, approvereject CHAR(1)) AS $$
	DECLARE enddate DATE;
	BEGIN 
		enddate := (SELECT B.edate 
								FROM Bids B 
								WHERE B.petowner=POname AND B.petname=nameOfPet 
									AND B.caretaker=CTname AND B.avail=availDate);
		UPDATE Bids SET status=approvereject
			WHERE petowner=POname AND petname=nameOfPet AND caretaker=CTname
				AND edate = enddate;
	END; $$
LANGUAGE plpgsql;

CREATE OR REPLACE PROCEDURE rateCareTaker(POname VARCHAR, nameOfPet VARCHAR,
			CTname VARCHAR, availdate DATE, givenrating INTEGER, givenreview VARCHAR) AS $$
	DECLARE enddate DATE;
	BEGIN 
		enddate := (SELECT B.edate 
					FROM Bids B 
					WHERE B.petowner=POname AND B.petname=nameOfPet 
						AND B.caretaker=CTname AND B.avail=availdate);
		/*Only update ratings and reviews of those where avail=edate*/
		UPDATE Bids SET rating=givenrating
			WHERE petowner=POname AND petname=nameOfPet AND caretaker=CTname 
				AND edate=enddate AND avail=edate;
		UPDATE Bids SET review=givenreview
			WHERE petowner=POname AND petname=nameOfPet AND caretaker=CTname 
				AND edate=enddate AND avail=edate;
	END; $$
LANGUAGE plpgsql;


DROP TRIGGER IF EXISTS checkIsValidBid ON Bids; 
CREATE OR REPLACE FUNCTION isValidBid()
RETURNS TRIGGER AS $$
	DECLARE minfee NUMERIC;
	DECLARE petcategory VARCHAR;
	DECLARE oldmult NUMERIC;
	DECLARE newmult NUMERIC;
	DECLARE isupdate BOOLEAN;
	DECLARE oldmaxpet INTEGER;
	DECLARE newmaxpet INTEGER;
	BEGIN
		isupdate := (OLD.petowner IS NOT NULL AND OLD.petname IS NOT NULL AND OLD.caretaker IS NOT NULL AND OLD.avail IS NOT NULL);
		IF NOT isupdate THEN
			IF NOT (SELECT isAvailable(NEW.caretaker, NEW.avail)) THEN
				RAISE EXCEPTION 'caretaker is unavailable on that date';
				RETURN NULL;
			END IF;
			SELECT P.category INTO petcategory 
				FROM Pets P WHERE P.petowner=NEW.petowner AND P.petname=NEW.petname;
			IF petcategory NOT IN (SELECT A.category FROM AbleToCare A WHERE A.caretaker=NEW.caretaker) THEN
				RAISE EXCEPTION 'caretaker is unable to care for this pet type';
				RETURN NULL;
			END IF;
			SELECT A.feeperday INTO minfee 
				FROM AbleToCare A WHERE A.caretaker=NEW.caretaker AND A.category=petcategory;
			IF NEW.price<minfee THEN
				RAISE EXCEPTION 'bid price is below caretakers fee per day for that pet';
				RETURN NULL;
			END IF;
			IF NEW.status!='p' OR NEW.rating IS NOT NULL THEN 
				RAISE EXCEPTION 'initial status and rating for bid should be pending and null';
				RETURN NULL;
			END IF;
			IF EXISTS (SELECT * FROM Bids B WHERE B.petowner=NEW.petowner AND B.petname=NEW.petname 
					AND B.avail=NEW.avail AND B.status='a') THEN 
				RAISE EXCEPTION 'pet already has an existing appointment with another caretaker on this date';
				RETURN NULL;
			END IF;
			RETURN NEW;
		END IF;
		/*Is an update*/
		IF NEW.status!=OLD.status THEN /*status update*/
		/*When updating to approve bid*/
			IF NEW.status='a' THEN 
				/*When updating to approve a bid, check if number of pets taken for that day is within max pet limit*/
				IF NOT (SELECT isAvailable(NEW.caretaker, NEW.avail)) THEN
					RAISE EXCEPTION 'this date is fully booked, cannot approve';
					RETURN NULL;
				END IF;
				IF OLD.status='r' THEN /*check the bid is not already rejected*/
					RAISE EXCEPTION 'cannot approve already rejected bid';
					RETURN NULL;
				END IF;
				/*When updating to approve bid, all other bids for the pet on this same date will be rejected*/
				/*Note when we reject a bid on an avail date, all other similar bids with same edate should also be rejected*/
				UPDATE Bids SET status='r' 
					WHERE petowner=NEW.petowner AND petname=NEW.petname AND caretaker!=NEW.caretaker 
						AND NEW.edate IN 
							(SELECT B.edate 
								FROM Bids B 
								WHERE B.petowner=NEW.petowner AND B.petname=NEW.petname AND B.status!='r' AND B.avail=NEW.avail);
				/*If caretaker becomes fully booked, all other bids pending for this caretaker on this date will be rejected*/
				/*Note when we reject a bid on an avail date, all other similar bids with same edate should also be rejected*/
				IF (SELECT COUNT(*)
						FROM Bids B
						WHERE B.caretaker=NEW.caretaker AND B.avail=NEW.avail AND B.status='a'
						)=(SELECT computeMaxPet(NEW.caretaker)-1) THEN
					UPDATE Bids SET status='r' 
						WHERE caretaker=NEW.caretaker AND edate!=NEW.edate AND status='p'
							AND edate IN 
								(SELECT B.edate 
									FROM Bids B 
									WHERE B.caretaker=NEW.caretaker AND B.avail=NEW.avail AND B.status='p');				
				END IF;
			END IF;
		END IF;
		/*Update on rating*/
		IF NEW.rating!=OLD.rating OR (NEW.rating IS NOT NULL AND OLD.rating IS NULL) OR (NEW.rating IS NULL AND OLD.rating IS NOT NULL) THEN
			IF NEW.rating IS NOT NULL THEN 		
				IF OLD.status!='a' THEN 
					RAISE EXCEPTION 'cannot update ratings on bid that is not approved';
					RETURN NULL;
				END IF;
				IF OLD.rating IS NOT NULL THEN
					RAISE EXCEPTION 'cannot update rating that has been set already';
					RETURN NULL;
				END IF;
				/*Update fees per day if average ratings change*/
				SELECT computeMaxPriceMultiplier(OLD.caretaker) INTO oldmult;
				SELECT computeUpdatedMaxPriceMultiplier(OLD.caretaker, NEW.rating) INTO newmult;
				IF newmult!=oldmult THEN
					UPDATE AbleToCare SET feeperday=(SELECT getBasePrice(category))
						WHERE caretaker=OLD.caretaker AND feeperday>newmult*(SELECT getBasePrice(category));
				END IF;
				IF OLD.caretaker IN (SELECT P.username FROM PartTimers P) THEN /*Only part-timers*/
					SELECT computeMaxPet(OLD.caretaker) INTO oldmaxpet;
					SELECT computeUpdatedMaxPet(OLD.caretaker, NEW.rating) INTO newmaxpet;
					/*if max pets decrease, some previously available dates no longer available
					 *  then have to update pending bids on unavailable dates*/
					IF newmaxpet<oldmaxpet THEN 
						UPDATE Bids SET status='r'
							WHERE caretaker=OLD.caretaker AND status='p' 
								AND edate IN 
								(SELECT B.edate 
									FROM Bids B 
									WHERE B.caretaker=caretaker AND B.status='p' AND NOT (SELECT isAvailable(caretaker, avail, newmaxpet)) );	
					END IF;
				END IF;
			ELSE /*new rating is null and old rating is not null*/
				RAISE EXCEPTION 'cannot update rating that has been set already to null';
				RETURN NULL;			
			END IF;
		END IF;
		RETURN NEW;
	END; $$
LANGUAGE plpgsql;

CREATE TRIGGER checkIsValidBid
BEFORE INSERT OR UPDATE ON Bids
FOR EACH ROW EXECUTE PROCEDURE isValidBid();



-- Managing leaves
-- ===============

CREATE OR REPLACE PROCEDURE applyLeave(ctname VARCHAR, startdate DATE, enddate DATE) AS $$
	DECLARE leaveDate DATE;
	BEGIN 
		leaveDate := startdate;
		WHILE leaveDate<= enddate LOOP 
			DELETE FROM Availability WHERE caretaker=ctname AND avail=leaveDate;
			leaveDate := leaveDate + 1;
		END LOOP;
	END; $$
LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS checkIsValidLeave ON Availability;
CREATE OR REPLACE FUNCTION isValidLeave()
RETURNS TRIGGER AS $$
	DECLARE yearofleave INTEGER;
	DECLARE isvalid150days BOOLEAN;
	DECLARE error VARCHAR;
	BEGIN 
		IF OLD.caretaker IN (SELECT P.username FROM PartTimers P) THEN 
			RAISE EXCEPTION 'part-timers cannot take leave';
			RETURN NULL;
		END IF;
		IF OLD.avail IN (SELECT B.avail FROM Bids B WHERE B.caretaker=OLD.caretaker AND B.status='a') THEN
			RAISE EXCEPTION 'caretaker is occupied that day';
			RETURN NULL;
		END IF;
		yearofleave := (SELECT EXTRACT(YEAR FROM OLD.avail));
		isvalid150days := (SELECT COUNT(*)>=2 OR (COUNT(*)>=1 AND MAX(D.enddate-D.startdate+1)>=300)
					FROM
						(SELECT X.avail AS startdate, X.avail+MIN(Y.avail-X.avail) AS enddate
						FROM 
							(SELECT AV.avail
							FROM Availability AV
							WHERE AV.caretaker=OLD.caretaker AND AV.avail!=CAST(OLD.avail AS DATE) 
								AND (SELECT EXTRACT(YEAR FROM AV.avail))=yearofleave
								AND AV.avail-1 NOT IN (SELECT A.avail 
														FROM Availability A 
														WHERE A.caretaker=OLD.caretaker AND A.avail!=CAST(OLD.avail AS DATE) 
														AND (SELECT EXTRACT(YEAR FROM A.avail))=yearofleave) ) X
							INNER JOIN 
							(SELECT AV.avail
							FROM Availability AV
							WHERE AV.caretaker=OLD.caretaker AND AV.avail!=CAST(OLD.avail AS DATE) 
								AND (SELECT EXTRACT(YEAR FROM AV.avail))=yearofleave
								AND AV.avail+1 NOT IN (SELECT A.avail 
														FROM Availability A 
														WHERE A.caretaker=OLD.caretaker AND A.avail!=CAST(OLD.avail AS DATE) 
														AND (SELECT EXTRACT(YEAR FROM A.avail))=yearofleave) ) Y
						ON X.avail<= Y.avail
						GROUP BY X.avail ) D
					WHERE D.enddate-D.startdate+1>=150 );
		IF NOT isvalid150days THEN
			RAISE EXCEPTION 'violates 150 days constraint';
			RETURN NULL;
		END IF;
		
		/*place bids affected by leave into invalidated bids*/
		INSERT INTO InvalidatedBids(petowner, petname, caretaker, sdate, edate, transferType, paymentType, price)
		SELECT X.petowner, X.petname, X.caretaker, X.avail, X.edate, Y.transferType, Y.paymentType, Y.price
		FROM
			(SELECT D.petowner, D.petname, D.caretaker, MIN(D.avail) AS avail, D.edate
			FROM Bids D
			WHERE caretaker=OLD.caretaker AND status='p' 
				AND edate IN 
				(SELECT B.edate 
				FROM Bids B 
				WHERE B.caretaker=caretaker AND B.status='p' AND B.avail=OLD.avail )
			GROUP BY D.petowner, D.petname, D.caretaker, D.edate) X
			NATURAL JOIN Bids Y
		;
		
		/*delete bids on leave dates*/
		
		DELETE FROM Bids
			WHERE caretaker=OLD.caretaker AND status='p' 
				AND edate IN 
				(SELECT B.edate 
				FROM Bids B 
				WHERE B.caretaker=caretaker AND B.status='p' AND B.avail=OLD.avail );
		RETURN OLD;
	END; $$
LANGUAGE plpgsql;

						
CREATE TRIGGER checkIsValidLeave
BEFORE DELETE ON Availability
FOR EACH ROW EXECUTE PROCEDURE isValidLeave();
