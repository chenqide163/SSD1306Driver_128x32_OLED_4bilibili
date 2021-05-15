package oled.oled;

import com.pi4j.io.i2c.I2CFactory;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class OledMain {
    private static Logger LOGGER = Logger.getLogger(OledMain.class);
    public static void main(String[] args) throws IOException, I2CFactory.UnsupportedBusNumberException, InterruptedException {
        LOGGER.debug("begin to show~");
        OLEDDisplayDriver.getInstance().initOLEDDisplay();

        //显示封面图片1
        OLEDDisplayDriver.getInstance().display(DrawImg.getCoverTile());
        Thread.sleep(2000);

        //显示封面图片1
        OLEDDisplayDriver.getInstance().display(DrawImg.getCoverTail());
        Thread.sleep(2000);

        //滚动显示爬取到的图片名称
        DrawImg.getTitleList();

        //显示爬虫假数据
        DrawImg.printCrawlerData();
    }

}
