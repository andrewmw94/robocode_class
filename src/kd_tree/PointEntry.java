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
public class PointEntry {

    public final double[] pointCoordinates;
    public final double[] dataObject;

    public PointEntry(double[] coordinates, double[] object) {
        this.pointCoordinates = coordinates;
        this.dataObject = object;
    }
    
    @Override
    public String toString() {
        String s = "";
        for (int i = 0; i < pointCoordinates.length; i++) {
            s = s + pointCoordinates[i] + ", ";
        }
        return s;
    }
}
