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
		RETURN (SELECT CAST('2019-12-31'AS DATE));   /* dummy current date. For live application, it should returns (SELECT CURRENT_DATE)*/
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
			IF NEW.caretaker=NEW.petowner THEN
				RAISE EXCEPTION 'a user who is both a caretaker and petowner cannot bid for his own services';
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
			IF NEW.status!='p' OR NEW.rating IS NOT NULL OR NEW.review IS NOT NULL THEN 
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
			IF OLD.avail!=OLD.edate THEN 
				RAISE EXCEPTION 'Can only rated once for each transaction, the availability should equal end date for the bid to be rated';
				RETURN NULL;
			END IF;			
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
		/*update on review*/
		IF NEW.review!=OLD.review OR (NEW.review IS NOT NULL AND OLD.review IS NULL) OR (NEW.review IS NULL AND OLD.review IS NOT NULL) THEN
			IF OLD.avail!=OLD.edate THEN 
				RAISE EXCEPTION 'Can only review once for each transaction, the availability should equal end date for the bid to be reviewed';
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
		ELSE
			SELECT currentDate() INTO sdate;
			SELECT CAST(sdate + INTERVAL '2 years' - INTERVAL '1 day' AS DATE) INTO edate;
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

CREATE OR REPLACE PROCEDURE updateIsPaid(POname VARCHAR, nameOfPet VARCHAR, 
			CTname VARCHAR, availdate DATE, ispaidval BOOLEAN) AS $$
	DECLARE enddate DATE;
	BEGIN 
		enddate := (SELECT B.edate 
								FROM Bids B 
								WHERE B.petowner=POname AND B.petname=nameOfPet 
									AND B.caretaker=CTname AND B.avail=availDate);
		UPDATE Bids SET isPaid=ispaidval
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

-- ------------------------------------------------------------
-- -----------------Sample Data and Test Cases-----------------
-- ------------------------------------------------------------

-- CALL addPCSadmin('master', 'master@hotmail.com', 'thebottomline', 'marina bay sands', 96667224);

-- CALL addPetOwner('john','john@gmail', 'hello', 'jurong', 92345332, 2131231, 34536435);
-- CALL addPetOwner('jack','jack@gmail', 'hello', 'jurong', 92232232, 65433546, 245325);
-- CALL addPetOwner('jane','jane@gmail', 'hello', 'tampines', 92345332, 35663456,4653445);
-- CALL addPetOwner('mary','mary@gmail', 'hello', 'hougang', 92345332, 4567574, 356434);
-- CALL addPetOwner('bill','bill@gmail', 'hello', 'toapayoh', 92345522, 47456754,254354);


-- CALL addCareTaker('bill','bill@gmail', 'hello', 'toapayoh', 92345522, 47456754, 254354, TRUE, 'master');
-- CALL addCareTaker('jean','jean@gmail', 'hello', 'marina', 97788522, 3456364, 2453243, FALSE, 'master');
-- CALL addCareTaker('amanda','amanda@gmail', 'hello', 'yishun', 97733292, 2345324,43224, FALSE, 'master');
-- CALL addCareTaker('germ','germ@gmail', 'hello', 'yishun', 97733292, 234525, 23141243, TRUE, 'master');
-- CALL addCareTaker('edmund','germ@gmail', 'hello', 'bandemeer', 97733292, 234524, 412334, TRUE, 'master');
-- CALL addCareTaker('linyi','linyi@gmail', 'hello', 'bandemeer', 97423292, 232224, 498334, TRUE, 'master');

-- CALL addAvailableDates('bill', '2020-01-01', '2021-03-28');
-- CALL addAvailableDates('germ', '2020-01-01', '2021-03-28');
-- CALL addAvailableDates('edmund', '2020-01-01', '2021-03-28');

-- INSERT INTO PetTypes VALUES('cat', 25);
-- INSERT INTO PetTypes VALUES('dog', 35);
-- INSERT INTO PetTypes VALUES('bird', 15);

-- INSERT INTO Pets VALUES('bill', 'wolfie', 'hi', NULL, 'dog');
-- INSERT INTO Pets VALUES('bill', 'meow', 'hi', NULL, 'cat');
-- INSERT INTO Pets VALUES('bill', 'birdie', 'hi', NULL, 'bird');
-- INSERT INTO Pets VALUES('john', 'wolfie', 'hi', NULL, 'dog');
-- INSERT INTO Pets VALUES('john', 'miao', 'hi', NULL, 'cat');
-- INSERT INTO Pets VALUES('john', 'birdie', 'hi', NULL, 'bird');
-- INSERT INTO Pets VALUES('jack', 'wolf', 'hi', NULL, 'dog');
-- INSERT INTO Pets VALUES('jack', 'meow', 'hi', NULL, 'cat');
-- INSERT INTO Pets VALUES('jack', 'birdb', 'hi', NULL, 'bird');
-- INSERT INTO Pets VALUES('jane', 'fierce', 'hi', NULL, 'dog');
-- INSERT INTO Pets VALUES('jane', 'kitty', 'hi', NULL, 'cat');
-- INSERT INTO Pets VALUES('jane', 'birdie', 'hi', NULL, 'bird');
-- INSERT INTO Pets VALUES('mary', 'canine', 'hi', NULL, 'dog');
-- INSERT INTO Pets VALUES('mary', 'miao', 'hi', NULL, 'cat');
-- INSERT INTO Pets VALUES('mary', 'chirp', 'hi', NULL, 'bird');

-- INSERT INTO AbleToCare VALUES('bill', 'cat', 25);
-- INSERT INTO AbleToCare VALUES('bill', 'dog', 35);
-- INSERT INTO AbleToCare VALUES('bill', 'bird', 15);
-- INSERT INTO AbleToCare VALUES('jean', 'cat', 25);
-- INSERT INTO AbleToCare VALUES('jean', 'dog', 35);
-- INSERT INTO AbleToCare VALUES('jean', 'bird', 15);
-- INSERT INTO AbleToCare VALUES('amanda', 'cat', 25);
-- INSERT INTO AbleToCare VALUES('amanda', 'dog', 35);
-- INSERT INTO AbleToCare VALUES('amanda', 'bird', 15);
-- INSERT INTO AbleToCare VALUES('germ', 'cat', 25);
-- INSERT INTO AbleToCare VALUES('germ', 'dog', 35);
-- INSERT INTO AbleToCare VALUES('germ', 'bird', 15);
-- INSERT INTO AbleToCare VALUES('edmund', 'cat', 25);
-- INSERT INTO AbleToCare VALUES('edmund', 'dog', 35);
-- INSERT INTO AbleToCare VALUES('edmund', 'bird', 15);
-- INSERT INTO AbleToCare VALUES('linyi', 'cat', 25);
-- INSERT INTO AbleToCare VALUES('linyi', 'dog', 35);
-- INSERT INTO AbleToCare VALUES('linyi', 'bird', 15);
-- /*
-- CALL enterBid('bill', 'wolfie', 'bill', '2020-01-03', '2020-01-10', 'pcs', 'cash', 100); --shouldnt be able to bid for himself
-- */
-- CALL enterBid('john', 'wolfie', 'bill', '2020-01-01','2020-01-15','pcs', 'cash', 40);
-- CALL enterBid('jane', 'kitty', 'bill', '2020-01-05','2020-01-16','pcs', 'cash', 40);
-- CALL enterBid('jack', 'meow', 'bill', '2020-01-07', '2020-01-20', 'pcs', 'creditcard', 38);
-- CALL enterBid('john', 'wolfie', 'jean', '2020-01-01','2020-01-20','pcs', 'cash', 40);
-- SELECT computeMaxPet('bill');
-- SELECT CASE 
-- 		WHEN 'jean' NOT IN (SELECT P.username FROM PartTimers P) THEN 5
-- 		WHEN avgrating>4.7 THEN 5
-- 		WHEN avgrating>4.0 THEN 4
-- 		WHEN avgrating>3.0 THEN 3
-- 		ELSE 2
-- 		END
-- 	FROM (SELECT MAX(CB.caretaker) AS caretaker, AVG(CB.rating) AS avgrating
-- 			FROM (SELECT * FROM Bids UNION SELECT * FROM BidsWithoutPetOwner) CB 
-- 			WHERE CB.caretaker='jean') AVGR
-- 	;

-- CALL approveRejectBid('john', 'wolfie', 'bill', '2020-01-01', 'a');
-- /*CALL enterBid('john', 'wolfie', 'amanda', '2020-01-03','2020-01-20','pcs', 'cash', 40);*/ /*meant to raise error*/
-- CALL updateIsPaid('john', 'wolfie', 'bill', '2020-01-01', true);
-- CALL approveRejectBid('jane', 'kitty', 'bill', '2020-01-05', 'a');
-- CALL updateIsPaid('jane', 'kitty', 'bill', '2020-01-05', true);

-- /*  --should raise errors
-- UPDATE Bids SET rating=5
-- 	WHERE petowner='john' AND petname='wolfie' AND caretaker='bill' AND avail= (SELECT CAST('2020-01-01' AS DATE));
-- UPDATE Bids SET review='awesome'
-- 	WHERE petowner='john' AND petname='wolfie' AND caretaker='bill' AND avail=(SELECT CAST('2020-01-01' AS DATE));
-- */
-- CALL rateCareTaker('john', 'wolfie', 'bill', '2020-01-01', 5, 'awesome');  
-- /*DELETE FROM Users WHERE username='john'; */
-- SELECT computeMaxPet('bill');

-- SELECT CASE 
-- 		WHEN 'bill' NOT IN (SELECT P.username FROM PartTimers P) THEN 5
-- 		WHEN avgrating>4.7 THEN 5
-- 		WHEN avgrating>4.0 THEN 4
-- 		WHEN avgrating>3.0 THEN 3
-- 		ELSE 2
-- 		END
-- 	FROM (SELECT AVG(CB.rating) AS avgrating
-- 			FROM (SELECT * FROM Bids UNION SELECT * FROM BidsWithoutPetOwner) CB 
-- 			WHERE CB.caretaker='bill') AVGR
-- 	;
	
	
-- SELECT computeMaxPriceMultiplier('bill');
-- UPDATE AbleToCare SET feeperday=30 WHERE caretaker='bill' AND category='cat';
-- UPDATE AbleToCare SET feeperday=42 WHERE caretaker='bill' AND category='dog';
-- UPDATE AbleToCare SET feeperday=18 WHERE caretaker='bill' AND category='bird';
-- CALL enterBid('mary', 'canine', 'bill', '2020-01-05','2020-01-17','pcs', 'cash', 42); /*should be rejected when bills rating falls*/
-- CALL rateCareTaker('jane', 'kitty', 'bill', '2020-01-05', 1, 'lousy'); /*Bill's average rating falls to 3, max pet slots should fall, prices reset to base*/

-- SELECT CASE 
-- 		WHEN 'bill' NOT IN (SELECT P.username FROM PartTimers P) THEN 5
-- 		WHEN avgrating>4.7 THEN 5
-- 		WHEN avgrating>4.0 THEN 4
-- 		WHEN avgrating>3.0 THEN 3
-- 		ELSE 2
-- 		END
-- 	FROM (SELECT AVG(CB.rating) AS avgrating
-- 			FROM (SELECT * FROM Bids UNION SELECT * FROM BidsWithoutPetOwner) CB 
-- 			WHERE CB.caretaker='bill') AVGR
-- 	;

-- SELECT computeMaxPet('bill');
-- SELECT computeMaxPriceMultiplier('bill');

-- CALL addAvailableDates('jean', '2020-01-01', '2021-03-28');
-- CALL enterBid('jane', 'birdie', 'jean', '2020-01-21','2020-01-28','pcs', 'cash', 40);
-- CALL applyLeave('jean', '2020-01-23', '2020-02-24');
-- CALL addAvailableDates('jean', '2020-01-01', '2021-03-28');

-- /*
-- CREATE OR REPLACE FUNCTION currentDate()
-- RETURNS DATE AS $$
-- 	BEGIN
-- 		RETURN (SELECT CAST('2021-01-01'AS DATE));
-- 	END; $$
-- LANGUAGE plpgsql;
-- */
-- /*
-- CALL enterBid('jane', 'birdie', 'jean', '2020-01-21','2020-01-28','pcs', 'cash', 40);
-- CALL approveRejectBid('jane', 'birdie', 'jean', '2020-01-22', 'a');
-- DELETE FROM Users WHERE username='jane';
-- CALL applyLeave('jean', '2020-01-23', '2020-02-24'); */ /*meant to raise error*/
-- CALL addAvailableDates('jean', '2020-01-01', '2021-03-28');
-- /* CALL applyLeave('jean', '2020-02-23', '2020-03-24'); */ /*meant to raise error*/
-- SELECT caretaker, avail FROM availability WHERE caretaker='jean';

-- /*Below are test cases for the change base price trigger*/
-- CALL enterBid('john', 'birdie', 'jean', '2020-02-21','2020-02-28','pcs', 'cash', 45);
-- CALL enterBid('jack', 'birdb', 'amanda', '2020-02-21','2020-02-28','pcs', 'cash', 45);
-- CALL enterBid('mary', 'chirp', 'germ', '2020-02-21','2020-02-28','pcs', 'cash', 45);
-- CALL enterBid('jane', 'birdie', 'edmund', '2020-02-21','2020-02-28','pcs', 'cash', 45);
-- CALL approveRejectBid('john', 'birdie', 'jean', '2020-02-21', 'a');
-- CALL updateIsPaid('john', 'birdie', 'jean', '2020-02-21', true);
-- CALL approveRejectBid('jack', 'birdb', 'amanda', '2020-02-21', 'a');
-- CALL updateIsPaid('jack', 'birdb', 'amanda', '2020-02-21', true);
-- CALL approveRejectBid('mary', 'chirp', 'germ', '2020-02-21', 'a');
-- CALL updateIsPaid('mary', 'chirp', 'germ', '2020-02-21', true);
-- CALL approveRejectBid('jane', 'birdie', 'edmund', '2020-02-21', 'a');
-- CALL updateIsPaid('jane', 'birdie', 'edmund', '2020-02-21', true);
-- CALL rateCareTaker('john', 'birdie', 'jean', '2020-02-21', 5, 'great');
-- CALL rateCareTaker('jack', 'birdb', 'amanda', '2020-02-21', 5, 'great');
-- CALL rateCareTaker('mary', 'chirp', 'germ', '2020-02-21', 5, 'great');
-- CALL rateCareTaker('jane', 'birdie', 'edmund', '2020-02-21', 5, 'great');
-- --DELETE FROM Users WHERE username='john';
-- --DELETE FROM Users WHERE username='jack';
-- --DELETE FROM Users WHERE username='mary';
-- --DELETE FROM Users WHERE username='jane';


-- UPDATE AbleToCare SET feeperday=30 WHERE caretaker='jean' AND category='cat';
-- UPDATE AbleToCare SET feeperday=42 WHERE caretaker='jean' AND category='dog';
-- UPDATE AbleToCare SET feeperday=18 WHERE caretaker='jean' AND category='bird';

-- UPDATE AbleToCare SET feeperday=29 WHERE caretaker='amanda' AND category='cat';
-- UPDATE AbleToCare SET feeperday=41 WHERE caretaker='amanda' AND category='dog';
-- UPDATE AbleToCare SET feeperday=17 WHERE caretaker='amanda' AND category='bird';

-- UPDATE AbleToCare SET feeperday=28 WHERE caretaker='germ' AND category='cat';
-- UPDATE AbleToCare SET feeperday=40 WHERE caretaker='germ' AND category='dog';
-- UPDATE AbleToCare SET feeperday=16 WHERE caretaker='germ' AND category='bird';

-- UPDATE AbleToCare SET feeperday=27 WHERE caretaker='edmund' AND category='cat';
-- UPDATE AbleToCare SET feeperday=39 WHERE caretaker='edmund' AND category='dog';
-- UPDATE AbleToCare SET feeperday=15 WHERE caretaker='edmund' AND category='bird';

-- /*UPDATE PetTypes SET baseprice=17 WHERE category='bird';*/ /*Jeans bird fee should remain at 18*/
-- /*UPDATE PetTypes SET baseprice=40 WHERE category='dog';*/ /*amanda and jeans fee for dog should remain above 40*/
-- /*UPDATE PetTypes SET baseprice=23.5 WHERE category='cat';*/ /*germ and edmunds fee for cat should remain the same*/


-- /*
-- CREATE OR REPLACE FUNCTION currentDate()
-- RETURNS DATE AS $$
-- 	BEGIN
-- 		RETURN (SELECT CAST('2021-01-01'AS DATE));
-- 	END; $$
-- LANGUAGE plpgsql;
-- */
-- /*DELETE FROM Users WHERE username='john'; */
-- /*DELETE FROM Pets WHERE petowner='john' AND petname='wolfie';*/
-- /*DELETE FROM Users WHERE username = 'bill';*/

-- /*CALL addPetOwner('john','john@gmail', 'hello', 'jurong', 92345332, 2131231, 34536435);*/

-- ---------------------------------------------------------------------
-- -----------------Queries to view certain information-----------------
-- ---------------------------------------------------------------------

-- /*View bids(approve or pending only) for a particular care taker*/
-- SELECT petowner, petname, status, category, price, sdate, edate, paymenttype, transfertype
-- FROM
-- 	(SELECT B.petowner, B.petname, MAX(B.avail) AS sdate, B.edate
-- 	FROM combinedBids() B
-- 	WHERE B.caretaker='bill'
-- 	GROUP BY B.petowner, B.petname, B.edate) A
-- 	NATURAL JOIN
-- 	(SELECT *
-- 	FROM combinedBids() B NATURAL LEFT JOIN Pets P
-- 	WHERE B.edate=B.avail AND B.caretaker='bill') B
-- WHERE B.status!='r'
-- ;

-- SELECT* FROM combinedbids(); 
-- /*DELETE FROM Users WHERE username='master'; */



-- /*View availability of each caretaker*/
-- SELECT AV.caretaker, AV.avail
-- FROM
-- 	(SELECT B.caretaker, B.avail, COUNT(*) AS cnt
-- 	FROM combinedBids() B
-- 	WHERE B.status ='a'
-- 	GROUP BY B.caretaker, B.avail
-- 	UNION 
-- 	SELECT A.caretaker, A.avail, 0 AS cnt 
-- 	FROM Availability A
-- 	WHERE NOT EXISTS (SELECT * FROM combinedBids() B WHERE B.caretaker=A.caretaker AND B.avail=A.avail AND B.status='a')
-- 	) AV
-- WHERE AV.cnt<(SELECT computeMaxPet(AV.caretaker)) 
-- ORDER BY avail ASC
-- ;

-- /*View their fee per day and their range of settable prices*/
-- SELECT A.caretaker, A.category, A.feeperday, P.baseprice, P.baseprice*(SELECT computeMaxPriceMultiplier('bill')) AS upperlimit
-- FROM AbleToCare A NATURAL JOIN PetTypes P
-- WHERE A.caretaker='bill';

-- /*View each caretaker's average rating and number of rating*/
-- SELECT CB.caretaker, AVG(CB.rating) AS avgrating, COUNT(CB.rating) AS numRatings
-- FROM (SELECT * FROM Bids UNION SELECT * FROM BidsWithoutPetOwner) CB
-- GROUP BY CB.caretaker
-- UNION
-- SELECT CT.username AS caretaker, NULL AS avgrating, 0 AS numratings
-- FROM CareTakers CT
-- WHERE CT.username NOT IN (SELECT CB1.caretaker FROM (SELECT * FROM Bids UNION SELECT * FROM BidsWithoutPetOwner) CB1);


-- /*compute max pet*/
-- SELECT CASE 
-- 		WHEN 'bill' NOT IN (SELECT P.username FROM PartTimers P) THEN 5
-- 		WHEN avgrating>4.7 THEN 5
-- 		WHEN avgrating>4.0 THEN 4
-- 		WHEN avgrating>3.0 THEN 3
-- 		ELSE 2
-- 		END
-- 	FROM (SELECT AVG(CB.rating) AS avgrating
-- 			FROM (SELECT * FROM Bids UNION SELECT * FROM BidsWithoutPetOwner) CB 
-- 			WHERE CB.caretaker='bill') AVGR
-- 	;

-- /* AVAILABILITY (availability is all the dates that is not on leave) */
-- /* Support the browsing for caretakers by pet owners */ 
-- /* input: start date, end date, pet category */
-- /* output: caretaker, pet category, atc.feeperday, start date, end date */
-- SELECT atc.caretaker, atc.category, atc.feeperday, AVGRC.avgrating, AVGRC.numratings, '2020-01-01' AS startdate, '2020-01-06' AS enddate
-- FROM AbleToCare atc 
-- 	NATURAL JOIN ( /*Table of each caretaker's average rating and number of rating*/
-- 		SELECT CB.caretaker, AVG(CB.rating) AS avgrating, COUNT(CB.rating) AS numratings
-- 		FROM (SELECT * FROM Bids UNION SELECT * FROM BidsWithoutPetOwner) CB
-- 		GROUP BY CB.caretaker
-- 		UNION
-- 		SELECT CT.username AS caretaker, NULL AS avgrating, 0 AS numratings
-- 		FROM CareTakers CT
-- 		WHERE CT.username NOT IN (SELECT CB1.caretaker FROM (SELECT * FROM Bids UNION SELECT * FROM BidsWithoutPetOwner) CB1)
-- 		) AVGRC
-- WHERE EXISTS 
-- 	(SELECT 1 FROM
-- 		(SELECT availCT.caretaker, COUNT(*) AS days
-- 			FROM /*table of caretaker's availability to take on another pet on that date, within a restricted date range*/
-- 				(SELECT AV.caretaker, AV.avail 
-- 					FROM /*table of tabulating number of pets a caretaker has to take care on a work day*/
-- 						(SELECT B.caretaker, B.avail, COUNT(*) AS cnt
-- 							FROM Bids B
-- 							WHERE B.status ='a'
-- 							GROUP BY B.caretaker, B.avail
-- 							UNION 
-- 							SELECT A.caretaker, A.avail, 0 AS cnt 
-- 							FROM Availability A
-- 							WHERE NOT EXISTS (SELECT * FROM Bids B WHERE B.caretaker=A.caretaker AND B.avail=A.avail AND B.status='a')
-- 						) AV
-- 					WHERE AV.cnt<(/*This nested query computes the max pet limit of AV.caretaker*/
-- 									SELECT CASE 
-- 										WHEN AV.caretaker NOT IN (SELECT P.username FROM PartTimers P) THEN 5
-- 											/*means caretaker is full-time, default value 5. Look at part-time case below*/
-- 										WHEN avgrating>4.7 THEN 5
-- 										WHEN avgrating>4.0 THEN 4
-- 										WHEN avgrating>3.0 THEN 3
-- 										ELSE 2
-- 										END
-- 									FROM (SELECT AVG(CB.rating) AS avgrating
-- 											FROM (SELECT * FROM Bids UNION SELECT * FROM BidsWithoutPetOwner) CB 
-- 											WHERE CB.caretaker=AV.caretaker) AVGR
-- 									) 
-- 						AND (AV.avail BETWEEN '2020-01-01' AND '2020-01-06') /*restrict to date range*/
-- 				)AS availCT
-- 		GROUP BY availCT.caretaker
-- 		HAVING COUNT(*) = (CAST(MAX('2020-01-06') AS date) - CAST(MIN('2020-01-01') AS date)) +1
-- 		/*means caretaker is available evey day within the date range*/
-- 		) AS t
-- 	WHERE t.caretaker = ATC.caretaker
-- 	)
-- 	AND atc.category = 'dog'
-- ORDER BY avgrating DESC NULLS LAST, numratings DESC, feeperday ASC; 



-- /*bids must be approved, isPaid, bids dates between the month*/
-- /* one transaction: daily fee * no of days in transaction */
-- /*calculate wage = sum(of all transaction)*/

-- /*for parttimers */
-- SELECT CB.caretaker, SUM(price)*0.75 AS salary, COUNT(*) AS petdaysclocked
-- FROM (SELECT * FROM Bids UNION SELECT * FROM BidsWithoutPetOwner) CB 
-- WHERE CB.avail <= CAST('2020-02-01' AS DATE)  + interval '1 month'
-- 	AND CB.avail >= CAST('2020-02-01' AS DATE) 
-- 	AND CB.isPaid = TRUE 
-- 	AND CB.status = 'a' 
-- 	AND CB.caretaker IN (SELECT PT.username FROM PartTimers PT)
-- GROUP BY CB.caretaker
-- UNION
-- SELECT PT.username AS caretaker, 0.0 AS salary , 0 AS petdaysclocked
-- FROM PartTimers PT
-- WHERE PT.username NOT IN (SELECT B.caretaker 
-- 							FROM (SELECT * FROM Bids UNION SELECT * FROM BidsWithoutPetOwner) B 
-- 							WHERE B.avail <= CAST('2020-02-01' AS DATE)  + interval '1 month'
-- 								AND B.avail >= CAST('2020-02-01' AS DATE)
-- 								AND B.isPaid = TRUE 
-- 								AND B.status = 'a')
-- ;

-- /*for fulltimers */
-- /*fulltimers wage*/

-- SELECT CT.username AS caretaker, (
-- 	SELECT CASE 
-- 			WHEN SUM(OFS.price) IS NOT NULL THEN 3000+SUM(OFS.price)*0.8
-- 			ELSE 3000
-- 			END
-- 	FROM (
-- 		SELECT *
-- 		FROM (SELECT * FROM Bids UNION SELECT * FROM BidsWithoutPetOwner) CB
-- 		WHERE CB.caretaker=CT.username AND CB.avail <= CAST('2020-02-01' AS DATE)  + interval '1 month'
-- 			AND CB.avail >= CAST('2020-02-01' AS DATE)
-- 			AND CB.isPaid = TRUE 
-- 			AND CB.status = 'a'
-- 		ORDER BY CB.price ASC 
-- 		OFFSET 60 
-- 		) OFS
-- 	) AS salary,
-- 	(
-- 	SELECT COUNT(*) AS petdaysclocked
-- 	FROM (SELECT * FROM Bids UNION SELECT * FROM BidsWithoutPetOwner) CB
-- 	WHERE CB.caretaker=CT.username AND CB.avail <= CAST('2020-02-01' AS DATE)  + interval '1 month'
-- 		AND CB.avail >= CAST('2020-02-01' AS DATE)
-- 		AND CB.isPaid = TRUE 
-- 		AND CB.status = 'a'
-- 	) AS petdaysclocked
-- FROM CareTakers CT
-- WHERE CT.username NOT IN (SELECT PT.username FROM PartTimers PT)
-- ;




-- /*For testing only*/
-- /*SELECT CASE
-- 	WHEN SUM(OFS.price) IS NOT NULL THEN 3000+SUM(OFS.price)*0.8
-- 	ELSE 3000
-- 	END
-- FROM (
-- 	SELECT *
-- 	FROM (SELECT * FROM Bids UNION SELECT * FROM BidsWithoutPetOwner) CB
-- 	WHERE CB.caretaker='jean' AND CB.avail <= CAST('2020-02-01' AS DATE)  + interval '1 month'
-- 		AND CB.avail >= CAST('2020-02-01' AS DATE)
-- 		AND CB.isPaid = TRUE 
-- 		AND CB.status = 'a'
-- 	ORDER BY CB.price ASC 
-- 	OFFSET 5 
-- 	) OFS
-- ;*/

-- /*combined wages for a particular month with caretaker's ratings and num ratings*/
-- SELECT WG.caretaker, WG.contract, WG.salary, WG.petdaysclocked, RT.avgrating, RT.numratings
-- FROM
-- 	(
-- 	SELECT CB.caretaker, 'Part-Time' AS contract, SUM(price)*0.75 AS salary, COUNT(*) AS petdaysclocked
-- 	FROM (SELECT * FROM Bids UNION SELECT * FROM BidsWithoutPetOwner) CB 
-- 	WHERE CB.avail <= CAST('2020-02-01' AS DATE)  + interval '1 month'
-- 		AND CB.avail >= CAST('2020-02-01' AS DATE) 
-- 		AND CB.isPaid = TRUE 
-- 		AND CB.status = 'a' 
-- 		AND CB.caretaker IN (SELECT PT.username FROM PartTimers PT)
-- 	GROUP BY CB.caretaker
-- 	UNION
-- 	SELECT PT.username AS caretaker, 'Part-Time' AS contract, 0.0 AS salary , 0 AS petdaysclocked
-- 	FROM PartTimers PT
-- 	WHERE PT.username NOT IN (SELECT B.caretaker 
-- 								FROM (SELECT * FROM Bids UNION SELECT * FROM BidsWithoutPetOwner) B 
-- 								WHERE B.avail <= CAST('2020-02-01' AS DATE)  + interval '1 month'
-- 									AND B.avail >= CAST('2020-02-01' AS DATE)
-- 									AND B.isPaid = TRUE 
-- 									AND B.status = 'a')
-- 	UNION
-- 	SELECT CT.username AS caretaker, 'Full-Time' AS contract, (
-- 		SELECT CASE 
-- 				WHEN SUM(OFS.price) IS NOT NULL THEN 3000+SUM(OFS.price)*0.8
-- 				ELSE 3000
-- 				END
-- 		FROM (
-- 			SELECT *
-- 			FROM (SELECT * FROM Bids UNION SELECT * FROM BidsWithoutPetOwner) CB
-- 			WHERE CB.caretaker=CT.username AND CB.avail <= CAST('2020-02-01' AS DATE)  + interval '1 month'
-- 				AND CB.avail >= CAST('2020-02-01' AS DATE)
-- 				AND CB.isPaid = TRUE 
-- 				AND CB.status = 'a'
-- 			ORDER BY CB.price ASC 
-- 			OFFSET 60 
-- 			) OFS
-- 		) AS salary,
-- 		(
-- 		SELECT COUNT(*) AS petdaysclocked
-- 		FROM (SELECT * FROM Bids UNION SELECT * FROM BidsWithoutPetOwner) CB
-- 		WHERE CB.caretaker=CT.username AND CB.avail <= CAST('2020-02-01' AS DATE)  + interval '1 month'
-- 			AND CB.avail >= CAST('2020-02-01' AS DATE)
-- 			AND CB.isPaid = TRUE 
-- 			AND CB.status = 'a'
-- 		) AS petdaysclocked
-- 	FROM CareTakers CT
-- 	WHERE CT.username NOT IN (SELECT PT.username FROM PartTimers PT)
-- 	) WG
-- 	NATURAL JOIN
-- 	(SELECT CB.caretaker, AVG(CB.rating) AS avgrating, COUNT(CB.rating) AS numratings
-- 	FROM (SELECT * FROM Bids UNION SELECT * FROM BidsWithoutPetOwner) CB
-- 	GROUP BY CB.caretaker
-- 	UNION
-- 	SELECT CT.username AS caretaker, NULL AS avgrating, 0 AS numratings
-- 	FROM CareTakers CT
-- 	WHERE CT.username NOT IN (SELECT CB1.caretaker FROM (SELECT * FROM Bids UNION SELECT * FROM BidsWithoutPetOwner) CB1)
-- 	) RT
-- ORDER BY RT.avgrating ASC, RT.numratings ASC, WG.petdaysclocked ASC
-- ;


-- /*FOR TESTING ONLY IGNORE, DONT USE, View salaries of worker my month, uses CTE*/
-- WITH transactions AS
-- 	(SELECT CB.petowner, CB.petname, CB.caretaker, CB.avail, CB.price, EXTRACT(MONTH FROM CB.avail) AS month_, EXTRACT(YEAR FROM CB.avail) AS year_
-- 	FROM (SELECT * FROM Bids UNION SELECT * FROM BidsWithoutPetOwner) CB
-- 	WHERE CB.status='a' AND CB.isPaid
-- 	)
-- SELECT CT.username AS caretaker, MY.year_, MY.month_, 
-- 	(SELECT CASE
-- 		WHEN CT.username NOT IN (SELECT PT.username FROM PartTimers PT) THEN
-- 		(SELECT CASE 
-- 			WHEN SUM(OFS.price) IS NOT NULL THEN 3000+SUM(OFS.price)*0.8
-- 			ELSE 3000
-- 			END
-- 		FROM (
-- 			SELECT *
-- 			FROM transactions CB
-- 			WHERE CB.caretaker=CT.username AND CB.year_=MY.year_ AND CB.month_=MY.month_
-- 			ORDER BY CB.price ASC 
-- 			OFFSET 60 
-- 			) OFS
-- 		)
-- 	ELSE 
-- 		(
-- 		SELECT CASE 
-- 			WHEN SUM(price) IS NOT NULL THEN SUM(price)*0.75
-- 			ELSE 0
-- 			END
-- 		FROM transactions CB 
-- 		WHERE CB.caretaker=CT.username AND CB.year_=MY.year_ AND CB.month_=MY.month_
-- 		)
-- 	END
-- 	)AS salary
-- FROM CareTakers CT, (SELECT DISTINCT T0.month_, T0.year_ FROM transactions T0) MY
-- ;

-- /*FOR TESTING ONLY IGNORE, DONT USE, Total salary paid for each month, uses CTE*/
-- WITH transactions AS
-- 	(SELECT CB.petowner, CB.petname, CB.caretaker, CB.avail, CB.price, EXTRACT(MONTH FROM CB.avail) AS month_, EXTRACT(YEAR FROM CB.avail) AS year_
-- 	FROM (SELECT * FROM Bids UNION SELECT * FROM BidsWithoutPetOwner) CB
-- 	WHERE CB.status='a' AND CB.isPaid
-- 	)
-- SELECT CTS.year_, CTS.month_, SUM(CTS.salary) AS totalsalarypaid
-- FROM (
-- 	SELECT CT.username AS caretaker, MY.year_, MY.month_,  
-- 		(SELECT CASE
-- 			WHEN CT.username NOT IN (SELECT PT.username FROM PartTimers PT) THEN
-- 			(SELECT CASE 
-- 				WHEN SUM(OFS.price) IS NOT NULL THEN 3000+SUM(OFS.price)*0.8
-- 				ELSE 3000
-- 				END
-- 			FROM (
-- 				SELECT *
-- 				FROM transactions CB
-- 				WHERE CB.caretaker=CT.username AND CB.year_=MY.year_ AND CB.month_=MY.month_
-- 				ORDER BY CB.price ASC 
-- 				OFFSET 60 
-- 				) OFS
-- 			)
-- 		ELSE 
-- 			(
-- 			SELECT CASE 
-- 				WHEN SUM(price) IS NOT NULL THEN SUM(price)*0.75
-- 				ELSE 0
-- 				END
-- 			FROM transactions CB 
-- 			WHERE CB.caretaker=CT.username AND CB.year_=MY.year_ AND CB.month_=MY.month_
-- 			)
-- 		END
-- 		)AS salary
-- 	FROM CareTakers CT, (SELECT DISTINCT T0.month_, T0.year_ FROM transactions T0) MY
-- 	) AS CTS
-- GROUP BY CTS.year_, CTS.month_
-- ;

-- /*FOR TESTING ONLY IGNORE, DONT USE, total revenue and total pets for each month, uses CTE*/
-- WITH transactions AS
-- 	(SELECT CB.petowner, CB.petname, CB.caretaker, CB.avail, CB.price, EXTRACT(MONTH FROM CB.avail) AS month_, EXTRACT(YEAR FROM CB.avail) AS year_
-- 	FROM (SELECT * FROM Bids UNION SELECT * FROM BidsWithoutPetOwner) CB
-- 	WHERE CB.status='a' AND CB.isPaid
-- 	)
-- SELECT TR0.year_, TR0.month_, SUM(TR0.price) AS totalrevenue, COUNT(DISTINCT CONCAT(TR0.petowner, ',', TR0.petname) ) AS totalpets
-- FROM transactions TR0
-- GROUP BY TR0.year_, TR0.month_
-- ;

-- /* --for checking previous table
-- WITH transactions AS
-- 	(SELECT CB.petowner, CB.petname, CB.caretaker, CB.avail, CB.price, EXTRACT(MONTH FROM CB.avail) AS month_, EXTRACT(YEAR FROM CB.avail) AS year_
-- 	FROM (SELECT * FROM Bids UNION SELECT * FROM BidsWithoutPetOwner) CB
-- 	WHERE CB.status='a' AND CB.isPaid
-- 	)
-- SELECT DISTINCT TR0.year_, TR0.month_, TR0.petowner, TR0.petname
-- FROM transactions TR0
-- ORDER BY TR0.month_ ASC
-- ;
-- */

-- /*Total profit, revenue, salary paid and total pets for each month, uses CTE*/
-- /*Note to Boshen, limit the date range in the where condition of the CTE table*/
-- WITH transactions AS
-- 	(SELECT CB.petowner, CB.petname, CB.caretaker, CB.avail, CB.price, EXTRACT(MONTH FROM CB.avail) AS month_, EXTRACT(YEAR FROM CB.avail) AS year_
-- 	FROM (SELECT * FROM Bids UNION SELECT * FROM BidsWithoutPetOwner) CB
-- 	WHERE CB.status='a' AND CB.isPaid
-- 	)
-- SELECT TSP.year_, TSP.month_, TRP.totalrevenue-TSP.totalsalarypaid AS profit, TRP.totalrevenue, TSP.totalsalarypaid, TRP.totalpets
-- FROM (/*This table tabulates the total salary paid for each month*/
-- 	SELECT CTS.year_, CTS.month_, SUM(CTS.salary) AS totalsalarypaid
-- 	FROM ( /*This table tabulates the salary for each caretaker for a given month and year*/
-- 		SELECT CT.username AS caretaker, MY.year_, MY.month_,  
-- 			(SELECT CASE
-- 				WHEN CT.username NOT IN (SELECT PT.username FROM PartTimers PT) THEN /*Full-timers case*/
-- 				(SELECT CASE 
-- 					WHEN SUM(OFS.price) IS NOT NULL THEN 3000+SUM(OFS.price)*0.8
-- 					ELSE 3000
-- 					END
-- 				FROM (
-- 					SELECT *
-- 					FROM transactions CB
-- 					WHERE CB.caretaker=CT.username AND CB.year_=MY.year_ AND CB.month_=MY.month_
-- 					ORDER BY CB.price ASC 
-- 					OFFSET 60 
-- 					) OFS
-- 				)
-- 			ELSE /*part-timers case*/
-- 				(
-- 				SELECT CASE 
-- 					WHEN SUM(price) IS NOT NULL THEN SUM(price)*0.75
-- 					ELSE 0
-- 					END
-- 				FROM transactions CB 
-- 				WHERE CB.caretaker=CT.username AND CB.year_=MY.year_ AND CB.month_=MY.month_
-- 				)
-- 			END
-- 			)AS salary
-- 		FROM CareTakers CT, (SELECT DISTINCT T0.month_, T0.year_ FROM transactions T0) MY
-- 		) AS CTS
-- 	GROUP BY CTS.year_, CTS.month_
-- 	) TSP
-- 	NATURAL JOIN 
-- 	( /*This table tabulates the total revenue and the total number pets served for each month*/
-- 	SELECT TR0.year_, TR0.month_, SUM(TR0.price) AS totalrevenue, COUNT(DISTINCT CONCAT(TR0.petowner, ',', TR0.petname) ) AS totalpets
-- 	FROM transactions TR0
-- 	GROUP BY TR0.year_, TR0.month_
-- 	) TRP
-- ORDER BY TSP.year_ ASC, TSP.month_ ASC
-- ;

