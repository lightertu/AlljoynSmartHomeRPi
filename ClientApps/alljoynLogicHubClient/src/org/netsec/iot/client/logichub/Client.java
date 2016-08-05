/*
 * Copyright Rui Tu All rights reserved.
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
package org.netsec.iot.client.logichub;

import java.util.Map;
import java.lang.Thread;

import org.alljoyn.bus.AboutListener;
import org.alljoyn.bus.AboutObjectDescription;
import org.alljoyn.bus.AboutProxy;
import org.alljoyn.bus.AnnotationBusException;
import org.alljoyn.bus.BusAttachment;
import org.alljoyn.bus.BusException;
import org.alljoyn.bus.Mutable;
import org.alljoyn.bus.ProxyBusObject;
import org.alljoyn.bus.SessionListener;
import org.alljoyn.bus.SessionOpts;
import org.alljoyn.bus.Status;
import org.alljoyn.bus.Variant;

public class Client {
    static {
        System.loadLibrary("alljoyn_java");
    }

    static BusAttachment mBus;
    private static ProxyBusObject temperatureProxyObject;
    private static TemperatureInterface temperatureInterface;

    private static ProxyBusObject lcdProxyObject;
    private static LCDInterface lcdInterface;

    private static boolean isLCDJoined = false;
    private static boolean isTemperatureJoined = false;


    static class MyAboutListenser implements AboutListener {
        private void printBusMessage(String busName, int version, short port, AboutObjectDescription[] objectDescriptions, Map<String, Variant> aboutData) {
            System.out.println("Announced BusName:     " + busName);
            System.out.println("Announced Version:     " + version);
            System.out.println("Announced SessionPort: " + port);
            System.out.println("Announced ObjectDescription: ");
            if(objectDescriptions != null) {
                for(AboutObjectDescription o : objectDescriptions) {
                    System.out.println("\t" + o.path);
                    for (String s : o.interfaces) {
                        System.out.println("\t\t" + s);
                    }
                }
            }

            System.out.println("Contents of Announced AboutData:");
            try {
                for (Map.Entry<String, Variant> entry : aboutData.entrySet()) {
                    System.out.print("\tField: " + entry.getKey() + " = ");

                    if (entry.getKey().equals("AppId")) {
                        byte[] appId = entry.getValue().getObject(byte[].class);
                        for (byte b : appId) {
                            System.out.print(String.format("%02X", b));
                        }
                    } else if (entry.getKey().equals("SupportedLanguages")) {
                        String[] supportedLanguages = entry.getValue().getObject(String[].class);
                        for (String s : supportedLanguages) {
                            System.out.print(s + " ");
                        }
                    } else {
                        System.out.print(entry.getValue().getObject(String.class));
                    }
                    System.out.print("\n");
                }
            } catch (AnnotationBusException e1) {
                e1.printStackTrace();
            } catch (BusException e1) {
                e1.printStackTrace();
            }
        }

        private void printAboutData(AboutProxy aboutProxy) {
            try {
                System.out.println("Calling getAboutData:");

                Map<String, Variant> aboutData_en;
                aboutData_en = aboutProxy.getAboutData("en");

                for (Map.Entry<String, Variant> entry : aboutData_en.entrySet()) {
                    System.out.print("\tField: " + entry.getKey() + " = ");

                    if (entry.getKey().equals("AppId")) {
                        byte[] appId = entry.getValue().getObject(byte[].class);
                        for (byte b : appId) {
                            System.out.print(String.format("%02X", b));
                        }
                    } else if (entry.getKey().equals("SupportedLanguages")) {
                        String[] supportedLanguages = entry.getValue().getObject(String[].class);
                        for (String s : supportedLanguages) {
                            System.out.print(s + " ");
                        }
                    } else {
                        System.out.print(entry.getValue().getObject(String.class));
                    }
                    System.out.print("\n");
                }

                System.out.println("Calling getVersion:");
                System.out.println("\tVersion = " + aboutProxy.getVersion());
            } catch (BusException e1) {
                e1.printStackTrace();
            }
        }

        public void announced(String busName, int version, short port, AboutObjectDescription[] objectDescriptions, Map<String, Variant> aboutData) {

            /* ===================== print bus messages ================== */
            printBusMessage(busName, version, port, objectDescriptions, aboutData);
            /* =========================================================== */

            SessionOpts sessionOpts = new SessionOpts();
            sessionOpts.traffic = SessionOpts.TRAFFIC_MESSAGES;
            sessionOpts.isMultipoint = true;
            sessionOpts.proximity = SessionOpts.PROXIMITY_ANY;
            sessionOpts.transports = SessionOpts.TRANSPORT_ANY;

            Mutable.IntegerValue sessionId = new Mutable.IntegerValue();

            mBus.enableConcurrentCallbacks();

            Status status = mBus.joinSession(busName, port, sessionId, sessionOpts, new SessionListener());
            if (status != Status.OK) {
                return;
            }
            System.out.println(String.format("BusAttachement.joinSession successful sessionId = %d", sessionId.value));

            System.out.println("\n\nCreating AboutProxy object and calling remote methods.");
            AboutProxy aboutProxy = new AboutProxy(mBus, busName, sessionId.value);
            System.out.println("Calling getObjectDescription:");
            try {
                AboutObjectDescription aod[] = aboutProxy.getObjectDescription();
                if(aod != null) {
                    for(AboutObjectDescription o : aod) {
                        System.out.println("\t" + o.path);
                        switch (o.path)  {


                            /* register one bus temperature */
                            case("/org/netsec/iot/service/temperature"):
                                temperatureProxyObject =  mBus.getProxyBusObject(busName,
                                        o.path,
                                        sessionId.value,
                                        new Class<?>[] { TemperatureInterface.class});

                                temperatureInterface = temperatureProxyObject.getInterface(TemperatureInterface.class);
                                isTemperatureJoined = true;
                                break;


                            /* register lcd bus object */
                            case("/org/netsec/iot/service/lcd"):
                                lcdProxyObject =  mBus.getProxyBusObject(busName,
                                        o.path,
                                        sessionId.value,
                                        new Class<?>[] { LCDInterface.class});

                                lcdInterface = lcdProxyObject.getInterface(LCDInterface.class);
                                isLCDJoined = true;
                                break;

                        }
                        for (String s : o.interfaces) {
                            System.out.println("\t\t" + s);
                        }
                    }
                }

                /* ===================== print About Data ===================== */
                printAboutData(aboutProxy);
                /* ============================================================ */

            } catch (BusException e1) {
                e1.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        mBus = new BusAttachment("Basic About Client Sample", BusAttachment.RemoteMessage.Receive);

        Status status = mBus.connect();
        if (status != Status.OK) {
            return;
        }

        System.out.println("BusAttachment.connect successful on " + System.getProperty("org.alljoyn.bus.address"));

        MyAboutListenser mAboutListener = new MyAboutListenser();
        mBus.registerAboutListener(mAboutListener);

        String ifaces[] = {"org.netsec.iot.service.*"};
        status = mBus.whoImplements(ifaces);

        if (status != Status.OK) {
            return;
        }

        System.out.println("BusAttachment.whoImplements successful " + "org.netsec.iot.service.*");

        while(!isTemperatureJoined || !isLCDJoined) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                System.out.println("Program interupted");
            }
        }
        System.out.println("Both session are now joined!");
        try {
            while (true) {
                /* composition of two methods */
                lcdInterface.display(0, 3, Double.toString(temperatureInterface.getTemperature()));
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (BusException e1) {
            e1.printStackTrace();
        }
    }
}
