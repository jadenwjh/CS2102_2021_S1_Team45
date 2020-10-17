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

//get an User by username
app.get("/Users/:username", async (req, res) => {
  try {
    const getUser = await pool.query(`SELECT * FROM Users WHERE username = ${req.params.username};`);
    res.json(getUser.rows[0]);
  } catch (err) {
    console.error(err.message);
  }
});

//create an User
app.post("/Users", async (req, res) => {
  try {
    const query = `INSERT INTO Users (username, email, profile, address, phoneNum)
    VALUES (${req.body.username}, ${req.body.email}, ${req.body.profile}, ${req.body.address}, ${req.body.phoneNum}) 
    RETURNING *;`
    const newTodo = await pool.query(query);

    res.json(newTodo.rows[0]);
  } catch (err) {
    console.error(err.message);
  }
});

//update an User's info
app.put("/Users/:username", async (req, res) => {
  try {
    var updateArray = [];

    for (const [k, v] of Object.entries(req.body)) {
      updateArray.push(`${k} = ${v}`);
    }

    const updateUser = await pool.query(
      `UPDATE Users SET ${updateArray} WHERE username = ${req.params.username};`
    );
    //console.log(`UPDATE Users SET ${updateArray} WHERE username = ${req.params.username};`);
    res.json("User was updated");
  } catch (err) {
    console.error(err.message);
  }
});

//delete a todo
app.delete("/Users/:username", async (req, res) => {
  try {
    const deleteUser = await pool.query(`DELETE FROM Users WHERE username = ${req.params.username};`);
    res.json(`User ${req.params.username} was deleted`);
  } catch (err) {
    console.error(err.message);
  }
});


/* 
=====
Debug
=====
*/ 



/* 
    #################################
    #        Complex Triggers       #
    #################################
*/





// Start Server
app.listen(PORT, () => {
  console.log(`Server is starting on port ${PORT}`);
});
