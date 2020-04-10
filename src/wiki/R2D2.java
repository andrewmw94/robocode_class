/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wiki;

import robocode.*;
import robocode.util.Utils;
import java.awt.geom.*;     // for Point2D's
import java.lang.*;         // for Double and Integer objects
import java.util.ArrayList; // for collection of waves

/**
 *
 * @author awells
 */
public class R2D2 extends AdvancedRobot {

    // This is a rectangle that represents an 800x600 battle field,
    // used for a simple, iterative WallSmoothing method (by Kawigi).
    // If you're not familiar with WallSmoothing, the wall stick indicates
    // the amount of space we try to always have on either end of the tank
    // (extending straight out the front or back) before touching a wall.
    public static Rectangle2D.Double _fieldRect
            = new java.awt.geom.Rectangle2D.Double(18, 18, 764, 564);
    public static double WALL_STICK = 160;

    public Point2D.Double projected_point = new Point2D.Double(0, 0);
    public double _oppEnergy = 100;
    public boolean clockwise = true;

    public void run() {
        while (true) {
            // basic mini-radar code
            turnRadarRightRadians(Double.POSITIVE_INFINITY);
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {

        //Scanning for the enemy robot
        double absBearing = e.getBearingRadians() + getHeadingRadians();
        setTurnRadarRightRadians(Utils.normalRelativeAngle(absBearing - getRadarHeadingRadians()) * 2);

        //Shooting at the robot
        double absoluteBearing = getHeadingRadians() + e.getBearingRadians();
        setTurnGunRightRadians(Utils.normalRelativeAngle(absoluteBearing
                - getGunHeadingRadians() + (e.getVelocity() * Math.sin(e.getHeadingRadians()
                - absoluteBearing) / 13.0)));
        setFire(3.0);
//        
//        setTurnGunRightRadians(Utils.normalRelativeAngle(absBearing - getGunHeadingRadians()));
//        if (getEnergy() > 3 && getGunHeat() == 0) {
//            setFireBullet(3);
//        }

        //Detect enemy bullet fired
        double bulletPower = _oppEnergy - e.getEnergy();
        if (bulletPower < 3.01 && bulletPower > 0.09) {// the enemy shot
            if (Math.random() > 0.5) {
                clockwise = true;
            } else {
                clockwise = false;
            }
        }
        _oppEnergy = e.getEnergy();

        //moving around the enemy robot
        Point2D.Double our_loc = new Point2D.Double(getX(), getY());
        double lateralVelocity = getVelocity() * Math.sin(e.getBearingRadians());
        int dir = lateralVelocity >= 0 ? 1 : -1;

        setTurnRightRadians(Utils.normalRelativeAngle(wallSmoothing(our_loc, absBearing + Math.PI / 2, dir) - getHeadingRadians())); // turn

        if (clockwise) {
            setAhead(8.0);

        } else {
            setAhead(-8.0);
        }
    }

    // CREDIT: from CassiusClay, by PEZ
    //   - returns point length away from sourceLocation, at angle
    // robowiki.net?CassiusClay
    public static Point2D.Double project(Point2D.Double sourceLocation, double angle, double length) {
        return new Point2D.Double(sourceLocation.x + Math.sin(angle) * length,
                sourceLocation.y + Math.cos(angle) * length);
    }

    // CREDIT: Iterative WallSmoothing by Kawigi
    //   - return absolute angle to move at after account for WallSmoothing
    // robowiki.net?WallSmoothing
    public double wallSmoothing(Point2D.Double botLocation, double angle, int orientation) {
        while (!_fieldRect.contains(project(botLocation, angle, 160))) {
            angle += orientation * 0.05;
        }

        projected_point = project(botLocation, angle, 160);
        return angle;
    }

    public void onPaint(java.awt.Graphics2D g) {
        g.setColor(java.awt.Color.red);

        g.drawOval((int) projected_point.x, (int) projected_point.y, 10, 10);

    }

}
