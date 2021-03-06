package doughawkes.fmserver.services;

import java.util.UUID;

import doughawkes.fmserver.dataAccess.Database;
import hawkes.model.Person;
import hawkes.model.User;
import doughawkes.fmserver.services.fromJSON.DataPool;
import hawkes.model.request.FillRequest;
import hawkes.model.request.RegisterRequest;
import hawkes.model.result.RegisterResult;

/**
 * This class takes RegisterRequest objects in order to create a new user and its database
 * person representation including the generated family. Creates a new user account,
 * generates 4 generations of ancestor data for the new user, logs the user in,
 * and returns an auth token with the username and personID
 */
public class RegisterService {
    private boolean success;
    /**
     * RegisterService constructor
     */
    public RegisterService() {}

    /**
     * Use info in request to make a User with generated data,
     * verify its existence in the user dao object and populate
     * with data if doesn't yet exist and create a new user entry in
     * the database using the user dao object. Result will be called
     * to send the result back.
     * @param r RegisterRequest object containing new user data for registration
     * @return the registerResult object containing the authtoken, username, and password
     */
    public RegisterResult register(RegisterRequest r) {
        success = true;
        Database database = new Database();

        // Check database for an already existing user with this username
        User existingUser = database.getUserDao().findUser(r.getUserName());
        String existingUserName = existingUser.getUserName();
        if (existingUserName != null) {
            if (existingUserName.equals(r.getUserName())) {
                success = false;
                //throw new UsernameTakenException();
            }
        }

        //create a new user account
        // first put the data into the user object
        User user = makeUserFromRegisterRequest(r);

        //add the user to the database
        boolean addUserSuccess = database.getUserDao().addUser(user);

        //generate 4 generations of ancestor data for the new user including themselves,
        // with gender, names, email from the register request set for the user person.
//        Person userPerson = generateUserPerson(user);
//        boolean addPersonSuccess = database.getPersonDao().addPerson(userPerson);
        int defaultGenerations = 4;
        FillRequest fillRequest = new FillRequest(user.getUserName(), defaultGenerations);
        FillService fillService = new FillService();
        fillService.fill(fillRequest, user, database);

        //log in the user
        String authTokenString = database.getAuthTokenDao().generateAuthToken(r.getUserName());
        boolean authTokenSuccess = database.getAuthTokenDao().isSuccess();
        RegisterResult registerResult
                = new RegisterResult(authTokenString, user.getUserName(), user.getPersonId());


/*        LoginService loginService = new LoginService();
        LoginRequest loginRequest = new LoginRequest(r.getUserName(), r.getPassword());
        LoginResult loginResult = loginService.login(loginRequest);
        // fill the registerResult with the same fields contained in the loginResult.
        // If any part of the transaction fails, this result will
        // need to be changed to an error message instead.
        RegisterResult registerResult = new RegisterResult(loginResult);*/

        // SET DATABASE allTransactionsSucceeded to the result of ANDing the booleans of each Dao above.
        //database.setAllTransactionsSucceeded(addUserSuccess && generateFamilySuccess &&
        //                                     loginSuccess && authTokenLookupSuccess);

        //this can return a redundant boolean (true for successful transaction, false for fail.
        if (!success || !addUserSuccess || !fillService.isSuccess() || !authTokenSuccess) {

            System.out.println("RegisterService failed. registerService success " + success
                             + " addUserSuccess " + addUserSuccess
                             + " fillService.isSuccess " + fillService.isSuccess()
                             + " addUserSuccess " + addUserSuccess);
            database.setAllTransactionsSucceeded(false);
        }
        database.endTransaction();

        // Potential errors: Request property missing or has invalid value, Username already
        // taken by another user, Internal server error




//        registerResult.setAuthToken("blahblah789");
//        registerResult.setUserName("theusername");
//        registerResult.setPersonId("personID3983");

        return registerResult;
    }

    private User makeUserFromRegisterRequest(RegisterRequest r) {
        //create a unique personID to add to the user object
        String userPersonID = UUID.randomUUID().toString();

        User user = new User(r.getUserName(), r.getPassword(), r.getEmail(),
                r.getFirstName(), r.getLastName(),
                r.getGender().charAt(0), userPersonID);
        return user;
    }

    private Person generateUserPerson(User user) {
        Person person = new Person();

        person.setPersonID(UUID.randomUUID().toString());
        person.setDescendant(user.getUserName());
        person.setFirstName(user.getFirstName());
        person.setLastName(user.getLastName());
        person.setGender(user.getGender());

        DataPool dataPool = new DataPool();
        String fatherPersonID = UUID.randomUUID().toString();
        person.setFather(fatherPersonID);
        String motherPersonID = UUID.randomUUID().toString();
        person.setMother(motherPersonID);
        person.setSpouse(null);

        return person;
    }

    public boolean isSuccess() {
        return success;
    }
}


