# Pushcrew Java API

This is a Java wrapper around the [Pushcrew REST API](http://api.pushcrew.com/docs/introduction-to-rest-api).

## Building

To build, you must install SBT.

$ sbt
> publish

## Using

Currently only `sendToAll` and `sendToList` are supported. Usage is straightforward:

```java
import com.pushcrew.client.*;

...etc...

PushcrewClient client = new PushcrewClient(apiKey);

PushcrewResponses.SendResponse response = client.sendToAll("title", "message", "http://www.pushcrew.com");

PushcrewResponses.NotificationStatus status = client.checkStatus(response);
System.out.println("The request ID is " + status.requestId + " and the status was " + status.status);

PushcrewResponses.SendResponse response = client.sendToList("title", "message", "http://www.pushcrew.com", Arrays.asList("subscriber1", "subscriber2", "subscriber2"));
System.out.println("There were " + response.countDelivered + " messages delivered and " + response.countClicked + " responses which were clicked.")

```
