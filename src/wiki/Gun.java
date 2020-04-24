/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wiki;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import kd_tree.Kd_tree;
import kd_tree.PointEntry;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;
import static wiki.BasicSurfer._oppEnergy;
import static wiki.BasicSurfer.bulletVelocity;
import static wiki.BasicSurfer.project;

import robocode.*;
import static wiki.BasicSurfer.NUM_DIMENSIONS_FOR_KDTREE;

/**
 *
 * @author Andrew The Mega-Noob
 */
public class Gun {

    final static int NUM_DIMENSIONS_FOR_KDTREE = 3;

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

    //distance, velocity, acceleration
    public Kd_tree my_kdtree = new Kd_tree(NUM_DIMENSIONS_FOR_KDTREE);

    ArrayList<Wave> waves = new ArrayList<Wave>();

    Wave last_wave_added = null;

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
        last_wave_added = w;

        if (me.getGunHeat() <= me.getGunCoolingRate()) {
//            System.out.println("Gun: Turn gun right: " + Utils.normalRelativeAngle(aim(_enemyLocation) - me.getGunHeadingRadians() + me.getHeadingRadians()));
            me.setTurnGunRightRadians(Utils.normalRelativeAngle(aim() - me.getGunHeadingRadians()));
            me.setFire(2.0);
        } else {
            me.setTurnGunRightRadians(Utils.normalRelativeAngle(w.directAngle - me.getGunHeadingRadians()));
        }
        last_enemy_velocity = e.getVelocity();
        processWaves(_enemyLocation, me.getTime());
    }

    void processWaves(Point2D.Double enemy_loc, long curr_time) {
        System.out.println("Gun: We are processing waves");
        for (int i = 0; i < waves.size(); i++) {
            Wave w = waves.get(i);
            w.distanceTraveled = w.bulletVelocity * (curr_time - w.fireTime);
            if (w.distanceTraveled > Point2D.distance(enemy_loc.x, enemy_loc.y, w.fireLocation.x, w.fireLocation.y)) {
                //distance, velocity, acceleration
                double[] coordinates = new double[NUM_DIMENSIONS_FOR_KDTREE];
                coordinates[0] = w.enemy_distance;
                coordinates[1] = w.enemy_velocity;
                coordinates[2] = w.enemy_acceleration;

                double[] object = new double[1];
                object[0] = getFactor(w, enemy_loc);
                PointEntry p = new PointEntry(coordinates, object);

                my_kdtree.addPoint(p);

                System.out.println("Gun: We are removing a wave");
                waves.remove(w);
                i--;
            }
        }
        //Test whether a wave hase reached the enemy robot

    }

    double aim() {

        if (last_wave_added == null) {
            return 0.0;
        }

        double[] coordinates = new double[NUM_DIMENSIONS_FOR_KDTREE];
        coordinates[0] = last_wave_added.enemy_distance;
        coordinates[1] = last_wave_added.enemy_velocity;
        coordinates[2] = last_wave_added.enemy_acceleration;

        double[] object = new double[1];
        PointEntry p = new PointEntry(coordinates, object);
        PointEntry nearest_point = my_kdtree.getNearestPoint(p);

        if (nearest_point == null) {
            return last_wave_added.directAngle;
        }
        
//        return last_wave_added.directAngle;

        double gf = nearest_point.dataObject[0];

        return convertGFToAbsBearing(gf, last_wave_added);
    }

    public static double convertGFToAbsBearing(double GF, Wave w) {
        return w.directAngle + maxEscapeAngle(w.bulletVelocity) * w.direction * GF;
    }

    public static double bulletVelocity(double power) {
        return (20D - (3D * power));
    }

    // Given the EnemyWave that the bullet was on, and the point where we
    // were hit, calculate the index into our stat array for that factor.
    public static double getFactor(Wave ew, Point2D.Double targetLocation) {
        double offsetAngle = (absoluteBearing(ew.fireLocation, targetLocation)
                - ew.directAngle);
        return Utils.normalRelativeAngle(offsetAngle)
                / maxEscapeAngle(ew.bulletVelocity) * ew.direction;
    }

    // CREDIT: from CassiusClay, by PEZ
    //   - returns point length away from sourceLocation, at angle
    // robowiki.net?CassiusClay
    public static Point2D.Double project(Point2D.Double sourceLocation, double angle, double length) {
        return new Point2D.Double(sourceLocation.x + Math.sin(angle) * length,
                sourceLocation.y + Math.cos(angle) * length);
    }

    // got this from RaikoMicro, by Jamougha, but I think it's used by many authors
    //  - returns the absolute angle (in radians) from source to target points
    public static double absoluteBearing(Point2D.Double source, Point2D.Double target) {
        return Math.atan2(target.x - source.x, target.y - source.y);
    }

    public static double maxEscapeAngle(double velocity) {
        return Math.asin(8.0 / velocity);
    }

    public void onPaint(java.awt.Graphics2D g) {
        g.setColor(java.awt.Color.green);
        System.out.println("Gun: " + waves.size());
        for (Wave w : waves) {
            Point2D.Double center = w.fireLocation;

            //int radius = (int)(w.distanceTraveled + w.bulletVelocity);
            //hack to make waves line up visually, due to execution sequence in robocode engine
            //use this only if you advance waves in the event handlers (eg. in onScannedRobot())
            //NB! above hack is now only necessary for robocode versions before 1.4.2
            //otherwise use: 
            int radius = (int) w.distanceTraveled;

            g.drawOval((int) (center.x - radius), (int) (center.y - radius), radius * 2, radius * 2);

        }
    }

}
