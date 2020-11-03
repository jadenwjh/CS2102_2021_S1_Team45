DROP TABLE IF EXISTS BidsWithoutPetOwner;
DROP TABLE IF EXISTS InvalidatedBids;
DROP TABLE IF EXISTS Bids;
DROP TABLE IF EXISTS Availability;
DROP TABLE IF EXISTS Pets;
DROP TABLE IF EXISTS AbleToCare;
DROP TABLE IF EXISTS PetTypes;
DROP TABLE IF EXISTS PartTimers;
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


CREATE TABLE PartTimers (
	username VARCHAR PRIMARY KEY REFERENCES CareTakers(username) ON DELETE CASCADE
);

CREATE TABLE PetTypes (
	category VARCHAR PRIMARY KEY NOT NULL,
	baseprice FLOAT(4) NOT NULL
);

CREATE TABLE AbleToCare (
	caretaker VARCHAR REFERENCES CareTakers(username) ON DELETE CASCADE,
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
    
    FOREIGN KEY(petowner, petname) REFERENCES Pets(petowner, petname) ON DELETE CASCADE,
	FOREIGN KEY(caretaker, avail) REFERENCES Availability(caretaker, avail) ON DELETE CASCADE,
	PRIMARY KEY(petowner, petname, caretaker, avail)
	
);

CREATE TABLE BidsWithoutPetOwner(
	petowner VARCHAR NOT NULL,
	petname VARCHAR NOT NULL,
	caretaker VARCHAR NOT NULL,
	avail DATE NOT NULL,
	
	edate DATE NOT NULL, 
	transferType VARCHAR NOT NULL, 
    paymentType VARCHAR NOT NULL CHECK(paymentType='creditcard' OR paymentType='cash'), 
    price FLOAT(4) NOT NULL,
    isPaid BOOLEAN NOT NULL, 
    status CHAR(1) NOT NULL CHECK(status='a'), /* a-accepted*/
    review VARCHAR, 
    rating INTEGER CHECK(rating>=1 AND rating <=5),
    
	FOREIGN KEY(caretaker, avail) REFERENCES Availability(caretaker, avail) ON DELETE CASCADE,
	PRIMARY KEY(petowner, petname, caretaker, avail)

);

CREATE TABLE InvalidatedBids ( /*For pet owners to check their bids rejected due to application of leave or deletion of caretakers*/
	petowner VARCHAR NOT NULL,
	petname VARCHAR NOT NULL,
	caretaker VARCHAR NOT NULL,
	sdate DATE NOT NULL,
	
	edate DATE NOT NULL, 
	transferType VARCHAR NOT NULL, 
    paymentType VARCHAR NOT NULL CHECK(paymentType='creditcard' OR paymentType='cash'), 
    price FLOAT(4) NOT NULL,
    
    FOREIGN KEY(petowner, petname) REFERENCES Pets(petowner, petname) ON DELETE CASCADE,
	PRIMARY KEY(petowner, petname, caretaker, edate)
);

-------------------------------------------------------------------
-----------------Key Helper Functions for Triggers-----------------
-------------------------------------------------------------------


CREATE OR REPLACE FUNCTION currentDate() /*To track today's date, currently uses dummy*/
RETURNS DATE AS $$
	BEGIN
		RETURN (SELECT CURRENT_DATE);   /* dummy current date. For live application, it should returns (SELECT CURRENT_DATE)*/
	END; $$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION combinedBids() /*To be used only in triggers*/
RETURNS TABLE(
	petowner VARCHAR,
	petname VARCHAR,
	caretaker VARCHAR,
	avail DATE,
	
	edate DATE, 
	transferType VARCHAR, 
    paymentType VARCHAR, 
    price FLOAT(4),
    isPaid BOOLEAN, 
    status CHAR(1),
    review VARCHAR, 
    rating INTEGER
	) AS $$
	BEGIN
		RETURN QUERY (SELECT * FROM Bids UNION SELECT * FROM BidsWithoutPetOwner);
	END;$$
LANGUAGE plpgsql;

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
		IF ctname NOT IN (SELECT caretaker FROM combinedBids() ) THEN
			RETURN 1.0;
		END IF;
		SELECT AVG(B.rating) INTO avgratings FROM combinedBids() B WHERE B.caretaker=ctname;
		RETURN (SELECT mapAvgRatingToMultiplier(avgratings));
	END; $$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION computeUpdatedMaxPriceMultiplier(ctname VARCHAR, newrating INTEGER)
RETURNS NUMERIC AS $$
	DECLARE sumratings NUMERIC;
	DECLARE numratings NUMERIC;
	DECLARE avgratings NUMERIC;
	BEGIN
		IF ctname NOT IN (SELECT caretaker FROM combinedBids() ) THEN
			RETURN (SELECT mapAvgRatingToMultiplier(newrating));
		END IF;
		SELECT SUM(B.rating) INTO sumratings
			FROM combinedBids() B WHERE B.caretaker=ctname;
		SELECT COUNT(B.rating) INTO numratings
			FROM combinedBids() B WHERE B.caretaker=ctname;		
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
		SELECT AVG(B.rating) INTO avgratings FROM combinedBids() B WHERE B.caretaker=ctname;
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
			FROM combinedBids() B WHERE B.caretaker=ctname;
		SELECT COUNT(B.rating) INTO numratings
			FROM combinedBids() B WHERE B.caretaker=ctname;		
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

DROP FUNCTION IF EXISTS isAvailable(ctname VARCHAR, datebooked DATE, maxpetslots INTEGER);
CREATE OR REPLACE FUNCTION isAvailable(ctname VARCHAR, datebooked DATE, maxpet INTEGER DEFAULT NULL)
RETURNS BOOLEAN AS $$
	BEGIN
		IF maxpet IS NULL THEN 
			maxpet := (SELECT computeMaxPet(ctname));
		END IF;
		RETURN (SELECT COUNT(*)
					FROM combinedBids() B
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

------------------------------------------
-----------------Triggers-----------------
------------------------------------------


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
		IF NOT isupdate THEN /*Insertion of a new bid*/
			IF NEW.avail<=(SELECT currentDate()) THEN
				RAISE EXCEPTION 'bids should only be for dates tomorrow onwards (i.e. greater than current date)';
				RETURN NULL;
			END IF;
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
			IF EXISTS (SELECT * FROM combinedBids() B WHERE B.petowner=NEW.petowner AND B.petname=NEW.petname 
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
				IF NEW.avail<=(SELECT currentDate()) THEN
					RAISE EXCEPTION 'approved bids should only be for dates tomorrow onwards (i.e. greater than current date)';
					RETURN NULL;
				END IF;
				/*When updating to approve a bid, check if number of pets taken for that day is within max pet limit*/
				IF NOT (SELECT isAvailable(NEW.caretaker, NEW.avail)) THEN
					RAISE EXCEPTION 'this date is fully booked, cannot approve';
					RETURN NULL;
				END IF;
				IF OLD.status='r' THEN /*check the bid is not already rejected*/
					RAISE EXCEPTION 'cannot approve already rejected bid';
					RETURN NULL;
				END IF;
				/*When updating to approve bid, all other bids pending for the pet on this same date will be rejected*/
				/*Note when we reject a bid on an avail date, all other similar bids with same edate should also be rejected*/
				UPDATE Bids SET status='r' 
					WHERE petowner=NEW.petowner AND petname=NEW.petname AND caretaker!=NEW.caretaker AND status='p'
						AND NEW.edate IN 
							(SELECT B.edate 
								FROM Bids B 
								WHERE B.petowner=NEW.petowner AND B.petname=NEW.petname AND B.status='p' AND B.avail=NEW.avail);
				/*If caretaker becomes fully booked, all other bids pending for this caretaker on this date will be rejected*/
				/*Note when we reject a bid on an avail date, all other similar bids with same edate should also be rejected*/
				IF (SELECT COUNT(*)
						FROM combinedBids() B
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

DROP TRIGGER IF EXISTS checkIsValidLeave ON Availability;
CREATE OR REPLACE FUNCTION isValidLeave()
RETURNS TRIGGER AS $$
	DECLARE yearofleave INTEGER;
	DECLARE isvalid150days BOOLEAN;
	DECLARE error VARCHAR;
	BEGIN 
		IF OLD.caretaker NOT IN (SELECT username FROM caretakers) THEN /*This means delete cascade from caretakers is called*/
			RETURN OLD; /*bypass all the validLeave conditions*/
		END IF; /*This condition to ensure on delete cascade from caretakers still work*/
		IF OLD.caretaker IN (SELECT P.username FROM PartTimers P) THEN 
			RAISE EXCEPTION 'part-timers cannot take leave';
			RETURN NULL;
		END IF;
		IF OLD.avail IN (SELECT B.avail FROM combinedBids() B WHERE B.caretaker=OLD.caretaker AND B.status='a') THEN
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
		
		/*place pending or rejected bids affected by leave into invalidated bids*/

		INSERT INTO InvalidatedBids(petowner, petname, caretaker, sdate, edate, transferType, paymentType, price)
		SELECT X.petowner, X.petname, X.caretaker, X.avail, X.edate, Y.transferType, Y.paymentType, Y.price
		FROM
			(SELECT D.petowner, D.petname, D.caretaker, MIN(D.avail) AS avail, D.edate
			FROM Bids D
			WHERE caretaker=OLD.caretaker AND status!='a' 
				AND edate IN 
				(SELECT B.edate 
				FROM Bids B 
				WHERE B.caretaker=caretaker AND B.status!='a' AND B.avail=OLD.avail )
			GROUP BY D.petowner, D.petname, D.caretaker, D.edate) X
			NATURAL JOIN Bids Y
		WHERE NOT EXISTS (SELECT * 
							FROM InvalidatedBids I 
							WHERE I.petowner=X.petowner AND I.petname=X.petname 
							AND I.caretaker=X.caretaker AND I.edate = X.edate)
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

DROP TRIGGER IF EXISTS deleteBid ON Bids; 
CREATE OR REPLACE FUNCTION deletingBid()
RETURNS TRIGGER AS $$ 
	BEGIN 
		IF OLD.status = 'a' AND OLD.caretaker IN (SELECT username FROM Caretakers) THEN 
			INSERT INTO BidsWithoutPetOwner VALUES (OLD.petowner, OLD.petname, OLD.caretaker, OLD.avail, 
					OLD.edate, OLD.transferType, OLD.paymentType, OLD.price, OLD.isPaid, OLD.status, 
					OLD.review, OLD.rating);
		END IF;
		RETURN OLD;
	END; $$ 
LANGUAGE plpgsql; 

CREATE TRIGGER deleteBid
BEFORE DELETE ON Bids
FOR EACH ROW EXECUTE PROCEDURE deletingBid();

DROP TRIGGER IF EXISTS checkIsDeletablePetOwner ON PetOwners; 
CREATE OR REPLACE FUNCTION isDeletablePetOwner()
RETURNS TRIGGER AS $$ 
	BEGIN 
		IF EXISTS (SELECT * FROM Bids B 
				WHERE B.petowner=OLD.username AND B.status='a' AND (SELECT currentDate())<=B.avail) THEN
			RAISE EXCEPTION 'this petowner account is still being serviced by a caretaker in the future!';
			RETURN NULL;
		END IF;
		RETURN OLD;
	END; $$ 
LANGUAGE plpgsql; 


CREATE TRIGGER checkIsDeletablePetOwner
BEFORE DELETE ON PetOwners
FOR EACH ROW EXECUTE PROCEDURE isDeletablePetOwner();

DROP TRIGGER IF EXISTS checkIsDeletablePet ON Pets; 
CREATE OR REPLACE FUNCTION isDeletablePet()
RETURNS TRIGGER AS $$ 
	BEGIN 
		IF EXISTS (SELECT * FROM Bids B 
				WHERE B.petowner=OLD.petowner AND B.petname=OLD.petname AND B.status='a' AND (SELECT currentDate())<=B.avail) THEN
			RAISE EXCEPTION 'this pet account is still being serviced by a caretaker in the future!';
			RETURN NULL;
		END IF;
		RETURN OLD;
	END; $$ 
LANGUAGE plpgsql; 

CREATE TRIGGER checkIsDeletablePet
BEFORE DELETE ON Pets
FOR EACH ROW EXECUTE PROCEDURE isDeletablePet();


DROP TRIGGER IF EXISTS checkIsDeletableCareTaker ON CareTakers; 
CREATE OR REPLACE FUNCTION isDeletableCareTaker()
RETURNS TRIGGER AS $$ 
	BEGIN 
		IF EXISTS (SELECT * FROM combinedBids() B 
				WHERE B.caretaker=OLD.username AND B.status='a' AND (SELECT currentDate())<=B.avail) THEN
			RAISE EXCEPTION 'this caretaker is servicing a petowner in the future!';
			RETURN NULL;
		END IF;
		
		/*place pending or rejected bids affected by removal of caretaker into invalidated bids*/
		INSERT INTO InvalidatedBids(petowner, petname, caretaker, sdate, edate, transferType, paymentType, price)
		SELECT X.petowner, X.petname, X.caretaker, X.avail, X.edate, Y.transferType, Y.paymentType, Y.price
		FROM
			(SELECT D.petowner, D.petname, D.caretaker, MIN(D.avail) AS avail, D.edate
			FROM Bids D
			WHERE caretaker=OLD.username AND status!='a'
			GROUP BY D.petowner, D.petname, D.caretaker, D.edate) X
			NATURAL JOIN Bids Y
		WHERE NOT EXISTS (SELECT * 
							FROM InvalidatedBids I 
							WHERE I.petowner=X.petowner AND I.petname=X.petname 
							AND I.caretaker=X.caretaker AND I.edate = X.edate)
		;
		
		RETURN OLD;
	END; $$ 
LANGUAGE plpgsql;

CREATE TRIGGER checkIsDeletableCareTaker
BEFORE DELETE ON CareTakers
FOR EACH ROW EXECUTE PROCEDURE isDeletableCareTaker();

DROP TRIGGER IF EXISTS checkIsDeletableAdmin ON PCSAdmins; 
CREATE OR REPLACE FUNCTION isDeletableAdmin()
RETURNS TRIGGER AS $$ 
	BEGIN 
		IF EXISTS (SELECT * FROM CareTakers C
				WHERE C.manager=OLD.username) THEN
			RAISE EXCEPTION 'admin still manages caretakers';
			RETURN NULL;
		END IF;
		
		RETURN OLD;
	END; $$ 
LANGUAGE plpgsql;

CREATE TRIGGER checkIsDeletableAdmin
BEFORE DELETE ON PCSAdmins
FOR EACH ROW EXECUTE PROCEDURE isDeletableAdmin();


------------------------------------------
---------Procedures and Functions---------
------------------------------------------

CREATE OR REPLACE PROCEDURE
addPCSadmin(username VARCHAR, email VARCHAR, profile VARCHAR,
		address VARCHAR, phone INTEGER) AS 
$$ 
	BEGIN 
		IF username NOT IN (SELECT U.username FROM Users U) THEN
			INSERT INTO Users VALUES(username, email, profile, address, phone);
		END IF;
		INSERT INTO PCSadmins VALUES(username);
	END; $$
LANGUAGE plpgsql;

CREATE OR REPLACE PROCEDURE
addPetOwner(username VARCHAR, email VARCHAR, profile VARCHAR,
		address VARCHAR, phone INTEGER, creditcard INTEGER, bankacc INTEGER) AS 
$$ 
	BEGIN 
		IF username NOT IN (SELECT U.username FROM Users U) THEN
			INSERT INTO Users VALUES(username, email, profile, address, phone);
		END IF;
		IF username NOT IN (SELECT C.username FROM Consumers C) THEN
			INSERT INTO Consumers VALUES(username, creditcard, bankacc);
		END IF;
		INSERT INTO PetOwners VALUES(username);
	END; $$
LANGUAGE plpgsql;

CREATE OR REPLACE PROCEDURE
addCareTaker(username VARCHAR, email VARCHAR, profile VARCHAR,
		address VARCHAR, phone INTEGER, creditcard INTEGER, bankacc INTEGER, isPartTime BOOLEAN, manager VARCHAR) AS 
$$ 
	DECLARE sdate DATE;
	DECLARE edate DATE;

	BEGIN 
		IF username NOT IN (SELECT U.username FROM Users U) THEN
			INSERT INTO Users VALUES(username, email, profile, address, phone);
		END IF;
		IF username NOT IN (SELECT C.username FROM Consumers C) THEN
			INSERT INTO Consumers VALUES(username, creditcard, bankacc);
		END IF;
		INSERT INTO Caretakers VALUES(username, manager);
		IF isPartTime THEN
			INSERT INTO PartTimers VALUES(username);
		END IF;
		SELECT CURRENT_DATE INTO sdate;
		SELECT CURRENT_DATE + INTERVAL '2 year' – INTERVAL '1 day' INTO edate;
		IF isPartTime = false THEN 
			CALL addAvailableDates(username, sdate, edate);
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

CREATE OR REPLACE FUNCTION 
viewMySalary(myUsername VARCHAR, startofMonth DATE) 
RETURNS TABLE (caretaker VARCHAR, salary FLOAT) AS $$
	BEGIN 
		/*parttimers*/	
		IF myUsername IN (SELECT p.username FROM Parttimers p) THEN 
			RETURN QUERY (
			SELECT bb.caretaker, sum*0.75 AS ptsalary
			FROM (
				SELECT b.caretaker, SUM(price)
				FROM Bids b 
				WHERE b.avail < startofMonth + interval '1 month'
				AND b.avail >= startofMonth 
				AND b.isPaid = TRUE 
				AND b.status = 'a' 
				GROUP BY b.caretaker 

				UNION 

				SELECT bw.caretaker, SUM(price)
				FROM BidsWithoutPetOwner bw
				WHERE bw.avail < startofMonth + interval '1 month'
				AND bw.avail >= startofMonth
				AND bw.isPaid = TRUE 
				AND bw.status = 'a' 
				GROUP BY bw.caretaker
			) AS bb
			WHERE bb.caretaker = myUsername);
		END IF;

		/*fulltimer*/ 
		IF myUsername IN (SELECT c.username FROM Caretakers c EXCEPT (SELECT pt.username FROM Parttimers pt)) THEN 
			RETURN QUERY (
			SELECT bb.caretaker, 3000 + ((wage - 3000) * 0.8 ) AS FTsalary
			FROM (
				SELECT b.caretaker, COUNT(*) as petdays, SUM(price) as wage
				FROM Bids b 
				WHERE b.avail < startofMonth + interval '1 month'
				AND b.avail >= startofMonth
				AND b.isPaid = TRUE 
				AND b.status = 'a' 
				GROUP BY b.caretaker 

				UNION 

				SELECT bw.caretaker, COUNT(*) as petdays, SUM(price)
				FROM BidsWithoutPetOwner bw
				WHERE bw.avail < startofMonth + interval '1 month'
				AND bw.avail >= startofMonth 
				AND bw.isPaid = TRUE 
				AND bw.status = 'a' 
				GROUP BY bw.caretaker
			) AS bb
			WHERE petdays > 60 
			AND bb.caretaker = myUsername

			UNION 

			SELECT bb.caretaker, 3000 AS FTsalary
			FROM (
				SELECT b.caretaker, COUNT(*) as petdays, SUM(price) as wage
				FROM Bids b 
				WHERE b.avail < startofMonth + interval '1 month'
				AND b.avail >= startofMonth
				AND b.isPaid = TRUE 
				AND b.status = 'a' 
				GROUP BY b.caretaker 

				UNION 

				SELECT bw.caretaker, COUNT(*) as petdays, SUM(price)
				FROM BidsWithoutPetOwner bw
				WHERE bw.avail < startofMonth + interval '1 month'
				AND bw.avail >= startofMonth 
				AND bw.isPaid = TRUE 
				AND bw.status = 'a' 
				GROUP BY bw.caretaker 
			) AS bb
			WHERE petdays <= 60 
			AND bb.caretaker = myUsername);
		END IF; 
	END;  $$
LANGUAGE plpgsql;

