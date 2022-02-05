package Solver;

import java.sql.Array;
import java.util.ArrayList;
import java.util.TreeMap;

public class Solver {

    /*
    The basic idea with this program is to feed in a grid which is then processed,
    the process involves going over every tile on the grid and checking whether it has 2 adjacent tiles of the same type
    there is also some logic to make the program consider two adjacent colours as the center, for instance:
    if there was an RYR then clearly upon tile Y both Rs are visible and the program can proceed.
    However if there was an RYYR then upon either Y tile the program would not be able to see the two Rs, in this case
    the double tile is recognised and then treated as the center, this way both Rs can be detected and this becomes
     a valid start point.
     From the start point there are two simultaneous "heads of the snake" that compare adjacent tiles and then select
     appropriate moves from the adjacent tiles. One of these snakes must invert its move set, so as to have a final
     output that can be followed organically by the user

     Each path is evaluated to see if it is one of the better paths,
     then all paths of the top 3 point brackets are returned to the user.
     */


    /*
    NOTES:

     */

    static ArrayList<ArrayList<Character>> grid = new ArrayList();

    static String inputString = "";
    static ArrayList<Pair> list1 = new ArrayList<>();
    static Pair centerPair = null;
    static ArrayList<Pair> list2 = new ArrayList<>();

    static ArrayList<ArrayList<Pair>> goodPaths = new ArrayList<>();


    public static TreeMap<Integer, ArrayList<String>> run(String input){
        inputString = input;
        buildGrid(inputString);

        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 4; col++) {
                if (checkTileHasPath(col, row)) {
                    //do single start
                    getPathFromTile(col, row);
                }
                int similarTilesCount = countTileDoubleStart(col, row);
                if (similarTilesCount > 0) {
                    char[] adjTiles = getAdjacentTiles(col, row, 'O');
                    char target = grid.get(col).get(row);

                    for (int i = 0; i < adjTiles.length; i++) {
                        if (target == adjTiles[i] && target != 'O') {
                            //do double start
                            Pair tempPair = new Pair(col, row);
                            tempPair.move(getMoveFromIndex(i));
                            getPathFromTile(col, row, tempPair.x, tempPair.y);
                        }
                    }
                }
            }
        }
        TreeMap<Integer, ArrayList<String>> sortedPaths =sortPaths(goodPaths);
        return sortedPaths;
    }

    public static void reset(){

        buildGrid(inputString);
        list1.clear();
        list2.clear();
        centerPair = null;
    }

    public static void getPathFromTile(int x, int y){
        char[] tiles = getAdjacentTiles(x, y, 'O');
        int idx1 = -1, idx2 = -1;
        boolean found = false;
        for (int i = 0; i < 4; i++) {
            if (found){break;}
            for (int j = 0; j < 4; j++) {
                if(i!=j && tiles[i] == tiles[j] && tiles[i] != 'O'){
                    idx1 = i;
                    idx2 = j;
                    found=true;
                    break;
                }
            }
        }

        centerPair = new Pair(x, y);
        Pair newPoint1 = new Pair(centerPair);
        Pair newPoint2 = new Pair(centerPair);

        newPoint1.move(getMoveFromIndex(idx1));
        newPoint2.move(getMoveFromIndex(idx2));

        list1.add(newPoint1);
        list2.add(newPoint2);

        searchPath(newPoint1, newPoint2);

        goodPaths.add(sumListsToPath());
        reset();
    }



    public static void getPathFromTile(int x1, int y1, int x2, int y2){
        char move = getMoveFromPoints(x1, y1, x2, y2, false);
        char invMove = invertMove(move);

        if(x1 ==-1 || x2 == -1 || y1 == -1 || y2 == -1){
            System.out.print("flag");
        }
        Pair point1 = new Pair(x1, y1);
        point1.prevMove = invMove;
        setTileOpen(point1);
        list1.add(point1);

        Pair point2 = new Pair(x1, y1);
        point2.move(move);
        setTileOpen(point2);
        list1.add(point2);

        searchPath(point1, point2);
        goodPaths.add(sumListsToPath());
        reset();
    }

    public static ArrayList<Pair> sumListsToPath(){
        ArrayList<Pair> out = new ArrayList<>();
        for (int i = list1.size()-1; i >= 0; i--) {
            Pair tempPair = list1.get(i);
            tempPair.prevMove = invertMove(tempPair.prevMove);
            out.add(tempPair);
        }
        if(centerPair !=null){
            out.add(centerPair);
        }

        char prevChar = out.get(0).prevMove;
        out.get(0).prevMove = 'S';
        for (int i = 1; i < list1.size() + (centerPair == null ? 0 : 1); i++) {
            char tempChar = out.get(i).prevMove;
            out.get(i).prevMove = prevChar;
            prevChar = tempChar;
        }
        for (int i = 0; i < list2.size(); i++) {
            out.add(list2.get(i));
        }



        return out;
    }

    public static TreeMap<Integer, ArrayList<String>> sortPaths(ArrayList<ArrayList<Pair>> goodPaths){
        TreeMap<Integer, ArrayList<String>> out =  new TreeMap<Integer, ArrayList<String>>();


        for (int i = 0; i < goodPaths.size(); i++) {
            if(goodPaths.get(i)== null){
                continue;
            }
            if(goodPaths.get(i).size()==0){
                continue;
            }
            StringBuilder pathOut = new StringBuilder();
            char lastColour = 'O';
            int oldX = 0;
            int oldY = 0;


            int blockCount = 0;
            int tileCount = 0;
            pathOut.append("Start: "+ goodPaths.get(i).get(0).x + ", " + goodPaths.get(i).get(0).y +" which is: "+grid.get(goodPaths.get(i).get(0).x).get(goodPaths.get(i).get(0).y) +" \nRoute: " );

            for (int j = 0; j < goodPaths.get(i).size(); j++) {
                int newX = goodPaths.get(i).get(j).x;
                int newY = goodPaths.get(i).get(j).y;
                char currColour = grid.get(newX).get(newY);
                if(currColour =='O'){
                    break;
                }

                if(newX == 3 && newY == 4){
                    //System.out.print("flag");
                }
                boolean start = tileCount==0?true:false;
                char move = getMoveFromPoints(oldX,oldY,newX,newY, start);
                if(move == 'O'){
                    continue;
                }
                else{
                    tileCount++;

                }
                pathOut.append(move + " to colour: " + currColour+ ", ");

                if(currColour!= lastColour){
                    blockCount++;
                }
                lastColour = currColour;
                oldX = newX;
                oldY = newY;
            }
            int points = blockCount*tileCount;
            ArrayList<String> paths;
            pathOut.append("\n");


            if(out.containsKey(points)){
                paths = out.get(points);
                if(paths.contains(pathOut.toString())){
                    continue;
                }
            }
            else{
                paths = new ArrayList<>();
            }
            paths.add(pathOut.toString());
            out.put(points, paths);
        }
        return out;
    }

    public static void searchPath(Pair p1, Pair p2){
        char[] adj1 = getAdjacentTiles(p1);
        char[] adj2 = getAdjacentTiles(p2);

        ArrayList<Pair> pairs = commonTile(adj1, p1, adj2, p2);
        while (pairs!=null){
            Pair newPoint1 = pairs.get(0);
            list1.add(newPoint1);
            setTileOpen(newPoint1);

            Pair newPoint2 = pairs.get(1);
            list2.add(newPoint2);
            setTileOpen(newPoint2);

            adj1 = getAdjacentTiles(newPoint1);
            adj2 = getAdjacentTiles(newPoint2);

            pairs = commonTile(adj1, newPoint1, adj2, newPoint2);
        }
    }

    public static ArrayList<Pair> commonTile(char[] moves1, Pair point1, char[] moves2, Pair point2){
        ArrayList<Pair> out = new ArrayList<>();

        for (int i = 0; i < moves1.length; i++) {
            for (int j = 0; j < moves2.length; j++) {
                if(moves1[i] == moves2[j] && moves1[i]!= 'O') {
                    Pair t1 = new Pair(point1);
                    Pair t2 = new Pair(point2);

                    switch(i){
                        case 0:
                            t1.move('U');
                            break;
                        case 1:
                            t1.move('R');
                            break;
                        case 2:
                            t1.move('D');
                            break;
                        case 3:
                            t1.move('L');
                            break;
                    }
                    switch (j){
                        case 0:
                            t2.move('U');
                            break;
                        case 1:
                            t2.move('R');
                            break;
                        case 2:
                            t2.move('D');
                            break;
                        case 3:
                            t2.move('L');
                            break;
                    }

                    if(t1.x == t2.x && t1.y == t2.y){
                        continue;
                    }
                    out.add(t1);
                    out.add(t2);
                    return out;
                }
            }
        }
        return null;
    }

    public static void setTileOpen(Pair p){
        setTileOpen(p.x, p.y);
    }
    public static void setTileOpen(int x, int y){
        grid.get(x).set(y, 'O');
    }

    public static int countTileDoubleStart(int x, int y){
        int count = 0;
        char[] adjTiles = getAdjacentTiles(x, y, 'O');
        for (int i = 0; i < adjTiles.length; i++) {
            if(grid.get(x).get(y) == adjTiles[i]){
                count++;
            }
        }
        return count;
    }

    public static boolean checkTileHasPath(int x, int y){
        //we use O to notate no previous move, as it is the start tile.
        char[] adjTiles = getAdjacentTiles(x, y, 'O');
        //If there are duplicates in this set then it is a valid start tile for a path,
        //regardless of if that path is 3 tiles or 14 tiles, it is still valid.
        return checkDuplicate(adjTiles);
    }

    public static boolean checkDuplicate(char[] arr){
        for (int i = 0; i < arr.length; i++) {
            for (int j = i+1; j < arr.length ; j++) {
                if(i!=j && arr[i]==arr[j] && arr[i] != 'O'){
                    return true;
                }
            }
        }
        return false;
    }

    //Feed string from bottom left corner of the board continuing right til the end,
    //then restart at the next row on the left continue rightwards then upwards until complete e.g.
    //5678
    //1234
    public static void buildGrid(String input){
        grid.clear();
        for (int i = 0; i < 4; i++) {
            grid.add(new ArrayList<Character>());
        }
        for (int i = 0; i < input.length(); i++) {
            grid.get(i%4).add(input.charAt(i));
        }
    }

    public static char getMoveFromIndex(int index){
        switch (index){
            case 0:
                return 'U';
            case 1:
                return 'R';
            case 2:
                return 'D';
            case 3:
                return 'L';
            default:
                return 'N';
        }
    }

    public static char[] getAdjacentTiles(Pair point){
        return getAdjacentTiles(point.x, point.y, point.prevMove);
    }

    public static char[] getAdjacentTiles(int x, int y, char previousMove){
        char U_Tile = y+1 < 10 ? grid.get(x).get(y+1) : 'O';
        char R_Tile = x+1 < 4  ? grid.get(x+1).get(y) : 'O';
        char D_Tile = y-1 >= 0 ? grid.get(x).get(y-1) : 'O';
        char L_Tile = x-1 >= 0 ? grid.get(x-1).get(y) : 'O';

        char[] out =  new char[4];

        //Since we never match open tiles ('O'), then we can replace the previous move tile with an open tile
        //This will also be done again in the board copy for this iteration as this will allow less limits in the
        //recursive method.
        out[0] = previousMove!='D' ? U_Tile: 'O';
        out[1] = previousMove!='L' ? R_Tile: 'O';
        out[2] = previousMove!='U' ? D_Tile: 'O';
        out[3] = previousMove!='R' ? L_Tile: 'O';

        return out;
    }

    public static char getMoveFromPoints(Pair p1, Pair p2){
        return getMoveFromPoints(p1.x, p1.y, p2.x, p2.y, false);
    }
    //Returns the move from point1 to point2
    public static char getMoveFromPoints(int x1, int y1, int x2, int y2, boolean startTile){
        int xDiff = x1 - x2;
        int yDiff = y1 - y2;

        if(startTile){
            return'S';
        }

        if(xDiff == -1){
            return 'R';
        }
        else if(xDiff == 1){
            return 'L';
        }
        if(yDiff == -1){
            return 'U';
        }
        else if(yDiff==1){
            return 'D';
        }
        return 'O';

    }


    public static char invertMove(char move){
        switch (move){
            case 'U':
                return 'D';
            case 'R':
                return 'L';
            case 'D':
                return 'U';
            case 'L':
                return 'R';
        }
        return 'O';
    }

}
