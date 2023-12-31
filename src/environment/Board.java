package environment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import game.GameElement;
import game.Goal;
import game.Obstacle;
import game.Snake;

public abstract class Board extends Observable implements Serializable {
	//constants
	public static final long PLAYER_PLAY_INTERVAL = 100;
	public static final long REMOTE_REFRESH_INTERVAL = 200;
	public static final int NUM_COLUMNS = 30;
	public static final int NUM_ROWS = 30;
	public static final int NUM_GOALS_TO_WIN = 9;
	//attributes
	protected Cell[][] cells;
	private BoardPosition goalPosition;
	protected LinkedList<Snake> snakes = new LinkedList<Snake>();
	private LinkedList<Obstacle> obstacles= new LinkedList<Obstacle>();
	protected boolean isFinished;

	public Board() {
		cells = new Cell[NUM_COLUMNS][NUM_ROWS];
		for (int x = 0; x < NUM_COLUMNS; x++) {
			for (int y = 0; y < NUM_ROWS; y++) {
				cells[x][y] = new Cell(new BoardPosition(x, y));
			}
		}
	}

	public Cell[][] getCells() {
		return cells;
	}

	public void setCells(Cell[][] cells) {
		this.cells = cells;
	}

	public BoardPosition getGoalPosition() {
		return goalPosition;
	}

	public void setGoalPosition(BoardPosition goalPosition) {
		this.goalPosition = goalPosition;
	}

	public LinkedList<Snake> getSnakes() {
		return snakes;
	}

	public void setSnakes(LinkedList<Snake> snakes) {
		this.snakes = snakes;
	}

	public LinkedList<Obstacle> getObstacles() {
		return obstacles;
	}

	public void setObstacles(LinkedList<Obstacle> obstacles) {
		this.obstacles = obstacles;
	}

	public void setFinished(boolean finished) {
		isFinished = finished;
	}

	public boolean isFinished() {
		return isFinished;
	}

	public Cell getCell(BoardPosition cellCoord) {
		return cells[cellCoord.x][cellCoord.y];
	}

	public void addSnake(Snake snake){
		this.snakes.add(snake);
	}

	public BoardPosition getRandomPosition() {
		return new BoardPosition((int) (Math.random() * NUM_ROWS), (int) (Math.random() * NUM_ROWS));
	}

	public boolean isObstacleFree(Cell cell){
		for (Obstacle obstacle : obstacles){
			if (obstacle.getBoardPosition().equals(cell.getPosition())){
				return false;
			}
		}
		return true;
	}

	public boolean isSnakeFree(Cell cell){
		for (Snake snake : snakes){
			if (snake.getPath().contains(cell.getPosition())){
				return false;
			}
		}
		return true;
	}
	
	public void addGameElement(GameElement gameElement) throws InterruptedException{
		boolean placed = false;
		while(!placed) {
			BoardPosition pos=getRandomPosition();
			if(!getCell(pos).isOcupied() && !getCell(pos).isOcupiedByGoal()) {
				boolean trySetGameElement = getCell(pos).setGameElement(gameElement);
				if (!trySetGameElement){
					addGameElement(gameElement);
				}
				if(gameElement instanceof Goal) {
					setGoalPosition(pos);
					System.out.println("Goal placed at: " + pos);
				}
				//Added by me
				if(gameElement instanceof Obstacle){
					((Obstacle) gameElement).setBoardPosition(pos);
					((Obstacle)gameElement).setCell(this.getCell(pos));
				}
				placed=true;
			}
		}
	}
	//TODO ver o comentario no setGameElement
	public void changeGoalPosition(Goal goal){
		boolean placed = false;
		while(!placed) {
			BoardPosition pos=getRandomPosition();
			if(!getCell(pos).isOcupied() && !getCell(pos).isOcupiedByGoal()) {
				try{
					boolean tryGameElement = getCell(pos).setGameElement(goal);
					if(!tryGameElement){
						changeGoalPosition(goal);
					}
					setGoalPosition(pos);
				}catch (InterruptedException e){
					e.printStackTrace();
				}
				System.out.println("Goal changed to: " + pos);
				placed = true;
			}
		}
	}

	public Snake getSnakeAt(BoardPosition pos){
		for (Snake snake : snakes){
			if (snake.getSnakeHead().equals(pos)){
				return snake;
			}
		}
		return null;
	}
	//temp
	public synchronized List<BoardPosition> getNeighboringPositions(Cell cell) {
		ArrayList<BoardPosition> possibleCells=new ArrayList<BoardPosition>();
		BoardPosition pos=cell.getPosition();
		if(pos.x>0)
			possibleCells.add(pos.getCellLeft());
		if(pos.x<NUM_COLUMNS-1)
			possibleCells.add(pos.getCellRight());
		if(pos.y>0)
			possibleCells.add(pos.getCellAbove());
		if(pos.y<NUM_ROWS-1)
			possibleCells.add(pos.getCellBelow());
		return possibleCells;

	}
	public boolean isWithinBounds(BoardPosition position){
		return position.x >= 0 && position.y >= 0 && position.x < Board.NUM_ROWS && position.y < Board.NUM_COLUMNS;
	}
	protected Goal addGoal() {
		Goal goal=new Goal(this);
		try{
			addGameElement( goal);
		}catch (InterruptedException e){
			e.printStackTrace();
		}
		return goal;
	}

	protected void addObstacles(int numberObstacles) {
		// clear obstacle list , necessary when resetting obstacles.
		getObstacles().clear();
		while(numberObstacles>0) {
			Obstacle obs=new Obstacle(this);
			try{
				addGameElement( obs);
			}catch (InterruptedException e){
				e.printStackTrace();
			}
			getObstacles().add(obs);
			numberObstacles--;
		}
	}

	@Override
	public void setChanged() {
		super.setChanged();
		notifyObservers();
	}

	public abstract void init(); 
	
	public abstract void handleKeyPress(int keyCode);

	public abstract void handleKeyRelease();

}