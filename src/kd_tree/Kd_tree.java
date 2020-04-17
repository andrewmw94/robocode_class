/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kd_tree;

import java.util.PriorityQueue;

/**
 *
 * @author awells
 */
public class Kd_tree {

    KDTreeNode root_node;

    public Kd_tree(int dimension) {
        root_node = new KDTreeNode(dimension);
    }

    public void addPoint(PointEntry p) {
        KDTreeNode curr_node = root_node;
        curr_node.add_point(p);
    }

    public PointEntry getNearestPoint(PointEntry p) {
        return root_node.get_nearest_point(p, Double.POSITIVE_INFINITY);
    }
    
    public PriorityQueue<PointEntryComparator> getKNearestPoints(PointEntry p, int k) {
        return root_node.get_k_nearest_points(p, k);
    }

    //We use the squared distance because we only care about the relative distances between pairs of points
    private double distanceSquared(PointEntry p1, PointEntry p2) {
        double sum = 0.0;
        for (int i = 0; i < p1.pointCoordinates.length; i++) {
            sum += (p1.pointCoordinates[i] - p2.pointCoordinates[i]) * (p1.pointCoordinates[i] - p2.pointCoordinates[i]);
        }
        return sum;
    }

}
