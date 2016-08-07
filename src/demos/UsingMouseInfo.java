package demos;

/**
 * Created by Andrew on 07/08/2016.
 */

import java.awt.MouseInfo;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

import javax.swing.*;

public class UsingMouseInfo {
    public static void main(String[] args) throws InterruptedException{
        while(true){
            //Thread.sleep(100);
            System.out.println("(" + MouseInfo.getPointerInfo().getLocation().x +
                    ", " +
                    MouseInfo.getPointerInfo().getLocation().y + ")");
        }
    }
}
