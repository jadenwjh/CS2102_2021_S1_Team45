# RESTful API for CS2102 Database Systems Project

### Stack: Node.js, express, PostgreSQL

### Deployed on heroku.com at https://shielded-oasis-35437.herokuapp.com/

&nbsp;
> **NOTE**: Contents inside request body should always be in application/json format

&nbsp;

# Debug

- ## Show all tables:      `GET /debug`

&nbsp;

- ## Show all contents of a table:      `GET /debug/:table`

    ### Params:
    - `table` : name of the table to show

    ### Example:
    - GET https://shielded-oasis-35437.herokuapp.com/debug/pcsadmins

        Output:

        `[{"username":"pdepport0"},{"username":"rwardington1"}]`

&nbsp;

- ## Insert into a table:      `POST /debug/:table`
    
    ### Params:
    - `table` : name of the table to show

    ### Body:
    For all values needed:
    - `{column_name}` : column_value 

    ### Example:
    - POST https://shielded-oasis-35437.herokuapp.com/debug/pettypes

        Body:

        ```
        {
            "category": "Meowth", 
            "baseprice": 100
        }
        ```

        Output:

        `{"category": "Meowth", "baseprice": 100}`

&nbsp;

- ## Delete from a table:       `DELETE /debug/:table`
    
    ### Params:

    - `table` : name of the table to show

    ### Body:

    For all values needed to identify a row / rows, 

    - `{column_name}` : `{column_value}`


    ### Example:

    - DELETE https://shielded-oasis-35437.herokuapp.com/debug/pettypes

        Body:

        ```
        {
            "category": "Meowth"
        }
        ```

        Output:

        `{"category": "Meowth", "baseprice": 100}`


--------------------------------------------------------------------------------------------
&nbsp;

# App-specific queries


&nbsp;

## Login/Register
--------------------

- ## login:              `POST /Users/login`

    ### Body:

    - `username`: string, username

    - `password`: string, user password

    - `acctype`: string, one of ['petowner', 'caretaker', 'both'], case insensitive

    ### Example:
    - POST https://shielded-oasis-35437.herokuapp.com/Users/login

        Body:

        ```
        {
        "username": "rwardington1",
        "password": "5CKVY4dgk",
        "acctype": "petowner"
        }   
        ```

        Output:

        `[{"username": "rwardington1", "password": "5CKVY4dgk"}]`


&nbsp;

- ## Register:              `POST /Users/register`
    ### Body

    - `username` : string

    - `email` : string

    - `password` : string

    - `profile` : string

    - `address` : string

    - `phoneNum` : integer

    - `creditCard` : integer

    - `bankAcc` : integer. Note that SQL distinguishes integer from biginteger. Don't use any number above 2^31 - 1.

    - `acctype` : string, one of ['petowner', 'caretaker', 'both'], case insensitive

    >   If `acctype` is not petowner, the following are required:
 

    - `isPartTime` : boolean

    - `admin` :  string, username of an existing admin 

    ### Example
     - POST https://shielded-oasis-35437.herokuapp.com/Users/register

        Body:

        ```
        {
        'username': 'ccarnewp8',
        'email': 'bkieldp8@xing.com',
        'password': 'pqTDqBWnC',
        'profile': 'In quis justo. Maecenas rhoncus aliquam lacus. Morbi quis tortor id nulla ultrices aliquet.',
        'address': '52393 Killdeer Terrace',
        'phoneNum': 89298662,
        'creditCard': 70902008,
        'bankAcc': 823127208,
        'acctype': 'both',
        'isPartTime': True,
        'admin': 'pdepport0'
        }
        ```

        Output: (same as input body)

        `{'username': 'ccarnewp8',
        'email': 'bkieldp8@xing.com',
        'password': 'pqTDqBWnC',
        'profile': 'In quis justo. Maecenas rhoncus aliquam lacus. Morbi quis tortor id nulla ultrices aliquet.',
        'address': '52393 Killdeer Terrace',
        'phoneNum': 89298662,
        'creditCard': 70902008,
        'bankAcc': 823127208,
        'acctype': 'both',
        'isPartTime': True,
        'admin': 'pdepport0'}`


&nbsp;

## Pet Owner Operations
--------------------

- ## Get user information:              `GET /PetOwner/:petowner`

    ### Params:

    - `petowner`: string, petowner's username

    ### Example:
    - GET https://shielded-oasis-35437.herokuapp.com/petowner/ccarnewp8

        Output:

        `[{"username": "ccarnewp8","email": "bkieldp8@xing.com","password": "pqTDqBWnC","profile": "In quis justo. Maecenas rhoncus aliquam lacus. Morbi quis tortor id nulla ultrices aliquet.","address": "52393 Killdeer Terrace","phonenum": 89298662,"creditcard": 70902008,"bankacc": 823127208,"petname": null,"petprofile": null,"specialreq": null,"category": null}]`

        > Null values are due to the petowner not having any pets.

&nbsp;
- ## Get information about current bids:         `GET /PetOwner/Bids/:petowner`

    ### Params:

    - `petowner`: string, petowner's username

    ### Example:
    - GET https://shielded-oasis-35437.herokuapp.com/petowner/bids/ccarnewp8

        Output:

        # TODO 

&nbsp;
- ## Leave rating and reviews for caretaker   `POST /PetOwner/RatingsReviews`  

    ### Body:

    - `petowner`: string

    - `petname` : string

    - `caretaker` : string

    - `avail` : string for date in the form of 'yyyy-mm-dd'

    - `rating` : integer between 1 and 5 inclusive

    - `review` : string

    ### Example 
    - POST https://shielded-oasis-35437.herokuapp.com/PetOwner/RatingsReviews

        Body:

        Output:


        # TODO


&nbsp;
- ## Get past bid records               `GET /PetOwner/Bids/:petowner/history`

    ### Params:
    - `petowner`: string

    ### Example 
    - GET https://shielded-oasis-35437.herokuapp.com/PetOwner/Bids/:petowner/history

    # TODO 

&nbsp;
- ## Get available caretakers           `POST /PetOwner/findCareTaker`

    ### Body: 
    - `petowner` : string
    - `petname` : string
    - `sdate` : string, start date in the format 'yyyy-mm-dd'
    - `edate` : string, end date in the format 'yyyy-mm-dd'


&nbsp;
- ## Get caretaker rating and reviews `GET /PetOwner/RatingsReviews/:caretaker`

    ### Params:
    - `caretaker` : string

    ### Example: 
    - GET https://shielded-oasis-35437.herokuapp.com/PetOwner/RatingsReviews/:caretaker

    # todo


&nbsp;
- ## Insert bid for a caretaker `POST /PetOwner/Bids`

    ### Body: 
    - `petowner` : string
    - `petname` : string
    - `caretaker` : string
    - `sdate` : string, start date in the format 'yyyy-mm-dd'
    - `edate` : string, end date in the format 'yyyy-mm-dd'
    - `transferType` : string
    - `paymentType` : string, 'creditcard' or 'cash'
    - `price` : float

    ### Example
    - POST https://shielded-oasis-35437.herokuapp.com/PetOwner/Bids
    # TODO


&nbsp;
## Manage Pets
----------------


&nbsp;
- ## get all Pets owned by a petowner `GET /PetOwner/Pets/:petowner`

    ### Params:
    - `petowner` : string




&nbsp;
- ## get a Pet by petname and petowner `GET /PetOwner/Pets/:petowner/:petname`

    ### Params:
    - `petowner` : string


&nbsp;
- ## Create a pet `POST /PetOwner/Pets`

    ### Body:
    - `petowner` : string
    - `petname` : string
    - `profile` : string
    - `specialReq` : string
    - `category` : string, one of the existing pet types

    ### Example:
    - POST https://shielded-oasis-35437.herokuapp.com/PetOwner/Pets

    Body:
    ```
    {
    "petowner": "daleksidze6",
    "petname": "Gabbey",
    "profile": "Heloderma horridum",
    "specialreq": "reinvent web-enabled convergence",
    "category": "bird"
    }
    ```

    Output:

    `{"petowner": "daleksidze6","petname": "Gabbey","profile": "Heloderma horridum","specialreq": "reinvent web-enabled convergence","category": "bird"}`



&nbsp;
- ## Update a pet's info   `PUT /PetOwner/Pets/:petowner/:petname`

    ### Params:
    - `petowner` : string
    - `petname` : string

    ### Body: 
    > Note: the body can consist of any subset of the following:
    - `petowner` : string
    - `petname` : string
    - `profile` : string
    - `specialReq` : string
    - `category` : string, one of the existing pet types

    ### Example:
    - PUT https://shielded-oasis-35437.herokuapp.com/petowner/pets/daleksidze6/Gabbey

    Body:
    ```
    {
    "petname": "Gabbey",
    "profile": "A horrendous birb!!!",
    "specialReq": "entertain with classical music",
    "category": "bird"
    }
    ```

    Output:

    `"Pet Gabbey belonging to daleksidze6 was updated"`



&nbsp;
- ## Delete a pet `DELETE /PetOwner/Pets/:petowner/:petname`

    ### Params:
    - `petowner` : string
    - `petname` : string

    ### Example:
    - DELETE https://shielded-oasis-35437.herokuapp.com/petowner/pets/daleksidze6/Gabbey

    Output:
    
    `"Pet Gabbey belonging to daleksidze6 was deleted"`



&nbsp;
## Care Taker
--------------


&nbsp;
- ## Get all of the caretaker's own information `GET /CareTaker/:caretaker`

    ### Params:
    - `caretaker` : string


&nbsp;
- ## Get all available days `GET /CareTaker/available/:caretaker`

    ### Params:
    - `caretaker` : string


&nbsp;
- ## Get own ratings and reviews `GET /CareTaker/RatingsReviews/:caretaker`

    ### Params:
    - `caretaker` : string

&nbsp;
- ## Get all bids for self `GET /CareTaker/Bids/:caretaker`

    ### Params:
    - `caretaker` : string

&nbsp;
- ## Respond to a bid `PUT /CareTaker/Bids`

    ### Body:
    - `petowner` : string
    - `petname` : string
    - `caretaker` : string
    - `avail` : string, date of a bid
    - `approveReject` : single character string, 'a' = accept, 'p' = pending, 'r' = reject

    ### Example:



&nbsp;
- ## Get range of prices that can be set `GET /CareTaker/pricing/:caretaker`

    ### Params:
    - `caretaker` : string

&nbsp;
- ## Set price for a pettype `PUT /CareTaker/pricing`

    ### Body:
    - `caretaker` : string
    - `petType` : string
    - `price` : integer

&nbsp;
- ## For fulltimers: apply for leave  `POST /CareTaker/leaves`

    ### Body:
    - `username` : string
    - `sdate` : string, start date in the format 'yyyy-mm-dd'
    - `edate` : string, end date in the format 'yyyy-mm-dd'

&nbsp;
- ## For parttimers: Schedule available days `POST /CareTaker/available`

    ### Body:
    - `username` : string
    - `sdate` : string, start date in the format 'yyyy-mm-dd'
    - `edate` : string, end date in the format 'yyyy-mm-dd'



&nbsp;
## PCS Admin
------------


&nbsp;
- ## Add an admin `POST /Admin`

    ### Body:
    - `username` : string
    - `email` : string
    - `password` : string
    - `profile` : string
    - `address` : string
    - `phoneNum` : integer


&nbsp;
- ## Get pet type base prices `GET /Admin/PetTypes`

    ### Example:
    GET https://shielded-oasis-35437.herokuapp.com/Admin/PetTypes


&nbsp;
- ## Set base price for a pet type `PUT /Admin/PetTypes`

    ### Body:
    - `basePrice` : float
    - `category` : string, pet type whose base price to change

    ### Example:
    PUT https://shielded-oasis-35437.herokuapp.com/Admin/PetTypes

    Body:
    ```
    {
        "basePrice": 101,
        "category": "meowth"
    }
    ```

    Output: 
    `PetType meowth was updated`

&nbsp;
- ## Get all caretakers and their ratings `GET /Admin/summary`

    ### Example:
    GET https://shielded-oasis-35437.herokuapp.com/Admin/summary


&nbsp;
- ## Get all salaries (in progress)