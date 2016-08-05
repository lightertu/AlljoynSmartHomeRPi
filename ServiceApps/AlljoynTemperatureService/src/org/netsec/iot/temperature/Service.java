/*
 * Copyright Rui Tu. All rights reserved.
 *
 *    Permission to use, copy, modify, and/or distribute this software for any
 *    purpose with or without fee is hereby granted, provided that the above
 *    copyright notice and this permission notice appear in all copies.
 *
 *    THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 *    WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 *    MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 *    ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 *    WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 *    ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 *    OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package org.netsec.iot.temperature;

import org.alljoyn.bus.AboutObj;
import org.alljoyn.bus.BusAttachment;
import org.alljoyn.bus.BusListener;
import org.alljoyn.bus.BusObject;
import org.alljoyn.bus.Mutable;
import org.alljoyn.bus.SessionOpts;
import org.alljoyn.bus.SessionPortListener;
import org.alljoyn.bus.Status;

import java.io.FileNotFoundException;
import java.io.IOException;

public class Service {
    static {
        System.loadLibrary("alljoyn_java");
    }

    private static final short CONTACT_PORT=42;

    static boolean sessionEstablished = false;
    static int sessionId;

    /* bus object class */
    public static class TemperatureService implements SampleInterface, BusObject {
    	private TemperatureReader _tr = new TemperatureReader();
    	private double _absZero = -273.15;
        public String echo(String str) {
            return str;
        }
        public double getTemperature() {
            double curTemp = _absZero;
            try {
                curTemp = _tr.read();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return curTemp;
        }
    }


    public static void main(String[] args) throws FileNotFoundException {

        System.out.println("Setting Up environment to get Temperature running");

        BusAttachment mBus;
        mBus = new BusAttachment("AppName", BusAttachment.RemoteMessage.Receive);

        Status status;

        TemperatureService myTemperatureService = new TemperatureService();

        status = mBus.registerBusObject(myTemperatureService, "/org/netsec/iot/temperatureService");
        if (status != Status.OK) {
            return;
        }
        System.out.println("BusAttachment.registerBusObject successful");

        mBus.registerBusListener(new BusListener());

        status = mBus.connect();
        if (status != Status.OK) {

            return;
        }
        System.out.println("BusAttachment.connect successful on " + System.getProperty("org.alljoyn.bus.address"));

        Mutable.ShortValue contactPort = new Mutable.ShortValue(CONTACT_PORT);

        SessionOpts sessionOpts = new SessionOpts();
        sessionOpts.traffic = SessionOpts.TRAFFIC_MESSAGES;
        sessionOpts.isMultipoint = true;
        sessionOpts.proximity = SessionOpts.PROXIMITY_ANY;
        sessionOpts.transports = SessionOpts.TRANSPORT_ANY;

        status = mBus.bindSessionPort(contactPort, sessionOpts,
                new SessionPortListener() {
            public boolean acceptSessionJoiner(short sessionPort, String joiner, SessionOpts sessionOpts) {
                System.out.println("SessionPortListener.acceptSessionJoiner called");
                if (sessionPort == CONTACT_PORT) {
                    return true;
                } else {
                    return false;
                }
            }
            public void sessionJoined(short sessionPort, int id, String joiner) {
                System.out.println(String.format("SessionPortListener.sessionJoined(%d, %d, %s)", sessionPort, id, joiner));
                sessionId = id;
                sessionEstablished = true;
            }
        });
        if (status != Status.OK) {
            return;
        }

        AboutObj aboutObj = new AboutObj(mBus);
        status = aboutObj.announce(contactPort.value, new MyAboutData());
        if (status != Status.OK) {
            System.out.println("Announce failed " + status.toString());
            return;
        }
        System.out.println("Announce called announcing SessionPort: " + contactPort.value);

        while (!sessionEstablished) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                System.out.println("Thread Exception caught");
                e.printStackTrace();
            }
        }
        System.out.println("BusAttachment session established");

        while (true) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                System.out.println("Thread Exception caught");
                e.printStackTrace();
            }
        }
    }
}
