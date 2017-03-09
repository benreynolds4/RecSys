import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;


public class Recomender {
	private static HashMap<Integer, User> users = new HashMap<>();  // User ID -> User
	private static HashMap<Integer, Item> items = new HashMap<>(); // Item ID -> Item
	private static ArrayList<Rating> ratings = new ArrayList<>();  // Lists of Ratings
	static HashMap<String, Double> already_calculated = new HashMap<>(); // Hashmap used for memoization.
	private static Double meanItemRatingCoverage;
	public static void main(String [] args) throws IOException {
		long startTime = System.nanoTime();
		 readFile();
		 // Task 1:
		  System.out.println("Task 1:");
		  calculateUserStats();
		  calculateItemStats();
	
		 // Task 2
		  System.out.println("Task 2:");
		/* System.out.println("Task 2");
		   for(int i=0; i< 10; i++) {
		 	 long startTime = System.nanoTime(); */
			 LeaveOneOut();
		/*	 long stopTime = System.nanoTime();
			 System.out.println((double)(stopTime - startTime) / 1000000000.0 );
		 	} */

		// Creates Neighbours for each user
		 for(User user: users.values()){
			 similarityFunction(user.getID());
		 }

		  int nSize = 300;
		  // Task 3
		  /*for(int i=0; i< 40; i++) {
		  	 nSize = 150;
		  	 System.out.println("Neighbourhood Size: " + nSize ); */
		      System.out.println("Task 3:");
			  LeaveOneOutNeighbours(nSize); 	// Task 3
		 	  System.out.println("Task 4:");
			  LeaveOneOutResnicks(nSize);		// Task 4
			 long stopTime = System.nanoTime();
			 System.out.println("Time:" + (double)(stopTime - startTime) / 1000000000.0 );
		 	/*} */


	}

	// Uses Memoization
	// Result of this function is to add neighbours to each user and with a cosine simularity function.

	public static void similarityFunction(int user_id) {
		User currentUser = users.get(user_id);

		for(User user: users.values()) {
			// key is used for the key of the hashmap of memoization.
			String key = Integer.toString(user.getID()) + "-" + Integer.toString(currentUser.getID());
			if(user.getID() == user_id) {  					// if user is current user then continue to next iteration
				continue;
			}else if(already_calculated.containsKey(key)) {
				// Check if we've already calculated this and if so add the user to neighbour
				currentUser.add_neighbour(already_calculated.get(key), user);
			} else {

				 HashMap<Integer, Integer> ratingsItemIDToScoreCurrentUser = currentUser.getRatingsIDToScore();
				 HashMap<Integer, Integer> ratingsItemIDToScoreOtherUser = user.getRatingsIDToScore();
				 
				 int sumCommonRatings = 0;
				 int sumCurrentUserRatings = 0;
				 int sumOtherUserRatings = 0;
				 
				 for(int itemCurrentUser : ratingsItemIDToScoreCurrentUser.keySet()) {
					 // iterate of the current users items
					 // if the other use has rated the current users item
					  if(ratingsItemIDToScoreOtherUser.containsKey(itemCurrentUser)) {
						int currentUserRating = ratingsItemIDToScoreCurrentUser.get(itemCurrentUser);
						int otherUserRating = ratingsItemIDToScoreOtherUser.get(itemCurrentUser);
						sumCurrentUserRatings += currentUserRating;
						sumOtherUserRatings += otherUserRating;
						int sum = currentUserRating + currentUserRating;
						sumCommonRatings += sum;
					 }
				 }
				 // Grab the cosine score
				 Double cosineScore = calculateCosineScore(sumCommonRatings,
						 sumCurrentUserRatings, sumOtherUserRatings);
				 currentUser.add_neighbour(cosineScore, user);
				 String newKey1 = Integer.toString(user.getID()) + "-" + Integer.toString(currentUser.getID());
				 String newKey2 =Integer.toString(currentUser.getID()) + "-" + Integer.toString(user.getID());
				 // Add new keys to whats been already calculated in both directions so we can easily
				 // check if the next pair has been caculated already.
				 already_calculated.put(newKey1, cosineScore);
				 already_calculated.put(newKey2, cosineScore);
			}
		}
	}

	/* This method calculated a cosine similarity score for two different users */
	private static Double calculateCosineScore(int sumCommonRatings, int sumCurrentUserRatings,
											   int sumOtherUserRatings) {
		Double cosinescore = sumCommonRatings /
				(Math.sqrt(sumCurrentUserRatings * sumCurrentUserRatings) * Math.sqrt(sumOtherUserRatings
						* sumOtherUserRatings));
		return cosinescore;
	}

	/* This method returns a predicted value for user and an item based on other users predictions.
	 * It has a side effect that it adds the prediction to the users Datastructure.
	 */
	private static Double resnicksPrediction(int user_id, int itemID, int nSize) throws FileNotFoundException {
		User currentUser = users.get(user_id);
		Double sumTopLine = 0.0;
		Double sumBottomLine = 0.0;
		Double meanCurrentUserRating = currentUser.getMean();
		for(User user: currentUser.getNeighbourhood(nSize)) {
				if(user.hasRating(itemID)) { // If the neighbour has a rating for current item.
					Double meanOtherUserRating = user.getMean();
					Double simularity = currentUser.getSimularityScore(user.getID());
					sumTopLine += ((user.getValue(itemID) - meanOtherUserRating) * simularity);
					sumBottomLine += Math.abs(simularity);
				}
		}
		Double prediction = meanCurrentUserRating + (sumTopLine / sumBottomLine);
		currentUser.addResnicksPrediction(itemID, prediction);
		return prediction;
		// This return NaN when the neighbour doesn't have a rating for current item. This is rectified in the
		// Leave one out resnicks method by checking for NaN.
	}

	/* Run the leave one out tests on new resnicks predictions. Task 4.
	*  This method creates a CSV file for the data in the form User, Item, Actual Rating, Predicted
	*  Rating and RMSE.
	*  This method also calculates coverage and total average RMSE.
	*  */
	private static void LeaveOneOutResnicks(int nSize) throws  FileNotFoundException {
		Double count  = 0.0, totalCount = 0.0;
		PrintWriter pw = new PrintWriter(new File("task4-neighbourhood" + nSize + ".csv"));
		Double totalRMSES = 0.0;
		for(User user: users.values()) {
			for(int itemID: user.getItems()) {
				Double prediction = resnicksPrediction(user.getID(), itemID, nSize);
				StringBuilder sb = new StringBuilder();
				if(!Double.isNaN(prediction)) { // The NaN check reffered to in resnicksPrediction()
						sb.append(user.getID() + " , " + itemID + " , " + user.getValue(itemID) + " , " +
								prediction + " , " + Math.abs(user.getValue(itemID) - prediction) + "\n");
						totalRMSES += Math.abs(user.getValue(itemID) - prediction);
						pw.write(sb.toString());
						count++;
				}
				totalCount++;
			}
		}
		pw.close();
		System.out.println("Total Count:" + totalCount);
		Double averageRMSE = (Double) (totalRMSES / count);
		Double coverage = (count / totalCount ) * 100;
		System.out.println("Average RMSE: "+averageRMSE);
		System.out.println("Coverage: " + coverage + '%');
	}

	/*  This method runs the leave one out tests on out neighbourhoods. Task 3.
	 *  This method creates a CSV file for the data in the form User, Item, Actual Rating, Predicted
	 *  Rating and RMSE.
	 *  This method also calculates coverage and total average RMSE.
	 */
	private static void LeaveOneOutNeighbours(int nSize) throws FileNotFoundException {
		Double count  = 0.0, totalCount = 0.0;
		PrintWriter pw = new PrintWriter(new File("task3-neighbourhood" + nSize + ".csv"));
		Double totalRMSES = 0.0;
		for(int user_id : users.keySet()) {
			User user = users.get(user_id);
			ArrayList<User> neighbours = user.getNeighbourhood(nSize);
			for(int item_id : user.getItems()) {
				 StringBuilder sb = new StringBuilder();
				 Double prediction = meanItemRating(user_id, item_id, neighbours);
				 if(prediction != 0.0) {
						sb.append(user_id + " , " + item_id + " , " + user.getValue(item_id) + " , " +
								 prediction + " , " + Math.abs(user.getValue(item_id) - prediction) + "\n");
						totalRMSES += Math.abs(user.getValue(item_id) - prediction);
						pw.write(sb.toString());
						count++;
				 }
				 totalCount++;
			}
		}
		pw.close();
		System.out.println("Total Count:" + totalCount);
		Double averageRMSE = (Double) (totalRMSES / count);
		Double coverage = (count / totalCount ) * 100;
		System.out.println("Average RMSE: "+averageRMSE);
		System.out.println("Coverage: " + coverage + '%');
	}

	/*  This method runs the leave one out tests on out on mean item rating for task 2.
	 *  This method creates a CSV file for the data in the form User, Item, Actual Rating, Predicted
	 *  Rating and RMSE.
	 *  This method also calculates coverage and total average RMSE.
	 */
	private static void LeaveOneOut() throws FileNotFoundException {
		Double count  = 0.0, totalCount = 0.0;
		PrintWriter pw = new PrintWriter(new File("task2.csv"));
		Double totalRMSES = 0.0;
		for(int user_id : users.keySet()) {
			User user = users.get(user_id);
			for(int item_id : user.getItems()) {
				 StringBuilder sb = new StringBuilder();
				Double prediction = meanItemRating(user_id, item_id);
				if( prediction != 0.0) {
					sb.append(user_id + " , " + item_id + " , " + user.getValue(item_id) + " , " +
							 prediction + " , " + Math.abs(user.getValue(item_id) - prediction) + "\n");
					totalRMSES += Math.abs(user.getValue(item_id) - prediction);
					pw.write(sb.toString());
					count++;
				}
				totalCount++;
			}
		}
		pw.close();
		System.out.println("Total Count:" + totalCount);
		Double averageRMSE = (Double) (totalRMSES / count);
		Double coverage = (Double) (count / totalCount);
		System.out.println("Average RMSE: " + averageRMSE);
		System.out.println("Coverage: " + coverage + '%');
	}

	/* Calls the item object to get the mean item rating for given user item pair. Used in Task 2.
	 */
	private static Double meanItemRating(int user_id, int item_id) {
		Item item = items.get(item_id);
		return item.meanItemRating(user_id);
	}

	/* Calls the item object to get the mean item rating for given user item pair but this method
	 * only uses the neighbours gven to calculate the mean item ratings. Used in Task 3.
	 */
	private static Double meanItemRating(int user_id, int item_id, ArrayList<User> neighbours) {
		Item item = items.get(item_id);
		return item.meanItemRating(user_id, neighbours);
	}

	/* This method reads the input file line by line and
	 * and populates the Data Structures and Maps created accordingly.
	 */
	private static void readFile() throws IOException {
		File file = new File("dataset-large.txt");
		InputStream is = Recomender.class.getResourceAsStream("dataset-large.txt");
		System.out.print(is);
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
       // BufferedReader br = new BufferedReader(new FileReader(file));
        String st;
        while((st=br.readLine()) != null){
        	String[] line = st.split(",");
        	int user_id = Integer.parseInt(line[0]);
        	int item_id = Integer.parseInt(line[1]);
        	int rating_val = Integer.parseInt(line[2]);
        	
        	Rating rating = new Rating(user_id, rating_val, item_id);
        	ratings.add(rating);
        	
        	if(items.containsKey(item_id)) {
        		Item item = items.get(item_id);
        		item.add_rating(user_id, rating_val, rating);
        	} else {
        		Item item = new Item(item_id, rating_val, user_id, rating);
        		items.put(item_id, item);
        	}
        	if(users.containsKey(user_id)) {
        		User user = users.get(user_id);
        		user.add_rating(rating_val, item_id, rating);
        	} else {
        		User user = new User(user_id, rating_val, item_id, rating);
        		users.put(user_id, user);
        	}
        }
        br.close();
	}



	/* This method iterates over all the users in the users map and calls the user object to calculate the
	 * statistics for that user. It also prints out the statistics as the results of task 1.
	 */
	private static void calculateUserStats() {
		 Iterator<?> it = users.entrySet().iterator();
		 while (it.hasNext()) {
	        HashMap.Entry userMap = (HashMap.Entry)it.next();
	        User user = (User) userMap.getValue();
	        user.calculateStatistics();
	        System.out.println("User ID:" + userMap.getKey());
	        System.out.println("Max Rating:" + user.getMax());
	        System.out.println("Min Rating:" + user.getMin());
	        System.out.println("Standard Deviation:" + user.getStandardDeviation());
	        System.out.println("Mean Rating:" + user.getMean());
	        System.out.println("Median Rating:" + user.getMedian());
	        System.out.println("------------------------");
		 }
	}
	/* This method iterates over all the items in the items map and calls the item object to calculate the
	 * statistics for that item. It also prints out the statistics as the results of task 1.
	 */
	private static void calculateItemStats() {
		for(Item item: items.values()){
	        item.calculateStatistics();
	        System.out.println("Max Rating:" + item.getMax());
	        System.out.println("Min Rating:" + item.getMin());
	        System.out.println("Standard Deviation:" + item.getStandardDeviation());
	        System.out.println("Mean Rating:" + item.getMean());
	        System.out.println("Median Rating:" + item.getMedian());
	        System.out.println("------------------------");
		 }
	}

	// Given a rating value returns the number of ratings with that value
	// Used in task 1.
	private static int numberRatingsForValue(int value) {
		int count = 0;
		for(Rating rating : ratings) {
			if(rating.getValue() == value) {
				count++;
			}
		}
		return count;
	}
	
	private static int numberOfUsers() {
		return users.size();
	}
	
	private static int numberOfRatings() {
		return ratings.size();
	}
	
	private static int numberOfItems() {
		return items.size();
	}
}
