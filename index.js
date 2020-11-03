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
=======================
|    login/register   |
=======================
*/ 

// Login
app.post("/Users/login", async (req, res) => {
  try {
    var accountType;
    if (req.body.acctype==="petowner") {
      accountType = ["PetOwners", "PetOwners"];
    } else if (req.body.acctype==="caretaker") {
      accountType = ["CareTakers", "CareTakers"];
    } else if (req.body.acctype==="both") {
      accountType = ["PetOwners", "CareTakers"];
    } else {
      throw Error("Unknown account type. acctype must be one of ['petowner', 'caretaker', 'both'], case insensitive.");
    }
    const getUsers = await pool.query(
      `SELECT Users.*, '${req.body.acctype}' AS acctype
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
      if (req.body.isPartTime === false) {
        queryStr = queryStr.concat(
          `
          CALL addAvailabledates(
            '${req.body.username}', 
            date(date_trunc('year', now())), 
            date(date_trunc('year', now()) + interval '2 year' - interval '1 day')
            );
          `
        )
      }
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
      `SELECT MIN(avail) AS avail, caretaker, edate, transferType, paymentType, price, isPaid, status, rating, review, Pets.* 
      FROM Bids AS B1 LEFT JOIN Pets on B1.petowner = Pets.petowner AND B1.petname = Pets.petname
      WHERE B1.petowner = '${req.params.petowner}'
      AND (SELECT sum(B2.rating) FROM Bids AS B2 
        WHERE B1.edate = B2.avail
        AND B1.petowner = B2.petowner
        AND B1.petname = B2.petname
        AND B1.caretaker = B2.caretaker
        AND B1.edate = B2.edate) IS NULL
      AND status = 'p'
      AND (SELECT currentDate()) <= B1.edate
      GROUP BY caretaker, edate, transferType, paymentType, price, isPaid, status, rating, review, 
      Pets.petowner, Pets.petname, Pets.profile, Pets,specialReq, Pets.category
      ORDER BY edate;`
    );
    res.json(getRating.rows);
  } catch (err) {
    console.error(err.message);
  }
});

// Leave rating and reviews for caretaker
app.post("/PetOwner/RatingsReviews", async (req, res) => {
  try {
    const _ = await pool.query(
      `CALL rateCareTaker('${req.body.petowner}', '${req.body.petname}',
      '${req.body.caretaker}', '${req.body.avail}', ${req.body.rating}, '${req.body.review}');
      
      UPDATE Bids SET isPaid = ${req.body.isPaid} 
      WHERE avail = '${req.body.avail}'
      AND petowner = '${req.body.petowner}'
      AND petname = '${req.body.petname}'
      AND caretaker = '${req.body.caretaker}'; `
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
      `SELECT MIN(avail) AS avail, caretaker, edate, transferType, paymentType, price, isPaid, status, rating, review, Pets.* 
      FROM Bids LEFT JOIN Pets on Bids.petowner = Pets.petowner AND Bids.petname = Pets.petname
      WHERE Bids.petowner = '${req.params.petowner}'
      AND (SELECT sum(B2.rating) FROM Bids AS B2 
        WHERE Bids.edate = B2.avail
        AND Bids.petowner = B2.petowner
        AND Bids.petname = B2.petname
        AND Bids.caretaker = B2.caretaker
        AND Bids.edate = B2.edate) IS NULL 
      AND status='a'
      AND (SELECT currentDate()) >= edate
      GROUP BY caretaker, edate, transferType, paymentType, price, isPaid, status, rating, review, 
      Pets.petowner, Pets.petname, Pets.profile, Pets,specialReq, Pets.category
      ORDER BY Bids.edate;`
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
      `SELECT atc.caretaker, atc.category, atc.feeperday, '${req.body.sdate}' AS startdate, '${req.body.edate}' AS enddate
      FROM AbleToCare atc
      WHERE EXISTS (
        SELECT 1 FROM (
          SELECT availCT.caretaker, COUNT(*) AS days
          FROM (

            SELECT AV.caretaker, AV.avail 
            FROM (

              SELECT B.caretaker, B.avail, COUNT(*) AS cnt
              FROM Bids B
              WHERE B.status ='a'
              GROUP BY B.caretaker, B.avail

              UNION 
              
              SELECT Bw.caretaker, Bw.avail, COUNT(*) AS cnt
              FROM BidsWithoutPetOwner bw
              WHERE Bw.status ='a'
              GROUP BY Bw.caretaker, Bw.avail

              UNION 

              SELECT A.caretaker, A.avail, 0 AS cnt 
              FROM Availability A
              WHERE NOT EXISTS (SELECT * FROM Bids B WHERE B.caretaker=A.caretaker AND B.avail=A.avail AND B.status='a')

            ) AV
            WHERE AV.cnt<(SELECT computeMaxPet(AV.caretaker)) AND (AV.avail BETWEEN '${req.body.sdate}' AND '${req.body.edate}')
            ORDER BY avail ASC

          ) AS availCT
          GROUP BY availCT.caretaker
          HAVING COUNT(*) = (CAST(MAX('${req.body.edate}') AS date) - CAST(MIN('${req.body.sdate}') AS date)) +1
        ) AS t
      )
      AND atc.category = '${req.body.category}'; 
      `
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
      FROM Bids LEFT JOIN Pets on Bids.petowner = Pets.petowner AND Bids.petname = Pets.petname
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
      updateArray.push(`${k} = '${v}'`);
    }

    const updatePet = await pool.query(
      `UPDATE Pets SET ${updateArray} 
      WHERE petowner = '${req.params.petowner}' AND petname = '${req.params.petname}';`
    );

    res.json(`Pet ${req.params.petname} belonging to ${req.params.petowner} was updated`);
  } catch (err) {
    console.error(err.message);
  }
});

//delete a Pet
app.delete("/PetOwner/Pets/:petowner/:petname", async (req, res) => {
  try {
    const deletePet = await pool.query(
      `DELETE FROM Pets 
      WHERE petowner = '${req.params.petowner}' AND petname = '${req.params.petname}';`
    );
    res.json(`Pet ${req.params.petname} belonging to ${req.params.petowner} was deleted`);
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
      FROM Bids LEFT JOIN Pets on Bids.petowner = Pets.petowner AND Bids.petname = Pets.petname
      WHERE caretaker = '${req.params.caretaker}'
      AND rating IS NOT NULL
      ORDER BY edate;`
    );
    res.json(getRating.rows);
  } catch (err) {
    console.error(err.message);
  }
});

// Get all pending bids for self
app.get("/CareTaker/Bids/:caretaker", async (req, res) => {
  try {
    const getBid = await pool.query(
      `SELECT MIN(avail) AS avail, caretaker, edate, transferType, paymentType, price, isPaid, status, rating, review, Pets.*
      FROM Bids LEFT JOIN Pets on Bids.petowner = Pets.petowner AND Bids.petname = Pets.petname
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

/*  fulltime
    --------
*/

// Apply for leave
app.post("/CareTaker/leaves", async (req, res) => {
  try {
    const _ = await pool.query(`CALL applyLeave('${req.body.username}', '${req.body.sdate}', '${req.body.edate}');`);

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

// Get all caretakers and rating
app.get("/Admin/summary", async (req, res) => {
  try {
    const caretakerSummary = await pool.query(
      `SELECT caretaker, AVG(rating) AS averageRating from Bids 
      GROUP BY caretaker 
      ORDER BY averageRating;`
    );

    res.json(caretakerSummary.rows);
  } catch (err) {
    console.error(err.message);
  }
});
// TODO update

// Get all salaries
// TODO 

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
