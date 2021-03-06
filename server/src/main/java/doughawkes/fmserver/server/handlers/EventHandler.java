package doughawkes.fmserver.server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import doughawkes.fmserver.dataAccess.Database;
import hawkes.model.Event;
import doughawkes.fmserver.services.EventService;
import hawkes.model.result.ErrorMessage;
import hawkes.model.result.EventResult;

/**
 * Created by yo on 6/2/17.
 */

public class EventHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("Event handler");
        boolean success = false;

        try {
            if (exchange.getRequestMethod().toLowerCase().equals("get")) {

                // Get the HTTP request headers
                Headers reqHeaders = exchange.getRequestHeaders();
                // Check to see if an "Authorization" header is present
                if (reqHeaders.containsKey("Authorization")) {
                    // Extract the auth token from the "Authorization" header
                    String authTokenString = reqHeaders.getFirst("Authorization");
                    // Verify that the auth token is the one we're looking for
                    // (this is not realistic, because clients will use different
                    // auth tokens over time, not the same one all the time).
                    // TODO: PROBABLY PUT THIS IN SERVICE: VVV
                    Database database = new Database();
                    String userName = database.getAuthTokenDao().lookup(authTokenString);
                    boolean authTokenValid = database.getAuthTokenDao().isSuccess();
                    if (authTokenValid) {
                        System.out.println("Authtoken and its timestamp valid");
                    }
                    else {
                        System.out.println("Authtoken invalid or its timestamp is expired.");
                        String message = "Authtoken invalid or its timestamp is expired.";
                        database.setAllTransactionsSucceeded(false);
                        database.endTransaction();
                        sendErrorMessage(exchange, message);
                        return;
                    }

                    database.endTransaction();
                    //TODO: PROBABLY PUT THIS IN SERVICE ^^^

                    String theURI = exchange.getRequestURI().toString();
                    String[] eventInstructions = theURI.split("/");

                    //length 2 for just Event, length 3 (index 2) for the EventID
                    Gson gson = new Gson();
                    String respData = "";

                    EventService eventService = new EventService();
                    // I'm going to use an actual event object since an eventResult would be redundant
                    Event event = null;
                    ArrayList<Event> userFamilyEventsResult = null;
                    if (eventInstructions.length == 3) {
                        //just getting the one user event
                        String eventID = eventInstructions[2];
                        event = eventService.getEvent(eventID, userName);
                        if (event == null) {
                            String message = "This event does not belong to someone "
                                           + "in the User family.";
                            sendErrorMessage(exchange, message);
                            return;
                        }

                    }
                    else if (eventInstructions.length == 2) {
                        // getting the array of the user's family events
                        userFamilyEventsResult = eventService.getUserFamilyEvents(authTokenString);
                    }



                    if (!eventService.isSuccess()) {
                        String message = "Event retreival failed.";
                        // TODO the load values could be wrong (missing, invalid) also
                        sendErrorMessage(exchange, message);
                        return;
                    } else {
                        if (eventInstructions.length == 3) {
                            respData = gson.toJson(event);
                        }
                        if (eventInstructions.length == 2) {
                            EventResult eventResult = new EventResult();
                            eventResult.setData(userFamilyEventsResult);
                            respData = gson.toJson(eventResult);
                        }
                    }

                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
                    OutputStream respBody = exchange.getResponseBody();
                    writeString(respData, respBody);
                    respBody.close();

                    success = true;
                }
            }

            if (!success) {
                exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, 0);
                exchange.getResponseBody().close();
            }
        } catch (IOException e) {
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, 0);
            exchange.getResponseBody().close();
            e.printStackTrace();
        }

    }

    private void sendErrorMessage(HttpExchange exchange, String message) {
        Gson gson = new Gson();
        ErrorMessage errorMessage = new ErrorMessage(message);
        String respData = gson.toJson(errorMessage);
        try {
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, 0);
            OutputStream respBody = exchange.getResponseBody();
            writeString(respData, respBody);
            respBody.close();
            exchange.getResponseBody().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    The readString method shows how to read a String from an InputStream.
*/
    private String readString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        InputStreamReader sr = new InputStreamReader(is);
        char[] buf = new char[1024];
        int len;
        while ((len = sr.read(buf)) > 0) {
            sb.append(buf, 0, len);
        }
        return sb.toString();
    }

    /*
    The writeString method shows how to write a String to an OutputStream.
*/
    private void writeString(String str, OutputStream os) throws IOException {
        OutputStreamWriter sw = new OutputStreamWriter(os);
        sw.write(str);
        sw.flush();
    }

}
