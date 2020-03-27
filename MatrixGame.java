

/**
 * @author Marco Iannella
 *
 */
public class MatrixGame {

    public enum Color{ RED, GREEN, BLUE, WHITE, YELLOW, ORANGE, EMPTY, OUTOFBOUND };//Il valore OUTOFBOUND sará da implementare. Permetterá di giocare alla modalitá riproduci immagine sulla matrice intera e non su quella interna
    Color matrixGame[][], matrixModel[][];
    private final int WIDTH, HEIGHT;
    private double score;
    private int moves;
    private final String matrixToReproduce;
    int empty[]; //coordinates of the empty card. empty[0] = row, empty[1] = column

    public MatrixGame(int width, int height){
        this.score = 200;
        this.WIDTH = width;
        this.HEIGHT = height;
        matrixGame = new Color[WIDTH][HEIGHT];
        this.moves = 0;
        assert this.WIDTH > 4;
        assert this.HEIGHT > 4;
        matrixModel = new Color[WIDTH - 2][HEIGHT - 2];
        this.empty = new int[2];

        do{
            matrixModel = generateMatrix(WIDTH, HEIGHT, matrixModel,false);
        }while(!quantityOfColorModelMatrix());

        matrixGame = generateMatrix(WIDTH, HEIGHT, matrixGame,true);
        matrixToReproduce = matrixModelToString();
    }

    public MatrixGame(JSONObject state) throws JSONException {
        JSONObject jo = state;
        JSONArray ja = new JSONArray(jo.get("emptyPos").toString());
        this.empty = new int[2];
        this.empty[0] = ja.getInt(0);
        this.empty[1] = ja.getInt(1);
        this.WIDTH = jo.getInt("width");
        this.HEIGHT = jo.getInt("height");
        this.moves = jo.getInt("moves");
        this.matrixToReproduce = jo.getString("matrixReproduce");
        restoreMatrixFromString(jo.getString("matrixGame"), true);
        restoreMatrixFromString(jo.getString("matrixReproduce"), false);
    }

    /**
     * Method for the composition of the Game Matrix
     * @return String containing all the elements of the array
     **/
    public String matrixGameToString(){
        int i,j;
        String array = "{";
        for(i = 0; i < this.HEIGHT; i++){
            for(j = 0; j < this.WIDTH; j++){
                array += this.matrixGame[i][j];
                if((i + 1) * (j + 1) != (WIDTH * HEIGHT)){
                    array += ",";
                }
            }
        }
        return array + "}";
    }

    /**
     * This method find out the subMatrix to analyze in the matrix game
     * @return Resultant String of the subMatrix
     */
    private String matrixGameSubMatrixToString(){
        int i,j;
        String array = "{";
        for(i = 1; i < this.HEIGHT -1; i++){
            for(j = 1; j < this.WIDTH-1; j++){
                array += this.matrixGame[i][j];
                if( ((i) * (j)) != ((WIDTH-2) * (HEIGHT-2)) ){
                    array += ",";
                }
            }
        }
        return array+"}";
    }

    /**
     * Method for the composition of the Model Matrix
     * @return String containing all the elements of the array
     **/
    private String matrixModelToString(){
        int i,j;
        String array = "{";
        for(i = 0; i < this.HEIGHT-2; i++){
            for(j =0; j < this.WIDTH-2; j++){
                array += this.matrixModel[i][j];
                if((i + 1) * (j + 1) != (WIDTH - 2) * (HEIGHT - 2)){
                    array += ",";
                }
            }
        }
        return array+"}";
    }

    /**
     * Method that generates a Matrix of colors
     * @param width is the width of the matrix
     * @param height is the height of the matrix
     * @param matrix is the matrix that we want to populate
     * @param isGame verify if the matrix is the model or game one
     * @return the matrix populated
     **/
    private Color[][] generateMatrix(int width, int height, Color[][] matrix, boolean isGame){
        int i,j,casual, randomEmptyX, randomEmptyY;
        Random r = new Random();
        int hash=0;
        int quantityOfCards;
        HashMap<String,Integer> cards = new HashMap<String,Integer>();
        quantityOfCards = 4;
        cards.put("RED", quantityOfCards);
        cards.put("ORANGE", quantityOfCards);
        cards.put("BLUE", quantityOfCards);
        cards.put("WHITE", quantityOfCards);
        cards.put("GREEN", quantityOfCards);
        cards.put("YELLOW", quantityOfCards);
        if(!isGame){
            width -= 2;
            height -= 2;
        }else{
            cards.put("EMPTY", 1);
        }

        randomEmptyX = r.nextInt(width);
        randomEmptyY = r.nextInt(height);
        for(i = 0; i < width; i++){
            for(j = 0; j < height; j++){
                if(i == randomEmptyX && j == randomEmptyY && isGame){
                    matrix[i][j] = Color.EMPTY;
                    cards.put("EMPTY", 0);
                    this.empty[0] = randomEmptyX;
                    this.empty[1] = randomEmptyY;
                }else{
                    do{
                        casual = r.nextInt(6);
                    }while(cards.get(Color.values()[casual].toString()) == 0);
                    matrix[i][j] = Color.values()[casual];
                    hash = cards.get(Color.values()[casual].toString());
                    cards.put(Color.values()[casual].toString(), --hash);
                }
            }

        }
        return matrix;
    }

    /**
     * Get the matrix to reproduce
     * @return String of the matrix that should be reproduced
     *
     **/
    public String getMatrixToReproduce(){
        return matrixToReproduce;
    }

    /**
     * Method that verify if the submatrix of the matrix game is equals to the given matrix
     * @return true if it is verified (The player win), else return false
     *
     */
    public boolean winningMove(){
        return matrixGameSubMatrixToString().equals(matrixToReproduce);
    }

    /**
     * This Method verify if in the model matrix there are more than 3 cards of the same color
     * @return true if there are less than 4 cards on the matrix else false
     */
    private boolean quantityOfColorModelMatrix(){
        HashMap<String,Integer> cards = new HashMap<String,Integer>();
        int i,j, value;

        for(i = 0; i < this.WIDTH - 2; i++){
            for(j = 0; j < this.HEIGHT -2 ; j++){
                if(cards.containsKey(matrixModel[i][j].toString())){
                    value = cards.get(matrixModel[i][j].toString());
                    cards.put(matrixModel[i][j].toString(), ++value);
                }else{
                    cards.put(matrixModel[i][j].toString(), 1);
                }
            }
        }

        for(Map.Entry<String,Integer> entry : cards.entrySet())
            if(entry.getValue() == 4)
                return false;
        return true;
    }

    /**
     * Method which verify if a move is possible
     * @param row is a reference at the X point
     * @param column is a reference at the Y point
     * @return true if card can be moved false if not
     */
    private boolean isMovableCard(int row, int column){
        int i;
        if(this.matrixGame[row][column] == Color.EMPTY || this.matrixGame[row][column] == Color.OUTOFBOUND)
            return false;
        else{
            for(i = 0; i < this.matrixGame.length; i++){
                if(this.matrixGame[row][i] == Color.EMPTY){
                    return true;
                }
            }
            for(i = 0; i < this.matrixGame.length; i++){
                if(this.matrixGame[i][column] == Color.EMPTY){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Method that moves cards
     * @param row is the row touched by the user
     * @param column id the column touched by the user
     * @return true if something is moved, else false
     */
    private boolean movingCard(int row, int column){

        int i;
        if(isMovableCard(row, column) && winningMove() == false){
            //if touched card can be moved
            if(this.empty[0] == row){
                //if the row of touched card = row  of empty card
                if(column - this.empty[1] > 0){
                    //
                    for(i = this.empty[1]; i < column; i++)
                        this.matrixGame[row][i] = this.matrixGame[row][i+1];
                    this.matrixGame[row][column] = Color.EMPTY;
                    this.empty[1] = column;
                    this.moves++;

                    if(this.moves <= 15){
                        this.score -= 0.25;
                    }else if(this.moves > 15 && this.moves <= 25){
                        this.score -= 0.30;
                    }else if(this.moves > 25 && this.moves <= 60) {
                        this.score -= 0.40;
                    }else if(this.moves > 60 && this.score > 1)
                    this.score -= 0.50;

                    return true;

                }else if(column - this.empty[1] < 0){
                    for(i = this.empty[1]; i > column; i--)
                        this.matrixGame[row][i] = this.matrixGame[row][i-1];
                    this.matrixGame[row][column] = Color.EMPTY;
                    this.empty[1] = column;
                    this.moves++;

                    if(this.moves <= 15){
                        this.score -= 0.25;
                    }else if(this.moves > 15 && this.moves <= 25){
                        this.score -= 0.30;
                    }else if(this.moves > 25 && this.moves <= 60) {
                        this.score -= 0.40;
                    }else if(this.moves > 60 && this.score > 1)
                        this.score -= 0.50;

                    return true;
                }

            }else if(this.empty[1] == column){
                //if the column of touched card = column of empty card
                if(row - this.empty[0] > 0){
                    for(i = this.empty[0]; i < row; i++)
                        this.matrixGame[i][column] = this.matrixGame[i+1][column];
                    this.matrixGame[row][column] = Color.EMPTY;
                    this.empty[0] = row;
                    this.moves++;

                    if(this.moves <= 15){
                        this.score -= 0.25;
                    }else if(this.moves > 15 && this.moves <= 25){
                        this.score -= 0.30;
                    }else if(this.moves > 25 && this.moves <= 60) {
                        this.score -= 0.40;
                    }else if(this.moves > 60 && this.score > 1)
                        this.score -= 0.50;

                    return true;

                }else if(row - this.empty[0] < 0){
                    for(i = this.empty[0]; i > row; i--)
                        this.matrixGame[i][column] = this.matrixGame[i-1][column];
                    this.matrixGame[row][column] = Color.EMPTY;
                    this.empty[0] = row;
                    this.moves++;


                    if(this.moves <= 15){
                        this.score -= 0.25;
                    }else if(this.moves > 15 && this.moves <= 25){
                        this.score -= 0.30;
                    }else if(this.moves > 25 && this.moves <= 60) {
                        this.score -= 0.40;
                    }else if(this.moves > 60 && this.score > 1)
                        this.score -= 0.50;


                    return true;
                }

            }
        }
        return false;
    }
    public boolean letsPlay(int x, int y){
        if(movingCard(x, y))
            return true;
        return false;

    }

    /**Method that counts moves during the game
     * @return number of moves of the instance
     */
    public int getMoves(){
        return this.moves;
    }
    public int getDimension(){
        return this.WIDTH;
    }


    /**
     * Method that save game state and return a stringifed version
     * @return
     * @throws JSONException
     */
    public JSONObject saveState() throws JSONException{
        JSONObject jo = new JSONObject();
        JSONArray ja = new JSONArray();
        ja.put(this.empty[0]);
        ja.put(this.empty[1]);
        jo.put("matrixGame", this.matrixGameToString());
        jo.put("matrixReproduce", this.matrixToReproduce);
        jo.put("width", this.WIDTH);
        jo.put("height", this.HEIGHT);
        jo.put("moves", this.moves);
        jo.put("emptyPos", ja);
        return jo;
    }

    /**
     * Restore the matrix from string
     * @param matrix
     */
    private void restoreMatrixFromString(String matrix, boolean isGame){
        int i,j,k=0;
        matrix = matrix.substring(1, matrix.length()-1);
        String[] array = matrix.split(",");
        Color[][] realMatrix;
        int width, height;
        if(isGame){
            width = height = this.WIDTH;
            this.matrixGame = new Color[width][height];
            realMatrix = this.matrixGame;
        }else{
            width = height = this.WIDTH -2;
            this.matrixModel = new Color[width][height];
            realMatrix = this.matrixModel;
        }

        for(i = 0; i < width; i++){
            for(j = 0; j < height; j++){
                realMatrix[i][j] = Color.valueOf(array[k++]);
            }
        }

    }

    public double getScore(){
        return this.score;
    }
     // da fare in serata

}





