package util.misc;

import java.util.List;

public class DynamicCompilationResponse {
	
	boolean success = false;
	
	List<DynamicCompilationReport> reports;

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean b) {
		success = b;
	}

	public List<DynamicCompilationReport> getReports() {
		return reports;
	}

	public void setReports(List<DynamicCompilationReport> r) {
		reports = r;
	}
	
}