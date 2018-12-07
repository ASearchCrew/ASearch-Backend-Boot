package com.asearch.logvisualization.push;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestPushController {

    private final String TOPIC = "JavaSampleApproach";

    @Autowired
    WebPushNotificationService webPushNotificationsService;

    @RequestMapping(value = "/send", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<String> send() throws JSONException {

        JSONObject body = new JSONObject();
//        body.put("to", "/topics/" + TOPIC);
//        body.put("to", "eGf_iPd4Ll4:APA91bEYQ8BvqnmBR29p7sjG2L_0VXgWVS0JJOV0mHKB8uLz20tBg3qIFy-Wg8NiGe0MsXi1vT2WPkLvKLG6-GujYGx62JiEy0ZlkB1naxWsJ7KGk3awlSAhKdTDm17KZ2JNNAFlfjHb");
        body.put("to", "cZMpnQHA5Ho:APA91bG7gs-SxzCYcN_47NIWQx2jKnLUOYqNjN_lnpHtLVovoct_AySJ4EnBKJLTTr5NUersLOH2MaaBPFosj72y7JbnCJ98DycSKwj2Cy-naey9vvXcrA8c4g_FQaZQmcNWR8HUUgqT");
        body.put("priority", "high");

        JSONObject notification = new JSONObject();
        notification.put("title", "JSA Notification");
        notification.put("body", "Happy Message!");

        JSONObject data = new JSONObject();
        data.put("Key-1", "JSA Data 1");
        data.put("Key-2", "JSA Data 2");

        body.put("notification", notification);
        body.put("data", data);

/**
 {
 "notification": {
 "title": "JSA Notification",
 "body": "Happy Message!"
 },
 "data": {
 "Key-1": "JSA Data 1",
 "Key-2": "JSA Data 2"
 },
 "to": "/topics/JavaSampleApproach",
 "priority": "high"
 }
 */

        HttpEntity<String> request = new HttpEntity<>(body.toString());

        CompletableFuture<String> pushNotification = webPushNotificationsService.send(request);
        CompletableFuture.allOf(pushNotification).join();

        try {
            String firebaseResponse = pushNotification.get();

            return new ResponseEntity<>(firebaseResponse, HttpStatus.OK);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>("Push Notification ERROR!", HttpStatus.BAD_REQUEST);
    }


    @RequestMapping(value = "/sendd", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<String> sendd() throws JSONException {

        JSONObject body = new JSONObject();
//        body.put("to", "/topics/" + TOPIC);
        body.put("to", "eGf_iPd4Ll4:APA91bEYQ8BvqnmBR29p7sjG2L_0VXgWVS0JJOV0mHKB8uLz20tBg3qIFy-Wg8NiGe0MsXi1vT2WPkLvKLG6-GujYGx62JiEy0ZlkB1naxWsJ7KGk3awlSAhKdTDm17KZ2JNNAFlfjHb");
//        body.put("to", "cZMpnQHA5Ho:APA91bG7gs-SxzCYcN_47NIWQx2jKnLUOYqNjN_lnpHtLVovoct_AySJ4EnBKJLTTr5NUersLOH2MaaBPFosj72y7JbnCJ98DycSKwj2Cy-naey9vvXcrA8c4g_FQaZQmcNWR8HUUgqT");
        body.put("priority", "high");

        JSONObject notification = new JSONObject();
        notification.put("title", "JSA Notification");
        notification.put("body", "Happy Message!");

        JSONObject data = new JSONObject();
        data.put("Key-1", "JSA Data 1");
        data.put("Key-2", "JSA Data 2");

        body.put("notification", notification);
        body.put("data", data);

/**
 {
 "notification": {
 "title": "JSA Notification",
 "body": "Happy Message!"
 },
 "data": {
 "Key-1": "JSA Data 1",
 "Key-2": "JSA Data 2"
 },
 "to": "/topics/JavaSampleApproach",
 "priority": "high"
 }
 */

        HttpEntity<String> request = new HttpEntity<>(body.toString());

        CompletableFuture<String> pushNotification = webPushNotificationsService.send(request);
        CompletableFuture.allOf(pushNotification).join();

        try {
            String firebaseResponse = pushNotification.get();

            return new ResponseEntity<>(firebaseResponse, HttpStatus.OK);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>("Push Notification ERROR!", HttpStatus.BAD_REQUEST);
    }

}
