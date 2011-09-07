package util.misc;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;

public class DynamicDiagnosticListener
	implements DiagnosticListener<JavaFileObject> {
	
	private List<DynamicCompilationReport> reports = new ArrayList<DynamicCompilationReport>();
	
	public List<DynamicCompilationReport> getReports() {
		return reports;
	}
	
	public void report(Diagnostic<? extends JavaFileObject> d) {
		if (null != d) {
			DynamicCompilationReport report = new DynamicCompilationReport();
	    	report.setCode(d.getCode());
	    	report.setLineNumber(d.getLineNumber());
	    	report.setMessage(d.getMessage(Locale.ENGLISH));
	    	report.setSource(null != d.getSource() ? d.getSource().toString() : null);
	    	reports.add(report);
		}
	}

}