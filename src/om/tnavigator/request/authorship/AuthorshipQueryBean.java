package om.tnavigator.request.authorship;

public class AuthorshipQueryBean {

	private String retrieveQuery;

	private String updateQuery;

	public String getRetrieveQuery() {
		return retrieveQuery;
	}

	public String getUpdateQuery() {
		return updateQuery;
	}

	public AuthorshipQueryBean(String retrieve, String update) {
		retrieveQuery = retrieve;
		updateQuery = update;
	}

}
