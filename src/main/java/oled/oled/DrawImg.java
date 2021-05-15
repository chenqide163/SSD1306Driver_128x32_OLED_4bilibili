package oled.oled;

import com.pi4j.io.i2c.I2CFactory;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Date;
import java.util.Random;

public class DrawImg {
    private static Logger LOGGER = Logger.getLogger(DrawImg.class);

    public static void main(String[] args) throws IOException {

        //读取原始位图
        BufferedImage destImg = getCoverTail();
        ImageIO.write(destImg, "jpg", new File("D:\\test2.jpg"));

        destImg = getCoverTail();
        ImageIO.write(destImg, "jpg", new File("D:\\test2.jpg"));

    }

    public static BufferedImage getCoverTile() {
        BufferedImage image = new BufferedImage(128, 32,
                BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g = image.createGraphics();
        //从树莓派的输出上，看到微软雅黑字体名为：Microsoft YaHei，指定字体为15像素
        g.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
        g.setColor(new Color(0xffffffff));
        Date date = new Date();
        g.drawString("Java", 0, 15);
        g.drawString("玩转", 0, 31);
        g.setFont(new Font("Microsoft YaHei", Font.PLAIN, 28));
        g.drawString("树莓派", 37, 28);

        return image;
    }

    public static BufferedImage getCoverTail() {
        BufferedImage image = new BufferedImage(128, 32,
                BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g = image.createGraphics();
        //从树莓派的输出上，看到微软雅黑字体名为：Microsoft YaHei，指定字体为15像素
        g.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
        g.setColor(new Color(0xffffffff));
        Date date = new Date();
        g.drawString("ssd1306 128x32", 0, 15);
        g.drawString("陈琦玩派派", 50, 31);

        return image;
    }

    /**
     * 输出爬虫数据
     *
     * @throws IOException
     * @throws InterruptedException
     * @throws I2CFactory.UnsupportedBusNumberException
     */
    public static void printCrawlerData() throws IOException, InterruptedException, I2CFactory.UnsupportedBusNumberException {
        int movieNum = 342;
        int postNum = 845;
        int diskUsage = 20;
        String ip = getRaspiIP();
        for (; ; ) {
            BufferedImage image = new BufferedImage(128, 32,
                    BufferedImage.TYPE_BYTE_BINARY);
            Graphics2D g = image.createGraphics();
            //从树莓派的输出上，看到微软雅黑字体名为：Microsoft YaHei，指定字体为15像素
            g.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
            g.setColor(new Color(0xffffffff));

            long time = ((new Date()).getTime()) / 1000;
            if (time % 9 < 3) {
                Date date = new Date();
                String dayTiem = String.format("%tm-%td  %tH:%tM:%tS", date, date, date, date, date);
                g.drawString(dayTiem, 0, 15);
                g.drawString("IP: " + ip, 0, 31);
            } else if (time % 9 < 6) {
                g.drawString("已爬内容: " + (movieNum += (new Random()).nextInt(3)), 0, 15);
                g.drawString("已爬海报: " + (postNum += (new Random()).nextInt(4)), 0, 31);
            } else {
                g.drawString("CPU: " + (60 + (new Random()).nextInt(30)) + "%", 0, 15);
                g.drawString("磁盘占用: " + (diskUsage += (new Random()).nextFloat()) + "%", 0, 31);
            }


            OLEDDisplayDriver.getInstance().display(image);
            Thread.sleep(800);
        }
    }

    /**
     * 滚动显示爬取到的图片名称
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public static void getTitleList() throws IOException, InterruptedException {
        LOGGER.debug("start to run getTitleList");
        String[] Tiles = {"BT-170 Glamorous . Mio Futaba.jpg",
                "BT-171 Nympho Sisters . Azumi Nakama, Mayumi Sakanishi, Hikaru Nakamura.jpg",
                "BT-172 Lesbian Orgy . Haruka Aizawa, Aya Kisaki.jpg",
                "BT-173-A TOKIMEKI Compilation . Mari Tashiro, Mikako Minami, Nana, Nana Jinguji.jpg",
                "BT-174-A The Punishment For A Mature Beauty . Chihiro Akino.jpg",
                "BT-175 Cock Lover Sucking Immediately . Saya Otomi.jpg",
                "BT-176-A Welcome to Luxury Soapland Compilation . Riho Kodaka, Yua Natsuki, Hitomi Shibuya.jpg",
                "CW3D2BD-02 3D CATWALK POISON 02 . Maria Ozawa.jpg",
                "CWPBD-136 CATWALK POISON 136 Former Model’s First Japorn . Risa Mizuki.jpg",
                "CWPBD-157 CATWALK POISON 157 Share Girl . Mihane Yuuki.jpg",
                "CWPBD-158 CATWALK POISON 158 After School . Nozomi Momoki.jpg",
                "KG-10 Sakurano.jpg",
                "KG-11 Akira Shiratori.jpg",
                "KG-12 Miu Satsuki.jpg",
                "KG-13 Nagisa Minazuki.jpg",
                "KG-14 Hayama Kumiko.jpg",
                "KG-15 Natsuki Yui.jpg",
                "KG-17 Yuki Takizawa.jpg",
                "KG-18 Maiko Ohshiro.jpg",
                "KG-19 Mami Kato.jpg",
                "KG-20 Emiri Suzuki.jpg",
                "CWPBD-136 Former Model’s First Japorn . Risa Mizuki.jpg",
                "CWPBD-157 Share Girl . Mihane Yuuki.jpg",
                "CWPBD-158 After School . Nozomi Momoki.jpg",
                "CWPBD-161 Don’t Cum 24 . Rina Nanase.jpg",
                "CWPBD-162 Extreme Fuck With School Uniform JK . Sakura Nozomi.jpg",
                "CWPBD-163 Luxury Soap . Ryo Ikushima.jpg",
                "CWPBD-164 Luxury Soap . Serina Fukami.jpg",
                "CWPBD-165 Gokusya . Ryo Ikushima.jpg",
                "CWPBD-166 DEBUT . Ruri Tachibana.jpg"};

        for (int i = 0; i < Tiles.length - 2; i++) {
            BufferedImage image = new BufferedImage(128, 32,
                    BufferedImage.TYPE_BYTE_BINARY);
            Graphics2D g = image.createGraphics();
            //从树莓派的输出上，看到微软雅黑字体名为：Microsoft YaHei，指定字体为15像素
            g.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
            g.setColor(new Color(0xffffffff));

            g.drawString(Tiles[i], 0, 10);
            g.drawString(Tiles[i + 1], 0, 20);
            g.drawString(Tiles[i + 2], 0, 31);

            OLEDDisplayDriver.getInstance().display(image);
            Thread.sleep(300);
        }
    }

    /**
     * 获取树莓派IP
     *
     * @return
     */
    private static String getRaspiIP() {
        InputStream in = null;
        BufferedReader read = null;
        try {
            String command = "hostname -I | cut -d' ' -f1";
            Process pro = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", command});
            pro.waitFor();
            in = pro.getInputStream();
            read = new BufferedReader(new InputStreamReader(in));
            String result = "";
            String line;

            while ((line = read.readLine()) != null) {
                result = result + line + "\n";
            }
            LOGGER.debug("getRaspiIP is : " + result);
            return result;
        } catch (Exception e) {
            LOGGER.error(e);
            return "do not get the IP!";
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (read != null) {
                try {
                    read.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
