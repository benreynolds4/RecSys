
public class Rating {
	private int user_id;
	private int rating_val;
	private int item_id;


	/* Initialise a rating when creating datastructures from the input file. */
	public Rating(int user_id, int rating_val, int item_id) {
		this.user_id = user_id;
		this.rating_val = rating_val;
		this.item_id = item_id;
	}

	// Get a ratings value
	public int getValue() {
		return this.rating_val;
	}
}
