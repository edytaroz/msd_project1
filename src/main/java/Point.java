import java.util.ArrayList;
import java.util.Random;



public class Point{
    // typ 4 to ogień
    // typ 2 to wyjście
    // typ 3 to ściana
    private static final int SFMAX = 100000;
    public boolean blocked = false;
    public ArrayList<Point> neighbors;
    public static Integer []types ={0,1,2,3,4};
    public int type;
    public double staticField;
    public boolean isPedestrian;
    public int x;
    public int y;
    public float smokeDensity = 0;
    private int iterationInt;
    private int age = 0;
    private static int peopleConst = 2; // 10 / (10 - 8)
    public boolean isAlive;
    private static float burningVal = 0.3f;
    private static float smokePoisoningVal = 3f;
    public int timeOfDeath;
    public double ingestedSmoke;

    private int waitToMove = 0;

    public Point(int x,int y) {
        type=0;
        staticField = SFMAX;
        neighbors= new ArrayList<Point>();
        this.x = x;
        this.y = y;
        iterationInt = 0;
        isAlive = true;
        timeOfDeath = -1;
        ingestedSmoke = 0;
    }

    public void clear() {
        staticField = SFMAX;
        blocked = false;
        smokeDensity = 0;
        isAlive = true;
        timeOfDeath = -1;
        ingestedSmoke = 0;
    }

    public boolean calcStaticField() {
        double smalestField = 100000;
        if(!(neighbors.size()== 0 || this.type ==1)){
            for(Point neighbour : neighbors){
                smalestField = (smalestField>neighbour.staticField+1? neighbour.staticField+1 : smalestField);
            }
            if(this.staticField>smalestField){
                this.staticField = smalestField;
                return true;
            }
        }
        return false;
    }

    public int move(){
        if(isPedestrian){
            checkAliveStatus();
        }
        if (isPedestrian && !blocked && isAlive && ((iterationInt % peopleConst != 0 && smokeDensity < 0.71) || (iterationInt % peopleConst == 0 && smokeDensity >= 0.71))) {
            if (checkMove() == 1) {
                ingestedSmoke += smokeDensity;
                Random random = new Random();
                Point nextP = this;
                ArrayList<Point> nextPos = new ArrayList<Point>();
                double theSmallest = this.staticField;
                for (Point neigh : neighbors) {
                    if (neigh.staticField < theSmallest && !neigh.isPedestrian && neigh.type != 4 && neigh.type != 1 && neigh.isAlive)
                        theSmallest = neigh.staticField;
                }
                for (Point neigh : neighbors) {
                    if (neigh.staticField == theSmallest && !neigh.isPedestrian && neigh.type != 4 && neigh.type != 1 && neigh.isAlive)
                        nextPos.add(neigh);
                }
                if (!nextPos.isEmpty()) {
                    int id = random.nextInt(nextPos.size());
                    nextP = nextPos.get(id);
                    if (!nextP.isPedestrian) {
                        if (nextP.type != 2) {
                            nextP.isPedestrian = true;
                            nextP.setAge(this.getAge());
                            nextP.blocked = true;
                            nextP.ingestedSmoke = ingestedSmoke;
                        }
                        this.isPedestrian = false;
                        this.ingestedSmoke = 0;
                        this.age = 0;
                        this.waitToMove = 0;
                    }
                } else if (nextP == this) { //trying random choose
                    for (Point neigh : neighbors) {
                        int tmp = random.nextInt(100);
                        if (!neigh.isPedestrian && neigh.type != 1 && neigh.type != 4 && tmp < 10 * waitToMove)
                            nextP = neigh;
                    }
                    if (!nextP.isPedestrian) {
                        if (nextP.type != 2) {
                            nextP.isPedestrian = true;
                            nextP.setAge(this.getAge());
                            nextP.blocked = true;
                            nextP.ingestedSmoke = ingestedSmoke;
                        }
                        this.isPedestrian = false;
                        this.ingestedSmoke = 0;
                        this.age = 0;
                        this.waitToMove = 0;
                    }
                }
            }
        }
        iterationInt++;
        return 0;

    }

    public void addNeighbor(Point nei) {
        neighbors.add(nei);
    }

    public void makeClean(){
        this.smokeDensity = 0;
        for(Point p: neighbors){
            p.smokeDensity = 0;

        }
    }

    private void checkAliveStatus(){
        if(isAlive && (smokeDensity >= burningVal || ingestedSmoke >= smokePoisoningVal)){
            isAlive = false;
            timeOfDeath = iterationInt;
        }
    }

    public int checkMove(){
        if( this.isPedestrian) {
            if (isAlive && (age >= 4 && age <= 18)) {
                if (waitToMove <= 1) {
                    waitToMove = 2;
                } else {
                    waitToMove--;
                }
            } else if (isAlive && (age >= 19 && age <= 35)) {
                if (waitToMove <= 1) {
                    waitToMove = 1;
                } else {
                    waitToMove--;
                }
            } else if (isAlive && (age >= 36 && age <= 50)) {
                if (waitToMove <= 1) {
                    waitToMove = 3;
                } else {
                    waitToMove--;
                }
            } else if (isAlive && (age >= 51 && age <= 70)) {
                if (waitToMove <= 1) {
                    waitToMove = 4;
                } else {
                    waitToMove--;
                }
            } else if (isAlive && (age >= 71)) {
                if (waitToMove <= 1) {
                    waitToMove = 5;
                } else {
                    waitToMove--;
                }
            }
        }else{
            waitToMove = 0;
        }
        return waitToMove;
    }
    public int undead(){
        isAlive = true;
        isPedestrian = false;
        type = 0;
        ingestedSmoke = 0;
        age = 0;
        return timeOfDeath;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }



}