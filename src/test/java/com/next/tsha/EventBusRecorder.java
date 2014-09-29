package com.next.tsha;

import com.google.common.eventbus.Subscribe;


/**
 * Created by agerardi on 25/09/2014.
 */
public class EventBusRecorder {
    @Subscribe
    public void receiveMessage(String message) {
        System.out.println(" Messaggio Ricevuto:  " + message);
    }
}
