package pt.ulisboa.tecnico.cnv.solver;

import java.util.LinkedList;
import java.util.List;

public class Coordinate {

	private final int x, y;
	
	public Coordinate(final int x, final int y) { this.x = x; this.y = y;};
	
	public int getX() {
		return this.x;
	}
	
	public int getY() {
		return this.y;
	}

	@Override
	public String toString() {
		return String.format("(%d, %d)", this.x, this.y);
	}
	
	public List<Coordinate> getAllNeighboors(final Solver sol){
		final List<Coordinate> neighbors = new LinkedList<Coordinate>();
		if(sol.isValidCoordinate(x-1, y) && (sol.isUnvisitedPassage(x-1,y) || sol.isVisitedPassage(x-1,y))) {
			neighbors.add(new Coordinate(x-1, y));
		} 
		if(sol.isValidCoordinate(x+1, y) && (sol.isUnvisitedPassage(x+1,y) || sol.isVisitedPassage(x+1,y))) {
			neighbors.add(new Coordinate(x+1, y));
		} 
		if(sol.isValidCoordinate(x, y-1) && (sol.isUnvisitedPassage(x,y-1) || sol.isVisitedPassage(x,y-1))) {
			neighbors.add(new Coordinate(x, y-1));
		} 
		if(sol.isValidCoordinate(x, y+1) && (sol.isUnvisitedPassage(x,y+1) || sol.isVisitedPassage(x,y+1))) {
			neighbors.add(new Coordinate(x, y+1));
		}
		return neighbors;
	}
	
	public List<Coordinate> getUnvisitedNeighboors(final Solver sol){
		final List<Coordinate> neighbors = new LinkedList<>();

		if(sol.isValidCoordinate(x-1, y) && sol.isUnvisitedPassage(x-1,y)) {
			neighbors.add(new Coordinate(x-1, y));
		} 
		if(sol.isValidCoordinate(x+1, y) && sol.isUnvisitedPassage(x+1,y)){
			neighbors.add(new Coordinate(x+1, y));
		} 
		if(sol.isValidCoordinate(x, y-1) && sol.isUnvisitedPassage(x,y-1)){
			neighbors.add(new Coordinate(x, y-1));
		} 
		if(sol.isValidCoordinate(x, y+1) && sol.isUnvisitedPassage(x,y+1)){
			neighbors.add(new Coordinate(x, y+1));
		}
		return neighbors;
	}
	
	@Override
	public boolean equals(final Object obj) {
		final Coordinate c = (Coordinate) obj;
		if(c == null) {
			return false;
		}
		return c.x == this.x && c.y == this.y;
	}

/*
	@Override
	public int hashCode() {
		return x-y;
	}

*/
}
