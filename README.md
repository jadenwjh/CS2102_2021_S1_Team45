# RESTful API for CS2102 Database Systems Project

### Stack: Node.js, express, PostgreSQL

### Deployed on heroku.com at https://shielded-oasis-35437.herokuapp.com/

&nbsp;
> **NOTE**: Contents inside request body should always be in application/json format

&nbsp;

# Debug

- ## Show all tables: <div style="text-align: right"> `GET /debug`</div>

&nbsp;

- ## Show all contents of a table:<div style="text-align: right"> `GET /debug/:table`</div>

    ### Params:
    - `table` : name of the table to show

    ### Example:
    - GET https://shielded-oasis-35437.herokuapp.com/debug/pcsadmins

        Output:

        `[{"username":"pdepport0"},{"username":"rwardington1"}]`

&nbsp;

- ## Insert into a table:<div style="text-align: right">`POST /debug/:table`</div>
    
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

- ## Delete from a table:<div style="text-align: right">`DELETE /debug/:table`</div>
    
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

- ## login:<div style="text-align: right">`POST /Users/login`</div>

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

- ## Register:<div style="text-align: right">`POST /Users/register`</div>
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

- ## Get user information:<div style="text-align: right">`GET /PetOwner/:petowner`</div>

    ### Params:

    - `petowner`: string, petowner's username

    ### Example:
    - GET https://shielded-oasis-35437.herokuapp.com/petowner/ccarnewp8

        Output:

        `[{"username": "ccarnewp8","email": "bkieldp8@xing.com","password": "pqTDqBWnC","profile": "In quis justo. Maecenas rhoncus aliquam lacus. Morbi quis tortor id nulla ultrices aliquet.","address": "52393 Killdeer Terrace","phonenum": 89298662,"creditcard": 70902008,"bankacc": 823127208,"petname": null,"petprofile": null,"specialreq": null,"category": null}]`

        > Null values are due to the petowner not having any pets.

&nbsp;
- ## Get information about current bids:<div style="text-align: right">`GET /PetOwner/Bids/:petowner`</div>

    ### Params:

    - `petowner`: string, petowner's username

    ### Example:
    - GET https://shielded-oasis-35437.herokuapp.com/petowner/bids/ccarnewp8

        Output:

        > Bids that are pending / accepted with no ratings

        `[{"petowner":"daleksidze6","petname":"Gabbey","caretaker":"jgastickep2","avail":"2020-06-04T16:00:00.000Z","edate":"2020-06-04T16:00:00.000Z","transfertype":"f2f","paymenttype":"cash","price":100,"ispaid":false,"status":"p","review":null,"rating":null}]`

&nbsp;
- ## Leave rating and reviews for caretaker <div style="text-align: right">`POST /PetOwner/RatingsReviews`  </div>

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
        ```
        {
            "petowner": "daleksidze6",
            "petname": "Gabbey",
            "caretaker": "jgastickep2",
            "avail": "2020-06-05",
            "rating": 5,
            "review": "very good!!!"
        }
        ```

        Output:

        `"Ratings and reviews updated"`



&nbsp;
- ## Get past bid records <div style="text-align: right">`GET /PetOwner/Bids/:petowner/history`</div>

    ### Params:
    - `petowner`: string

    ### Example 
    - GET https://shielded-oasis-35437.herokuapp.com/PetOwner/Bids/daleksidze6/history

    Output:

    > Only contains bids that have been rejected or rated

    `[{"petowner":"daleksidze6","petname":"Gabbey","caretaker":"jgastickep2","avail":"2020-01-01T00:00:00.000Z","edate":"2020-01-05T00:00:00.000Z","transfertype":"f2f","paymenttype":"cash","price":100,"ispaid":false,"status":"r","review":null,"rating":null},{"petowner":"daleksidze6","petname":"Gabbey","caretaker":"jgastickep2","avail":"2020-01-02T00:00:00.000Z","edate":"2020-01-05T00:00:00.000Z","transfertype":"f2f","paymenttype":"cash","price":100,"ispaid":false,"status":"r","review":null,"rating":null}, ...]`


&nbsp;
- ## Get available caretakers <div style="text-align: right">`POST /PetOwner/findCareTaker`</div>

    ### Body: 
    - `petowner` : string
    - `petname` : string
    - `sdate` : string, start date in the format 'yyyy-mm-dd'
    - `edate` : string, end date in the format 'yyyy-mm-dd'

> ## Not working yet...


&nbsp;
- ## Get caretaker rating and reviews <div style="text-align: right">`GET /PetOwner/RatingsReviews/:caretaker`</div>

    ### Params:
    - `caretaker` : string

    ### Example: 
    - GET https://shielded-oasis-35437.herokuapp.com/petowner/ratingsreviews/jgastickep2

    Output:

    `[{"caretaker":"jgastickep2","rating":5,"review":"This pet care works too well. It buoyantly improves my football by a lot.","petowner":"daleksidze6","petname":"Gabbey","edate":"2020-03-05T00:00:00.000Z"},{"caretaker":"jgastickep2","rating":5,"review":"This pet care works certainly well. It accidentally improves my baseball by a lot.","petowner":"daleksidze6","petname":"Gabbey","edate":"2020-04-05T00:00:00.000Z"},{"caretaker":"jgastickep2","rating":4,"review":"It only works when I am Azerbaijan.","petowner":"daleksidze6","petname":"Gabbey","edate":"2020-05-05T00:00:00.000Z"},{"caretaker":"jgastickep2","rating":3,"review":"talk about contempt!","petowner":"daleksidze6","petname":"Gabbey","edate":"2020-06-05T00:00:00.000Z"},{"caretaker":"jgastickep2","rating":4,"review":"heard about this on timba radio, decided to give it a try.","petowner":"daleksidze6","petname":"Gabbey","edate":"2020-07-05T00:00:00.000Z"}]`


&nbsp;
- ## Insert bid for a caretaker <div style="text-align: right">`POST /PetOwner/Bids`</div>

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
    
    Body:
    ```
    {
    "petowner": "daleksidze6",
    "petname": "Gabbey",
    "caretaker": "jgastickep2",
    "sdate": "2020-06-01",
    "edate": "2020-06-05",
    "transferType": "f2f",
    "paymentType": "cash",
    "price": 100
    }
    ```

    Output:
    `{"petowner":"daleksidze6","petname":"Gabbey","caretaker":"jgastickep2","sdate":"2020-06-01","edate":"2020-06-05","transferType":"f2f","paymentType":"cash","price":100}`

&nbsp;
## Manage Pets
----------------


&nbsp;
- ## get all Pets owned by a petowner <div style="text-align: right">`GET /PetOwner/Pets/:petowner`

    ### Params:
    - `petowner` : string

    ### Example:
    - GET https://shielded-oasis-35437.herokuapp.com/petowner/pets/dayerst2

    Output:
    `[{"petowner":"dayerst2","petname":"Zabrina","profile":"Phalaropus lobatus","specialreq":"deliver real-time e-markets","category":"bird"},{"petowner":"dayerst2","petname":"Candra","profile":"Loxodonta africana","specialreq":"embrace customized markets","category":"hamster"}]`




&nbsp;
- ## get a Pet by petname and petowner <div style="text-align: right">`GET /PetOwner/Pets/:petowner/:petname`

    ### Params:
    - `petowner` : string

    ### Example:
    - GET https://shielded-oasis-35437.herokuapp.com/petowner/pets/jeymerp9/Priscella

    Output:
    `{"petowner":"jeymerp9","petname":"Priscella","profile":"Coracias caudata","specialreq":"orchestrate seamless partnerships","category":"dog"}`

&nbsp;
- ## Create a pet <div style="text-align: right">`POST /PetOwner/Pets`

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
- ## Update a pet's info <div style="text-align: right"> `PUT /PetOwner/Pets/:petowner/:petname`</div>

    ### Params:
    - `petowner` : string
    - `petname` : string

    ### Body: 
    > Note: this body can consist of any subset of the following:
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
- ## Delete a pet <div style="text-align: right">`DELETE /PetOwner/Pets/:petowner/:petname`

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
- ## Get all of the caretaker's own information <div style="text-align: right">`GET /CareTaker/:caretaker`

    ### Params:
    - `caretaker` : string

    ### Example:
    - GET https://shielded-oasis-35437.herokuapp.com/caretaker/jeymerp9

    Output:
    `[{"username":"jeymerp9","email":"llamdinp9@alexa.com","password":"dldmbEA","profile":"Coracias caudata","address":"10481 Muir Lane","phonenum":83427591,"creditcard":86158725,"bankacc":944642361,"petowner":"jeymerp9","petname":"Priscella","specialreq":"orchestrate seamless partnerships","category":"dog"}]`

&nbsp;
- ## Get own AbleToCare info <div style="text-align: right">`GET /CareTaker/AbleToCare/:caretaker`

    ### Params:
    - `caretaker` : string

    ### Example:
    - GET https://shielded-oasis-35437.herokuapp.com/caretaker/abletocare/sperezpj

    Output:
    `[{"caretaker":"sperezpj","category":"bird","feeperday":20},{"caretaker":"sperezpj","category":"hamster","feeperday":10}]`

&nbsp;
- ## Add AbleToCare info <div style="text-align: right">`POST /CareTaker/AbleToCare`

    ### Body:
    - `caretaker` : string
    - `category` : string, pet type
    - `feeperday` : float, fee that the caretaker wanna charge

    ### Example:
    - POST https://shielded-oasis-35437.herokuapp.com/caretaker/abletocare

    Body:
    ```
    {
    "caretaker": "jtitheridgeb",
    "category": "dog",
    "feeperday": 45
    }
    ```

    Output:
    `{"caretaker": "jtitheridgeb", "category": "dog", "feeperday": 45}`

&nbsp;
- ## Get all available days <div style="text-align: right">`GET /CareTaker/available/:caretaker`

    ### Params:
    - `caretaker` : string

    ### Example:
    - GET https://shielded-oasis-35437.herokuapp.com/caretaker/available/jgastickep2

    Output:

    `[{"caretaker":"jgastickep2","avail":"2020-01-01T00:00:00.000Z"},{"caretaker":"jgastickep2","avail":"2020-01-02T00:00:00.000Z"}, ...]`


&nbsp;
- ## Get own ratings and reviews <div style="text-align: right">`GET /CareTaker/RatingsReviews/:caretaker`

    ### Params:
    - `caretaker` : string

    ### Example:
    - GET https://shielded-oasis-35437.herokuapp.com/caretaker/ratingsreviews/jgastickep2

    Output:

    `[{"caretaker":"jgastickep2","rating":5,"review":"This pet care works too well. It buoyantly improves my football by a lot.","petowner":"daleksidze6","petname":"Gabbey","edate":"2020-03-05T00:00:00.000Z"},{"caretaker":"jgastickep2","rating":5,"review":"This pet care works certainly well. It accidentally improves my baseball by a lot.","petowner":"daleksidze6","petname":"Gabbey","edate":"2020-04-05T00:00:00.000Z"},{"caretaker":"jgastickep2","rating":4,"review":"It only works when I am Azerbaijan.","petowner":"daleksidze6","petname":"Gabbey","edate":"2020-05-05T00:00:00.000Z"},{"caretaker":"jgastickep2","rating":3,"review":"talk about contempt!","petowner":"daleksidze6","petname":"Gabbey","edate":"2020-06-05T00:00:00.000Z"},{"caretaker":"jgastickep2","rating":4,"review":"heard about this on timba radio, decided to give it a try.","petowner":"daleksidze6","petname":"Gabbey","edate":"2020-07-05T00:00:00.000Z"}]`

&nbsp;
- ## Get all bids for self <div style="text-align: right">`GET /CareTaker/Bids/:caretaker`

    ### Params:
    - `caretaker` : string

    ### Example:
    - GET https://shielded-oasis-35437.herokuapp.com/caretaker/bids/jgastickep2

    Output:

    `[{"petowner":"daleksidze6","petname":"Gabbey","caretaker":"jgastickep2","avail":"2020-05-31T16:00:00.000Z","edate":"2020-06-04T16:00:00.000Z","transfertype":"f2f","paymenttype":"cash","price":100,"ispaid":false,"status":"p","review":null,"rating":null}, ...]`

&nbsp;
- ## Respond to a bid <div style="text-align: right">`PUT /CareTaker/Bids`

    ### Body:
    - `petowner` : string
    - `petname` : string
    - `caretaker` : string
    - `avail` : string, date of a bid
    - `approveReject` : single character string, 'a' = accept, 'p' = pending, 'r' = reject

    ### Example:
    - PUT https://shielded-oasis-35437.herokuapp.com/caretaker/bids

    Body:
    ```
    {
    "petowner": "daleksidze6",
    "petname": "Gabbey",
    "caretaker": "jgastickep2",
    "avail": "2020-06-05",
    "approveReject": "a"
    }
    ```
    Output:

    `{"petowner":"daleksidze6","petname":"Gabbey","caretaker":"jgastickep2","avail":"2020-06-05","approveReject":"a"}`


&nbsp;
- ## Get range of prices that can be set <div style="text-align: right">`GET /CareTaker/pricing/:caretaker`

    ### Params:
    - `caretaker` : string

    ### Example:
    - GET https://shielded-oasis-35437.herokuapp.com/caretaker/pricing/jeymerp9

    Output:
    `[{"caretaker":"jeymerp9","category":"dog","feeperday":45,"baseprice":45,"upperlimit":45}]`

    > Note: upperlimit is same as baseprice because this caretaker has no reviews yet

&nbsp;
- ## Set price for a pettype <div style="text-align: right">`PUT /CareTaker/pricing`

    ### Body:
    - `caretaker` : string
    - `petType` : string
    - `price` : integer

    ### Example:
    - PUT https://shielded-oasis-35437.herokuapp.com/caretaker/pricing

    Body:
    ```
    {
    "caretaker": "jgastickep2",
    "petType": "bird",
    "price": 22
    }
    ```

    Output:
    `"jgastickep2 now charges 22 for 'bird'"`

&nbsp;
- ## For fulltimers: apply for leave  <div style="text-align: right">`POST /CareTaker/leaves`

    ### Body:
    - `username` : string
    - `sdate` : string, start date in the format 'yyyy-mm-dd'
    - `edate` : string, end date in the format 'yyyy-mm-dd'

    ### Example:
    - POST https://shielded-oasis-35437.herokuapp.com/caretaker/leaves

    Body:
    ```
    {
    "username": "jgastickep2",
    "sdate": "2021-06-01",
    "edate": "2021-07-01"
    }
    ```

    Output:

    `"Leave application 2021-06-01 - 2021-07-01 successful for 'jgastickep2'"`

&nbsp;
- ## For parttimers: Schedule available days <div style="text-align: right">`POST /CareTaker/available`

    ### Body:
    - `username` : string
    - `sdate` : string, start date in the format 'yyyy-mm-dd'
    - `edate` : string, end date in the format 'yyyy-mm-dd'

    ### Example:
    - POST https://shielded-oasis-35437.herokuapp.com/caretaker/available

    Body:
    ```
    {
    "username": "jeymerp9",
    "sdate": "2020-06-01",
    "edate": "2021-02-01"
    }
    ```
    Output:
    `"Available Dates 2020-06-01 - 2021-02-01 successfully added for 'jeymerp9'"`


&nbsp;
## PCS Admin
------------


&nbsp;
- ## Add an admin <div style="text-align: right">`POST /Admin`

    ### Body:
    - `username` : string
    - `email` : string
    - `password` : string
    - `profile` : string
    - `address` : string
    - `phoneNum` : integer


&nbsp;
- ## Get pet type base prices <div style="text-align: right">`GET /Admin/PetTypes`

    ### Example:
    GET https://shielded-oasis-35437.herokuapp.com/Admin/PetTypes

    Output:

    `[{"category": "cat","baseprice": 40},{ "category": "dog", "baseprice": 45},{"category": "bird","baseprice": 20},{ "category": "rabbit", "baseprice": 25},{"category": "hamster","baseprice": 10}]`


&nbsp;
- ## Set base price for a pet type <div style="text-align: right">`PUT /Admin/PetTypes`

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
- ## Get all caretakers and their ratings <div style="text-align: right">`GET /Admin/summary`

    ### Example:
    GET https://shielded-oasis-35437.herokuapp.com/Admin/summary

    Output:
    
    `[{"caretaker":"jgastickep2","averagerating":"4.2000000000000000"}]`


&nbsp;
- ## Get all salaries (in progress)