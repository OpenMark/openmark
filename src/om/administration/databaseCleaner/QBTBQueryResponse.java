package om.administration.databaseCleaner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class QBTBQueryResponse {


	private List<TestBankData> outOfSyncTests
		= new ArrayList<TestBankData>();

	private List<TestQuestionReferences> Tests;
	
	private List<TestXML> TestXML = new ArrayList<TestXML>();
	

	private List<String> archiveDirs= new ArrayList<String>();
	
	
	public List<String> getArchiveDirs() {
		return archiveDirs;
	}
	
	public String getArchiveDir(int i) {
		return archiveDirs.get(i);
	}

	public String getArchiveDirsAsString() {
		String archiveDir=null;
		if (this.archiveDirs.size() >0 ){
			archiveDir=this.archiveDirs.get(0);
			for (int i = 1; i < this.archiveDirs.size(); i++) {
				archiveDir=archiveDir+",";
				archiveDir=archiveDir + this.archiveDirs.get(i);
			}
		}
		return archiveDir;
	}
 
	public void addUniqArchiveDir(String archiveDir) {
		if (null != archiveDir && !this.archiveDirs.contains(archiveDir)) {		
			this.archiveDirs.add(archiveDir);
		}
	}
	public List<TestXML> getTestXML() {
		return TestXML;
	}

	public void addTestXML(TestXML tx) {
		if (null != tx) {
			getTestXML().add(tx);
		}
	}

	public List<TestQuestionReferences> getTests() {
		if (null == Tests) {
			Tests = new ArrayList<TestQuestionReferences>(); 
		}
		return Tests;
	}

	public void addTest(Set<TestQuestionReferences> itqr) {
		if (null != itqr ? itqr.size() > 0 : false) {
			getTests().addAll(itqr);
		}
	}

	public void addOutOfSyncTest(TestBankData check) {
		if (null != check) {
			outOfSyncTests.add(check);
		}
	}

	public List<TestBankData> getOutOfSyncTests() {
		return Collections.unmodifiableList(outOfSyncTests);
	}


}
