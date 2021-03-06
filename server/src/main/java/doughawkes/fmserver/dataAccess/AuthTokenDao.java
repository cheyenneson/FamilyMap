package doughawkes.fmserver.dataAccess;
import hawkes.model.AuthToken;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;


/**
 * class that deals with the database and makes changes and lookups
 * for AuthToken entries
 */
public class AuthTokenDao {
    Connection connection;
    public static int timeLimitMinutes;
    private boolean success;
    /**
     * creates new AuthTokenDao object to interact with the database
     */
    public AuthTokenDao() {

    }

    /**
     * adds an AuthToken to the database
     * @param a AuthToken of interest
     * @return true or false for success
     */
    boolean addAuthToken(AuthToken a) {
        return false;
    }

    /**
     * looks up a AuthToken in the database
     * @param authString AuthToken string of interest
     * @return the AuthToken object successfully found
     */
    public String lookup(String authString) {
        success = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String userName = null;

        String sql = "select username from authtoken where (token = ?) "
                   + "and (? - logintime) < ?";

        try {
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, authString);

            // java's time now in seconds
            Date date = new Date();
            int milliSeconds = 1000;
            int seconds = 60;
            int timeNow = ((int) date.getTime()) / (milliSeconds);
            System.out.println("timenow: " + timeNow);
            stmt.setInt(2, timeNow);

            int timeLimit = timeLimitMinutes * seconds;
            stmt.setInt(3, timeLimit);

            rs = stmt.executeQuery();

            while (rs.next()) {

                //TODO THIS WAS WORKING BUT I'M CHANGING IT TO USERNAME
//                String authToken = rs.getString("token");
//                if (authToken != null) {
//                    System.out.println(authToken);
//                    success = true;
//                }
                userName = rs.getString("username");
                if (userName != null) {
                    success = true;
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return userName;

    }

    public String generateAuthToken(String userName) {
        success = false;

        PreparedStatement stmt = null;
        String authTokenString = "";

        try {
            String sql = "insert into authtoken (token, logintime, username) values (?, ?, ?)";
            stmt = connection.prepareStatement(sql);
            //random token
            authTokenString = UUID.randomUUID().toString();
            stmt.setString(1, authTokenString);

            // java's time now in seconds
            Date date = new Date();
            int milliSeconds = 1000;
            int timeNow = ((int) date.getTime()) / (milliSeconds);
            stmt.setInt(2, timeNow);
            System.out.println("timenow: " + timeNow);

            stmt.setString(3, userName);
            System.out.println("about to execute update");
            if  (stmt.executeUpdate() == 1) {
                System.out.println("AuthToken entry added to database pending transaction commit.");
                success = true;
            }
            else throw new SQLException();

        } catch (SQLException e) {
            System.out.println("Update to generate and add new AuthToken to database failed.");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (stmt != null) try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        success = true;
        return authTokenString;
    }

    /**
     * deletes a AuthToken entry in the database
     * @param a AuthToken of interest
     * @return the AuthToken object successfully deleted
     */
    boolean delete(AuthToken a) {
        return false;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public boolean clear() {
        PreparedStatement stmt = null;

        try {
            String sql = "delete from authtoken";
            stmt = connection.prepareStatement(sql);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setTimeLimitMinutes(int timeLimitMinutes) {
        this.timeLimitMinutes = timeLimitMinutes;
    }
}
