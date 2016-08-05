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
package org.netsec.iot.lcd;

import com.pi4j.wiringpi.*;

class LcdDriver {

    private int _LCDAddr;
    private int _BLEN;
    private int _fd;

    public LcdDriver() {
        _LCDAddr = 0x27; 
        _BLEN    = 1;
        _fd      = I2C.wiringPiI2CSetup(_LCDAddr);
        init();
    }
    
    private void write_word(int data){
        int temp = data;
        if ( _BLEN == 1 )
            temp |= 0x08;
        else
            temp &= 0xF7;

        I2C.wiringPiI2CWrite(_fd, temp);
    }


    private void send_command(int comm){
        int buf;
        // Send bit7-4 firstly
        buf = comm & 0xF0;
        buf |= 0x04;			// RS = 0, RW = 0, EN = 1
        write_word(buf);
        Gpio.delay(2);
        buf &= 0xFB;			// Make EN = 0
        write_word(buf);

        // Send bit3-0 secondly
        buf = (comm & 0x0F) << 4;
        buf |= 0x04;			// RS = 0, RW = 0, EN = 1
        write_word(buf);
        Gpio.delay(2);
        buf &= 0xFB;			// Make EN = 0
        write_word(buf);
    }

    private void send_data(int data){
        int buf;
        // Send bit7-4 firstly
        buf = data & 0xF0;
        buf |= 0x05;			// RS = 1, RW = 0, EN = 1
        write_word(buf);
        Gpio.delay(2);
        buf &= 0xFB;			// Make EN = 0
        write_word(buf);

        // Send bit3-0 secondly
        buf = (data & 0x0F) << 4;
        buf |= 0x05;			// RS = 1, RW = 0, EN = 1
        write_word(buf);
        Gpio.delay(2);
        buf &= 0xFB;			// Make EN = 0
        write_word(buf);
    }

    public void init(){
        send_command(0x33);	// Must initialize to 8-line mode at first
        Gpio.delay(5);
        send_command(0x32);	// Then initialize to 4-line mode
        Gpio.delay(5);
        send_command(0x28);	// 2 Lines & 5*7 dots
        Gpio.delay(5);
        send_command(0x0C);	// Enable display without cursor
        Gpio.delay(5);
        send_command(0x01);	// Clear Screen
        I2C.wiringPiI2CWrite(_fd, 0x08);
    }

    public void clear(){
        send_command(0x01);	//clear Screen
    }

    public void write(int x, int y, String data){
        int addr, i;
        int tmp;
        if (x < 0)  x = 0;
        if (x > 15) x = 15;
        if (y < 0)  y = 0;
        if (y > 1)  y = 1;

        // Move cursor
        addr = 0x80 + 0x40 * y + x;
        send_command(addr);
        
        tmp = data.length();
        for (i = 0; i < tmp; i++){
            send_data(data.charAt(i));
        }
    }
}


