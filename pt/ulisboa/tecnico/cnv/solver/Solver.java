package pt.ulisboa.tecnico.cnv.solver;

import pt.ulisboa.tecnico.cnv.util.Gradient;
import pt.ulisboa.tecnico.cnv.util.ImageBuilder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Solver {

    public static final byte UNEXPLORED = '0';
    public static final byte EXPLORED = '1';
    public static final byte QUEUED = 'Q';

    private final SolverArgumentParser ap;
    private final SolverStrategy strategy;
    private double[][] data = null;
    private final byte[][] visitedGrid;

    private double min = Double.MAX_VALUE;
    private double max = Double.MIN_VALUE;
    private int maxX = -1;
    private int maxY = -1;

    protected Solver(final SolverArgumentParser ap, SolverStrategy strategy) {
        this.ap = ap;
        this.strategy = strategy;
        this.visitedGrid = new byte[ap.getWidth()][ap.getHeight()];

        for(int i = 0; i < ap.getWidth(); i++) {
            for(int j = 0; j < ap.getHeight(); j++) {
                this.visitedGrid[i][j] = UNEXPLORED;
            }
        }
    }

    public boolean isDebugging() {
        return this.ap.isDebugging();
    }

    public double[][] getData() {
        return data;
    }

    public Integer getWidth() {
        return this.ap.getWidth();
    }

    public Integer getHeight() {
        return this.ap.getHeight();
    }

    public Integer getX0() {
        return ap.getX0();
    }

    public Integer getY0() {
        return ap.getY0();
    }

    public Integer getX1() {
        return ap.getX1();
    }

    public Integer getY1() {
        return ap.getY1();
    }

    private double[][] readData() throws IOException, ClassNotFoundException {
        // Deserialize the chosen image .dat file.
        final FileInputStream fis = new FileInputStream(ap.getInputImage());
        final ObjectInputStream iis = new ObjectInputStream(fis);
        return (double[][]) iis.readObject();

    }

    @Override
    public String toString() {
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        final Date date = new Date();

        final StringBuilder sb = new StringBuilder(this.ap.getSolverStrategy().toString())
                .append("_").append(this.ap.getWidth())
                .append("x")
                .append(this.ap.getHeight())
                .append("_").append(dateFormat.format(date))
                .append(".png");

        return sb.toString();
    }

    public BufferedImage solveImage() throws IOException, ClassNotFoundException {

        // Read data into the 2D double array.
        this.data = this.readData();

        // From here we have the data loaded in a 2D double array, next step is solving.
        // Solve surface on the given coordinates and produce image with a depiction of the path.

        // Find out the maximum within the search rectangle.
        for(int x = this.ap.getX0(); x < this.ap.getX1(); x++) {
            for(int y = this.ap.getY0(); y < this.ap.getY1(); y++) {
                if(data[x][y] < this.min) {
                    this.min = data[x][y];
                }
                if(data[x][y] > this.max) {
                    this.max = data[x][y];
                    this.maxX = x;
                    this.maxY = y;
                }
            }
        }

        if(ap.isDebugging()) {
            System.out.println(String.format("> Target is the maximum value at (%d, %d) within rectangle upper-left (%d, %d) and lower-right (%d, %d).", this.getTargetX(), this.getTargetY(), this.getX0(), this.getY0(), this.getX1(), this.getY1()));
            System.out.println(String.format("> Starting at (%d, %d).", this.getStartX(), this.getStartY()));
        }
        // Execute the solving strategy.
        this.strategy.solve(this);

        if(ap.isDebugging()) {
            System.out.println(String.format("> Solution path is %d pixels long.", this.path.size()));
        }

        // Rebuild the image based on the user-provided .dat file.
        final BufferedImage bufferedImage = ImageBuilder.buildImage(data, ImageBuilder.colors);
        final Graphics2D gfx = bufferedImage.createGraphics();
        final Color[] pathColorGradient = Gradient.createGradient(Color.BLACK, Color.WHITE, path.size());

        // Draw the solution path while maintaining the original image visible.
        int i = 0;
        int gradIndex = 0;
        for(final Coordinate c: this.path) {


            if( ! ap.drawPathDashed() || (i > 0 && i % 50 > 0)) {
                gfx.setPaint ( pathColorGradient[gradIndex] );
                gfx.fillRect ( c.getX(), c.getY(), 1, 1 );
            }
            else if (i > 0 && i % 50 == 0) {
                i = -100;
            }
            gradIndex++;
            i++;
        }

        // Draw circles over the starting and finishing positions.
        final int circleRadius = 10;
        final Color startColor = pathColorGradient[0];
        final Color finishColor = pathColorGradient[pathColorGradient.length-1];

        // Circle over the start.
        final Coordinate startCoordinate = this.path.get(0);
        this.drawCircle(startCoordinate, circleRadius, startColor, finishColor, gfx);

        // Circle over the finish.
        final Coordinate finishCoordinate = this.path.get(this.path.size()-1);
        this.drawCircle(finishCoordinate, circleRadius, finishColor, startColor, gfx);

        if(ap.isDebugging()) {
            System.out.println(String.format("Start: (%d, %d) / Finish (%d, %d)", this.path.get(0).getX(), this.path.get(0).getY(), this.path.get(this.path.size()-1).getX(), this.path.get(this.path.size()-1).getY()));
        }

        // Draw surrounding rectangle from its upper-left corner (x0, y0).
        for(int x = this.getX0(); x <= this.getX1(); x++) {
            gfx.setPaint(Color.BLACK);
            gfx.fillRect(x, this.getY0(), 1, 1);
            gfx.fillRect(x, this.getY1(), 1, 1);

        }

        // All the way to its lower-right corner (x1, y1).
        for(int y = this.getY0(); y <= this.getY1(); y++) {
            gfx.fillRect(this.getX0(), y, 1, 1);
            gfx.fillRect(this.getX1(), y, 1, 1);
        }



        return bufferedImage;
    }

    private void drawCircle(final Coordinate circleCenter, final int circleRadius, final Color circleFill, final Color perimeterColor, final Graphics2D gfx) {

        final int radiusPow2 = circleRadius * circleRadius;

        for(int circleX = circleCenter.getX() - circleRadius, limX = circleCenter.getX() + circleRadius ; circleX < limX; circleX++) {
            for(int circleY = circleCenter.getY() - circleRadius, limY = circleCenter.getY() + circleRadius ; circleY < limY; circleY++) {
                final int circleAreaCheck = (circleX - circleCenter.getX()) * (circleX - circleCenter.getX()) + (circleY - circleCenter.getY()) * (circleY - circleCenter.getY());
                if(this.isValidCoordinate(circleX, circleY) && circleAreaCheck <= radiusPow2) {
                    if(circleAreaCheck < radiusPow2 - (circleRadius*6)) {
                        gfx.setPaint (circleFill);
                    }
                    else {
                        gfx.setPaint (perimeterColor);
                    }
                    gfx.fillRect ( circleX, circleY, 1, 1 );
                }
            }
        }

    }

    private final Integer velocity = 10000;

    public boolean isSolution(final Coordinate c) {
        return c.getX() == this.getTargetX() && c.getY() == this.getTargetY();
    }

    public boolean isValidCoordinate(final int x, final int y) {
        return this.getX0() <= x && x < this.getX1() && this.getY0() <= y && y < this.getY1();
    }

    public boolean isUnvisitedPassage(int x, int y) {
        return this.visitedGrid[x][y] != Solver.EXPLORED;
    }

    public boolean isVisitedPassage(int x, int y) {
        return this.visitedGrid[x][y] == Solver.EXPLORED;
    }

    public boolean isQueued(int x, int y) {
        return this.visitedGrid[x][y] == Solver.QUEUED;
    }

    public int getStartX() {
        return this.ap.getStartX();
    }

    public int getStartY() {
        return this.ap.getStartY();
    }

    public int getTargetX() {
        return this.maxX;
    }

    public int getTargetY() {
        return this.maxY;
    }

    private ArrayList<Coordinate> path = new ArrayList<>();

    public ArrayList<Coordinate> getPath() {
        return this.path;
    }

    public long currentRunTime = 0;
    /*
     * Running to the next map position.
     */

    private final int numberOfTimeMeasurements = 1000; // longest test was 13500, original is 4500
    public void run() {
        this.currentRunTime = 0;
        int minVelocityLoops = 10000;
        // Time to run into the next maze position.
        for(int k = 0; k < minVelocityLoops / this.velocity; k++) {
            for(int i = 0; i < numberOfTimeMeasurements; i++) {
                currentRunTime = System.currentTimeMillis();
            }
        }
    }

    public void setPos(int x, int y, byte cellStatus) {
        this.visitedGrid[x][y] = cellStatus;
    }
}
