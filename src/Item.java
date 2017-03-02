import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

public class Item {
	private Double meanRating;
	private int medianRating;
	private Double standardDeviationRating;
	private int minRating;
	private int maxRating;
	private HashMap<Integer, Integer> ratingsUserToScore = new HashMap<>(); // user -> rating value
	private HashMap<Integer, Rating> ratingsUserToRating = new HashMap<>(); // user -> rating
	private int item_id;

	/* Initialise an Item when creating datastructures from the input file. */
	public Item(int item_id, int rating_val, int user_id, Rating rating) {
		this.item_id = item_id;
		ratingsUserToScore.put(user_id, rating_val);
		ratingsUserToRating.put(user_id, rating);
	}

	/* Adds a ratings to the Hashmaps. Store both user id to rating and rating id
	 for ease of use.
	 */
	public void add_rating( int user_id, int rating_val, Rating rating) {
		ratingsUserToScore.put(user_id, rating_val);
		ratingsUserToRating.put(user_id, rating);
	}

	public int getItem_id() {
		return this.item_id;
	}


	// Returns mean item rating in relation to all the users in the data
	public Double meanItemRating(int user_id) {
		Iterator<?> it = ratingsUserToRating.entrySet().iterator();
		int total = 0;
		int size = ratingsUserToRating.size();
		while (it.hasNext()) {
	        HashMap.Entry ratingMap = (HashMap.Entry)it.next();
	        int rating_user = (int) ratingMap.getKey();
	        if(rating_user != user_id) {
	        	 Rating rating = (Rating) ratingMap.getValue();
	        	 total += rating.getValue();
	        }
		}
		return ( total / (double) size);
	}

	// Returns mean item rating in relation to all the users the users neighbourhood
	public Double meanItemRating(int user_id, ArrayList<User> neighbours) {
		int total = 0;
		int size = 0;
		for(User user : neighbours) {
			if(ratingsUserToRating.containsKey(user.getID())) {
				int rating_user = user.getID();
				if(rating_user != user_id) {
					Rating rating = ratingsUserToRating.get(user.getID());
					total += rating.getValue();
					size++;
				}
			}
		}
		if(size == 0) {
			return 0.0;
		}
		return ( total / (double) size);
	}
	
	//Mean,	median,	standard	deviation,	max,	min	ratings	per	item.
	
	public void calculateStatistics() {
		 int max = 0, total = 0, median = 0, min = 5;
		 int size = ratingsUserToScore.size();
		 Double mean = 0.0, standardDeviation = 0.0;
		 ArrayList<Integer> listOfRatings = new ArrayList<>();
		 for(int rating_val : ratingsUserToScore.values()) {
		        if(rating_val < min) {
		        	min = rating_val;
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
        for(int a : ratingsUserToScore.values())
            temp += (a-mean)*(a-mean);
        return temp/size;
    }
	
	private double getStdDev(Double variance)  {
        return Math.sqrt(variance);
    }

	// Used for testing
	public void printRatings() {
		for(int a : ratingsUserToScore.values()) {
			System.out.print(a + ' ');
		}
	}
}
