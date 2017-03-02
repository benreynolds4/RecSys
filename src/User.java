import java.util.*;

public class User {
	private int user_id;
	private int rating_score;
	private int item_id;
	private Double meanRating;
	private int medianRating;
	private Double standardDeviationRating;
	private int minRating;
	private int maxRating;
	private HashMap<Integer, Integer> ratingsItemIDToScore = new HashMap(); // Item ID - > Rating Val
	private HashMap<Integer, Rating> ratingsItemIDToRating = new HashMap(); // Item ID - > Rating Obj
	private ArrayList<Integer> items = new ArrayList<>();
	private HashMap<Double, User> neighbours = new HashMap(); // Store Sim Value to users
	private HashMap<Integer, Double> neighboursInverse = new HashMap(); // Store User ID -> Sim
    private HashMap<Integer, Double> resnicksPrediction = new HashMap(); // Store itemId - > Prediction


	// Initialise a user when first building the Datastructures from input file
	public User(int user_id, int rating_score, int item_id, Rating rating) {
		this.user_id = user_id;
		this.rating_score = rating_score;
		this.item_id = item_id;
		ratingsItemIDToScore.put(item_id, rating_score);
		ratingsItemIDToRating.put(item_id, rating);
		items.add(item_id);
	}

	// Adds a rating to an item id that the user has rated.
	public void add_rating(int rating_score, int item_id, Rating rating) {
		ratingsItemIDToScore.put(item_id, rating_score);
		ratingsItemIDToRating.put(item_id, rating);
		items.add(item_id);
	}

	// Returns all the users neighbours.
	public HashMap<Double, User> getNeighbours() {
		return this.neighbours;
	}

	// Returns the x closest neighbours to the user.
	public ArrayList<User> getNeighbourhood(int size) {

        ArrayList<User> neighorbourhood = new ArrayList<>();
        ArrayList<Double> list = new ArrayList(neighbours.keySet());
        Collections.sort(list);
        Collections.reverse(list);
        for (int i = 0; i < neighbours.size(); i++) {
            if (i > size) {
                break;
            }
            if (!Double.isNaN(list.get(i))) {
                neighorbourhood.add(neighbours.get(list.get(i)));
            }
        }
        return neighorbourhood;
    }

	public void addResnicksPrediction(int itemID, Double prediction) {
	    resnicksPrediction.put(itemID, prediction);
    }

    public Double getPredictionForItem(int itemID) {
		return resnicksPrediction.get(itemID);
    }

	// Adds a neighbour to the user.
	public void add_neighbour(Double score, User user) {
		neighboursInverse.put(user.getID(), score);
		neighbours.put(score, user);
	}

	// Returns the similarity score for a given user in the users neighbours.
	public Double getSimularityScore(int userID) {
		return neighboursInverse.get(userID);
	}

	// checks if the user has a rating for given item
	public boolean hasRating(int itemID) {
		if(this.ratingsItemIDToScore.containsKey(itemID)) {
			return true;
		}
		return false;
	}

	// Returns the map of ratings td -> score
	public HashMap<Integer, Integer> getRatingsIDToScore() {
		return this.ratingsItemIDToScore;
	}

	// Returns the list of items the user has rated
	public ArrayList<Integer> getItems() {
		return this.items;
	}

	// Returns the users id.
	public int getID() {
		return this.user_id;
	}

	// This method returns a rating value for a given item
	public int getValue(int item_id) {
		return ratingsItemIDToScore.get(item_id);
	}
	
	// Mean, median, standard deviation, max, min ratings per user.
	
	public void calculateStatistics() {
		 Iterator<?> it = ratingsItemIDToScore.entrySet().iterator();
		 int max = 0, total = 0, median = 0, min = 5;
		 int size = ratingsItemIDToScore.size();
		 Double mean = 0.0, standardDeviation = 0.0;
		 ArrayList<Integer> listOfRatings = new ArrayList<>();
		 while (it.hasNext()) {
		        HashMap.Entry rating = (HashMap.Entry)it.next();
		        int rating_val = (int) rating.getValue();
		        if(rating_val < min) {
		        	min = (int) rating.getValue();
		        }
		        if(rating_val > max) {
		        	max = rating_val;
		        }		
		        total += rating_val;
		        listOfRatings.add(rating_val);
		 }
		 Collections.sort(listOfRatings);
		 median = listOfRatings.get(size/2);
		 mean =  (double) (total / (double) size);
		 Double variance = getVariance(mean, size);
		 standardDeviation = getStdDev(variance);
		
		 this.maxRating = max;
		 this.minRating = min;
		 this.standardDeviationRating = standardDeviation;
		 this.meanRating = mean; 
		 this.medianRating = median;
	}
	
	public double getMean() {
		return this.meanRating;
	}
	
	public double getMedian() {
		return this.medianRating;
	}
	
	public double getStandardDeviation() {
		return this.standardDeviationRating;
	}
	
	public int getMax() {
		return this.maxRating;
	}
	
	public int getMin() {
		return this.minRating;
	}

	private double getVariance(Double mean, int size) {
        double temp = 0;
        for(int a : ratingsItemIDToScore.values())
            temp += (a-mean)*(a-mean);
        return temp/size;
    }
	
	private double getStdDev(Double variance)  {
        return Math.sqrt(variance);
    }
	

}
