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
public class KDTreeNode {

    final int max_bucket_size = 20;
    PointEntry[] list_of_points;
    int divider_dimension;
    double divider_value;
    KDTreeNode leftChild;
    KDTreeNode rightChild;
    double[][] bounding_box;//[dimension][0] = min; [dimension][1] = max
    boolean is_leaf_node;
    private int last_point_index;

    public KDTreeNode(int dimension) {
        bounding_box = new double[dimension][2];
        last_point_index = 0;
        is_leaf_node = true;
        divider_dimension = -1;
        list_of_points = new PointEntry[max_bucket_size + 1];
        leftChild = null;
        rightChild = null;
    }

    //Get closest point. Return null if point is greater than best_distance away
    public PointEntry get_nearest_point(PointEntry p, double best_distance) {
        //Make sure this node could have a closer point
        if (best_distance < distanceSquaredToBox(p, bounding_box)) {
            return null;
        }

        if (is_leaf_node) {
            PointEntry best_p = null;
            for (int i = 0; i < last_point_index; i++) {
                if (distanceSquared(p, list_of_points[i]) < best_distance) {
                    best_distance = distanceSquared(p, list_of_points[i]);
                    best_p = list_of_points[i];
                }
            }
            return best_p;
        } else {
            KDTreeNode good_child = null;
            KDTreeNode bad_child = null;
            if (divider_value < p.pointCoordinates[divider_dimension]) {
                good_child = rightChild;
                bad_child = leftChild;
            } else {
                good_child = leftChild;
                bad_child = rightChild;
            }
            PointEntry p1 = good_child.get_nearest_point(p, best_distance);//Get nearest child in the right tree
            if (p1 != null) {
                double d = distanceSquared(p, p1); //Get distance to this child
                PointEntry p2 = bad_child.get_nearest_point(p, d);
                if (p2 != null) {
                    return p2;
                } else {
                    return p1;
                }
            } else {
                return bad_child.get_nearest_point(p, best_distance);
            }
        }
    }

    public PriorityQueue<PointEntryComparator> get_k_nearest_points(PointEntry p, int k) {
        PriorityQueue<PointEntryComparator> closest_points = new PriorityQueue<>(k);

        double[] coordinates;
        coordinates = new double[p.pointCoordinates.length];
        for (int i = 0; i < p.pointCoordinates.length; i++) {
            coordinates[i] = 0.0;
        }

        for (int i = 0; i < k; i++) {
            PointEntry p1 = new PointEntry(coordinates.clone(), new double[]{0.0});
            closest_points.add(new PointEntryComparator(p, p1, Double.MAX_VALUE));
        }

        get_k_nearest_points(p, closest_points);

        return closest_points;

    }

    //Get closest point. Return null if point is greater than best_distance away
    private void get_k_nearest_points(PointEntry p, PriorityQueue<PointEntryComparator> closest_points) {
        //Make sure this node could have a closer point
        if (closest_points.peek().dist_squared < distanceSquaredToBox(p, bounding_box)) {
            return;
        }
        if (is_leaf_node) {
            PointEntry best_p = null;
            for (int i = 0; i < last_point_index; i++) {
                if (distanceSquared(p, list_of_points[i]) < closest_points.peek().dist_squared) {
                    closest_points.add(new PointEntryComparator(p, list_of_points[i], distanceSquared(p, list_of_points[i])));
                }
            }
            return;
        } else {
            KDTreeNode good_child = null;
            KDTreeNode bad_child = null;
            if (divider_value < p.pointCoordinates[divider_dimension]) {
                good_child = rightChild;
                bad_child = leftChild;
            } else {
                good_child = leftChild;
                bad_child = rightChild;
            }
            good_child.get_k_nearest_points(p, closest_points);
            bad_child.get_k_nearest_points(p, closest_points);
        }
    }

    public void add_point(PointEntry p) {
        if (is_leaf_node) {
            //if a leaf node can fit the point, just add to the list
            if (last_point_index < max_bucket_size) {
                list_of_points[last_point_index] = p;
                last_point_index++;
            } else {
                //split the node if it is too big
                int split_dimension = 0;
                double max_dimension_range = 0;
                for (int i = 0; i < bounding_box.length; i++) {
                    double[] min_max = bounding_box[i];
                    double range = min_max[1] - min_max[0];
                    if (range > max_dimension_range) {
                        split_dimension = i;
                        max_dimension_range = range;
                    }
                }
                double split_value = 0;
                //TODO: improve this by using the median or some approximation
                split_value = (bounding_box[split_dimension][0] + bounding_box[split_dimension][1]) / 2;

                double v1 = list_of_points[last_point_index / 2].pointCoordinates[split_dimension];
                double v2 = list_of_points[last_point_index - 1].pointCoordinates[split_dimension];
                double v3 = list_of_points[0].pointCoordinates[split_dimension];

                split(split_dimension, getMedian(v1, v2, v3));

            }
        } else {
            //if it's not a leaf node, go to the correct child
            if (p.pointCoordinates[divider_dimension] > divider_value) {
                rightChild.add_point(p);
            } else {
                leftChild.add_point(p);
            }
        }
        updateBoundingBox(p);
    }

    private void split(int dimension, double value) {
        assert is_leaf_node;//Throw error if not a leaf node

        leftChild = new KDTreeNode(bounding_box.length);
        rightChild = new KDTreeNode(bounding_box.length);

        for (int i = 0; i < last_point_index; i++) {
            PointEntry p = list_of_points[i];
            if (p.pointCoordinates[dimension] > value) {
                rightChild.add_point(p);
            } else {
                leftChild.add_point(p);
            }
        }

        is_leaf_node = false;
        last_point_index = 0;
        list_of_points = null;
        divider_dimension = dimension;
        divider_value = value;
    }

    private void updateBoundingBox(PointEntry p) {
        for (int i = 0; i < bounding_box.length; i++) {
            if (p.pointCoordinates[i] < bounding_box[i][0]) {
                bounding_box[i][0] = p.pointCoordinates[i];
            } else if (p.pointCoordinates[i] > bounding_box[i][1]) {
                bounding_box[i][1] = p.pointCoordinates[i];
            }
        }
    }

    public int getLastPointIndex() {
        return last_point_index;
    }

    public void setLastPointIndex(int index) {
        last_point_index = index;
    }

    //We use the squared distance because we only care about the relative distances between pairs of points
    private double distanceSquared(PointEntry p1, PointEntry p2) {
        double sum = 0.0;
        for (int i = 0; i < p1.pointCoordinates.length; i++) {
            sum += (p1.pointCoordinates[i] - p2.pointCoordinates[i]) * (p1.pointCoordinates[i] - p2.pointCoordinates[i]);
        }
        return sum;
    }

    //We use the squared distance because we only care about the relative distances between pairs of points
    private double distanceSquaredToBox(PointEntry p, double[][] box) {
        double sum = 0.0;
        for (int i = 0; i < p.pointCoordinates.length; i++) {
            if (p.pointCoordinates[i] < box[i][0]) {
                sum += (p.pointCoordinates[i] - box[i][0]) * (p.pointCoordinates[i] - box[i][0]);
            } else if (p.pointCoordinates[i] > box[i][1]) {
                sum += (p.pointCoordinates[i] - box[i][1]) * (p.pointCoordinates[i] - box[i][1]);
            } else {
                sum += 0;
            }
        }
        return sum;
    }

    private double getMedian(double v1, double v2, double v3) {
        if (v1 > v2) {
            if (v1 > v3) {
                if (v3 > v2) {
                    return v2;
                } else {
                    return v3;
                }
            } else {
                return v1;
            }
        } else {
            if (v2 > v3) {
                if (v3 > v1) {
                    return v3;
                } else {
                    return v1;
                }
            } else {
                return v2;
            }
        }
    }

}
