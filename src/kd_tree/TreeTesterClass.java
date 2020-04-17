/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kd_tree;

/**
 *
 * @author awells
 */
public class TreeTesterClass {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Kd_tree my_tree = new Kd_tree(2);

//        double[] arr = {0.0, 0.0};
//        double[] arr2 = {0.0};
//        PointEntry point = new PointEntry(arr, arr2);
//        my_tree.addPoint(point);
//
//        arr = new double[]{0.0, 1.0};
//        arr2 = new double[]{0.0};
//        point = new PointEntry(arr, arr2);
//        my_tree.addPoint(point);
        long start_time = System.nanoTime();

        int point_count = 10000;
        for (int i = 0; i < point_count; i++) {
            double[] arr = new double[]{Math.random(), Math.random()};
            double[] arr2 = new double[]{i};
            PointEntry point = new PointEntry(arr, arr2);
            my_tree.addPoint(point);
        }

        System.out.println("Building the tree took: " + (System.nanoTime() - start_time) / 1e9 + " seconds");

        start_time = System.nanoTime();
        int search_point_count = 10000;
        for (int i = 0; i < search_point_count; i++) {
            double[] arr = new double[]{Math.random(), Math.random()};
            double[] arr2 = new double[]{i};
            PointEntry point = new PointEntry(arr, arr2);
//            my_tree.getNearestPoint(point);
            my_tree.getKNearestPoints(point, 30);
        }
        System.out.println("Searching the tree took: " + (System.nanoTime() - start_time) / 1e9 + " seconds");

//        System.out.println("The nearest point is: " + nearest_p);
    }
}
