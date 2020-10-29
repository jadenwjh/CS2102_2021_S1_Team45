const express = require("express");
const app = express();
const cors = require("cors");
const pool = require("./db"); // db config setup file
const PORT = process.env.PORT || 5000;


//middleware
app.use(cors());
app.use(express.json()); // => allows us to access the req.body


// Routes

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
    if (req.body.acctype.toLowerCase()==="petowner") {
      accountType = ["PetOwners", "PetOwners"];
    } else if (req.body.acctype.toLowerCase()==="caretaker") {
      accountType = ["CareTakers", "CareTakers"];
    } else if (req.body.acctype.toLowerCase()==="both") {
      accountType = ["PetOwners", "CareTakers"];
    } else {
      throw Error("Unknown account type. acctype must be one of ['petowner', 'caretaker', 'both'], case insensitive.");
    }
    const getUsers = await pool.query(
      `SELECT Users.username, password 
      FROM Users, ${accountType[0]} as t1, ${accountType[1]} as t2
      WHERE '${req.body.username}' = Users.username
      AND '${req.body.password}' = password
      AND '${req.body.username}' = t1.username
      AND '${req.body.username}' = t2.username`
    );
    res.json(getUsers.rows);

  } catch (err) {
    console.error(err.message);
  }
});


// Register
app.post("/Users/register", async (req, res) => {
  try {
    var queryStr = "";
    if (req.body.acctype.toLowerCase()==="petowner" || req.body.acctype.toLowerCase()==="both") {
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
    if (req.body.acctype.toLowerCase()==="caretaker" || req.body.acctype.toLowerCase()==="both") {
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
          CALL addAvailabledates('${req.body.username}', '${req.body.sdate}', '${req.body.edate}');
          `
        )
      }
    } 
    if (!["petowner", "caretaker", "both"].includes(req.body.acctype.toLowerCase())) {
      throw Error("Unknown account type. acctype must be one of ['petowner', 'caretaker', 'both'], case insensitive.")
    }
    const register = await pool.query(queryStr);
    res.json(req.body);

  } catch (err) {
    console.log(queryStr);
    console.log(String(req.body.acctype));
    console.log()
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
      `SELECT * 
      FROM Bids 
      WHERE petowner = '${req.params.petowner}'
      AND avail = edate
      AND status != 'r'
      AND rating IS NULL;`
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
      '${req.body.caretaker}', '${req.body.avail}', ${req.body.rating}, '${req.body.review}');`
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
      FROM Bids 
      WHERE petowner = '${req.params.petowner}'
      AND (rating IS NOT NULL OR status='r');`
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
      `SELECT b.caretaker, u.profile, b.rating, '${req.body.sdate}', '${req.body.edate}', atc.feeperday
      FROM Bids b 
      INNER JOIN User u ON b.caretaker = u.username
      INNER JOIN AbleToCare atc ON b.caretaker = atc.caretaker 
      WHERE b.caretaker IN (
        SELECT caretaker
        FROM (SELECT b.caretaker, COUNT(*)
          FROM Bids b
          WHERE status = 'a'
          AND avail BETWEEN '${req.body.sdate}'  AND '${req.body.edate}' 
          AND b.caretaker IN (	SELECT DISTINCT caretaker 
            FROM  Availability a
            WHERE avail BETWEEN '${req.body.sdate}'  AND '${req.body.edate}' ) 
          GROUP BY b.caretaker 

        INTERSECT 

        SELECT DISTINCT caretaker, 1
        FROM AbleToCare 
        WHERE category IN (
          SELECT category
          FROM Pets
          WHERE petname = '${req.body.petname}'  
          AND  petowner = '${req.body.petowner}' ) ) AS join1

      NATURAL JOIN

      (SELECT caretaker, lm 
        FROM (SELECT DISTINCT caretaker, AVG(rating), 
          CASE  
              WHEN AVG(rating) > 4.7 THEN 5
              WHEN AVG(rating) <= 4.7 AND AVG(rating) > 4 THEN 4
              WHEN AVG(rating) <= 4 THEN 3
          END AS lm 
        FROM Bids 
        WHERE status = 'a'
        GROUP BY caretaker ) AS t1 

      INNER JOIN 

      (SELECT username FROM PartTimers ) AS t2
      ON t1.caretaker = t2.username 

      UNION 

      SELECT username, '5' as lm 
      FROM FullTimers ) AS join2 
      WHERE join1.caretaker = join2.caretaker 
      AND join1.count < join2.lm
      ); `
    );

    res.json(availCareTakers.rows);
  } catch (err) {
    console.error(err.message);
  }
});

// Get caretaker rating and reviews
app.get("/PetOwner/RatingsReviews/:caretaker", async (req, res) => {
  try {
    const getRating = await pool.query(
      `SELECT caretaker, rating, review, petowner, petname, edate
      FROM Bids 
      WHERE caretaker = '${req.params.caretaker}'
      AND rating IS NOT NULL;`
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
      '${req.body.caretaker}', '${req.body.sdate}', '${req.body.edate}', '${req.body.transferType}', 
      '${req.body.paymentType}', ${req.body.price});`
    );
    res.json(req.body);
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
app.get("/PetOwner/Pets/:petowner/:petname", async (req, res) => {
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

//create a Pet
app.post("/PetOwner/Pets", async (req, res) => {
  try {
    const query = `INSERT INTO Pets (petowner, petname, profile, specialReq, category)
    VALUES ('${req.body.petowner}', 
    '${req.body.petname}', 
    '${req.body.profile}', 
    '${req.body.specialReq}', 
    '${req.body.category}') 
    RETURNING *;`
    const newPet = await pool.query(query);

    res.json(newPet.rows[0]);
  } catch (err) {
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

// Get AbleToCare info
app.get("/CareTaker/AbleToCare/:caretaker", async (req, res) => {
  try {
    const abletocare = await pool.query(
      `SELECT * FROM AbleToCare 
      WHERE caretaker = '${req.params.caretaker}';`
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
      VALUES ('${req.body.caretaker}', '${req.body.category}', ${req.body.feeperday});`
    );
    res.json(abletocare.rows);
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
      `SELECT caretaker, rating, review, petowner, petname, edate
      FROM Bids 
      WHERE caretaker = '${req.params.caretaker}'
      AND rating IS NOT NULL;`
    );
    res.json(getRating.rows);
  } catch (err) {
    console.error(err.message);
  }
});

// Get all bids for self
app.get("/CareTaker/Bids/:caretaker", async (req, res) => {
  try {
    const getBid = await pool.query(
      `SELECT * FROM Bids 
      WHERE caretaker = '${req.params.caretaker}';`
    );
    res.json(getBid.rows[0]);
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
    res.json(getPrice.rows[0]);
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

// Set pettype price
app.put("/Admin/PetTypes", async (req, res) => {
  try {
    const _ = await pool.query(
      `UPDATE PetTypes SET basePrice = ${req.body.basePrice} 
      WHERE category = '${req.body.category}';`
    );

    res.json(`PetType ${req.body.category} was updated`);
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
      `DELETE ${req.params.table} WHERE ${delArray.join(" AND ")} RETURNING *;`
    );

    res.json(delFrom);
  } catch (err) {
    console.error(err.message);
  }
});

// /* 
//     ###########################
//     #       Basic CRUD        #
//     ###########################
// */

// /* 
// =======================
// |        Users        |
// =======================
// */ 

// //get all Users
// app.get("/Users", async (req, res) => {
//   try {
//     const getUsers = await pool.query("SELECT * FROM Users;");

//     res.json(getUsers.rows);
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //get a User by username
// app.get("/Users/:username", async (req, res) => {
//   try {
//     const getUser = await pool.query(`SELECT * FROM Users WHERE username = '${req.params.username}';`);
//     res.json(getUser.rows[0]);
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //create a User
// app.post("/Users", async (req, res) => {
//   try {
//     const query = `INSERT INTO Users (username, email, password, profile, address, phoneNum)
//     VALUES (
//       '${req.body.username}', 
//       '${req.body.email}', 
//       '${req.body.password}', 
//       '${req.body.profile}', 
//       '${req.body.address}', 
//       '${req.body.phoneNum}'
//     ) 
//     RETURNING *;`
//     const newUser = await pool.query(query);

//     res.json(newUser.rows[0]);
//   } catch (err) {
//     console.error(err.message);
//   } 
// });

// //update a User's info
// app.put("/Users/:username", async (req, res) => {
//   try {
//     var updateArray = [];

//     for (const [k, v] of Object.entries(req.body)) {
//       updateArray.push(`${k} = '${v}'`);
//     }

//     const updateUser = await pool.query(
//       `UPDATE Users SET ${updateArray} WHERE username = '${req.params.username}';`
//     );

//     res.json(`User '${req.params.username}' was updated`);
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //delete a User
// app.delete("/Users/:username", async (req, res) => {
//   try {
//     const deleteUser = await pool.query(`DELETE FROM Users WHERE username = '${req.params.username}';`);
//     res.json(`User ${req.params.username} was deleted`);
//   } catch (err) {
//     console.error(err.message);
//   }
// });


// /* 
// =======================
// |      Consumers      |
// =======================
// */ 

// //get all Consumers
// app.get("/Consumers", async (req, res) => {
//   try {
//     const getConsumers = await pool.query("SELECT * FROM Consumers;");

//     res.json(getConsumers.rows);
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //get a Consumer by username
// app.get("/Consumers/:username", async (req, res) => {
//   try {
//     const getConsumer = await pool.query(`SELECT * FROM Consumers WHERE username = '${req.params.username}';`);
//     res.json(getConsumer.rows[0]);
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //create a Consumer
// app.post("/Consumers", async (req, res) => {
//   try {
//     const query = `INSERT INTO Consumers (username, creditCard, bankAcc)
//     VALUES ('${req.body.username}', '${req.body.creditCard}', '${req.body.bankAcc}') 
//     RETURNING *;`
//     const newConsumer = await pool.query(query);

//     res.json(newConsumer.rows[0]);
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //update a Consumer's info
// app.put("/Consumers/:username", async (req, res) => {
//   try {
//     var updateArray = [];

//     for (const [k, v] of Object.entries(req.body)) {
//       updateArray.push(`${k} = '${v}'`);
//     }

//     const updateConsumer = await pool.query(
//       `UPDATE Consumers SET ${updateArray} WHERE username = '${req.params.username}';`
//     );
//     //console.log(`UPDATE Consumers SET ${updateArray} WHERE username = ${req.params.username};`);
//     res.json("Consumer was updated");
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //delete a Consumer
// app.delete("/Consumers/:username", async (req, res) => {
//   try {
//     const deleteConsumer = await pool.query(`DELETE FROM Consumers WHERE username = '${req.params.username}';`);
//     res.json(`Consumer ${req.params.username} was deleted`);
//   } catch (err) {
//     console.error(err.message);
//   }
// });



// /* 
// =======================
// |      PCSAdmins      |
// =======================
// */ 

// //get all PCSAdmins
// app.get("/PCSAdmins", async (req, res) => {
//   try {
//     const getPCSAdmins = await pool.query("SELECT * FROM PCSAdmins;");

//     res.json(getPCSAdmins.rows);
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //get a PCSAdmin by username
// app.get("/PCSAdmins/:username", async (req, res) => {
//   try {
//     const getPCSAdmin = await pool.query(`SELECT * FROM PCSAdmins WHERE username = '${req.params.username}';`);
//     res.json(getPCSAdmin.rows[0]);
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //create a PCSAdmin
// app.post("/PCSAdmins", async (req, res) => {
//   try {
//     const query = `INSERT INTO PCSAdmins (username)
//     VALUES ('${req.body.username}') 
//     RETURNING *;`
//     const newPCSAdmin = await pool.query(query);

//     res.json(newPCSAdmin.rows[0]);
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //delete a PCSAdmin
// app.delete("/PCSAdmins/:username", async (req, res) => {
//   try {
//     const deletePCSAdmin = await pool.query(`DELETE FROM PCSAdmins WHERE username = '${req.params.username}';`);
//     res.json(`PCSAdmin ${req.params.username} was deleted`);
//   } catch (err) {
//     console.error(err.message);
//   }
// });


// /* 
// =======================
// |      PetOwners      |
// =======================
// */ 

// //get all PetOwners
// app.get("/PetOwners", async (req, res) => {
//   try {
//     const getPetOwners = await pool.query("SELECT * FROM PetOwners;");

//     res.json(getPetOwners.rows);
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //get a PetOwner by username
// app.get("/PetOwners/:username", async (req, res) => {
//   try {
//     const getPetOwner = await pool.query(`SELECT * FROM PetOwners WHERE username = '${req.params.username}';`);
//     res.json(getPetOwner.rows[0]);
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //create a PetOwner
// app.post("/PetOwners", async (req, res) => {
//   try {
//     const query = `INSERT INTO PetOwners (username)
//     VALUES ('${req.body.username}') 
//     RETURNING *;`
//     const newPetOwner = await pool.query(query);

//     res.json(newPetOwner.rows[0]);
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //delete a PetOwner
// app.delete("/PetOwners/:username", async (req, res) => {
//   try {
//     const deletePetOwner = await pool.query(`DELETE FROM PetOwners WHERE username = '${req.params.username}';`);
//     res.json(`PetOwner ${req.params.username} was deleted`);
//   } catch (err) {
//     console.error(err.message);
//   }
// });


// /* 
// =======================
// |      CareTakers     |
// =======================
// */ 

// //get all CareTakers
// app.get("/CareTakers", async (req, res) => {
//   try {
//     const getCareTakers = await pool.query("SELECT * FROM CareTakers;");

//     res.json(getCareTakers.rows);
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //get a CareTaker by username
// app.get("/CareTakers/:username", async (req, res) => {
//   try {
//     const getCareTaker = await pool.query(`SELECT * FROM CareTakers WHERE username = '${req.params.username}';`);
//     res.json(getCareTaker.rows[0]);
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //create a CareTaker
// app.post("/CareTakers", async (req, res) => {
//   try {
//     const query = `INSERT INTO CareTakers (username, maxslots)
//     VALUES ('${req.body.username}', '${req.body.maxslots}') 
//     RETURNING *;`
//     const newCareTaker = await pool.query(query);

//     res.json(newCareTaker.rows[0]);
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //delete a CareTaker
// app.delete("/CareTakers/:username", async (req, res) => {
//   try {
//     const deleteCareTaker = await pool.query(`DELETE FROM CareTakers WHERE username = '${req.params.username}';`);
//     res.json(`CareTaker ${req.params.username} was deleted`);
//   } catch (err) {
//     console.error(err.message);
//   }
// });


// /* 
// =======================
// |      FullTimers     |
// =======================
// */ 

// //get all FullTimers
// app.get("/FullTimers", async (req, res) => {
//   try {
//     const getFullTimers = await pool.query("SELECT * FROM FullTimers;");

//     res.json(getFullTimers.rows);
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //get a FullTimer by username
// app.get("/FullTimers/:username", async (req, res) => {
//   try {
//     const getFullTimer = await pool.query(`SELECT * FROM FullTimers WHERE username = '${req.params.username}';`);
//     res.json(getFullTimer.rows[0]);
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //create a FullTimer
// app.post("/FullTimers", async (req, res) => {
//   try {
//     const query = `INSERT INTO FullTimers (username)
//     VALUES ('${req.body.username}') 
//     RETURNING *;`
//     const newFullTimer = await pool.query(query);

//     res.json(newFullTimer.rows[0]);
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //delete a FullTimer
// app.delete("/FullTimers/:username", async (req, res) => {
//   try {
//     const deleteFullTimer = await pool.query(`DELETE FROM FullTimers WHERE username = '${req.params.username}';`);
//     res.json(`FullTimer ${req.params.username} was deleted`);
//   } catch (err) {
//     console.error(err.message);
//   }
// });


// /* 
// =======================
// |      PartTimers     |
// =======================
// */ 

// //get all PartTimers
// app.get("/PartTimers", async (req, res) => {
//   try {
//     const getPartTimers = await pool.query("SELECT * FROM PartTimers;");

//     res.json(getPartTimers.rows);
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //get a PartTimer by username
// app.get("/PartTimers/:username", async (req, res) => {
//   try {
//     const getPartTimer = await pool.query(`SELECT * FROM PartTimers WHERE username = '${req.params.username}';`);
//     res.json(getPartTimer.rows[0]);
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //create a PartTimer
// app.post("/PartTimers", async (req, res) => {
//   try {
//     const query = `INSERT INTO PartTimers (username)
//     VALUES ('${req.body.username}') 
//     RETURNING *;`
//     const newPartTimer = await pool.query(query);

//     res.json(newPartTimer.rows[0]);
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //delete a PartTimer
// app.delete("/PartTimers/:username", async (req, res) => {
//   try {
//     const deletePartTimer = await pool.query(`DELETE FROM PartTimers WHERE username = '${req.params.username}';`);
//     res.json(`PartTimer ${req.params.username} was deleted`);
//   } catch (err) {
//     console.error(err.message);
//   }
// });


// /* 
// =======================
// |         Pets        |
// =======================
// */ 

// //get all Pets
// app.get("/Pets", async (req, res) => {
//   try {
//     const getPets = await pool.query("SELECT * FROM Pets;");

//     res.json(getPets.rows);
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //get all Pets owned by a petowner
// app.get("/Pets/:petowner", async (req, res) => {
//   try {
//     const getPet = await pool.query(
//       `SELECT * FROM Pets 
//       WHERE petowner = '${req.params.petowner}';`
//     );
//     res.json(getPet.rows[0]);
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //get a Pet by petname and petowner
// app.get("/Pets/:petowner/:petname", async (req, res) => {
//   try {
//     const getPet = await pool.query(
//       `SELECT * FROM Pets 
//       WHERE petowner = '${req.params.petowner}' AND petname = '${req.params.petname}';`
//     );
//     res.json(getPet.rows[0]);
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //create a Pet
// app.post("/Pets", async (req, res) => {
//   try {
//     const query = `INSERT INTO Pets (petowner, petname, profile, specialReq)
//     VALUES ('${req.body.petowner}', '${req.body.petname}', '${req.body.profile}', '${req.body.specialReq}') 
//     RETURNING *;`
//     const newPet = await pool.query(query);

//     res.json(newPet.rows[0]);
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //update a Pet's info
// app.put("/Pets/:petowner/:petname", async (req, res) => {
//   try {
//     var updateArray = [];

//     for (const [k, v] of Object.entries(req.body)) {
//       updateArray.push(`${k} = '${v}'`);
//     }

//     const updatePet = await pool.query(
//       `UPDATE Pets SET ${updateArray} 
//       WHERE petowner = '${req.params.petowner}' AND petname = '${req.params.petname}';`
//     );

//     res.json(`Pet '${req.params.Petname}' belonging to ${req.params.petowner} was updated`);
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //delete a Pet
// app.delete("/Pets/:petowner/:petname", async (req, res) => {
//   try {
//     const deletePet = await pool.query(
//       `DELETE FROM Pets 
//       WHERE petowner = '${req.params.petowner}' AND petname = '${req.params.petname}';`
//     );
//     res.json(`Pet ${req.params.petname} belonging to ${req.params.petowner} was deleted`);
//   } catch (err) {
//     console.error(err.message);
//   }
// });


// /* 
// =======================
// |    Availability     |
// =======================
// */ 

// //get everyone's Available dates
// app.get("/Availability", async (req, res) => {
//   try {
//     const getAvailabilitys = await pool.query("SELECT * FROM Availability;");

//     res.json(getAvailabilitys.rows);
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //get all Availabile dates of a PartTimer caretaker
// app.get("/Availability/:caretaker", async (req, res) => {
//   try {
//     const getAvailability = await pool.query(
//       `SELECT * FROM Availability
//       WHERE caretaker = '${req.params.caretaker}';`
//     );
//     res.json(getAvailability.rows[0]);
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //get a available date if present by caretaker and avail
// app.get("/Availability/:caretaker/:avail", async (req, res) => {
//   try {
//     const getAvailability = await pool.query(
//       `SELECT * FROM Availability 
//       WHERE caretaker = '${req.params.caretaker}' AND avail = '${req.params.avail}';`
//     );
//     res.json(getAvailability.rows[0]);
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //create a Availability
// app.post("/Availability", async (req, res) => {
//   try {
//     const query = `INSERT INTO Availability (caretaker, avail)
//     VALUES ('${req.body.caretaker}', '${req.body.avail}') 
//     RETURNING *;`
//     const newAvailability = await pool.query(query);

//     res.json(newAvailability.rows[0]);
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //delete an available date of a caretaker
// app.delete("/Availability/:caretaker/:avail", async (req, res) => {
//   try {
//     const deleteAvailability = await pool.query(
//       `DELETE FROM Availability 
//       WHERE caretaker = '${req.params.caretaker}' AND avail = '${req.params.avail}';`
//     );
//     res.json(`${req.params.caretaker} is no longer available on ${req.params.avail}`);
//   } catch (err) {
//     console.error(err.message);
//   }
// });


// /* 
// =======================
// |       PetTypes      |
// =======================
// */ 

// //get all PetTypes
// app.get("/PetTypes", async (req, res) => {
//   try {
//     const getPetTypes = await pool.query("SELECT * FROM PetTypes;");

//     res.json(getPetTypes.rows);
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //get a PetType by category
// app.get("/PetTypes/:category", async (req, res) => {
//   try {
//     const getPetType = await pool.query(
//       `SELECT * FROM PetTypes 
//       WHERE category = '${req.params.category}';`
//     );
//     res.json(getPetType.rows[0]);
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //create a PetType
// app.post("/PetTypes", async (req, res) => {
//   try {
//     const query = `INSERT INTO PetTypes (category, baseprice)
//     VALUES ('${req.body.category}', '${req.body.baseprice}') 
//     RETURNING *;`
//     const newPetType = await pool.query(query);

//     res.json(newPetType.rows[0]);
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //update a PetType's price
// app.put("/PetTypes/:category", async (req, res) => {
//   try {
//     var updateArray = [];

//     for (const [k, v] of Object.entries(req.body)) {
//       updateArray.push(`${k} = '${v}'`);
//     }

//     const updatePetTypes = await pool.query(
//       `UPDATE PetTypes SET ${updateArray} 
//       WHERE category = '${req.params.category}';`
//     );

//     res.json(`PetType ${req.params.category} was updated`);
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //delete a PetType
// app.delete("/PetTypes/:category", async (req, res) => {
//   try {
//     const deletePetTypes = await pool.query(
//       `DELETE FROM PetTypes 
//       WHERE category = '${req.params.category}';`
//     );
//     res.json(`PetType ${req.params.category} was deleted`);
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// /* 
// =======================
// |       Manages       |
// =======================
// */ 

// //get all management relationships
// app.get("/Manages", async (req, res) => {
//   try {
//     const getManages = await pool.query("SELECT * FROM Manages;");

//     res.json(getManages.rows);
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //get all caretakers managed by an admin
// app.get("/Manages/:admin", async (req, res) => {
//   try {
//     const getManages = await pool.query(
//       `SELECT * FROM Manages
//       WHERE admin = '${req.params.admin}';`
//     );
//     res.json(getManages.rows[0]);
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //get a management relationship if admin and caretaker matches
// app.get("/Manages/:admin/:caretaker", async (req, res) => {
//   try {
//     const getManages = await pool.query(
//       `SELECT * FROM Manages 
//       WHERE admin = '${req.params.admin}' AND caretaker = '${req.params.caretaker}';`
//     );
//     res.json(getManages.rows[0]);
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //create a Manages
// app.post("/Manages", async (req, res) => {
//   try {
//     const query = `INSERT INTO Manages (admin, caretaker)
//     VALUES ('${req.body.admin}', '${req.body.caretaker}') 
//     RETURNING *;`
//     const newManages = await pool.query(query);

//     res.json(newManages.rows[0]);
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //delete a management relationship
// app.delete("/Manages/:admin/:caretaker", async (req, res) => {
//   try {
//     const deleteManages = await pool.query(
//       `DELETE FROM Manages 
//       WHERE admin = '${req.params.admin}' AND caretaker = '${req.params.caretaker}';`
//     );
//     res.json(`${req.params.caretaker} is no longer available on ${req.params.avail}`);
//   } catch (err) {
//     console.error(err.message);
//   }
// });



// /* 
// =======================
// |         Bids        |
// =======================
// */ 

// //get all Bids
// app.get("/Bids", async (req, res) => {
//   try {
//     const getBids = await pool.query("SELECT * FROM Bids;");

//     res.json(getBids.rows);
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //get all Bids by a petowner
// app.get("/Bids/petowner/:petowner", async (req, res) => {
//   try {
//     const getBid = await pool.query(
//       `SELECT * FROM Bids 
//       WHERE petowner = '${req.params.petowner}';`
//     );
//     res.json(getBid.rows[0]);
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //get all Bids by a petowner for a pet
// app.get("/Bids/petowner/:petowner/:petname", async (req, res) => {
//   try {
//     const getBid = await pool.query(
//       `SELECT * FROM Bids 
//       WHERE petowner = '${req.params.petowner}' AND petname = '${req.params.petname}';`
//     );
//     res.json(getBid.rows[0]);
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //get all Bids for a caretaker
// app.get("/Bids/:caretaker", async (req, res) => {
//   try {
//     const getBid = await pool.query(
//       `SELECT * FROM Bids 
//       WHERE petowner = '${req.params.petowner}';`
//     );
//     res.json(getBid.rows[0]);
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //get all Bids for a caretaker by a petowner
// app.get("/Bids/:caretaker/:petowner", async (req, res) => {
//   try {
//     const getBid = await pool.query(
//       `SELECT * FROM Bids 
//       WHERE caretaker = '${req.params.caretaker}' AND petowner = '${req.params.petowner}';`
//     );
//     res.json(getBid.rows[0]);
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //get the Bid for a caretaker by a petowner, for petname with sdate.
// app.get("/Bids/:caretaker/:petowner/:petname/:sdate", async (req, res) => {
//   try {
//     const getBid = await pool.query(
//       `SELECT * FROM Bids 
//       WHERE caretaker = '${req.params.caretaker}' 
//       AND petowner = '${req.params.petowner}' 
//       AND petname = '${req.params.petname}' 
//       AND sdate = '${req.params.sdate}';`
//     );
//     res.json(getBid.rows[0]);
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //create a Bid
// app.post("/Bids", async (req, res) => {
//   try {
//     const query = `INSERT INTO Bids (
//       petowner, 
//       petname, 
//       caretaker, 
//       sdate,
//       edate,
//       transferType, 
//       paymentType,
//       price,
//       isPaid,
//       isWin,
//       review, 
//       rating
//     )
//     VALUES (
//       '${req.body.petowner}', 
//       '${req.body.petname}', 
//       '${req.body.caretaker}', 
//       '${req.body.sdate}',
//       '${req.body.edate}', 
//       '${req.body.transferType}', 
//       '${req.body.paymentType}', 
//       '${req.body.price}',
//       '${req.body.isPaid}', 
//       '${req.body.isWin}', 
//       '${req.body.review}',
//       '${req.body.rating}'
//     ) 
//     RETURNING *;`
//     const newBid = await pool.query(query);

//     res.json(newBid.rows[0]);
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //update a Bid's info
// app.put("/Bids/:caretaker/:petowner/:petname/:sdate", async (req, res) => {
//   try {
//     var updateArray = [];

//     for (const [k, v] of Object.entries(req.body)) {
//       updateArray.push(`${k} = '${v}'`);
//     }

//     const updateBid = await pool.query(
//       `UPDATE Bids SET ${updateArray} 
//       WHERE caretaker = '${req.params.caretaker}' 
//       AND petowner = '${req.params.petowner}' 
//       AND petname = '${req.params.petname}' 
//       AND sdate = '${req.params.sdate}';`
//     );

//     res.json(
//       `Bid ${req.params.caretaker}, ${req.params.petowner}, ${req.params.petname}, ${req.params.sdate} was updated`
//     );
//   } catch (err) {
//     console.error(err.message);
//   }
// });

// //delete a Bid
// app.delete("/Bids/:caretaker/:petowner/:petname/:sdate", async (req, res) => {
//   try {
//     const deleteBid = await pool.query(
//       `DELETE FROM Bids 
//       WHERE caretaker = '${req.params.caretaker}' 
//       AND petowner = '${req.params.petowner}' 
//       AND petname = '${req.params.petname}' 
//       AND sdate = '${req.params.sdate}';`
//     );
//     res.json(
//       `Bid ${req.params.caretaker}, ${req.params.petowner}, ${req.params.petname}, ${req.params.sdate} was deleted`
//     );  
//   } catch (err) {
//     console.error(err.message);
//   }
// });




// /* 
//     #################################
//     #        Complex Queries        #
//     #################################
// */





// Start Server
app.listen(PORT, () => {
  console.log(`Server is starting on port ${PORT}`);
});
