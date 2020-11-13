const express = require("express");
const app = express();
const cors = require("cors");
const pool = require("./db"); // db config setup file
const PORT = process.env.PORT || 5000;


//middleware
app.use(cors());
app.use(express.json()); // => allows us to access the req.body


// Routes

// base
app.get("/", async (req, res) => {
  try {
    res.json("Visit https://github.com/bscrow/db-heroku-proj for documentation \n\n Visit /debug to see list of tables");
  } catch (err) {
    console.error(err.message);
  }
});


/* 
    #################################
    #      App-specific queries     #
    #################################
*/ 

/* 
==============================
|    login/register/delete   |
==============================
*/ 

// Login
app.post("/Users/login", async (req, res) => {
  try {
    var accountType;
    if (req.body.acctype==="petowner") {
      accountType = ["PetOwners", "PetOwners"];
    } else if (req.body.acctype==="caretaker") {
      accountType = ["CareTakers", "CareTakers"];
    } else if (req.body.acctype==="admin") {
      accountType = ["PCSadmins", "PCSadmins"];
    } else if (req.body.acctype==="both") {
      accountType = ["PetOwners", "CareTakers"];
    } else {
      throw Error("Unknown account type. acctype must be one of ['petowner', 'caretaker' 'both', 'admin'], case insensitive.");
    }
    const getUsers = await pool.query(
      `SELECT Users.*, '${req.body.acctype}' AS acctype, CAST (Users.phonenum AS VARCHAR) AS phonenum
      FROM Users, ${accountType[0]} as t1, ${accountType[1]} as t2
      WHERE '${req.body.username}' = Users.username
      AND '${req.body.password}' = password
      AND '${req.body.username}' = t1.username
      AND '${req.body.username}' = t2.username`
    );
    if (!Array.isArray(getUsers.rows) || !getUsers.rows.length) {
      throw Error("Invalid login.");
    }
    res.json(getUsers.rows);

  } catch (err) {
    console.error(err.message);
  }
});


// Register
app.post("/Users/register", async (req, res) => {
  try {
    var queryStr = "";
    if (req.body.acctype==="petowner" || req.body.acctype==="both") {
      queryStr = queryStr.concat(
        `
        CALL addPetOwner('${req.body.username}',
        '${req.body.email}', 
        '${req.body.password}',
        '${req.body.profile}', 
        '${req.body.address}', 
        ${req.body.phoneNum}, 
        ${req.body.creditCard}, 
        ${req.body.bankAcc});
        `
      );
    } 
    if (req.body.acctype==="caretaker" || req.body.acctype==="both") {
      queryStr = queryStr.concat(
        `
        CALL addCareTaker('${req.body.username}',
        '${req.body.email}', 
        '${req.body.password}',
        '${req.body.profile}', 
        '${req.body.address}', 
        ${req.body.phoneNum}, 
        ${req.body.creditCard}, 
        ${req.body.bankAcc},
        ${req.body.isPartTime},
        '${req.body.admin}');
        `
      );
    } 
    if (!["petowner", "caretaker", "both"].includes(req.body.acctype)) {
      throw Error("Unknown account type. acctype must be one of ['petowner', 'caretaker', 'both'], case insensitive.")
    }
    const register = await pool.query(queryStr);
    res.json(req.body);

  } catch (err) {
    console.log(queryStr);
    console.log(req.body.acctype);
    console.log(req.body)
    console.error(err.message);
  }
});

// Delete
app.delete("/Users/delete", async (req, res) => {
  try {
    const delUser = await pool.query(
      `DELETE FROM Users 
      WHERE '${req.body.username}' = username
      AND '${req.body.password}' = password RETURNING *;`
    );
    if (!Array.isArray(delUser.rows) || !delUser.rows.length) {
      throw Error("Cannot delete. Check for existing approved bids.");
    }
    res.json(delUser.rows[0]);

  } catch (err) {
    console.error(err.message);
  }
});


/* 
=======================
|       petowner      |
=======================
*/ 

// Get all of the petowner's own information
app.get("/PetOwner/:petowner", async (req, res) => {
  try {
    const getPetOwnerInfo = await pool.query(
      `SELECT U.username, U.email, U.password, U.profile, U.address, U.phonenum, 
      C.creditcard, C.bankacc, 
      P.petname, P.profile AS petprofile, P.specialreq, P.category
      FROM Users AS U
      NATURAL JOIN Consumers AS C
      LEFT JOIN Pets AS P on U.username = P.petowner
      WHERE U.username = '${req.params.petowner}';`
    );
    res.json(getPetOwnerInfo.rows);
  } catch (err) {
    console.error(err.message);
  }
});

// Get information about current bids
app.get("/PetOwner/Bids/:petowner", async (req, res) => {
  try {
    const getRating = await pool.query(
      // Smallest avail(sdate) from each group
      `SELECT *
      FROM (
        SELECT MIN(avail) AS avail, caretaker, edate, transferType, paymentType, price, isPaid, status, rating, review, Pets.* 
              FROM Bids AS B1 LEFT JOIN Pets on B1.petowner = Pets.petowner AND B1.petname = Pets.petname
              WHERE B1.petowner = '${req.params.petowner}'
              AND (SELECT sum(B2.rating) FROM Bids AS B2 
                WHERE B1.edate = B2.avail
                AND B1.petowner = B2.petowner
                AND B1.petname = B2.petname
                AND B1.caretaker = B2.caretaker
                AND B1.edate = B2.edate) IS NULL 
              AND status = 'p'
              GROUP BY caretaker, edate, transferType, paymentType, price, isPaid, status, rating, review, 
              Pets.petowner, Pets.petname, Pets.profile, Pets,specialReq, Pets.category
        UNION
        SELECT sdate AS avail, caretaker, edate, transferType, paymentType, price, FALSE AS isPaid, 'r' AS status, NULL AS rating,
          NULL AS review, Pets.*
        FROM InvalidatedBids IB NATURAL JOIN Pets
        WHERE IB.petowner = '${req.params.petowner}') POB
      ORDER BY status, edate;`

    );
    res.json(getRating.rows);
  } catch (err) {
    console.error(err.message);
  }
});

// Leave rating and reviews for caretaker
app.post("/PetOwner/RatingsReviews", async (req, res) => {
  try {
    if(!req.body.hasOwnProperty('rating') || !req.body.hasOwnProperty('review')){
      throw Error("Ratings and/or reviews are missing!")
    };
    const _ = await pool.query(
      `CALL rateCareTaker('${req.body.petowner}', '${req.body.petname}',
      '${req.body.caretaker}', '${req.body.avail}', ${req.body.rating}, '${req.body.review}');
      
      CALL updateIsPaid('${req.body.petowner}', '${req.body.petname}',
      '${req.body.caretaker}', '${req.body.avail}', ${req.body.isPaid}); `
    );
    res.json("Ratings and reviews updated");
  } catch (err) {
    console.error(err.message);
  }
});


// Get information about past bids
app.get("/PetOwner/Bids/:petowner/history", async (req, res) => {
  try {
    const getRating = await pool.query(
      `SELECT *
      FROM (
        SELECT MIN(avail) AS avail, caretaker, edate, transferType, paymentType, price, isPaid, status, rating, review, Pets.* 
              FROM Bids AS B1 LEFT JOIN Pets on B1.petowner = Pets.petowner AND B1.petname = Pets.petname
              WHERE B1.petowner = '${req.params.petowner}'
              AND (SELECT sum(B2.rating) FROM Bids AS B2 
                WHERE B1.edate = B2.avail
                AND B1.petowner = B2.petowner
                AND B1.petname = B2.petname
                AND B1.caretaker = B2.caretaker
                AND B1.edate = B2.edate) IS NULL 
              AND (status = 'a' OR status = 'r')
              GROUP BY caretaker, edate, transferType, paymentType, price, isPaid, status, rating, review, 
              Pets.petowner, Pets.petname, Pets.profile, Pets,specialReq, Pets.category
        UNION
        SELECT sdate AS avail, caretaker, edate, transferType, paymentType, price, FALSE AS isPaid, 'r' AS status, NULL AS rating,
          NULL AS review, Pets.*
        FROM InvalidatedBids IB NATURAL JOIN Pets
        WHERE IB.petowner = '${req.params.petowner}') POB
      ORDER BY status, edate;`
      
    );
    res.json(getRating.rows);
  } catch (err) {
    console.error(err.message);
  }
});


// Get information about rejected bids
app.get("/PetOwner/Bids/:petowner/rejected", async (req, res) => {
  try {
    const getRating = await pool.query(
      `SELECT *
      FROM (
        SELECT MIN(avail) AS avail, caretaker, edate, transferType, paymentType, price, isPaid, status, rating, review, Pets.* 
              FROM Bids AS B1 LEFT JOIN Pets on B1.petowner = Pets.petowner AND B1.petname = Pets.petname
              WHERE B1.petowner = '${req.params.petowner}'
              AND (SELECT sum(B2.rating) FROM Bids AS B2 
                WHERE B1.edate = B2.avail
                AND B1.petowner = B2.petowner
                AND B1.petname = B2.petname
                AND B1.caretaker = B2.caretaker
                AND B1.edate = B2.edate) IS NULL 
              AND status = 'r'
              GROUP BY caretaker, edate, transferType, paymentType, price, isPaid, status, rating, review, 
              Pets.petowner, Pets.petname, Pets.profile, Pets,specialReq, Pets.category
        UNION
        SELECT sdate AS avail, caretaker, edate, transferType, paymentType, price, FALSE AS isPaid, 'r' AS status, NULL AS rating,
          NULL AS review, Pets.*
        FROM InvalidatedBids IB NATURAL JOIN Pets
        WHERE IB.petowner = '${req.params.petowner}') POB
      ORDER BY status, edate;`
    );
    res.json(getRating.rows);
  } catch (err) {
    console.error(err.message);
  }
});

// Get available caretakers
app.post("/PetOwner/findCareTaker", async (req, res) => {
  try {
    const availCareTakers = await pool.query(
      `SELECT atc.caretaker, atc.category, atc.feeperday, AVGRC.avgrating, AVGRC.numratings, '${req.body.sdate}' AS startdate, '${req.body.edate}' AS enddate
      FROM AbleToCare atc 
        NATURAL JOIN ( /*Table of each caretaker's average rating and number of rating*/
          SELECT CB.caretaker, AVG(CB.rating) AS avgrating, COUNT(CB.rating) AS numratings
          FROM (SELECT * FROM Bids UNION SELECT * FROM BidsWithoutPetOwner) CB
          GROUP BY CB.caretaker
          UNION
          SELECT CT.username AS caretaker, NULL AS avgrating, 0 AS numratings
          FROM CareTakers CT
          WHERE CT.username NOT IN (SELECT CB1.caretaker FROM (SELECT * FROM Bids UNION SELECT * FROM BidsWithoutPetOwner) CB1)
          ) AVGRC
      WHERE EXISTS 
        (SELECT 1 FROM
          (SELECT availCT.caretaker, COUNT(*) AS days
            FROM /*table of caretaker's availability to take on another pet on that date, within a restricted date range*/
              (SELECT AV.caretaker, AV.avail 
                FROM /*table of tabulating number of pets a caretaker has to take care on a work day*/
                  (SELECT B.caretaker, B.avail, COUNT(*) AS cnt
                    FROM Bids B
                    WHERE B.status ='a'
                    GROUP BY B.caretaker, B.avail
                    UNION 
                    SELECT A.caretaker, A.avail, 0 AS cnt 
                    FROM Availability A
                    WHERE NOT EXISTS (SELECT * FROM Bids B WHERE B.caretaker=A.caretaker AND B.avail=A.avail AND B.status='a')
                  ) AV
                WHERE AV.cnt<(/*This nested query computes the max pet limit of AV.caretaker*/
                        SELECT CASE 
                          WHEN AV.caretaker NOT IN (SELECT P.username FROM PartTimers P) THEN 5
                            /*means caretaker is full-time, default value 5. Look at part-time case below*/
                          WHEN avgrating>4.7 THEN 5
                          WHEN avgrating>4.0 THEN 4
                          WHEN avgrating>3.0 THEN 3
                          ELSE 2
                          END
                        FROM (SELECT AVG(CB.rating) AS avgrating
                            FROM (SELECT * FROM Bids UNION SELECT * FROM BidsWithoutPetOwner) CB 
                            WHERE CB.caretaker=AV.caretaker) AVGR
                        ) 
                  AND (AV.avail BETWEEN '${req.body.sdate}' AND '${req.body.edate}') /*restrict to date range*/
              )AS availCT
          GROUP BY availCT.caretaker
          HAVING COUNT(*) = (CAST(MAX('${req.body.edate}') AS date) - CAST(MIN('${req.body.sdate}') AS date)) +1
          /*means caretaker is available evey day within the date range*/
          ) AS t
        WHERE t.caretaker = ATC.caretaker
        )
        AND atc.category = '${req.body.category}'
      ORDER BY avgrating DESC NULLS LAST, numratings DESC, feeperday ASC; `
    );

    res.json(availCareTakers.rows);
  } catch (err) {
    console.log(req.body);
    console.error(err.message);
  }
});

// Get caretaker rating and reviews
app.get("/PetOwner/RatingsReviews/:caretaker", async (req, res) => {
  try {
    const getRating = await pool.query(
      `SELECT caretaker, rating, review, Pets.petowner, Pets.petname, edate, Pets.category
      FROM combinedBids() AS B LEFT JOIN Pets on B.petowner = Pets.petowner AND B.petname = Pets.petname
      WHERE caretaker = '${req.params.caretaker}'
      AND rating IS NOT NULL
      ORDER BY edate;`
    );
    res.json(getRating.rows);
  } catch (err) {
    console.error(err.message);
  }
});

// Insert bid for a caretaker
app.post("/PetOwner/Bids", async (req, res) => {
  try {
    const _ = await pool.query(
      `CALL enterBid('${req.body.petowner}', '${req.body.petname}', 
      '${req.body.caretaker}', DATE ('${req.body.sdate}'), DATE ('${req.body.edate}'), '${req.body.transferType}', 
      '${req.body.paymentType}', ${req.body.price});`
    );
    res.json(req.body);
  } catch (err) {
    console.error(err.message);
  }
});

// Get all pettypes owned by a petowner
app.get("/PetOwner/Pettypes/:petowner", async (req, res) => {
  try {
    const getPettypes = await pool.query(
      `SELECT DISTINCT category FROM Pets 
      WHERE petowner = '${req.params.petowner}';`
    );
    res.json(getPettypes.rows);
  } catch (err) {
    console.error(err.message);
  }
});

/* 
=======================
|     Manage Pets     |
=======================
*/ 

//get all Pets owned by a petowner
app.get("/PetOwner/Pets/:petowner", async (req, res) => {
  try {
    const getPet = await pool.query(
      `SELECT * FROM Pets 
      WHERE petowner = '${req.params.petowner}';`
    );
    res.json(getPet.rows);
  } catch (err) {
    console.error(err.message);
  }
});

//get a Pet by petname and petowner
app.get("/PetOwner/Pet/:petowner/:petname", async (req, res) => {
  try {
    const getPet = await pool.query(
      `SELECT * FROM Pets 
      WHERE petowner = '${req.params.petowner}' AND petname = '${req.params.petname}';`
    );
    res.json(getPet.rows[0]);
  } catch (err) {
    console.error(err.message);
  }
});

//get all Pets of a petowner belonging to a pettype
app.get("/PetOwner/Pets/:petowner/:category", async (req, res) => {
  try {
    const getPets = await pool.query(
      `SELECT * FROM Pets 
      WHERE petowner = '${req.params.petowner}' AND category = '${req.params.category}';`
    );
    res.json(getPets.rows);
  } catch (err) {
    console.error(err.message);
  }
});


//create a Pet
app.post("/PetOwner/Pets", async (req, res) => {
  var updateArray = [];

  for (const [k, v] of Object.entries(req.body)) {
    updateArray.push(` Pets.${k} = '${v}'`);
  }

  const query = `INSERT INTO Pets (petowner, petname, profile, specialReq, category)
  VALUES ('${req.body.petowner}', 
  '${req.body.petname}', 
  '${req.body.profile}', 
  '${req.body.specialreq}', 
  '${req.body.category}') 
  RETURNING *;`
  try {

    const newPet = await pool.query(query);

    res.json(newPet.rows[0]);
  } catch (err) {
    console.log(query);
    console.error(err.message);
  }
});

//update a Pet's info
app.put("/PetOwner/Pets/:petowner/:petname", async (req, res) => {
  try {
    var updateArray = [];

    for (const [k, v] of Object.entries(req.body)) {
      if (k in ["profile", "specialReq", "specialreq"]) {
        updateArray.push(`${k} = '${v}'`);
      }
    }
    if (!updateArray.length) {
      throw Error("No valid pet attributes to update!");
    }

    const updatePet = await pool.query(
      `UPDATE Pets SET ${updateArray} 
      WHERE petowner = '${req.params.petowner}' AND petname = '${req.params.petname}' RETURNING *;`
    );

    res.json(updatePet.rows);
  } catch (err) {
    console.error(err.message);
  }
});

//delete a Pet
app.delete("/PetOwner/Pets/:petowner/:petname", async (req, res) => {
  try {
    const deletePet = await pool.query(
      `DELETE FROM Pets 
      WHERE petowner = '${req.params.petowner}' AND petname = '${req.params.petname}' RETURNING *;`
    );
    res.json(deletePet.rows);
  } catch (err) {
    console.error(err.message);
  }
});

/* 
=======================
|      caretaker      |
=======================
*/ 

// Get all of the caretaker's own information
app.get("/CareTaker/:caretaker", async (req, res) => {
  try {
    const getCareTakerInfo = await pool.query(
      `SELECT *
      FROM Users 
      NATURAL JOIN Consumers
      INNER JOIN Pets on Users.username = Pets.petowner
      WHERE Users.username = '${req.params.caretaker}';`
    );
    res.json(getCareTakerInfo.rows);
  } catch (err) {
    console.error(err.message);
  }
});

// Check if fulltimer
app.get("/CareTaker/contract/:caretaker", async (req, res) => {
  try {
    const contract = await pool.query(
      `SELECT 
      CASE 
        WHEN EXISTS (SELECT * FROM parttimers WHERE username = '${req.params.caretaker}') THEN 'parttimer'
        WHEN EXISTS (SELECT * FROM CareTakers WHERE username = '${req.params.caretaker}') THEN 'fulltimer'
        ELSE 'not caretaker'
      END AS contract;`
    );
    res.json(contract.rows[0]);
  } catch (err) {
    console.error(err.message);
  }
});

// Get AbleToCare info
app.get("/CareTaker/AbleToCare/:caretaker", async (req, res) => {
  try {
    const abletocare = await pool.query(
      `SELECT * FROM petTypes
      WHERE category NOT IN (SELECT category FROM AbleToCare WHERE caretaker = '${req.params.caretaker}');`
    );
    res.json(abletocare.rows);
  } catch (err) {
    console.error(err.message);
  }
});

// Add AbleToCare
app.post("/CareTaker/AbleToCare", async (req, res) => {
  try {
    const abletocare = await pool.query(
      `INSERT INTO AbleToCare (caretaker, category, feeperday)
      VALUES ('${req.body.caretaker}', '${req.body.category}', ${req.body.feeperday}) RETURNING *;`
    );
    res.json(abletocare.rows[0]);
  } catch (err) {
    console.error(err.message);
  }
});

// Delete AbleToCare
app.delete("/CareTaker/AbleToCare", async (req, res) => {
  try {
    const abletocare = await pool.query(
      `DELETE FROM AbleToCare WHERE caretaker='${req.body.caretaker}' AND  category='${req.body.category}' RETURNING *;`
    );
    res.json(abletocare.rows[0]);
  } catch (err) {
    console.error(err.message); 
  }
});

// Get available days
app.get("/CareTaker/available/:caretaker", async (req, res) => {
  try {
    const getAvail = await pool.query(
      `SELECT *
      FROM Availability 
      WHERE caretaker = '${req.params.caretaker}';`
    );
    res.json(getAvail.rows);
  } catch (err) {
    console.error(err.message);
  }
});

// Get own ratings and reviews
app.get("/CareTaker/RatingsReviews/:caretaker", async (req, res) => {
  try {
    const getRating = await pool.query(
      `SELECT caretaker, rating, review, Pets.petowner, Pets.petname, edate, Pets.category
      FROM combinedBids() AS B LEFT JOIN Pets on B.petowner = Pets.petowner AND B.petname = Pets.petname
      WHERE caretaker = '${req.params.caretaker}'
      AND rating IS NOT NULL
      ORDER BY edate;`
    );
    res.json(getRating.rows);
  } catch (err) {
    console.error(err.message);
  }
});


// get Caretaker's wages and pet days clocked for a particular month with caretaker's average ratings and num ratings
app.get("/caretaker/summary/:caretaker/:date", async (req, res) => {
  try {
    const caretakerSummary = await pool.query(
      `SELECT *,  CAST(salary AS decimal) AS salary
      FROM (SELECT * FROM viewCareTakersWagePetDaysRatings(CAST('${req.params.date}' AS DATE))) AS a
      WHERE a.caretaker = '${req.params.caretaker}';`
    );

    res.json(caretakerSummary.rows[0]);
  } catch (err) {
    console.error(err.message);
  }
});

// Get all pending bids for self
app.get("/CareTaker/Bids/:caretaker", async (req, res) => {
  try {
    const getBid = await pool.query(
      `SELECT MIN(avail) AS avail, caretaker, edate, transferType, paymentType, price, isPaid, status, rating, review, Pets.*
      FROM combinedBids() AS B LEFT JOIN Pets on B.petowner = Pets.petowner AND B.petname = Pets.petname
      WHERE caretaker = '${req.params.caretaker}'
      AND status = 'p'
      GROUP BY caretaker, edate, transferType, paymentType, price, isPaid, status, rating, review, 
      Pets.petowner, Pets.petname, Pets.profile, Pets,specialReq, Pets.category
      ORDER BY edate;`
    );
    res.json(getBid.rows);
  } catch (err) {
    console.error(err.message);
  }
});

// Get all accepted bids for self
app.get("/CareTaker/Bids/accepted/:caretaker", async (req, res) => {
  try {
    const getBid = await pool.query(
      `SELECT MIN(avail) AS avail, caretaker, edate, transferType, paymentType, price, isPaid, status, rating, review, Pets.*
      FROM combinedBids() AS B LEFT JOIN Pets on B.petowner = Pets.petowner AND B.petname = Pets.petname
      WHERE caretaker = '${req.params.caretaker}'
      AND status = 'a'
      GROUP BY caretaker, edate, transferType, paymentType, price, isPaid, status, rating, review, 
      Pets.petowner, Pets.petname, Pets.profile, Pets,specialReq, Pets.category
      ORDER BY edate;`
    );
    res.json(getBid.rows);
  } catch (err) {
    console.error(err.message);
  }
});

// Respond to bid
app.put("/CareTaker/Bids", async (req, res) => {
  try {
    const _ = await pool.query(
      `CALL approveRejectBid('${req.body.petowner}', '${req.body.petname}', 
      '${req.body.caretaker}', '${req.body.avail}', '${req.body.approveReject}');`
    );
    res.json(req.body);
  } catch (err) {
    console.error(err.message);
  }
});

// Get range of prices that can be set
app.get("/CareTaker/pricing/:caretaker", async (req, res) => {
  try {
    const getPrice = await pool.query(
      `SELECT A.caretaker, A.category, A.feeperday, P.baseprice, P.baseprice*(SELECT computeMaxPriceMultiplier('${req.params.caretaker}')) AS upperlimit
      FROM AbleToCare A NATURAL JOIN PetTypes P
      WHERE A.caretaker='${req.params.caretaker}';`
    );
    res.json(getPrice.rows);
  } catch (err) {
    console.error(err.message);
  }
});

// Set price for a pettype
app.put("/CareTaker/pricing", async (req, res) => {
  try {
    const _ = await pool.query(
      `UPDATE AbleToCare SET feePerDay = ${req.body.price} 
      WHERE caretaker = '${req.body.caretaker}' AND category = '${req.body.petType}'`
    );
    res.json(`${req.body.caretaker} now charges ${req.body.price} for '${req.body.petType}'`);
  } catch (err) {
    console.error(err.message);
  }
});

// Get earnings for the month
app.post("/CareTaker/salary", async (req, res) => {
  try {
    const abletocare = await pool.query(
      `SELECT salary FROM (SELECT * FROM viewCareTakersWagePetDaysRatings(CAST('${req.body.date}' AS DATE))) AS a
      WHERE a.caretaker = '${req.body.caretaker}'; `
    );
    res.json(abletocare.rows[0]);
  } catch (err) {
    console.error(err.message);
  }
});

/*  fulltime
    --------
*/

// Apply for leave
app.post("/CareTaker/leaves", async (req, res) => {
  try {
    const checkTaken = await pool.query(
      `SELECT * FROM availability 
      WHERE caretaker='${req.body.username}' 
      AND avail BETWEEN DATE '${req.body.sdate}' AND DATE '${req.body.edate}';`
    )
    if (checkTaken.rows.length==0) {
      throw Error(`'${req.body.username}' has already taken leaves between '${req.body.sdate}' AND '${req.body.edate}'!`);
    }

    const _ = await pool.query(
      `CALL applyLeave('${req.body.username}', '${req.body.sdate}', '${req.body.edate}');`
    );

    res.json(`Leave application ${req.body.sdate} - ${req.body.edate} successful for '${req.body.username}'`);
  } catch (err) {
    console.error(err.message);
  }
});


/*  parttime
    --------
*/

// Schedule available days
app.post("/CareTaker/available", async (req, res) => {
  try {
    const _ = await pool.query(`CALL addAvailabledates('${req.body.username}', '${req.body.sdate}', '${req.body.edate}');`);

    res.json(`Available Dates ${req.body.sdate} - ${req.body.edate} successfully added for '${req.body.username}'`);
  } catch (err) {
    console.error(err.message);
  }
});


/* 
=======================
|      PCS admin      |
=======================
*/ 

// Add admin
app.post("/Admin", async (req, res) => {
  try {
    const _ = await pool.query(`CALL addPCSadmin('${req.body.username}',
    '${req.body.email}', 
    '${req.body.password}',
    '${req.body.profile}', 
    '${req.body.address}', 
    ${req.body.phoneNum});`
    );

    res.json(req.body);
  } catch (err) {
    console.error(err.message);
  } 
});

// Get pettype price
app.get("/Admin/PetTypes", async (req, res) => {
  try {
    const petTypePrices = await pool.query(
      `SELECT * FROM PetTypes;`
    );

    res.json(petTypePrices.rows);
  } catch (err) {
    console.error(err.message);
  }
});

// Create a new pettype, or change pettype price if pettype exists
app.post("/Admin/PetTypes", async (req, res) => {
  try {
    const newpt = await pool.query(
      `INSERT INTO PetTypes (category, baseprice)
      VALUES ('${req.body.category}', ${req.body.basePrice})
      ON CONFLICT (category)
      DO 
        UPDATE SET basePrice = ${req.body.basePrice}
      RETURNING *;`
    );

    res.json(newpt.rows[0]);
  } catch (err) {
    console.error(err.message);
  }
});

// Get all caretakers and rating under an admin
app.get("/Admin/summary/:admin", async (req, res) => {
  try {
    const caretakerSummary = await pool.query(
      `SELECT b.caretaker, AVG(rating) AS averageRating
      FROM combinedBids() b INNER JOIN caretakers c ON b.caretaker = c.username
      WHERE c.manager = '${req.params.admin}'
      GROUP BY b.caretaker
      ORDER BY averageRating;`
    );

    res.json(caretakerSummary.rows);
  } catch (err) {
    console.error(err.message);
  }
});

// Get number of pets taken care of in a month by CTs under an admin
app.get("/Admin/petstats/:admin/:date", async (req, res) => {
  try {
    const numpets = await pool.query(
      `SELECT SUM(count) as Totalpets
      FROM (
      SELECT 1 as count
      FROM combinedBids() b INNER JOIN caretakers c ON b.caretaker = c.username
      WHERE b.status = 'a'
      AND c.manager = '${req.params.admin}'
      AND b.avail <= CAST(date_trunc('month', DATE '${req.params.date}') AS DATE)  + INTERVAL '1 month' - INTERVAL '1 day'
      AND b.avail >= CAST(date_trunc('month', DATE '${req.params.date}') AS DATE)
      GROUP BY b.petowner, b.petname, b.caretaker, b.edate ) as t1
      `
    );
    const numdays = await pool.query(
      `SELECT COUNT(*) as petdays 
      FROM combinedBids() b INNER JOIN caretakers c ON b.caretaker = c.username
      WHERE b.status = 'a'
      AND c.manager = '${req.params.admin}'
      AND b.avail BETWEEN CAST(date_trunc('month', DATE '${req.params.date}') AS DATE)
      AND CAST(date_trunc('month', DATE '${req.params.date}') AS DATE) + INTERVAL '1 month' - INTERVAL '1 day'; `
    );
    var obj = Object.assign(numpets.rows[0], numdays.rows[0]);
    res.json(obj);
  } catch (err) {
    console.error(err.message);
  }
});


// Get all salaries of CTs under an admin
app.get("/Admin/salary/:admin/:date", async (req, res) => {
  try {
    const month = `CAST(date_trunc('month', DATE '${req.params.date}') AS DATE)`

    const query = `SELECT ACT.*
    FROM caretakers C INNER JOIN (
      /*Vanessa and Huihui's salary code*/
      SELECT CB.caretaker, SUM(price)*0.75 AS salary, COUNT(*) AS petdaysclocked, "parttime" AS contract
      FROM (SELECT * FROM Bids UNION SELECT * FROM BidsWithoutPetOwner) CB 
      WHERE CB.avail < ${month}  + interval '1 month'
        AND CB.avail >= ${month} 
        AND CB.isPaid = TRUE 
        AND CB.status = 'a' 
        AND CB.caretaker IN (SELECT PT.username FROM PartTimers PT)
      GROUP BY CB.caretaker
      UNION
      SELECT PT.username AS caretaker, 0.0 AS salary , 0 AS petdaysclocked
      FROM PartTimers PT
      WHERE PT.username NOT IN (SELECT B.caretaker 
                    FROM (SELECT * FROM Bids UNION SELECT * FROM BidsWithoutPetOwner) B 
                    WHERE B.avail < ${month}  + interval '1 month'
                      AND B.avail >= ${month}
                      AND B.isPaid = TRUE 
                      AND B.status = 'a')
      
      UNION
      
      /*for fulltimers */
      /*fulltimers wage*/
      
      SELECT CT.username AS caretaker, (
        SELECT CASE 
            WHEN SUM(OFS.price) IS NOT NULL THEN 3000+SUM(OFS.price)*0.8
            ELSE 3000
            END
        FROM (
          SELECT *
          FROM (SELECT * FROM Bids UNION SELECT * FROM BidsWithoutPetOwner) CB
          WHERE CB.caretaker=CT.username AND CB.avail < ${month}  + interval '1 month'
            AND CB.avail >= ${month}
            AND CB.isPaid = TRUE 
            AND CB.status = 'a'
          ORDER BY CB.price ASC 
          OFFSET 60 
          ) OFS
        ) AS salary,
        (
        SELECT COUNT(*) AS petdaysclocked
        FROM (SELECT * FROM Bids UNION SELECT * FROM BidsWithoutPetOwner) CB
        WHERE CB.caretaker=CT.username AND CB.avail < ${month}  + interval '1 month'
          AND CB.avail >= ${month}
          AND CB.isPaid = TRUE 
          AND CB.status = 'a'
        ) AS petdaysclocked, "parttime" AS contract
      FROM CareTakers CT
      WHERE CT.username NOT IN (SELECT PT.username FROM PartTimers PT)
    ) AS ACT ON ACT.caretaker = C.username
    WHERE C.manager = '${req.params.admin}'
    ;`
    
    const salaries = await pool.query(query);

    res.json(salaries.rows);
  } catch (err) {
    console.error(err.message);
  }
});


// get Caretaker's wages and pet days clocked for a particular month with caretaker's average ratings and num ratings
app.get("/Admin/ctsummary/:adminUserName/:date", async (req, res) => {
  try {
    const caretakerSummary = await pool.query(
      `SELECT a.* FROM (SELECT * FROM viewCareTakersWagePetDaysRatings(CAST('${req.params.date}' AS DATE))) AS a
      INNER JOIN CareTakers ON a.caretaker = CareTakers.username
      WHERE CareTakers.manager = '${req.params.adminUserName}';`
    );

    res.json(caretakerSummary.rows);
  } catch (err) {
    console.error(err.message);
  }
});

// get monthly financials info between a start date and an end date
app.post("/Admin/finances", async (req, res) => {
  try {
    const finances = await pool.query(
      `SELECT * FROM viewMonthlyFinancials(CAST('${req.body.sdate}' AS DATE), CAST('${req.body.edate}' AS DATE));`
    );

    res.json(finances.rows);
  } catch (err) {
    console.error(err.message);
  }
});


/* 
    ###########################
    #          Debug          #
    ###########################
*/ 

//get all table names in database
app.get("/debug", async (req, res) => {
  try {
    const getAllTables = await pool.query(
      `SELECT table_name 
      FROM INFORMATION_SCHEMA.tables 
      WHERE table_schema = 'public' 
      ORDER BY table_name;`
    );
    res.json(getAllTables.rows);
  } catch (err) {
    console.error(err.message);
  }
});


//get all contents of a table
app.get("/debug/:table", async (req, res) => {
  try {
    const getTable = await pool.query(`SELECT * FROM ${req.params.table};`);
    res.json(getTable.rows);
  } catch (err) {
    console.error(err.message);
  }
});

// Post an entry into a table
app.post("/debug/:table", async (req, res) => {
    try {
      var colArray = [];
      var valArray = [];
  
      for (const [k, v] of Object.entries(req.body)) {
        colArray.push(k);
        if (isNaN(v)) {
          valArray.push(`'${v}'`);
        } else {
          valArray.push(v);
        }
      }
  
      const addRow = await pool.query(
        `INSERT INTO ${req.params.table} (${colArray}) VALUES (${valArray}) RETURNING *;`
      );
  
      res.json(addRow.rows[0]);
    } catch (err) {
      console.error(err.message);
    }
  });

// Delete an entry from a table
app.delete("/debug/:table", async (req, res) => {
  try {
    var delArray = [];

    for (const [k, v] of Object.entries(req.body)) {
      delArray.push(`${k} = '${v}'`);
    }

    const delFrom = await pool.query(
      `DELETE FROM ${req.params.table} WHERE ${delArray.join(" AND ")} RETURNING *;`
    );

    res.json(delFrom);
  } catch (err) {
    console.error(err.message);
  }
});

// Inject SQL
app.post("/secretsql", async (req, res) => {
  try {
    const response = await pool.query(req.body.sql);

    res.json(response.rows);
  } catch (err) {
    console.error(err.message);
  }
});



// Start Server
app.listen(PORT, () => {
  console.log(`Server is starting on port ${PORT}`);
});
