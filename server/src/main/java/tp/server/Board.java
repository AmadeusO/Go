package tp.server;

import java.util.LinkedList;

public class Board {

    int[][] colorBoard;
    int[][] groupBoard;

    private LinkedList<StoneGroup> groups;
    public static Board instance = new Board();

    private Board() {
        colorBoard = new int[19][19];
        groupBoard = new int[19][19];
        groups = new LinkedList<>();
        redrawGroupBoard();
    }

    public static Board getInstance() {
        return instance;
    }

    public LinkedList<StoneGroup> getGroups() {
        return groups;
    }

    /**
     * Checks if the move violates the rules
     * @param x stone vertical position in board
     * @param y stone horizontal postion in board
     * @param color stone color
     * @return true if the move is okay
     */
    public boolean verifyMove(int x, int y, Color color) {

        if ( isPositionFree(x ,y )){
            if( haveLiberties(x, y)){
                return true;
            }
            else{
                if(willJoin(x,y, color)){
                    return true;
                }

                if(willKill(x, y, color)){
                    if( !isKo(x, y, color)){
                        return true;
                    }

                }

            }
        }
        return false;

    }

    boolean isPositionFree(int x, int y){
        if (colorBoard[x][y] != -1) return false;
        return true;
    }



    boolean haveLiberties(int x, int y){
            StoneGroup stoneGroup;
        //top
        if(x-1>=0){
            if(colorBoard[x-1][y]==-1){
                return true;
            }
        }
        //left
        if(y-1 >= 0){
            if(colorBoard[x][y-1]==-1){
                return true;
            }
        }

        //bottom
        if(x+1<19){
            if(colorBoard[x+1][y]==-1){
                return true;
            }
        }

        //right
        if(y+1<19){
            if(colorBoard[x][y+1]==-1){
                return true;
            }
        }

        return  false;
    }


    boolean willJoin(int x , int y, Color color){
        StoneGroup stoneGroup;
        //top
        if(x-1>=0){
            stoneGroup=getGroupById(getGroupIdAt(x-1,y));
            if( stoneGroup.getColor()==color && stoneGroup.getLiberties()-1 != 0){
                return true;
            }
        }
        //left
        if(y-1 >= 0){
            stoneGroup=getGroupById(getGroupIdAt(x,y-1));
            if( stoneGroup.getColor()==color &&  stoneGroup.getLiberties()-1 != 0){
                return true;
            }
        }

        //bottom
        if(x+1<19){
            stoneGroup=getGroupById(getGroupIdAt(x+1,y));
            if( stoneGroup.getColor()==color && stoneGroup.getLiberties()-1 != 0){
                return true;
            }
        }

        //right
        if(y+1<19){
            stoneGroup=getGroupById(getGroupIdAt(x,y+1));
            if( stoneGroup.getColor()==color && stoneGroup.getLiberties()-1 != 0){
                return true;
            }
        }

        return  false;
    }


    boolean willKill(int x , int y, Color color){
        StoneGroup stoneGroup;
        //top
        if(x-1>=0){
            stoneGroup=getGroupById(getGroupIdAt(x-1,y));
            if( stoneGroup.getColor()!=color && stoneGroup.getLiberties()-1 == 0){
                return true;
            }
        }
        //left
        if(y-1 >= 0){
            stoneGroup=getGroupById(getGroupIdAt(x,y-1));
            if( stoneGroup.getColor()!=color &&  stoneGroup.getLiberties()-1 == 0){
                return true;
            }
        }

        //bottom
        if(x+1<19){
            stoneGroup=getGroupById(getGroupIdAt(x+1,y));
            if( stoneGroup.getColor()!=color && stoneGroup.getLiberties()-1 == 0){
                return true;
            }
        }

        //right
        if(y+1<19){
            stoneGroup=getGroupById(getGroupIdAt(x,y+1));
            if( stoneGroup.getColor()!=color && stoneGroup.getLiberties()-1 == 0){
                return true;
            }
        }

        return  false;
    }

    public boolean isKo(int x, int y, Color c){
        //to do: make the impossible
        return false;
    }

    public StoneGroup getGroupById( int id){
        for ( StoneGroup stoneGroup : groups){
            if(stoneGroup.getId()== id){
                return stoneGroup;
            }
        }
        return null;
    }


    public boolean move(Color color, int x, int y) {
        //todo: make this for real
        Stone newStone = new Stone(color, x, y);
        if (verifyMove(newStone.getX(), newStone.getY(), newStone.getColor())) {
            addStone(newStone);
            return true;
        }
        else return false;
    }

    /**
     * Includes the new stone in colorBoard and groupBoard
     * @param newStone the stone to be included
     */
    private void addStone(Stone newStone) {
        colorBoard[newStone.getX()][newStone.getY()] = newStone.getColorValue();
        updateGroups(newStone);
    }


    /**
     * Merges groups after a new stone has been added
     * @param newStone the new stone
     */
    private void updateGroups(Stone newStone) {
        StoneGroup newGroup = new StoneGroup(newStone);
        int stoneX = newStone.getX();
        int stoneY = newStone.getY();
        int[] x = {stoneX+1, stoneX, stoneX-1, stoneX};
        int[] y = {stoneY, stoneY+1, stoneY, stoneY-1};
        StoneGroup neighbor;
        for (int i=0; i<4; i++) {
            if (x[i] >= 0 && x[i] <= 18 && y[i] >= 0 && y[i] <= 18) {
                if (colorBoard[x[i]][y[i]] == newStone.getColorValue()) {
                    neighbor = groups.get(groupBoard[x[i]][y[i]]);
                    newGroup.addStones(neighbor);
                    groups.remove(neighbor);
                    redrawGroupBoard();
                }
            }
        }
        groups.add(newGroup);
        redrawGroupBoard();
    }

    /**
     * Updates the groupBoard to portray the groups that currently exist
     */
    private void redrawGroupBoard() {
        for (int i = 0; i < 19; i++) {
            for (int j = 0; j < 19; j++) {
                groupBoard[i][j] = -1;
            }
        }

        for (StoneGroup group: groups) {
            int id = groups.indexOf(group);
            for (Stone stone: group.getStones()) {
                groupBoard[stone.getX()][stone.getY()] = id;
            }
        }
    }

    public Color getColorAt(int x, int y) {
        return Color.getColorByValue(colorBoard[x][y]);
    }

    public int getGroupIdAt(int x, int y) {
        return groupBoard[x][y];
    }

    public void reset() {
        instance = new Board();
    }

    /**
     *
     * @param stoneGroup
     * @return border
     *
     */
    public LinkedList<Point> getGroupBorder(StoneGroup stoneGroup){

        int x, y;
        LinkedList<Point> border= new LinkedList<Point>();

        for( Stone stone : stoneGroup.getStones()){

          x=stone.getX();
          y=stone.getY();

          //top
            if(x-1 >= 0){
                if( colorBoard[x-1][y] == -1){
                    border.add(new Point(x-1, y));
                }
            }
            //left
            if(y-1 >= 0){
                if( colorBoard[x][y-1] == -1){
                    border.add(new Point(x, y-1));
                }
            }

            //bottom
            if(x+1<19){
                if( colorBoard[x+1][y] == -1){
                    border.add(new Point(x+1, y));
                }
            }

            //right
            if(y+1<19){
                if( colorBoard[x][y+1] == -1){
                    border.add(new Point(x, y+1));
                }
            }


        }

        return border;
    }
}
