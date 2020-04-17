/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wiki;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;
import static wiki.BasicSurfer._oppEnergy;
import static wiki.BasicSurfer.bulletVelocity;
import static wiki.BasicSurfer.project;

import robocode.*;

/**
 *
 * @author Andrew The Mega-Noob
 */
public class Gun {

    class Wave {

        Point2D.Double fireLocation;
        long fireTime;
        double bulletVelocity, directAngle, distanceTraveled;
        int direction;

        double enemy_distance;
        double enemy_velocity;
        double enemy_acceleration;

        public Wave() {
        }
    }

    ArrayList<Wave> waves = new ArrayList<Wave>();
    
    double last_enemy_velocity;

    public void onScannedRobot(ScannedRobotEvent e, AdvancedRobot me) {
        Point2D.Double _myLocation = new Point2D.Double(me.getX(), me.getY());
        double lateralVelocity = e.getVelocity() * Math.sin(e.getBearingRadians());
        double absBearing = e.getBearingRadians() + me.getHeadingRadians();
        Point2D.Double _enemyLocation = project(_myLocation, absBearing, e.getDistance());

        Wave w = new Wave();
        w.bulletVelocity = bulletVelocity(2.0);
        w.directAngle = absBearing;
        w.direction = 1;
        w.distanceTraveled = 0;
        w.fireLocation = _myLocation;
        w.fireTime = me.getTime();

        w.enemy_distance = Point2D.distance(_myLocation.x, _myLocation.y, _enemyLocation.x, _enemyLocation.y);
        w.enemy_velocity = e.getVelocity();
        w.enemy_acceleration = last_enemy_velocity - e.getVelocity();

        waves.add(w);

        if (me.getGunHeat() == 0.0) {
            me.turnGunRightRadians(aim());
            me.fire(2.0);
        }
        last_enemy_velocity = e.getVelocity();
    }

    double aim() {
        return 0.0;
    }

    public static double bulletVelocity(double power) {
        return (20D - (3D * power));
    }
}
