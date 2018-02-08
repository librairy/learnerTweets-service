package org.librairy.service.learner.service.btm;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class Matrix {

    private int[][] map;

    public Matrix(Integer rows, Integer cols){
        this.map = new int[rows][cols];
    }

    public Matrix(){};

    public void increment(Integer row, Integer col){
        this.map[row][col] += 1;
    }

    public void decrement(Integer row, Integer col){
        this.map[row][col] -= 1;
    }

    public int get(Integer row, Integer col){
        return this.map[row][col];
    }

    public int[][] getMap() {
        return map;
    }

    public void setMap(int[][] map) {
        this.map = map;
    }
}
