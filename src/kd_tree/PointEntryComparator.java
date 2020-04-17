/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kd_tree;

/**
 *
 * @author Andrew The Mega-Noob
 */
public class PointEntryComparator implements Comparable<PointEntryComparator> {

    public PointEntry searchPoint;
    public PointEntry dataPoint;
    public double dist_squared;

    public PointEntryComparator(PointEntry searchP, PointEntry dataP, double dist_sqared) {
        this.searchPoint = searchP;
        this.dataPoint = dataP;
        this.dist_squared = dist_sqared;
    }

    public int compareTo(PointEntryComparator o) {
        if (dist_squared > o.dist_squared) {
            return 1;
        } else if (dist_squared < o.dist_squared) {
            return -1;
        } else {
            return 0;
        }
    }
}
