package Solver;

public class Pair {
    char prevMove = 'O';
    int x;
    int y = -1;

    public Pair(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Pair(Pair copy) {
        this.x = copy.x;
        this.y = copy.y;
    }

    public void Up() {
        ++this.y;
    }

    public void Right() {
        ++this.x;
    }

    public void Down() {
        --this.y;
    }

    public void Left() {
        --this.x;
    }

    public void move(char move) {
        this.prevMove = move;
        switch(move) {
            case 'D':
                this.Down();
                break;
            case 'L':
                this.Left();
                break;
            case 'R':
                this.Right();
                break;
            case 'U':
                this.Up();
        }

    }
}
