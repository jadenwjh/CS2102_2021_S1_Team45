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
app.get("/debug/:name", async (req, res) => {
  try {
    const getTable = await pool.query(`SELECT * FROM ${req.params.name};`);
    res.json(getTable.rows);
  } catch (err) {
    console.error(err.message);
  }
});

/* 
    ###########################
    #       Basic CRUD        #
    ###########################
*/

/* 
=======================
|        Users        |
=======================
*/ 

//get all Users
app.get("/Users", async (req, res) => {
  try {
    const getUsers = await pool.query("SELECT * FROM Users;");

    res.json(getUsers.rows);
  } catch (err) {
    console.error(err.message);
  }
});

//get a User by username
app.get("/Users/:username", async (req, res) => {
  try {
    const getUser = await pool.query(`SELECT * FROM Users WHERE username = '${req.params.username}';`);
    res.json(getUser.rows[0]);
  } catch (err) {
    console.error(err.message);
  }
});

//create a User
app.post("/Users", async (req, res) => {
  try {
    const query = `INSERT INTO Users (username, email, profile, address, phoneNum)
    VALUES (
      '${req.body.username}', 
      '${req.body.email}', 
      '${req.body.profile}', 
      '${req.body.address}', 
      '${req.body.phoneNum}'
    ) 
    RETURNING *;`
    const newUser = await pool.query(query);

    res.json(newUser.rows[0]);
  } catch (err) {
    console.error(err.message);
  }
});

//update a User's info
app.put("/Users/:username", async (req, res) => {
  try {
    var updateArray = [];

    for (const [k, v] of Object.entries(req.body)) {
      updateArray.push(`${k} = '${v}'`);
    }

    const updateUser = await pool.query(
      `UPDATE Users SET ${updateArray} WHERE username = '${req.params.username}';`
    );

    res.json(`User '${req.params.username}' was updated`);
  } catch (err) {
    console.error(err.message);
  }
});

//delete a User
app.delete("/Users/:username", async (req, res) => {
  try {
    const deleteUser = await pool.query(`DELETE FROM Users WHERE username = '${req.params.username}';`);
    res.json(`User ${req.params.username} was deleted`);
  } catch (err) {
    console.error(err.message);
  }
});


/* 
=======================
|      Consumers      |
=======================
*/ 

//get all Consumers
app.get("/Consumers", async (req, res) => {
  try {
    const getConsumers = await pool.query("SELECT * FROM Consumers;");

    res.json(getConsumers.rows);
  } catch (err) {
    console.error(err.message);
  }
});

//get a Consumer by username
app.get("/Consumers/:username", async (req, res) => {
  try {
    const getConsumer = await pool.query(`SELECT * FROM Consumers WHERE username = '${req.params.username}';`);
    res.json(getConsumer.rows[0]);
  } catch (err) {
    console.error(err.message);
  }
});

//create a Consumer
app.post("/Consumers", async (req, res) => {
  try {
    const query = `INSERT INTO Consumers (username, creditCard, bankAcc)
    VALUES ('${req.body.username}', '${req.body.creditCard}', '${req.body.bankAcc}') 
    RETURNING *;`
    const newConsumer = await pool.query(query);

    res.json(newConsumer.rows[0]);
  } catch (err) {
    console.error(err.message);
  }
});

//update a Consumer's info
app.put("/Consumers/:username", async (req, res) => {
  try {
    var updateArray = [];

    for (const [k, v] of Object.entries(req.body)) {
      updateArray.push(`${k} = '${v}'`);
    }

    const updateConsumer = await pool.query(
      `UPDATE Consumers SET ${updateArray} WHERE username = '${req.params.username}';`
    );
    //console.log(`UPDATE Consumers SET ${updateArray} WHERE username = ${req.params.username};`);
    res.json("Consumer was updated");
  } catch (err) {
    console.error(err.message);
  }
});

//delete a Consumer
app.delete("/Consumers/:username", async (req, res) => {
  try {
    const deleteConsumer = await pool.query(`DELETE FROM Consumers WHERE username = '${req.params.username}';`);
    res.json(`Consumer ${req.params.username} was deleted`);
  } catch (err) {
    console.error(err.message);
  }
});



/* 
=======================
|      PCSAdmins      |
=======================
*/ 

//get all PCSAdmins
app.get("/PCSAdmins", async (req, res) => {
  try {
    const getPCSAdmins = await pool.query("SELECT * FROM PCSAdmins;");

    res.json(getPCSAdmins.rows);
  } catch (err) {
    console.error(err.message);
  }
});

//get a PCSAdmin by username
app.get("/PCSAdmins/:username", async (req, res) => {
  try {
    const getPCSAdmin = await pool.query(`SELECT * FROM PCSAdmins WHERE username = '${req.params.username}';`);
    res.json(getPCSAdmin.rows[0]);
  } catch (err) {
    console.error(err.message);
  }
});

//create a PCSAdmin
app.post("/PCSAdmins", async (req, res) => {
  try {
    const query = `INSERT INTO PCSAdmins (username)
    VALUES ('${req.body.username}') 
    RETURNING *;`
    const newPCSAdmin = await pool.query(query);

    res.json(newPCSAdmin.rows[0]);
  } catch (err) {
    console.error(err.message);
  }
});

//delete a PCSAdmin
app.delete("/PCSAdmins/:username", async (req, res) => {
  try {
    const deletePCSAdmin = await pool.query(`DELETE FROM PCSAdmins WHERE username = '${req.params.username}';`);
    res.json(`PCSAdmin ${req.params.username} was deleted`);
  } catch (err) {
    console.error(err.message);
  }
});


/* 
=======================
|      PetOwners      |
=======================
*/ 

//get all PetOwners
app.get("/PetOwners", async (req, res) => {
  try {
    const getPetOwners = await pool.query("SELECT * FROM PetOwners;");

    res.json(getPetOwners.rows);
  } catch (err) {
    console.error(err.message);
  }
});

//get a PetOwner by username
app.get("/PetOwners/:username", async (req, res) => {
  try {
    const getPetOwner = await pool.query(`SELECT * FROM PetOwners WHERE username = '${req.params.username}';`);
    res.json(getPetOwner.rows[0]);
  } catch (err) {
    console.error(err.message);
  }
});

//create a PetOwner
app.post("/PetOwners", async (req, res) => {
  try {
    const query = `INSERT INTO PetOwners (username)
    VALUES ('${req.body.username}') 
    RETURNING *;`
    const newPetOwner = await pool.query(query);

    res.json(newPetOwner.rows[0]);
  } catch (err) {
    console.error(err.message);
  }
});

//delete a PetOwner
app.delete("/PetOwners/:username", async (req, res) => {
  try {
    const deletePetOwner = await pool.query(`DELETE FROM PetOwners WHERE username = '${req.params.username}';`);
    res.json(`PetOwner ${req.params.username} was deleted`);
  } catch (err) {
    console.error(err.message);
  }
});


/* 
=======================
|      CareTakers     |
=======================
*/ 

//get all CareTakers
app.get("/CareTakers", async (req, res) => {
  try {
    const getCareTakers = await pool.query("SELECT * FROM CareTakers;");

    res.json(getCareTakers.rows);
  } catch (err) {
    console.error(err.message);
  }
});

//get a CareTaker by username
app.get("/CareTakers/:username", async (req, res) => {
  try {
    const getCareTaker = await pool.query(`SELECT * FROM CareTakers WHERE username = '${req.params.username}';`);
    res.json(getCareTaker.rows[0]);
  } catch (err) {
    console.error(err.message);
  }
});

//create a CareTaker
app.post("/CareTakers", async (req, res) => {
  try {
    const query = `INSERT INTO CareTakers (username, maxslots)
    VALUES ('${req.body.username}', '${req.body.maxslots}') 
    RETURNING *;`
    const newCareTaker = await pool.query(query);

    res.json(newCareTaker.rows[0]);
  } catch (err) {
    console.error(err.message);
  }
});

//delete a CareTaker
app.delete("/CareTakers/:username", async (req, res) => {
  try {
    const deleteCareTaker = await pool.query(`DELETE FROM CareTakers WHERE username = '${req.params.username}';`);
    res.json(`CareTaker ${req.params.username} was deleted`);
  } catch (err) {
    console.error(err.message);
  }
});


/* 
=======================
|      FullTimers     |
=======================
*/ 

//get all FullTimers
app.get("/FullTimers", async (req, res) => {
  try {
    const getFullTimers = await pool.query("SELECT * FROM FullTimers;");

    res.json(getFullTimers.rows);
  } catch (err) {
    console.error(err.message);
  }
});

//get a FullTimer by username
app.get("/FullTimers/:username", async (req, res) => {
  try {
    const getFullTimer = await pool.query(`SELECT * FROM FullTimers WHERE username = '${req.params.username}';`);
    res.json(getFullTimer.rows[0]);
  } catch (err) {
    console.error(err.message);
  }
});

//create a FullTimer
app.post("/FullTimers", async (req, res) => {
  try {
    const query = `INSERT INTO FullTimers (username)
    VALUES ('${req.body.username}') 
    RETURNING *;`
    const newFullTimer = await pool.query(query);

    res.json(newFullTimer.rows[0]);
  } catch (err) {
    console.error(err.message);
  }
});

//delete a FullTimer
app.delete("/FullTimers/:username", async (req, res) => {
  try {
    const deleteFullTimer = await pool.query(`DELETE FROM FullTimers WHERE username = '${req.params.username}';`);
    res.json(`FullTimer ${req.params.username} was deleted`);
  } catch (err) {
    console.error(err.message);
  }
});


/* 
=======================
|      PartTimers     |
=======================
*/ 

//get all PartTimers
app.get("/PartTimers", async (req, res) => {
  try {
    const getPartTimers = await pool.query("SELECT * FROM PartTimers;");

    res.json(getPartTimers.rows);
  } catch (err) {
    console.error(err.message);
  }
});

//get a PartTimer by username
app.get("/PartTimers/:username", async (req, res) => {
  try {
    const getPartTimer = await pool.query(`SELECT * FROM PartTimers WHERE username = '${req.params.username}';`);
    res.json(getPartTimer.rows[0]);
  } catch (err) {
    console.error(err.message);
  }
});

//create a PartTimer
app.post("/PartTimers", async (req, res) => {
  try {
    const query = `INSERT INTO PartTimers (username)
    VALUES ('${req.body.username}') 
    RETURNING *;`
    const newPartTimer = await pool.query(query);

    res.json(newPartTimer.rows[0]);
  } catch (err) {
    console.error(err.message);
  }
});

//delete a PartTimer
app.delete("/PartTimers/:username", async (req, res) => {
  try {
    const deletePartTimer = await pool.query(`DELETE FROM PartTimers WHERE username = '${req.params.username}';`);
    res.json(`PartTimer ${req.params.username} was deleted`);
  } catch (err) {
    console.error(err.message);
  }
});


/* 
=======================
|         Pets        |
=======================
*/ 

//get all Pets
app.get("/Pets", async (req, res) => {
  try {
    const getPets = await pool.query("SELECT * FROM Pets;");

    res.json(getPets.rows);
  } catch (err) {
    console.error(err.message);
  }
});

//get all Pets owned by a petowner
app.get("/Pets/:petowner", async (req, res) => {
  try {
    const getPet = await pool.query(
      `SELECT * FROM Pets 
      WHERE petowner = '${req.params.petowner}';`
    );
    res.json(getPet.rows[0]);
  } catch (err) {
    console.error(err.message);
  }
});

//get a Pet by petname and petowner
app.get("/Pets/:petowner/:petname", async (req, res) => {
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
app.post("/Pets", async (req, res) => {
  try {
    const query = `INSERT INTO Pets (petowner, petname, profile, specialReq)
    VALUES ('${req.body.petowner}', '${req.body.petname}', '${req.body.profile}', '${req.body.specialReq}') 
    RETURNING *;`
    const newPet = await pool.query(query);

    res.json(newPet.rows[0]);
  } catch (err) {
    console.error(err.message);
  }
});

//update a Pet's info
app.put("/Pets/:petowner/:petname", async (req, res) => {
  try {
    var updateArray = [];

    for (const [k, v] of Object.entries(req.body)) {
      updateArray.push(`${k} = '${v}'`);
    }

    const updatePet = await pool.query(
      `UPDATE Pets SET ${updateArray} 
      WHERE petowner = '${req.params.petowner}' AND petname = '${req.params.petname}';`
    );

    res.json(`Pet '${req.params.Petname}' belonging to ${req.params.petowner} was updated`);
  } catch (err) {
    console.error(err.message);
  }
});

//delete a Pet
app.delete("/Pets/:petowner/:petname", async (req, res) => {
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
|    Availability     |
=======================
*/ 

//get everyone's Available dates
app.get("/Availability", async (req, res) => {
  try {
    const getAvailabilitys = await pool.query("SELECT * FROM Availability;");

    res.json(getAvailabilitys.rows);
  } catch (err) {
    console.error(err.message);
  }
});

//get all Availabile dates of a PartTimer caretaker
app.get("/Availability/:caretaker", async (req, res) => {
  try {
    const getAvailability = await pool.query(
      `SELECT * FROM Availability
      WHERE caretaker = '${req.params.caretaker}';`
    );
    res.json(getAvailability.rows[0]);
  } catch (err) {
    console.error(err.message);
  }
});

//get a available date if present by caretaker and avail
app.get("/Availability/:caretaker/:avail", async (req, res) => {
  try {
    const getAvailability = await pool.query(
      `SELECT * FROM Availability 
      WHERE caretaker = '${req.params.caretaker}' AND avail = '${req.params.avail}';`
    );
    res.json(getAvailability.rows[0]);
  } catch (err) {
    console.error(err.message);
  }
});

//create a Availability
app.post("/Availability", async (req, res) => {
  try {
    const query = `INSERT INTO Availability (caretaker, avail)
    VALUES ('${req.body.caretaker}', '${req.body.avail}') 
    RETURNING *;`
    const newAvailability = await pool.query(query);

    res.json(newAvailability.rows[0]);
  } catch (err) {
    console.error(err.message);
  }
});

//delete an available date of a caretaker
app.delete("/Availability/:caretaker/:avail", async (req, res) => {
  try {
    const deleteAvailability = await pool.query(
      `DELETE FROM Availability 
      WHERE caretaker = '${req.params.caretaker}' AND avail = '${req.params.avail}';`
    );
    res.json(`${req.params.caretaker} is no longer available on ${req.params.avail}`);
  } catch (err) {
    console.error(err.message);
  }
});


/* 
=======================
|   Unavailability    |
=======================
*/ 

//get all Unavailabile days
app.get("/Unavailability", async (req, res) => {
  try {
    const getUnavailabilitys = await pool.query("SELECT * FROM Unavailability;");

    res.json(getUnavailabilitys.rows);
  } catch (err) {
    console.error(err.message);
  }
});

//get all Unavailabile dates of a FullTimer caretaker
app.get("/Unavailability/:caretaker", async (req, res) => {
  try {
    const getUnavailability = await pool.query(
      `SELECT * FROM Unavailability
      WHERE caretaker = '${req.params.caretaker}';`
    );
    res.json(getUnavailability.rows[0]);
  } catch (err) {
    console.error(err.message);
  }
});

//get n unavailable date if present by caretaker and avail
app.get("/Unavailability/:caretaker/:avail", async (req, res) => {
  try {
    const getUnavailability = await pool.query(
      `SELECT * FROM Unavailability 
      WHERE caretaker = '${req.params.caretaker}' AND avail = '${req.params.avail}';`
    );
    res.json(getUnavailability.rows[0]);
  } catch (err) {
    console.error(err.message);
  }
});

//create a Unavailability
app.post("/Unavailability", async (req, res) => {
  try {
    const query = `INSERT INTO Unavailability (caretaker, avail)
    VALUES ('${req.body.caretaker}', '${req.body.avail}') 
    RETURNING *;`
    const newUnavailability = await pool.query(query);

    res.json(newUnavailability.rows[0]);
  } catch (err) {
    console.error(err.message);
  }
});

//delete an available date of a caretaker
app.delete("/Unavailability/:caretaker/:avail", async (req, res) => {
  try {
    const deleteUnavailability = await pool.query(
      `DELETE FROM Unavailability 
      WHERE caretaker = '${req.params.caretaker}' AND avail = '${req.params.avail}';`
    );
    res.json(`${req.params.caretaker} is no longer available on ${req.params.avail}`);
  } catch (err) {
    console.error(err.message);
  }
});



/* 
=======================
|       PetTypes      |
=======================
*/ 

//get all PetTypes
app.get("/PetTypes", async (req, res) => {
  try {
    const getPetTypes = await pool.query("SELECT * FROM PetTypes;");

    res.json(getPetTypes.rows);
  } catch (err) {
    console.error(err.message);
  }
});

//get a PetType by category
app.get("/PetTypes/:category", async (req, res) => {
  try {
    const getPetType = await pool.query(
      `SELECT * FROM PetTypes 
      WHERE category = '${req.params.category}';`
    );
    res.json(getPetType.rows[0]);
  } catch (err) {
    console.error(err.message);
  }
});

//create a PetType
app.post("/PetTypes", async (req, res) => {
  try {
    const query = `INSERT INTO PetTypes (category, baseprice)
    VALUES ('${req.body.category}', '${req.body.baseprice}') 
    RETURNING *;`
    const newPetType = await pool.query(query);

    res.json(newPetType.rows[0]);
  } catch (err) {
    console.error(err.message);
  }
});

//update a PetType's price
app.put("/PetTypes/:category", async (req, res) => {
  try {
    var updateArray = [];

    for (const [k, v] of Object.entries(req.body)) {
      updateArray.push(`${k} = '${v}'`);
    }

    const updatePetTypes = await pool.query(
      `UPDATE PetTypes SET ${updateArray} 
      WHERE category = '${req.params.category}';`
    );

    res.json(`PetType ${req.params.category} was updated`);
  } catch (err) {
    console.error(err.message);
  }
});

//delete a PetType
app.delete("/PetTypes/:category", async (req, res) => {
  try {
    const deletePetTypes = await pool.query(
      `DELETE FROM PetTypes 
      WHERE category = '${req.params.category}';`
    );
    res.json(`PetType ${req.params.category} was deleted`);
  } catch (err) {
    console.error(err.message);
  }
});

/* 
=======================
|       Manages       |
=======================
*/ 

//get all management relationships
app.get("/Manages", async (req, res) => {
  try {
    const getManages = await pool.query("SELECT * FROM Manages;");

    res.json(getManages.rows);
  } catch (err) {
    console.error(err.message);
  }
});

//get all caretakers managed by an admin
app.get("/Manages/:admin", async (req, res) => {
  try {
    const getManages = await pool.query(
      `SELECT * FROM Manages
      WHERE admin = '${req.params.admin}';`
    );
    res.json(getManages.rows[0]);
  } catch (err) {
    console.error(err.message);
  }
});

//get a management relationship if admin and caretaker matches
app.get("/Manages/:admin/:caretaker", async (req, res) => {
  try {
    const getManages = await pool.query(
      `SELECT * FROM Manages 
      WHERE admin = '${req.params.admin}' AND caretaker = '${req.params.caretaker}';`
    );
    res.json(getManages.rows[0]);
  } catch (err) {
    console.error(err.message);
  }
});

//create a Manages
app.post("/Manages", async (req, res) => {
  try {
    const query = `INSERT INTO Manages (admin, caretaker)
    VALUES ('${req.body.admin}', '${req.body.caretaker}') 
    RETURNING *;`
    const newManages = await pool.query(query);

    res.json(newManages.rows[0]);
  } catch (err) {
    console.error(err.message);
  }
});

//delete a management relationship
app.delete("/Manages/:admin/:caretaker", async (req, res) => {
  try {
    const deleteManages = await pool.query(
      `DELETE FROM Manages 
      WHERE admin = '${req.params.admin}' AND caretaker = '${req.params.caretaker}';`
    );
    res.json(`${req.params.caretaker} is no longer available on ${req.params.avail}`);
  } catch (err) {
    console.error(err.message);
  }
});



/* 
=======================
|         Bids        |
=======================
*/ 

//get all Bids
app.get("/Bids", async (req, res) => {
  try {
    const getBids = await pool.query("SELECT * FROM Bids;");

    res.json(getBids.rows);
  } catch (err) {
    console.error(err.message);
  }
});

//get all Bids by a petowner
app.get("/Bids/petowner/:petowner", async (req, res) => {
  try {
    const getBid = await pool.query(
      `SELECT * FROM Bids 
      WHERE petowner = '${req.params.petowner}';`
    );
    res.json(getBid.rows[0]);
  } catch (err) {
    console.error(err.message);
  }
});

//get all Bids by a petowner for a pet
app.get("/Bids/petowner/:petowner/:petname", async (req, res) => {
  try {
    const getBid = await pool.query(
      `SELECT * FROM Bids 
      WHERE petowner = '${req.params.petowner}' AND petname = '${req.params.petname}';`
    );
    res.json(getBid.rows[0]);
  } catch (err) {
    console.error(err.message);
  }
});

//get all Bids for a caretaker
app.get("/Bids/:caretaker", async (req, res) => {
  try {
    const getBid = await pool.query(
      `SELECT * FROM Bids 
      WHERE petowner = '${req.params.petowner}';`
    );
    res.json(getBid.rows[0]);
  } catch (err) {
    console.error(err.message);
  }
});

//get all Bids for a caretaker by a petowner
app.get("/Bids/:caretaker/:petowner", async (req, res) => {
  try {
    const getBid = await pool.query(
      `SELECT * FROM Bids 
      WHERE caretaker = '${req.params.caretaker}' AND petowner = '${req.params.petowner}';`
    );
    res.json(getBid.rows[0]);
  } catch (err) {
    console.error(err.message);
  }
});

//get the Bid for a caretaker by a petowner, for petname with sdate.
app.get("/Bids/:caretaker/:petowner/:petname/:sdate", async (req, res) => {
  try {
    const getBid = await pool.query(
      `SELECT * FROM Bids 
      WHERE caretaker = '${req.params.caretaker}' 
      AND petowner = '${req.params.petowner}' 
      AND petname = '${req.params.petname}' 
      AND sdate = '${req.params.sdate}';`
    );
    res.json(getBid.rows[0]);
  } catch (err) {
    console.error(err.message);
  }
});

//create a Bid
app.post("/Bids", async (req, res) => {
  try {
    const query = `INSERT INTO Bids (
      petowner, 
      petname, 
      caretaker, 
      sdate,
      edate,
      transferType, 
      paymentType,
      price,
      isPaid,
      isWin,
      review, 
      rating
    )
    VALUES (
      '${req.body.petowner}', 
      '${req.body.petname}', 
      '${req.body.caretaker}', 
      '${req.body.sdate}',
      '${req.body.edate}', 
      '${req.body.transferType}', 
      '${req.body.paymentType}', 
      '${req.body.price}',
      '${req.body.isPaid}', 
      '${req.body.isWin}', 
      '${req.body.review}',
      '${req.body.rating}'
    ) 
    RETURNING *;`
    const newBid = await pool.query(query);

    res.json(newBid.rows[0]);
  } catch (err) {
    console.error(err.message);
  }
});

//update a Bid's info
app.put("/Bids/:caretaker/:petowner/:petname/:sdate", async (req, res) => {
  try {
    var updateArray = [];

    for (const [k, v] of Object.entries(req.body)) {
      updateArray.push(`${k} = '${v}'`);
    }

    const updateBid = await pool.query(
      `UPDATE Bids SET ${updateArray} 
      WHERE caretaker = '${req.params.caretaker}' 
      AND petowner = '${req.params.petowner}' 
      AND petname = '${req.params.petname}' 
      AND sdate = '${req.params.sdate}';`
    );

    res.json(
      `Bid ${req.params.caretaker}, ${req.params.petowner}, ${req.params.petname}, ${req.params.sdate} was updated`
    );
  } catch (err) {
    console.error(err.message);
  }
});

//delete a Bid
app.delete("/Bids/:caretaker/:petowner/:petname/:sdate", async (req, res) => {
  try {
    const deleteBid = await pool.query(
      `DELETE FROM Bids 
      WHERE caretaker = '${req.params.caretaker}' 
      AND petowner = '${req.params.petowner}' 
      AND petname = '${req.params.petname}' 
      AND sdate = '${req.params.sdate}';`
    );
    res.json(
      `Bid ${req.params.caretaker}, ${req.params.petowner}, ${req.params.petname}, ${req.params.sdate} was deleted`
    );  
  } catch (err) {
    console.error(err.message);
  }
});




/* 
    #################################
    #        Complex Queries        #
    #################################
*/





// Start Server
app.listen(PORT, () => {
  console.log(`Server is starting on port ${PORT}`);
});
