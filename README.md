# RESTful API for CS2102 Database Systems Project

### Stack: Node.js, express, PostgreSQL

### Deployed on heroku.com at https://shielded-oasis-35437.herokuapp.com/

I might integrate swagger or a similar API generator but for now, I'm lazy and will just list it here:

&nbsp;

> **NOTE**: For requests bodies, when applicable, all keys in `()` must be present, while at least 1 key in `{}` needs to be present

&nbsp;

# Debug

- Show all tables:           `GET /debug`
- Show all contents of a table:       `GET /debug/:table`

--------------------------------------------------------------------------------------------
&nbsp;

# Basic CRUD


## Users

- Show all:              `GET /Users`
- Show 1 :                `GET /Users/:username`
- Create:               `POST /Users`
    - body: (username, email, profile, address, phoneNum)
- Update:               `PUT /Users/:username`
    - body: {email, profile, address, phoneNum}
- Delete:               `DELETE /Users/:username`

--------------------------------------------------------------------------------------------

## Consumers

- Show all:               `GET /Consumers`
- Show 1 :                 `GET /Consumers/:username`
- Create:                `POST /Consumers`
    - body: (username, creditCard, bankAcc)
- Update:                `PUT /Consumers/:username`
    - body: {creditCard, bankAcc}
- Delete:                 `DELETE /Consumers/:username`

--------------------------------------------------------------------------------------------

## PCSAdmins

- Show all:                `GET /PCSAdmins`
- Show 1 :                  `GET /PCSAdmins/:username`
- Create:                 `POST /PCSAdmins`
    - body: (username)
- Delete:                 `DELETE /PCSAdmins/:username`

--------------------------------------------------------------------------------------------

## PetOwners

- Show all:                `GET /PetOwners`
- Show 1 :                  `GET /PetOwners/:username`
- Create:                 `POST /PetOwners`
    - body: (username)
- Delete:                 `DELETE /PetOwners/:username`

--------------------------------------------------------------------------------------------

## Caretakers

- Show all:                `GET /CareTakers`
- Show 1 :                  `GET /CareTakers/:username`
- Create:                 `POST /CareTakers`
    - body: (username, maxslots)
- Delete:                 `DELETE /CareTakers/:username`

--------------------------------------------------------------------------------------------

## FullTimers

- Show all:                `GET /FullTimers`
- Show 1 :                  `GET /FullTimers/:username`
- Create:                 `POST /FullTimers`
    - body: (username)
- Delete:                 `DELETE /FullTimers/:username`

--------------------------------------------------------------------------------------------

## PartTimers

- Show all:                 `GET /PartTimers`
- Show 1 :                   `GET /PartTimers/:username`
- Create:                  `POST /PartTimers`
- Update:                  `PUT /PartTimers/:username`
    - body: (username)
- Delete:                  `DELETE /PartTimers/:username`

--------------------------------------------------------------------------------------------

## Pets

- Show all:                 `GET /Pets`
- Show all by a owner:           `GET /Pets/:petowner`
- Show 1 :                   `GET /Pets/:petowner/:petname`
- Create:                  `POST /Pets`
    - body: (petowner, petname, profile, specialReq)
- Update:                  `PUT /Pets/:petowner/:petname`
    - body: {petname, profile, specialReq}
- Delete:                  `DELETE /Pets/:petowner/:petname`

--------------------------------------------------------------------------------------------

## Availability

- Show all:                 `GET /Availability`
- Show all for a caretaker:          `GET /Availability/:caretaker`
- Show 1 :                   `GET /Availability/:caretaker/:avail`
- Create:                  `POST /Availability`
    - body: {caretaker, avail}
- Delete:                  `DELETE /Availability/:caretaker/:avail`

--------------------------------------------------------------------------------------------

## Unavailablity

- Show all:                    `GET /Unavailability`
- Show all for a caretaker:           `GET /Unavailability/:caretaker`
- Show 1 :                      `GET /Unavailability/:caretaker/:avail`
- Create:                  `POST /Unavailability`
    - body: {caretaker, avail}
- Delete:                   `DELETE /Unavailability/:caretaker/:avail`

--------------------------------------------------------------------------------------------

## PetTypes

- Show all:                  `GET /PetTypes`
- Show 1 :                    `GET /PetTypes/:category`
- Create:                   `POST /PetTypes`
    - body: (category, baseprice)
- Update:                    `PUT /PetTypes/:username`
    - body: (baseprice)
- Delete:                   `DELETE /PetTypes/:username`

--------------------------------------------------------------------------------------------

## Manages

- Show all:                 `GET /Manages`
- Show all under an admin:          `GET /Manages/:admin`
- Show 1 :                   `GET /Manages/:admin/:caretaker`
- Create:                   `POST /Manages`
    - body: (admin, caretaker)
- Delete:                   `DELETE /Manages/:admin/:caretaker`

--------------------------------------------------------------------------------------------

## Bids

- Show all:                   `GET /Bids`
- Show all by a petowner:              `GET /Bids/petowner/:petowner`
- Show all by a petowner for a pet:           `GET /Bids/petowner/:petowner/:petname`
- Show all for a caretaker:                  `GET /Bids/:caretaker`
- Show all between a caretaker and a petowner: `GET /Bids/:caretaker/:petowner`
- Show 1 :                    `GET /Bids/:caretaker/:petowner/:petname/:sdate`
- Create:                   `POST /Bids`
    - (
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
- Update:                  `PUT /Bids/:caretaker/:petowner/:petname/:sdate`
    - body: {
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
    }
- Delete:                   `DELETE /Bids/:caretaker/:petowner/:petname/:sdate`


--------------------------------------------------------------------------------------------
&nbsp;

# Complex Queries

Not implemented yet. Stay tuned =D
