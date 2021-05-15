/*
 * Copyright (c) 2016, Florian Frankenberger
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * Neither the name of the copyright holder nor the names of its contributors
 *   may be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package oled.oled;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.util.Arrays;

import org.apache.log4j.Logger;


/**
 * A raspberry pi driver for the 128x64 pixel OLED display (i2c bus).
 * The supported kind of display uses the SSD1306 driver chip and
 * is connected to the raspberry's i2c bus (bus 1).
 * <p>
 * Note that you need to enable i2c (using for example raspi-config).
 * Also note that you need to load the following kernel modules:
 * </p>
 * <pre>i2c-bcm2708</pre> and <pre>i2c_dev</pre>
 * <p>
 * Also note that it is possible to speed up the refresh rate of the
 * display up to ~60fps by adding the following to the config.txt of
 * your raspberry: dtparam=i2c1_baudrate=1000000
 * </p>
 * <p>
 * Sample usage:
 * </p>
 * <pre>
 * OLEDDisplay display = new OLEDDisplay();
 * display.drawStringCentered("Hello World!", 25, true);
 * display.update();
 * Thread.sleep(10000); //sleep some time, because the display
 *                      //is automatically cleared the moment
 *                      //the application terminates
 * </pre>
 * <p>
 * This class is basically a rough port of Adafruit's BSD licensed
 * SSD1306 library (https://github.com/adafruit/Adafruit_SSD1306)
 * </p>
 *
 * @author Florian Frankenberger
 */
public class OLEDDisplayDriver {

    private static Logger LOGGER = Logger.getLogger(OLEDDisplayDriver.class);

    private static final int DEFAULT_I2C_BUS = I2CBus.BUS_1;
    private static final int DEFAULT_DISPLAY_ADDRESS = 0x3C;

    static final int DISPLAY_WIDTH = 128;
    static final int DISPLAY_HEIGHT = 32;
    private static final int MAX_INDEX = (DISPLAY_HEIGHT / 8) * DISPLAY_WIDTH;

    private static final byte SSD1306_SETCONTRAST = (byte) 0x81;
    private static final byte SSD1306_DISPLAYALLON_RESUME = (byte) 0xA4;
    private static final byte SSD1306_DISPLAYALLON = (byte) 0xA5;
    private static final byte SSD1306_NORMALDISPLAY = (byte) 0xA6;
    private static final byte SSD1306_INVERTDISPLAY = (byte) 0xA7;
    private static final byte SSD1306_DISPLAYOFF = (byte) 0xAE;
    private static final byte SSD1306_DISPLAYON = (byte) 0xAF;

    private static final byte SSD1306_SETDISPLAYOFFSET = (byte) 0xD3;
    private static final byte SSD1306_SETCOMPINS = (byte) 0xDA;

    private static final byte SSD1306_SETVCOMDETECT = (byte) 0xDB;

    private static final byte SSD1306_SETDISPLAYCLOCKDIV = (byte) 0xD5;
    private static final byte SSD1306_SETPRECHARGE = (byte) 0xD9;

    private static final byte SSD1306_SETMULTIPLEX = (byte) 0xA8;

    private static final byte SSD1306_SETLOWCOLUMN = (byte) 0x00;
    private static final byte SSD1306_SETHIGHCOLUMN = (byte) 0x10;

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    private static final byte SSD1306_SETSTARTLINE = (byte) 0x40;

    private static final byte SSD1306_MEMORYMODE = (byte) 0x20;
    private static final byte SSD1306_COLUMNADDR = (byte) 0x21;
    private static final byte SSD1306_PAGEADDR = (byte) 0x22;

    private static final byte SSD1306_COMSCANINC = (byte) 0xC0;
    private static final byte SSD1306_COMSCANDEC = (byte) 0xC8;

    private static final byte SSD1306_SEGREMAP = (byte) 0xA0;

    private static final byte SSD1306_CHARGEPUMP = (byte) 0x8D;

    private static final byte SSD1306_EXTERNALVCC = (byte) 0x1;
    private static final byte SSD1306_SWITCHCAPVCC = (byte) 0x2;

    private static I2CBus bus;
    private static I2CDevice device;

    static {
        try {
            bus = I2CFactory.getInstance(DEFAULT_I2C_BUS);
            device = bus.getDevice(DEFAULT_DISPLAY_ADDRESS);
        } catch (UnsupportedBusNumberException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private OLEDDisplayDriver() {
    }

    private static OLEDDisplayDriver oledDisplayDriver = new OLEDDisplayDriver();

    public static OLEDDisplayDriver getInstance() {
        return oledDisplayDriver;
    }

    private void writeCommand(byte command) throws IOException {
        device.write(0x00, command);
    }

    public void initOLEDDisplay() throws IOException, UnsupportedBusNumberException {
        LOGGER.debug("initOLEDDisplay i2c bus");

        //add shutdown hook that clears the display
        //and closes the bus correctly when the software
        //if terminated.
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                shutdown();
            }
        });

        writeCommand((byte) 0xAE); //关显示

        writeCommand((byte) 0xD5); //设置显示始终 分比率
        writeCommand((byte) 0x80); //建议比率0x80

        writeCommand((byte) 0xA8); //设置MUX比率
        writeCommand((byte) 0x1F); //设置为0x1F，即十进制31

        writeCommand((byte) 0xD3); //设置显示补偿
        writeCommand((byte) 0x00); //no offset
        writeCommand((byte) (0x40 | 0x0)); //line #0

        writeCommand((byte) 0x8D); //是否使用电源
        writeCommand((byte) 0x14); //使用外置电源，固定值，见ssd1306文档

        writeCommand((byte) 0x20); //设置内存地址模式
        writeCommand((byte) 0x00); //水平地址模式

        writeCommand((byte) (0xA0 | 0x1)); //实在没搞懂这个是什么用处的

        writeCommand((byte) 0xC8); //设置列输出扫描方向,Scan from COM[N-1] to COM0
        writeCommand((byte) 0xDA); //设置列引脚硬件配置
        writeCommand((byte) 0x02); //上文已经描述该字段

        writeCommand((byte) 0x81); //设置对比度
        writeCommand((byte) 0x8F);

        writeCommand((byte) 0xD9); //设置预充电周期
        writeCommand((byte) 0xF1);
        writeCommand((byte) 0xDB); //设置VCOMH反压值
        writeCommand((byte) 0x40);
        writeCommand((byte) 0xA4); //启用输出GDDRAM中的数据
        writeCommand((byte) 0xA6); //设置正常显示，A7表示反转显示

        writeCommand((byte) 0xAF);//--turn on oled panel
    }

    /**
     * sends the current buffer to the display
     *
     * @throws IOException
     */
    public synchronized void display(Image srcImg) throws IOException {
        writeCommand(SSD1306_COLUMNADDR);
        writeCommand((byte) 0);   // Column start address (0 = reset)
        writeCommand((byte) (DISPLAY_WIDTH - 1)); // Column end address (127 = reset)

        writeCommand(SSD1306_PAGEADDR);
        writeCommand((byte) 0); // Page start address (0 = reset)
        writeCommand((byte) 7); // Page end address

        BufferedImage bufferedImage = new BufferedImage(DISPLAY_WIDTH, DISPLAY_HEIGHT, BufferedImage.TYPE_BYTE_BINARY);
        Graphics graphics = bufferedImage.getGraphics();

        //将原始位图按屏幕大小缩小后绘制到bufferedImage对象中
        graphics.drawImage(srcImg, 0, 0, DISPLAY_WIDTH, DISPLAY_HEIGHT, null);

        byte[] pixels = ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData();
        LOGGER.debug("pixels size = " + pixels.length);

        byte[] pixelsNew = new byte[DISPLAY_HEIGHT * DISPLAY_WIDTH / 8];
        Arrays.fill(pixelsNew, (byte) 0x00);
        int index = 0;
        int pixelIndex;
        for (int y = 0; y < DISPLAY_HEIGHT / 8; y++) {
            for (int x = 0; x < DISPLAY_WIDTH; x++) {
                for (int height = 7; height >= 0; height--) {
                    pixelsNew[index] = (byte) (pixelsNew[index] << 1);
                    pixelIndex = (y * 8 + height) * 16 + x / 8;
                    int value = (byte) ((pixels[pixelIndex] >> (7 - (x % 8))));
                    value &= 0x01;
                    if (value > 0) {
                        pixelsNew[index] |= 0x01;
                    } else {
                        pixelsNew[index] &= ~(0x01);
                    }
                }
                index++;
            }
        }

        for (int i = 0; i < ((DISPLAY_WIDTH * DISPLAY_HEIGHT / 8) / 16); i++) {
            // send a bunch of data in one xmission，每行写入16个字节
            device.write((byte) 0x40, pixelsNew, i * 16, 16);
        }
        writeCommand((byte) 0x2e);
    }

    private synchronized void shutdown() {
        try {
            //now we close the bus
            bus.close();
        } catch (IOException ex) {
            LOGGER.debug("Closing i2c bus");
        }
    }

}
